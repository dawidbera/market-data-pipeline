import { Component, ElementRef, OnInit, ViewChild, effect, AfterViewInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MarketDataService, Candle } from '../../services/market-data.service';

@Component({
  selector: 'app-chart',
  standalone: true,
  imports: [CommonModule], // CommonModule needed for @for, @empty etc.
  template: `
    <div class="relative w-full h-full bg-slate-900 rounded-lg shadow-lg overflow-hidden flex flex-col">
      <div class="absolute top-2 left-2 z-10 bg-slate-800/80 px-2 py-1 rounded text-xs text-slate-300">
        AAPL / USD
      </div>
      <div #chartContainer class="w-full h-[500px]"></div>
    </div>
  `,
  styles: []
})
export class ChartComponent implements AfterViewInit, OnDestroy {
  @ViewChild('chartContainer') chartContainer!: ElementRef;

  private chart: any | null = null; // Use any to bypass type issues
  private candlestickSeries: any | null = null;
  private resizeObserver: ResizeObserver | null = null;

  constructor(private marketData: MarketDataService) {
    // Effect to handle real-time updates
    effect(() => {
      const candle = this.marketData.latestCandle();
      if (candle && this.candlestickSeries) {
        this.updateChart(candle);
      }
    });

    // Effect to handle history loading
    effect(() => {
      const history = this.marketData.historicalCandles();
      if (history.length > 0 && this.candlestickSeries) {
        const data: any[] = history.map(c => ({
          time: (c.window_start / 1000) as any, // Lightweight charts expects seconds for unix time
          open: c.open,
          high: c.high,
          low: c.low,
          close: c.close
        }));
        (this.candlestickSeries as any).setData(data);
        (this.chart as any).timeScale().fitContent();
      }
    });
  }

  ngAfterViewInit() {
    if (!this.chartContainer) return;

    this.initChart(); 
    this.marketData.loadHistory('AAPL');
    
    // Handle resizing
    this.resizeObserver = new ResizeObserver(entries => {
      if (entries.length === 0 || !entries[0].contentRect) return;
      const { width, height } = entries[0].contentRect;
      (this.chart as any).applyOptions({ width, height });
    });
    this.resizeObserver.observe(this.chartContainer.nativeElement);
  }

  ngOnDestroy() {
    this.resizeObserver?.disconnect();
    (this.chart as any).remove();
  }

  private initChart() {
    this.chart = ((window as any).LightweightCharts as any).createChart(this.chartContainer.nativeElement, {
      layout: {
        background: { type: 'Solid', color: '#0f172a' }, // Slate 900
        textColor: '#cbd5e1', // Slate 300
      },
      grid: {
        vertLines: { color: '#1e293b' },
        horzLines: { color: '#1e293b' },
      },
      width: this.chartContainer.nativeElement.clientWidth,
      height: 500,
      timeScale: {
        timeVisible: true,
        secondsVisible: true,
      },
    });

    this.candlestickSeries = (this.chart as any).addCandlestickSeries({ 
      upColor: '#22c55e', // Green 500
      downColor: '#ef4444', // Red 500
      borderVisible: false,
      wickUpColor: '#22c55e',
      wickDownColor: '#ef4444',
    });
  }

  private updateChart(candle: Candle) {
    const item: any = {
      time: (candle.window_start / 1000) as any,
      open: candle.open,
      high: candle.high,
      low: candle.low,
      close: candle.close
    };
    (this.candlestickSeries as any).update(item);
  }
}
