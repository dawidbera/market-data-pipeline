import { Injectable, signal, WritableSignal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { WebSocketService } from './websocket.service';
import { map } from 'rxjs/operators';
import { firstValueFrom } from 'rxjs';

export interface Candle {
  symbol: string;
  open: number;
  high: number;
  low: number;
  close: number;
  volume: number;
  window_start: number; // timestamp in ms
  window_end: number;
}

export interface Alert {
  symbol: string;
  type: string;
  message: string;
  timestamp: number;
}

@Injectable({
  providedIn: 'root'
})
export class MarketDataService {
  
  // Signals for state management
  public latestCandle: WritableSignal<Candle | null> = signal(null);
  public alerts: WritableSignal<Alert[]> = signal([]);
  public historicalCandles: WritableSignal<Candle[]> = signal([]);

  private readonly API_URL = 'http://localhost:8080/api/market'; // API Gateway URL

  constructor(private wsService: WebSocketService, private http: HttpClient) {
    this.wsService.activateWithConfig();
    this.subscribeToCandles();
    this.subscribeToAlerts();
  }

  // Fetch initial history via REST
  async loadHistory(symbol: string) {
    try {
      const rawCandles = await firstValueFrom(
        this.http.get<any[]>(`${this.API_URL}/candles/${symbol}`)
      );
      const candles = rawCandles.map(c => ({
        symbol: c.symbol,
        open: Number(c.open),
        high: Number(c.high),
        low: Number(c.low),
        close: Number(c.close),
        volume: Number(c.volume),
        window_start: typeof c.window_start === 'string' ? Date.parse(c.window_start) : Number(c.window_start?.$numberLong || c.window_start),
        window_end: typeof c.window_end === 'string' ? Date.parse(c.window_end) : Number(c.window_end?.$numberLong || c.window_end)
      })) as Candle[];
      // The API returns most recent first, chart needs oldest first usually, or we sort it.
      // Let's ensure it's sorted by time ascending for the chart.
      const sorted = candles.sort((a, b) => a.window_start - b.window_start);
      this.historicalCandles.set(sorted);
    } catch (e) {
      console.error('Failed to load history', e);
    }
  }

  private subscribeToCandles() {
    // Hardcoded to AAPL for MVP
    this.wsService.watch('/topic/candles/AAPL').pipe(
      map(message => {
        const parsed = JSON.parse(message.body);
        return {
          symbol: parsed.symbol,
          open: Number(parsed.open),
          high: Number(parsed.high),
          low: Number(parsed.low),
          close: Number(parsed.close),
          volume: Number(parsed.volume),
          window_start: typeof parsed.window_start === 'string' ? Date.parse(parsed.window_start) : Number(parsed.window_start?.$numberLong || parsed.window_start),
          window_end: typeof parsed.window_end === 'string' ? Date.parse(parsed.window_end) : Number(parsed.window_end?.$numberLong || parsed.window_end)
        } as Candle;
      })
    ).subscribe({
      next: (candle) => {
        // console.log('Received candle:', candle);
        this.latestCandle.set(candle);
      },
      error: (err) => console.error('WS Error:', err)
    });
  }

  private subscribeToAlerts() {
    this.wsService.watch('/topic/alerts').pipe(
      map(message => JSON.parse(message.body) as Alert)
    ).subscribe({
      next: (alert) => {
        // console.log('Received alert:', alert);
        this.alerts.update(current => [alert, ...current].slice(0, 50)); // Keep last 50
      },
      error: (err) => console.error('WS Error:', err)
    });
  }
}
