package com.market.pipeline.ingestor;

import com.market.pipeline.common.avro.Tick;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Random;

/**
 * Component responsible for generating and publishing simulated market data ticks.
 * It simulates live ticks for various stock symbols and sends them to the "market.data.raw" Kafka topic.
 */
@Component
public class MarketDataIngestor {

    private static final Logger log = LoggerFactory.getLogger(MarketDataIngestor.class);
    private static final String TOPIC = "market.data.raw";
    private final KafkaTemplate<String, Tick> kafkaTemplate;
    private final Random random = new Random();
    private final String[] SYMBOLS = {"AAPL", "GOOGL", "MSFT", "AMZN", "TSLA"};

    public MarketDataIngestor(KafkaTemplate<String, Tick> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Periodically generates a new market tick with random price and volume, 
     * and publishes it to Kafka.
     */
    @Scheduled(fixedRate = 1000) // Every 1 second
    public void generateAndSendMarketData() {
        String symbol = SYMBOLS[random.nextInt(SYMBOLS.length)];
        double price = 100.0 + (random.nextDouble() * 50.0); // Price between 100 and 150
        long volume = 100 + random.nextInt(1000); // Volume between 100 and 1100
        long timestamp = Instant.now().toEpochMilli();

        Tick tick = Tick.newBuilder()
                .setSymbol(symbol)
                .setPrice(price)
                .setVolume(volume)
                .setTimestamp(timestamp)
                .build();

        kafkaTemplate.send(TOPIC, symbol, tick);
        log.info("Produced Tick: {}", tick);
    }
}
