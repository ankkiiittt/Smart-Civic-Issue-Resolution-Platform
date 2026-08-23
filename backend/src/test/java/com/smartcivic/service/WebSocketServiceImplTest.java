package com.smartcivic.service;

import com.smartcivic.service.impl.WebSocketServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class WebSocketServiceImplTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private WebSocketServiceImpl webSocketService;

    @Test
    void notifyIssueUpdate_SendsToCorrectDestinations() {
        webSocketService.notifyIssueUpdate(100L, "New issue reported");

        verify(messagingTemplate, times(1)).convertAndSend(eq("/topic/issues"), any(Map.class));
        verify(messagingTemplate, times(1)).convertAndSend(eq("/topic/issues/100"), any(Map.class));
    }

    @Test
    void notifySlaEscalation_SendsToCorrectDestinations() {
        webSocketService.notifySlaEscalation(100L, "Overdue escalation");

        verify(messagingTemplate, times(1)).convertAndSend(eq("/topic/escalations"), any(Map.class));
        verify(messagingTemplate, times(1)).convertAndSend(eq("/topic/issues/100"), any(Map.class));
    }
}
