import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AppComponent } from './app.component';
import { MarketDataService } from './services/market-data.service';
import { signal } from '@angular/core';
import { ChartComponent } from './components/chart/chart.component';
import { AlertsComponent } from './components/alerts/alerts.component';
import { CommonModule } from '@angular/common'; // Import CommonModule
import { Component } from '@angular/core'; // Import Component decorator

// Mock the child components
@Component({
  selector: 'app-chart',
  standalone: true,
  imports: [CommonModule], // Import CommonModule here
  template: '<div class="mock-chart">Mock Chart</div>'
})
class MockChartComponent {}

@Component({
  selector: 'app-alerts',
  standalone: true,
  imports: [CommonModule], // Import CommonModule here
  template: '<div class="mock-alerts">Mock Alerts</div>'
})
class MockAlertsComponent {}

class MockMarketDataService {
  latestCandle = signal(null);
  historicalCandles = signal([]);
  alerts = signal([]);
  loadHistory(symbol: string) {}
}

describe('AppComponent', () => {
  let component: AppComponent;
  let fixture: ComponentFixture<AppComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        AppComponent,
        MockChartComponent, // Import mock components
        MockAlertsComponent
      ],
      providers: [
        { provide: MarketDataService, useClass: MockMarketDataService }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(AppComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create the app', () => {
    expect(component).toBeTruthy();
  });

  it('should render the header title', () => {
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('h1')?.textContent).toContain('Market Data Pipeline');
  });

  it('should contain mock chart and alerts components', () => {
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('app-chart')).toBeTruthy();
    expect(compiled.querySelector('app-alerts')).toBeTruthy();
  });
});
