package com.market.pipeline.processor;

import com.market.pipeline.common.avro.Alert;
import com.market.pipeline.common.avro.Candle;
import com.market.pipeline.common.avro.Tick;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * Integration tests for the Processor Service.
 * Verifies that the StreamProcessor correctly processes raw ticks and produces alerts.
 */
public class ProcessorIntegrationTest extends AbstractProcessorIntegrationTest {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * Verifies that a high volume tick triggers a HIGH_VOLUME alert in the alerts topic.
     */
    @Test
    void shouldProcessTickAndProduceAggregatesAndAlerts() {
        // Prepare consumer for output topics
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "test-group");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class.getName());
        props.put("schema.registry.url", "http://" + schemaRegistry.getHost() + ":" + schemaRegistry.getMappedPort(8081));
        props.put("specific.avro.reader", "true");

        try (Consumer<String, Object> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(Collections.singletonList("market.data.alerts"));

            // Send a high volume tick to trigger alert
            Tick highVolumeTick = Tick.newBuilder()
                    .setSymbol("AAPL")
                    .setPrice(150.0)
                    .setVolume(2000L) // Trigger HIGH_VOLUME alert
                    .setTimestamp(Instant.now().toEpochMilli())
                    .build();

            kafkaTemplate.send("market.data.raw", "AAPL", highVolumeTick);

            // Verify alert
            await().atMost(Duration.ofSeconds(60)).untilAsserted(() -> {
                ConsumerRecords<String, Object> records = consumer.poll(Duration.ofMillis(500));
                assertThat(records).isNotEmpty();
                Alert alert = (Alert) records.iterator().next().value();
                assertThat(alert.getSymbol()).isEqualTo("AAPL");
                assertThat(alert.getType()).isEqualTo("HIGH_VOLUME");
            });
        }
    }
}
