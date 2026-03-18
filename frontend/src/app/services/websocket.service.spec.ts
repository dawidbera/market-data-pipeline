import { TestBed } from '@angular/core/testing';
import { WebSocketService } from './websocket.service';

describe('WebSocketService', () => {
  let service: WebSocketService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [WebSocketService]
    });
    service = TestBed.inject(WebSocketService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should configure and activate RxStomp on activateWithConfig', () => {
    spyOn(service, 'configure');
    spyOn(service, 'activate');

    service.activateWithConfig();

    expect(service.configure).toHaveBeenCalled();
    expect(service.activate).toHaveBeenCalled();
  });
});
