package com.market.pipeline.dashboard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for the Dashboard Backend.
 * Responsible for starting the Spring Boot application that serves the market dashboard data.
 */
@SpringBootApplication
public class DashboardApplication {
    /**
     * Entry point for the Dashboard Backend application.
     * 
     * @param args command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(DashboardApplication.class, args);
    }
}
