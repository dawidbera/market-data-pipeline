package com.market.pipeline.dashboard;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Basic application context loading test for {@link DashboardApplication}.
 */
@SpringBootTest
@ActiveProfiles("test")
class DashboardApplicationTests extends AbstractIntegrationTest {

    /**
     * Verifies that the Spring Boot application context loads successfully.
     */
    @Test
    void contextLoads() {
    }
}
