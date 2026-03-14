package com.market.pipeline.processor;

import com.market.pipeline.common.avro.Alert;
import com.market.pipeline.common.avro.Candle;
import com.market.pipeline.common.avro.Tick;
import io.confluent.kafka.schemaregistry.client.MockSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Topology test for {@link StreamProcessor}.
 * Verifies windowed aggregations and anomaly detection logic.
 */
class StreamProcessorTest {

    private TopologyTestDriver testDriver;
    private TestInputTopic<String, Tick> inputTopic;
    private TestOutputTopic<String, Candle> aggregatedTopic;
    private TestOutputTopic<String, Alert> alertsTopic;

    private SpecificAvroSerde<Tick> tickSerde;
    private SpecificAvroSerde<Candle> candleSerde;
    private SpecificAvroSerde<Alert> alertSerde;
    private SchemaRegistryClient schemaRegistryClient;

    /**
     * Sets up the Kafka Streams topology test driver and required serdes.
     * Uses a mock schema registry client for testing Avro serialization.
     */
    @BeforeEach
    void setup() {
        schemaRegistryClient = new MockSchemaRegistryClient();
        StreamsBuilder builder = new StreamsBuilder();
        StreamProcessor processor = new StreamProcessor();
        ReflectionTestUtils.setField(processor, "schemaRegistryUrl", "http://dummy:8081");
        ReflectionTestUtils.setField(processor, "schemaRegistryClient", schemaRegistryClient);
        processor.process(builder);

        Topology topology = builder.build();

        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "test-processor");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:1234");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, SpecificAvroSerde.class.getName());
        props.put("schema.registry.url", "http://dummy:8081");

        Map<String, String> config = Collections.singletonMap(
                AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://dummy:8081");

        tickSerde = new SpecificAvroSerde<>(schemaRegistryClient);
        tickSerde.configure(config, false);
        candleSerde = new SpecificAvroSerde<>(schemaRegistryClient);
        candleSerde.configure(config, false);
        alertSerde = new SpecificAvroSerde<>(schemaRegistryClient);
        alertSerde.configure(config, false);

        testDriver = new TopologyTestDriver(topology, props);

        inputTopic = testDriver.createInputTopic(
                "market.data.raw",
                Serdes.String().serializer(),
                tickSerde.serializer());
        aggregatedTopic = testDriver.createOutputTopic(
                "market.data.aggregated",
                Serdes.String().deserializer(),
                candleSerde.deserializer());
        alertsTopic = testDriver.createOutputTopic(
                "market.data.alerts",
                Serdes.String().deserializer(),
                alertSerde.deserializer());
    }

    /**
     * Closes the test driver after each test.
     */
    @AfterEach
    void tearDown() {
        if (testDriver != null) {
            testDriver.close();
        }
    }

    /**
     * Verifies that ticks with volume > 1000 trigger a HIGH_VOLUME alert.
     */
    @Test
    void testAnomalyDetection() {
        // Arrange
        Tick highVolumeTick = Tick.newBuilder()
                .setSymbol("AAPL")
                .setPrice(150.0)
                .setVolume(2000L) // > 1000
                .setTimestamp(Instant.now().toEpochMilli())
                .build();

        Tick normalVolumeTick = Tick.newBuilder()
                .setSymbol("MSFT")
                .setPrice(300.0)
                .setVolume(500L)
                .setTimestamp(Instant.now().toEpochMilli())
                .build();

        // Act
        inputTopic.pipeInput("AAPL", highVolumeTick);
        inputTopic.pipeInput("MSFT", normalVolumeTick);

        // Assert
        assertFalse(alertsTopic.isEmpty());
        Alert alert = alertsTopic.readRecord().getValue();
        assertEquals("AAPL", alert.getSymbol());
        assertEquals("HIGH_VOLUME", alert.getType());

        assertTrue(alertsTopic.isEmpty());
    }

    /**
     * Verifies that multiple ticks are correctly aggregated into OHLC candles.
     */
    @Test
    void testCandleAggregation() {
        // Arrange
        Instant start = Instant.parse("2024-01-01T10:00:00Z");
        Tick tick1 = Tick.newBuilder()
                .setSymbol("AAPL")
                .setPrice(100.0)
                .setVolume(100L)
                .setTimestamp(start.toEpochMilli())
                .build();
        Tick tick2 = Tick.newBuilder()
                .setSymbol("AAPL")
                .setPrice(110.0)
                .setVolume(150L)
                .setTimestamp(start.plusSeconds(30).toEpochMilli())
                .build();

        // Act
        inputTopic.pipeInput("AAPL", tick1);
        inputTopic.pipeInput("AAPL", tick2);

        // Assert
        List<KeyValue<String, Candle>> records = aggregatedTopic.readKeyValuesToList();
        
        // We expect at least one record per window type (1m and 5m) for each tick update
        assertTrue(records.size() >= 4); // 2 updates for 1m, 2 updates for 5m
        
        Candle lastCandle = records.get(records.size() - 1).value;
        assertEquals(100.0, lastCandle.getOpen());
        assertEquals(110.0, lastCandle.getHigh());
        assertEquals(100.0, lastCandle.getLow());
        assertEquals(110.0, lastCandle.getClose());
        assertEquals(250L, lastCandle.getVolume());
    }
}
