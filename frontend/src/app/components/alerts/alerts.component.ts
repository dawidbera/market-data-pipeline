import { Component, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MarketDataService, Alert } from '../../services/market-data.service';

@Component({
  selector: 'app-alerts',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="bg-slate-900 rounded-lg shadow-lg p-4 h-[500px] flex flex-col">
      <h2 class="text-xl font-bold text-slate-100 mb-4 flex items-center gap-2">
        <span class="w-2 h-2 rounded-full bg-red-500 animate-pulse"></span>
        Market Alerts
      </h2>
      
      <div class="overflow-y-auto flex-1 space-y-3 pr-2 scrollbar-thin scrollbar-thumb-slate-700">
        @for (alert of alerts(); track alert.timestamp) {
          <div class="bg-slate-800/50 p-3 rounded border-l-4 border-red-500 hover:bg-slate-800 transition-colors">
            <div class="flex justify-between items-start mb-1">
              <span class="font-bold text-red-400 text-sm">{{ alert.type }}</span>
              <span class="text-xs text-slate-500">{{ alert.timestamp | date:'HH:mm:ss' }}</span>
            </div>
            <p class="text-slate-300 text-sm">{{ alert.message }}</p>
            <div class="text-xs text-slate-500 mt-1 font-mono">{{ alert.symbol }}</div>
          </div>
        } @empty {
          <div class="text-center text-slate-500 py-10">
            No alerts detected yet...
          </div>
        }
      </div>
    </div>
  `,
  styles: []
})
export class AlertsComponent {
  alerts;

  constructor(private marketData: MarketDataService) {
    this.alerts = this.marketData.alerts;
  }
}
