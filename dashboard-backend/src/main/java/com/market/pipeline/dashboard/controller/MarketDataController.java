package com.market.pipeline.dashboard.controller;

import com.market.pipeline.dashboard.entity.AlertEntity;
import com.market.pipeline.dashboard.entity.CandleEntity;
import com.market.pipeline.dashboard.repository.AlertRepository;
import com.market.pipeline.dashboard.repository.CandleRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for exposing market data to the frontend.
 * Provides endpoints for historical candle data and potentially other market metrics.
 */
@RestController
@RequestMapping("/api/market")
@Tag(name = "Market Data API", description = "Endpoints for retrieving historical market data and alerts")
public class MarketDataController {

    private final CandleRepository candleRepository;
    private final AlertRepository alertRepository;

    /**
     * Constructs the controller with the required repositories.
     * 
     * @param candleRepository the repository for candle data
     * @param alertRepository the repository for alert data
     */
    public MarketDataController(CandleRepository candleRepository, AlertRepository alertRepository) {
        this.candleRepository = candleRepository;
        this.alertRepository = alertRepository;
    }

    /**
     * Retrieves historical candle data for a specific market symbol.
     * 
     * @param symbol the market symbol (e.g., "AAPL")
     * @return a list of historical candles ordered by window start time
     */
    @Operation(
        summary = "Get historical candles",
        description = "Retrieves a list of OHLC candles for a given symbol, ordered by most recent first.",
        responses = {
            @ApiResponse(responseCode = "200", description = "List of candles retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Symbol not found")
        }
    )
    @GetMapping("/candles/{symbol}")
    public List<CandleEntity> getHistoricalCandles(
            @Parameter(description = "Market symbol (e.g. AAPL, BTC/USD)", example = "AAPL")
            @PathVariable String symbol) {
        return candleRepository.findBySymbolOrderByWindowStartDesc(symbol);
    }

    /**
     * Retrieves historical alerts for a specific market symbol.
     * 
     * @param symbol the market symbol (e.g., "AAPL")
     * @return a list of historical alerts ordered by timestamp
     */
    @Operation(
        summary = "Get historical alerts",
        description = "Retrieves a list of anomaly alerts for a given symbol, ordered by most recent first.",
        responses = {
            @ApiResponse(responseCode = "200", description = "List of alerts retrieved successfully")
        }
    )
    @GetMapping("/alerts/{symbol}")
    public List<AlertEntity> getHistoricalAlerts(
            @Parameter(description = "Market symbol", example = "AAPL")
            @PathVariable String symbol) {
        return alertRepository.findBySymbolOrderByTimestampDesc(symbol);
    }
}
