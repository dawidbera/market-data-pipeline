package com.market.pipeline.performance;

import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

/**
 * Gatling simulation for stress-testing the market data ingestion pipeline.
 * It simulates thousands of stock tick requests hitting the ingestor-service.
 */
public class TickIngestionSimulation extends Simulation {

    private final HttpProtocolBuilder httpProtocol = http
            .baseUrl("http://localhost:8081") // Default port for ingestor-service
            .acceptHeader("application/json")
            .contentTypeHeader("application/json");

    private final String[] SYMBOLS = {"AAPL", "MSFT", "GOOGL", "AMZN", "TSLA"};
    private final Random random = new Random();

    // Feeder for random tick data
    private final Iterator<Map<String, Object>> feeder = Stream.generate((Supplier<Map<String, Object>>) () -> {
        String symbol = SYMBOLS[random.nextInt(SYMBOLS.length)];
        double price = 100.0 + (random.nextDouble() * 50.0);
        long volume = 100 + random.nextInt(1000);
        return Map.of(
                "symbol", symbol,
                "price", price,
                "volume", volume
        );
    }).iterator();

    private final ChainBuilder ingestTick =
            feed(feeder)
                    .exec(http("Ingest Tick")
                            .post("/api/ingest/tick")
                            .body(StringBody("{ \"symbol\": \"#{symbol}\", \"price\": #{price}, \"volume\": #{volume} }"))
                            .check(status().is(200)));

    private final ScenarioBuilder scn = scenario("Market Data Ingestion")
            .exec(ingestTick);

    public TickIngestionSimulation() {
        this.setUp(
                scn.injectOpen(
                        nothingFor(5), // Warm up
                        rampUsers(100).during(10), // Ramp up
                        constantUsersPerSec(50).during(60) // Steady state load
                )
        ).protocols(httpProtocol);
    }
}
