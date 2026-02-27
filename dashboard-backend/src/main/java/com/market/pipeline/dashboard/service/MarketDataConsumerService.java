package com.market.pipeline.dashboard.service;

import com.market.pipeline.common.avro.Alert;
import com.market.pipeline.common.avro.Candle;
import com.market.pipeline.common.avro.Tick;
import com.market.pipeline.dashboard.entity.AlertEntity;
import com.market.pipeline.dashboard.entity.CandleEntity;
import com.market.pipeline.dashboard.entity.TickEntity;
import com.market.pipeline.dashboard.repository.AlertRepository;
import com.market.pipeline.dashboard.repository.CandleRepository;
import com.market.pipeline.dashboard.repository.TickRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * Service responsible for consuming market data from Kafka topics.
 * It handles raw ticks, aggregated candles, and alerts, providing persistence 
 * to TimescaleDB, caching in Redis, and real-time updates via WebSockets.
 */
@Service
public class MarketDataConsumerService {

    private static final Logger log = LoggerFactory.getLogger(MarketDataConsumerService.class);
    private final SimpMessagingTemplate messagingTemplate;
    private final RedisTemplate<String, Object> redisTemplate;
    private final TickRepository tickRepository;
    private final CandleRepository candleRepository;
    private final AlertRepository alertRepository;

    public MarketDataConsumerService(SimpMessagingTemplate messagingTemplate, 
                                     RedisTemplate<String, Object> redisTemplate,
                                     TickRepository tickRepository,
                                     CandleRepository candleRepository,
                                     AlertRepository alertRepository) {
        this.messagingTemplate = messagingTemplate;
        this.redisTemplate = redisTemplate;
        this.tickRepository = tickRepository;
        this.candleRepository = candleRepository;
        this.alertRepository = alertRepository;
    }

    /**
     * Consumes raw market ticks and persists them to the database.
     * 
     * @param tick the market tick data in Avro format
     */
    @KafkaListener(topics = "market.data.raw")
    public void consumeRawData(Tick tick) {
        log.debug("Consumed Tick: {}", tick);
        tickRepository.save(TickEntity.builder()
                .symbol(tick.getSymbol())
                .price(tick.getPrice())
                .volume(tick.getVolume())
                .timestamp(Instant.ofEpochMilli(tick.getTimestamp()))
                .build());
    }

    /**
     * Consumes aggregated candle data (OHLC), sends updates to WebSockets, 
     * caches the latest value in Redis, and persists it to the database.
     * 
     * @param candle the aggregated candle data in Avro format
     */
    @KafkaListener(topics = "market.data.aggregated")
    public void consumeAggregatedData(Candle candle) {
        log.info("Consumed Aggregated Candle: {}", candle);
        // Send to WebSockets
        messagingTemplate.convertAndSend("/topic/candles/" + candle.getSymbol(), candle.toString());
        // Store latest in Redis
        redisTemplate.opsForValue().set("latest_candle:" + candle.getSymbol(), candle.toString());
        // Persist to DB
        candleRepository.save(CandleEntity.builder()
                .symbol(candle.getSymbol())
                .open(candle.getOpen())
                .high(candle.getHigh())
                .low(candle.getLow())
                .close(candle.getClose())
                .volume(candle.getVolume())
                .windowStart(Instant.ofEpochMilli(candle.getWindowStart()))
                .windowEnd(Instant.ofEpochMilli(candle.getWindowEnd()))
                .build());
    }

    /**
     * Consumes market alerts, broadcasts them via WebSockets, and persists them to the database.
     * 
     * @param alert the market alert data in Avro format
     */
    @KafkaListener(topics = "market.data.alerts")
    public void consumeAlerts(Alert alert) {
        log.info("Consumed Alert: {}", alert);
        // Send to WebSockets
        messagingTemplate.convertAndSend("/topic/alerts", alert.toString());
        // Persist to DB
        alertRepository.save(AlertEntity.builder()
                .symbol(alert.getSymbol())
                .type(alert.getType())
                .message(alert.getMessage())
                .timestamp(Instant.ofEpochMilli(alert.getTimestamp()))
                .build());
    }
}

