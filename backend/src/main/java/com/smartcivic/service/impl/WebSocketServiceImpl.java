package com.smartcivic.service.impl;

import com.smartcivic.service.WebSocketService;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class WebSocketServiceImpl implements WebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketServiceImpl(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public void notifyIssueUpdate(Long issueId, String message) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("issueId", issueId);
        payload.put("message", message);
        payload.put("timestamp", System.currentTimeMillis());
        payload.put("type", "UPDATE");

        // Broadcast to general stream and specific issue channel
        messagingTemplate.convertAndSend("/topic/issues", payload);
        messagingTemplate.convertAndSend("/topic/issues/" + issueId, payload);
    }

    @Override
    public void notifySlaEscalation(Long issueId, String message) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("issueId", issueId);
        payload.put("message", message);
        payload.put("timestamp", System.currentTimeMillis());
        payload.put("type", "ESCALATION");

        // Broadcast to escalations stream and specific issue channel
        messagingTemplate.convertAndSend("/topic/escalations", payload);
        messagingTemplate.convertAndSend("/topic/issues/" + issueId, payload);
    }
}
