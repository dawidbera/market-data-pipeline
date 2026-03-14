package com.market.pipeline.ingestor.controller;

import com.market.pipeline.common.avro.Tick;
import com.market.pipeline.ingestor.dto.TickRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

/**
 * REST Controller for receiving market data ticks from external sources.
 * It provides an endpoint for Gatling to perform stress-testing of the ingestion pipeline.
 */
@RestController
@RequestMapping("/api/ingest")
public class MarketDataController {

    private static final Logger log = LoggerFactory.getLogger(MarketDataController.class);
    private static final String TOPIC = "market.data.raw";
    private final KafkaTemplate<String, Tick> kafkaTemplate;

    public MarketDataController(KafkaTemplate<String, Tick> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Receives a tick request, converts it to Avro, and publishes to Kafka.
     * 
     * @param request the tick request from the caller
     */
    @PostMapping("/tick")
    public void ingestTick(@RequestBody TickRequest request) {
        long timestamp = request.getTimestamp() != null ? request.getTimestamp() : Instant.now().toEpochMilli();
        
        Tick tick = Tick.newBuilder()
                .setSymbol(request.getSymbol())
                .setPrice(request.getPrice())
                .setVolume(request.getVolume())
                .setTimestamp(timestamp)
                .build();

        kafkaTemplate.send(TOPIC, request.getSymbol(), tick);
        log.debug("API Ingested Tick: {}", tick);
    }
}
