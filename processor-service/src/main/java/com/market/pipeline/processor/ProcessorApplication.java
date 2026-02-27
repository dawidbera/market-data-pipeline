package com.market.pipeline.processor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafkaStreams;

/**
 * Main application class for the Processor Service.
 * Enables Kafka Streams and starts the processing topology for market data.
 */
@SpringBootApplication
@EnableKafkaStreams
public class ProcessorApplication {
    /**
     * Entry point for the Processor Service application.
     * 
     * @param args command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(ProcessorApplication.class, args);
    }
}
