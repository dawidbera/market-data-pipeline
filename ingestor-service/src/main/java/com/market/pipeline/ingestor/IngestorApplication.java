package com.market.pipeline.ingestor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main application class for the Ingestor Service.
 * Enables scheduling to periodically generate and send simulated market data.
 */
@SpringBootApplication
@EnableScheduling
public class IngestorApplication {
    /**
     * Entry point for the Ingestor Service application.
     * 
     * @param args command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(IngestorApplication.class, args);
    }
}
