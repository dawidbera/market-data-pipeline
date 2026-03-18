import { Component, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet } from '@angular/router';
import { ChartComponent } from './components/chart/chart.component';
import { AlertsComponent } from './components/alerts/alerts.component';
import { MarketDataService } from './services/market-data.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet, ChartComponent, AlertsComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent {
  now = new Date();
  
  // Dynamic metrics from service
  symbol = computed(() => this.marketData.latestCandle()?.symbol || 'AAPL');
  price = computed(() => this.marketData.latestCandle()?.close || 0);
  
  // Simulation for demo purposes if backend doesn't provide it yet
  latency = Math.floor(Math.random() * 50) + 120; // 120-170ms
  throughput = Math.floor(Math.random() * 10) + 40; // 40-50 req/s

  constructor(private marketData: MarketDataService) {
    // Update time every second
    setInterval(() => {
      this.now = new Date();
      // Jitter the latency/throughput slightly for visual effect
      this.latency = Math.floor(Math.random() * 50) + 120;
      this.throughput = Math.floor(Math.random() * 10) + 40;
    }, 1000);
  }
}
