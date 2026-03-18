import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AlertsComponent } from './alerts.component';
import { MarketDataService, Alert } from '../../services/market-data.service';
import { signal } from '@angular/core';
import { By } from '@angular/platform-browser';

class MockMarketDataService {
  alerts = signal<Alert[]>([]);
}

describe('AlertsComponent', () => {
  let component: AlertsComponent;
  let fixture: ComponentFixture<AlertsComponent>;
  let mockService: MockMarketDataService;

  beforeEach(async () => {
    mockService = new MockMarketDataService();

    await TestBed.configureTestingModule({
      imports: [AlertsComponent],
      providers: [
        { provide: MarketDataService, useValue: mockService }
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AlertsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should display "No alerts" when list is empty', () => {
    mockService.alerts.set([]);
    fixture.detectChanges();
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.textContent).toContain('No alerts detected yet');
  });

  it('should display alerts when signal is updated', () => {
    const mockAlert: Alert = {
      symbol: 'AAPL',
      type: 'PRICE_SPIKE',
      message: 'Huge spike!',
      timestamp: Date.now()
    };
    
    mockService.alerts.set([mockAlert]);
    fixture.detectChanges();
    
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.textContent).toContain('PRICE_SPIKE');
    expect(compiled.textContent).toContain('Huge spike!');
  });
});
