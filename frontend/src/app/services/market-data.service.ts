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
      const candles = await firstValueFrom(
        this.http.get<Candle[]>(`${this.API_URL}/candles/${symbol}`)
      );
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
      map(message => JSON.parse(message.body) as Candle)
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
