package com.market.pipeline.processor;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.lifecycle.Startables;
import org.testcontainers.utility.DockerImageName;

import java.nio.file.Path;
import java.util.stream.Stream;

import org.junit.jupiter.api.io.TempDir;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@SpringBootTest
@ActiveProfiles("test")
public abstract class AbstractProcessorIntegrationTest {

    @Configuration
    static class TestConfig {
        @Bean
        public NewTopic rawTopic() {
            return new NewTopic("market.data.raw", 1, (short) 1);
        }

        @Bean
        public NewTopic aggregatedTopic() {
            return new NewTopic("market.data.aggregated", 1, (short) 1);
        }

        @Bean
        public NewTopic alertsTopic() {
            return new NewTopic("market.data.alerts", 1, (short) 1);
        }
    }

    static final Network network = Network.newNetwork();

    static final KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.7.0"))
            .withNetwork(network)
            .withNetworkAliases("kafka");

    static final GenericContainer<?> schemaRegistry = new GenericContainer<>(DockerImageName.parse("confluentinc/cp-schema-registry:7.7.0"))
            .withNetwork(network)
            .withExposedPorts(8081)
            .withEnv("SCHEMA_REGISTRY_KAFKASTORE_BOOTSTRAP_SERVERS", "PLAINTEXT://kafka:9092")
            .withEnv("SCHEMA_REGISTRY_HOST_NAME", "schema-registry")
            .withEnv("SCHEMA_REGISTRY_LISTENERS", "http://0.0.0.0:8081");

    @TempDir
    static Path tempDir;

    static {
        Startables.deepStart(Stream.of(kafka, schemaRegistry)).join();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("spring.kafka.streams.properties.schema.registry.url",
                () -> "http://" + schemaRegistry.getHost() + ":" + schemaRegistry.getMappedPort(8081));
        registry.add("spring.kafka.producer.properties.schema.registry.url",
                () -> "http://" + schemaRegistry.getHost() + ":" + schemaRegistry.getMappedPort(8081));
        registry.add("spring.kafka.streams.properties.state.dir",
                () -> tempDir.toAbsolutePath().toString());
        registry.add("spring.kafka.streams.application-id",
                () -> "market-processor-test-" + java.util.UUID.randomUUID());
        registry.add("spring.kafka.streams.properties.auto.offset.reset", () -> "earliest");
    }
}
