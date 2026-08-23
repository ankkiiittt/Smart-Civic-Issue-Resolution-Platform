package com.smartcivic.service;

public interface WebSocketService {
    void notifyIssueUpdate(Long issueId, String message);
    void notifySlaEscalation(Long issueId, String message);
}
