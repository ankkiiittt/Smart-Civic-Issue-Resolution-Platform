import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

export interface WsMessage {
  issueId: number;
  message: string;
  timestamp: number;
  type: 'UPDATE' | 'ESCALATION';
}

export function connectWebSocket(
  onUpdate: (msg: WsMessage) => void,
  onEscalation: (msg: WsMessage) => void
) {
  const client = new Client({
    // Standard WS URL
    brokerURL: 'ws://localhost:8080/ws',
    reconnectDelay: 5000,
    heartbeatIncoming: 4000,
    heartbeatOutgoing: 4000,
  });

  // Fallback SockJS factory for older environment compatibility
  client.webSocketFactory = () => {
    return new SockJS('http://localhost:8080/ws');
  };

  client.onConnect = () => {
    console.log('STOMP client connected');

    // Subscribe to standard status change topics
    client.subscribe('/topic/issues', (message) => {
      try {
        const payload: WsMessage = JSON.parse(message.body);
        onUpdate(payload);
      } catch (err) {
        console.error('Failed to parse WS issue update', err);
      }
    });

    // Subscribe to critical SLA escalation notifications
    client.subscribe('/topic/escalations', (message) => {
      try {
        const payload: WsMessage = JSON.parse(message.body);
        onEscalation(payload);
      } catch (err) {
        console.error('Failed to parse WS SLA escalation', err);
      }
    });
  };

  client.onStompError = (frame) => {
    console.error('STOMP Broker Error:', frame.headers['message'], frame.body);
  };

  client.activate();

  // Return unsubscribe cleanup hook
  return () => {
    client.deactivate();
  };
}
