package com.smartcivic.scheduler;

import com.smartcivic.entity.*;
import com.smartcivic.repository.IssueHistoryRepository;
import com.smartcivic.repository.IssueRepository;
import com.smartcivic.service.WebSocketService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SlaSchedulerTest {

    @Mock
    private IssueRepository issueRepository;

    @Mock
    private IssueHistoryRepository issueHistoryRepository;

    @Mock
    private WebSocketService webSocketService;

    @InjectMocks
    private SlaScheduler slaScheduler;

    private Issue testIssue;

    @BeforeEach
    void setUp() {
        Category category = Category.builder()
                .id(1L)
                .name("Water Leak")
                .defaultSlaHours(24)
                .build();

        testIssue = Issue.builder()
                .id(100L)
                .title("Burst Pipe")
                .description("Water gushing onto main street.")
                .category(category)
                .status(IssueStatus.ACKNOWLEDGED)
                .priority(IssuePriority.MEDIUM)
                .slaDeadline(LocalDateTime.now().minusHours(2)) // Breached 2 hours ago
                .isEscalated(false)
                .build();
    }

    @Test
    void runSlaCheck_EscalatesOverdueIssues() {
        when(issueRepository.findUnresolvedOverdueIssues(any(LocalDateTime.class)))
                .thenReturn(List.of(testIssue));
        when(issueRepository.save(any(Issue.class))).thenReturn(testIssue);

        slaScheduler.runSlaCheck(LocalDateTime.now());

        assertTrue(testIssue.isEscalated());
        assertEquals(IssuePriority.CRITICAL, testIssue.getPriority());

        verify(issueRepository, times(1)).save(testIssue);
        verify(issueHistoryRepository, times(1)).save(any(IssueHistory.class));
        verify(webSocketService, times(1)).notifySlaEscalation(eq(100L), anyString());
    }

    @Test
    void runSlaCheck_NoOverdueIssues_DoesNothing() {
        when(issueRepository.findUnresolvedOverdueIssues(any(LocalDateTime.class)))
                .thenReturn(List.of());

        slaScheduler.runSlaCheck(LocalDateTime.now());

        verify(issueRepository, never()).save(any(Issue.class));
        verify(issueHistoryRepository, never()).save(any(IssueHistory.class));
        verify(webSocketService, never()).notifySlaEscalation(anyLong(), anyString());
    }
}
