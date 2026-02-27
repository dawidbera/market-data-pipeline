package com.market.pipeline.ingestor;

import com.market.pipeline.common.avro.Tick;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

/**
 * Unit test for {@link MarketDataIngestor}.
 */
@ExtendWith(MockitoExtension.class)
class MarketDataIngestorTest {

    @Mock
    private KafkaTemplate<String, Tick> kafkaTemplate;

    private MarketDataIngestor ingestor;

    @BeforeEach
    void setUp() {
        ingestor = new MarketDataIngestor(kafkaTemplate);
    }

    /**
     * Verifies that the ingestor generates a valid Tick and sends it to the expected Kafka topic.
     */
    @Test
    void testGenerateAndSendMarketData() {
        // Act
        ingestor.generateAndSendMarketData();

        // Assert
        ArgumentCaptor<Tick> tickCaptor = ArgumentCaptor.forClass(Tick.class);
        verify(kafkaTemplate).send(eq("market.data.raw"), anyString(), tickCaptor.capture());

        Tick sentTick = tickCaptor.getValue();
        assertNotNull(sentTick);
        assertNotNull(sentTick.getSymbol());
        assertTrue(sentTick.getPrice() >= 100.0 && sentTick.getPrice() <= 150.0);
        assertTrue(sentTick.getVolume() >= 100 && sentTick.getVolume() <= 1100);
        assertTrue(sentTick.getTimestamp() > 0);
    }
}
