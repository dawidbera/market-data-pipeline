package com.market.pipeline.ingestor;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Basic application context loading test for {@link IngestorApplication}.
 */
@SpringBootTest
@ActiveProfiles("test")
class IngestorApplicationTests {

    /**
     * Verifies that the Spring Boot application context loads successfully.
     */
    @Test
    void contextLoads() {
    }
}
