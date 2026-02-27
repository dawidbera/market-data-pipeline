package com.market.pipeline.processor;

import com.market.pipeline.common.avro.Alert;
import com.market.pipeline.common.avro.Candle;
import com.market.pipeline.common.avro.Tick;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.WindowStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;

/**
 * Kafka Streams processor that defines the topology for processing market data.
 * It performs windowed aggregations to create candles and detects anomalies in the data stream.
 */
@Component
public class StreamProcessor {

    private static final Logger log = LoggerFactory.getLogger(StreamProcessor.class);
    
    private static final String INPUT_TOPIC = "market.data.raw";
    private static final String OUTPUT_TOPIC_AGGREGATED = "market.data.aggregated";
    private static final String OUTPUT_TOPIC_ALERTS = "market.data.alerts";

    @Value("${spring.kafka.properties.schema.registry.url:http://localhost:8081}")
    private String schemaRegistryUrl;

    @Autowired(required = false)
    private SchemaRegistryClient schemaRegistryClient;

    /**
     * Defines the processing logic for the Kafka Streams topology.
     * 
     * @param streamsBuilder the builder used to define the streams
     */
    @Autowired
    public void process(StreamsBuilder streamsBuilder) {
        SpecificAvroSerde<Tick> tickSerde = getSpecificAvroSerde();

        KStream<String, Tick> ticks = streamsBuilder.stream(INPUT_TOPIC, Consumed.with(Serdes.String(), tickSerde));

        // 1. Windowed Aggregation (1 minute candles)
        aggregateCandles(ticks, "1m-candles", Duration.ofMinutes(1));

        // 2. Windowed Aggregation (5 minute candles)
        aggregateCandles(ticks, "5m-candles", Duration.ofMinutes(5));

        // 3. Simple Anomaly Detection (Alerts)
        detectAnomalies(ticks);
    }

    private <T extends org.apache.avro.specific.SpecificRecord> SpecificAvroSerde<T> getSpecificAvroSerde() {
        SpecificAvroSerde<T> serde = schemaRegistryClient != null 
                ? new SpecificAvroSerde<>(schemaRegistryClient) 
                : new SpecificAvroSerde<>();
        
        serde.configure(Collections.singletonMap("schema.registry.url", schemaRegistryUrl), false);
        return serde;
    }

    /**
     * Aggregates market ticks into OHLC candles for a specific window duration.
     * 
     * @param ticks the input stream of market ticks
     * @param name the name of the state store for this aggregation
     * @param windowSize the duration of the time window for aggregation
     */
    private void aggregateCandles(KStream<String, Tick> ticks, String name, Duration windowSize) {
        SpecificAvroSerde<Candle> candleSerde = getSpecificAvroSerde();

        ticks.groupByKey()
                .windowedBy(TimeWindows.ofSizeWithNoGrace(windowSize))
                .aggregate(
                        () -> (Candle) null,
                        (key, tick, aggregate) -> {
                            if (aggregate == null) {
                                return Candle.newBuilder()
                                        .setSymbol(tick.getSymbol())
                                        .setOpen(tick.getPrice())
                                        .setHigh(tick.getPrice())
                                        .setLow(tick.getPrice())
                                        .setClose(tick.getPrice())
                                        .setVolume(tick.getVolume())
                                        .setWindowStart(0L)
                                        .setWindowEnd(0L)
                                        .build();
                            }
                            aggregate.setHigh(Math.max(aggregate.getHigh(), tick.getPrice()));
                            aggregate.setLow(Math.min(aggregate.getLow(), tick.getPrice()));
                            aggregate.setClose(tick.getPrice());
                            aggregate.setVolume(aggregate.getVolume() + tick.getVolume());
                            return aggregate;
                        },
                        Materialized.<String, Candle, WindowStore<Bytes, byte[]>>as(name)
                                .withKeySerde(Serdes.String())
                                .withValueSerde(candleSerde)
                )
                .toStream()
                .map((windowedKey, candle) -> {
                    candle.setWindowStart(windowedKey.window().start());
                    candle.setWindowEnd(windowedKey.window().end());
                    return KeyValue.pair(windowedKey.key(), candle);
                })
                .to(OUTPUT_TOPIC_AGGREGATED, Produced.with(Serdes.String(), candleSerde));
    }

    /**
     * Monitors the tick stream for anomalies, such as extreme volume, and emits alerts.
     * 
     * @param ticks the input stream of market ticks
     */
    private void detectAnomalies(KStream<String, Tick> ticks) {
        SpecificAvroSerde<Alert> alertSerde = getSpecificAvroSerde();

        // Simple alert for high volume
        ticks.filter((key, tick) -> tick.getVolume() > 1000)
                .map((key, tick) -> {
                    Alert alert = Alert.newBuilder()
                            .setSymbol(tick.getSymbol())
                            .setType("HIGH_VOLUME")
                            .setMessage("Volume " + tick.getVolume() + " exceeded threshold 1000")
                            .setTimestamp(tick.getTimestamp())
                            .build();
                    return KeyValue.pair(key, alert);
                })
                .to(OUTPUT_TOPIC_ALERTS, Produced.with(Serdes.String(), alertSerde));

        // Price change detection could be implemented with a state store if needed
        // but for now, let's keep it simple with high volume alerts.
    }
}

