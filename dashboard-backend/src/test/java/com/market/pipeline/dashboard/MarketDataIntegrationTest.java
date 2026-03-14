package com.market.pipeline.dashboard;

import com.market.pipeline.common.avro.Alert;
import com.market.pipeline.common.avro.Candle;
import com.market.pipeline.common.avro.Tick;
import com.market.pipeline.dashboard.repository.AlertRepository;
import com.market.pipeline.dashboard.repository.CandleRepository;
import com.market.pipeline.dashboard.repository.TickRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * Integration tests for market data consumption in the Dashboard Backend.
 * Verifies that ticks, candles, and alerts published to Kafka are correctly 
 * persisted in the database and cached in Redis.
 */
public class MarketDataIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private TickRepository tickRepository;

    @Autowired
    private CandleRepository candleRepository;

    @Autowired
    private AlertRepository alertRepository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * Verifies that a tick published to Kafka is persisted to the database.
     */
    @Test
    void shouldConsumeAndPersistTick() {
        Tick tick = Tick.newBuilder()
                .setSymbol("AAPL")
                .setPrice(150.0)
                .setVolume(100L)
                .setTimestamp(Instant.now().toEpochMilli())
                .build();

        kafkaTemplate.send("market.data.raw", "AAPL", tick);

        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            assertThat(tickRepository.findAll()).hasSize(1);
            assertThat(tickRepository.findAll().get(0).getSymbol()).isEqualTo("AAPL");
        });
    }

    /**
     * Verifies that a candle published to Kafka is persisted to the database 
     * and cached in Redis.
     */
    @Test
    void shouldConsumeAndPersistCandleAndCacheInRedis() {
        Candle candle = Candle.newBuilder()
                .setSymbol("MSFT")
                .setOpen(300.0)
                .setHigh(310.0)
                .setLow(290.0)
                .setClose(305.0)
                .setVolume(1000L)
                .setWindowStart(Instant.now().toEpochMilli())
                .setWindowEnd(Instant.now().plusSeconds(60).toEpochMilli())
                .build();

        kafkaTemplate.send("market.data.aggregated", "MSFT", candle);

        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            assertThat(candleRepository.findAll()).hasSize(1);
            assertThat(redisTemplate.opsForValue().get("latest_candle:MSFT")).isNotNull();
        });
    }

    /**
     * Verifies that an alert published to Kafka is persisted to the database.
     */
    @Test
    void shouldConsumeAndPersistAlert() {
        Alert alert = Alert.newBuilder()
                .setSymbol("TSLA")
                .setType("HIGH_VOLATILITY")
                .setMessage("Price jumped 5%")
                .setTimestamp(Instant.now().toEpochMilli())
                .build();

        kafkaTemplate.send("market.data.alerts", "TSLA", alert);

        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            assertThat(alertRepository.findAll()).hasSize(1);
            assertThat(alertRepository.findAll().get(0).getSymbol()).isEqualTo("TSLA");
        });
    }
}
