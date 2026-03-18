import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ChartComponent } from './chart.component';
import { MarketDataService } from '../../services/market-data.service';
import { signal, WritableSignal } from '@angular/core';

class MockMarketDataService {
  latestCandle = signal(null);
  historicalCandles = signal([]);
  loadHistory(symbol: string) {}
}

describe('ChartComponent', () => {
  let component: ChartComponent;
  let fixture: ComponentFixture<ChartComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ChartComponent],
      providers: [
        { provide: MarketDataService, useClass: MockMarketDataService }
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ChartComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should have a chart container', () => {
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('div[class*="w-full h-[500px]"]')).toBeTruthy();
  });
});
