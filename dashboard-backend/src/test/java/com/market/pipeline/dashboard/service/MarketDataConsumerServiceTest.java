package com.market.pipeline.dashboard.service;

import com.market.pipeline.common.avro.Alert;
import com.market.pipeline.common.avro.Candle;
import com.market.pipeline.common.avro.Tick;
import com.market.pipeline.dashboard.repository.AlertRepository;
import com.market.pipeline.dashboard.repository.CandleRepository;
import com.market.pipeline.dashboard.repository.TickRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit test for {@link MarketDataConsumerService}.
 * Verifies that the service correctly interacts with Kafka, Redis, WebSockets, 
 * and the database upon consuming market data updates.
 */
@ExtendWith(MockitoExtension.class)
class MarketDataConsumerServiceTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;
    @Mock
    private RedisTemplate<String, Object> redisTemplate;
    @Mock
    private ValueOperations<String, Object> valueOperations;
    @Mock
    private TickRepository tickRepository;
    @Mock
    private CandleRepository candleRepository;
    @Mock
    private AlertRepository alertRepository;

    private MarketDataConsumerService consumerService;

    @BeforeEach
    void setUp() {
        consumerService = new MarketDataConsumerService(
                messagingTemplate, redisTemplate, tickRepository, candleRepository, alertRepository
        );
    }

    /**
     * Verifies that raw ticks are persisted into the database.
     */
    @Test
    void testConsumeRawData() {
        // Arrange
        Tick tick = Tick.newBuilder()
                .setSymbol("AAPL")
                .setPrice(150.0)
                .setVolume(100L)
                .setTimestamp(Instant.now().toEpochMilli())
                .build();

        // Act
        consumerService.consumeRawData(tick);

        // Assert
        verify(tickRepository).save(any());
    }

    /**
     * Verifies that aggregated candles are broadcast via WebSockets, 
     * cached in Redis, and persisted into the database.
     */
    @Test
    void testConsumeAggregatedData() {
        // Arrange
        Candle candle = Candle.newBuilder()
                .setSymbol("AAPL")
                .setOpen(140.0)
                .setHigh(150.0)
                .setLow(130.0)
                .setClose(145.0)
                .setVolume(1000L)
                .setWindowStart(Instant.now().toEpochMilli())
                .setWindowEnd(Instant.now().plusSeconds(60).toEpochMilli())
                .build();
        
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // Act
        consumerService.consumeAggregatedData(candle);

        // Assert
        verify(messagingTemplate).convertAndSend(eq("/topic/candles/AAPL"), anyString());
        verify(valueOperations).set(eq("latest_candle:AAPL"), anyString());
        verify(candleRepository).save(any());
    }

    /**
     * Verifies that alerts are broadcast via WebSockets and persisted into the database.
     */
    @Test
    void testConsumeAlerts() {
        // Arrange
        Alert alert = Alert.newBuilder()
                .setSymbol("AAPL")
                .setType("HIGH_VOLUME")
                .setMessage("Volume exceeded threshold")
                .setTimestamp(Instant.now().toEpochMilli())
                .build();

        // Act
        consumerService.consumeAlerts(alert);

        // Assert
        verify(messagingTemplate).convertAndSend(eq("/topic/alerts"), anyString());
        verify(alertRepository).save(any());
    }
}
