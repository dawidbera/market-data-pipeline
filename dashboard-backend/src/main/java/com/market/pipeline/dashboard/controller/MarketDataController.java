package com.market.pipeline.dashboard.controller;

import com.market.pipeline.dashboard.entity.CandleEntity;
import com.market.pipeline.dashboard.repository.CandleRepository;
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
public class MarketDataController {

    private final CandleRepository candleRepository;

    /**
     * Constructs the controller with the required repository.
     * 
     * @param candleRepository the repository for candle data
     */
    public MarketDataController(CandleRepository candleRepository) {
        this.candleRepository = candleRepository;
    }

    /**
     * Retrieves historical candle data for a specific market symbol.
     * 
     * @param symbol the market symbol (e.g., "AAPL")
     * @return a list of historical candles ordered by window start time
     */
    @GetMapping("/candles/{symbol}")
    public List<CandleEntity> getHistoricalCandles(@PathVariable String symbol) {
        return candleRepository.findBySymbolOrderByWindowStartDesc(symbol);
    }
}
