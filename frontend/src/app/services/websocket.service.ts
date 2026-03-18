import { Injectable } from '@angular/core';
import { RxStomp, RxStompConfig } from '@stomp/rx-stomp';
import { WebSocketSubject } from 'rxjs/webSocket';
import SockJS from 'sockjs-client';

@Injectable({
  providedIn: 'root',
})
export class WebSocketService extends RxStomp {
  constructor() {
    super();
  }

  public activateWithConfig(): void {
    const stompConfig: RxStompConfig = {
      // Typically login, passcode, host
      // brokerURL: 'ws://localhost:8080/ws-market', // Native WebSocket
      
      // Since the backend might be using SockJS fallback:
      webSocketFactory: () => {
        return new (SockJS as any)('http://localhost:8080/ws-market');
      },

      // Headers
      // connectHeaders: {
      //   login: 'guest',
      //   passcode: 'guest',
      // },

      // Heartbeat interval in milliseconds
      heartbeatIncoming: 0,
      heartbeatOutgoing: 20000,

      // Reconnect delay in milliseconds
      reconnectDelay: 200,

      // Debugging
      // debug: (msg: string) => {
      //   console.log(new Date(), msg);
      // },
    };

    this.configure(stompConfig);
    this.activate();
  }
}
