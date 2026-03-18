import { TestBed } from '@angular/core/testing';
import { MarketDataService, Candle, Alert } from './market-data.service';
import { WebSocketService } from './websocket.service';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { Subject, of } from 'rxjs';

class MockWebSocketService {
  private subject = new Subject<any>();

  activateWithConfig() {}
  
  watch(destination: string) {
    return this.subject.asObservable();
  }

  // Helper to simulate incoming message
  emit(body: any) {
    this.subject.next({ body: JSON.stringify(body) });
  }
}

describe('MarketDataService', () => {
  let service: MarketDataService;
  let wsService: MockWebSocketService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    wsService = new MockWebSocketService();

    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        MarketDataService,
        { provide: WebSocketService, useValue: wsService }
      ]
    });
    service = TestBed.inject(MarketDataService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should update latestCandle signal when a candle is received', () => {
    const mockCandle: Candle = {
      symbol: 'AAPL',
      open: 100,
      high: 110,
      low: 90,
      close: 105,
      volume: 1000,
      window_start: 1000,
      window_end: 2000
    };

    wsService.emit(mockCandle);

    expect(service.latestCandle()).toEqual(mockCandle);
  });

  it('should update alerts signal when an alert is received', () => {
    const mockAlert: Alert = {
      symbol: 'AAPL',
      type: 'PRICE_SPIKE',
      message: 'Price spike detected',
      timestamp: 1234567890
    };

    wsService.emit(mockAlert);

    const alerts = service.alerts();
    expect(alerts.length).toBe(1);
    expect(alerts[0]).toEqual(mockAlert);
  });

  it('should fetch history via REST', async () => {
    const mockHistory: Candle[] = [
      { symbol: 'AAPL', open: 100, high: 105, low: 95, close: 102, volume: 500, window_start: 1000, window_end: 2000 },
      { symbol: 'AAPL', open: 102, high: 108, low: 101, close: 107, volume: 600, window_start: 2000, window_end: 3000 }
    ];

    const promise = service.loadHistory('AAPL');

    const req = httpMock.expectOne('http://localhost:8080/api/market/candles/AAPL');
    expect(req.request.method).toBe('GET');
    req.flush(mockHistory);

    await promise;

    expect(service.historicalCandles().length).toBe(2);
    expect(service.historicalCandles()).toEqual(mockHistory);
  });
});
