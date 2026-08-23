package com.smartcivic.scheduler;

import com.smartcivic.entity.Issue;
import com.smartcivic.entity.IssueHistory;
import com.smartcivic.entity.IssuePriority;
import com.smartcivic.repository.IssueHistoryRepository;
import com.smartcivic.repository.IssueRepository;
import com.smartcivic.service.WebSocketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class SlaScheduler {

    private static final Logger log = LoggerFactory.getLogger(SlaScheduler.class);

    private final IssueRepository issueRepository;
    private final IssueHistoryRepository issueHistoryRepository;
    private final WebSocketService webSocketService;

    public SlaScheduler(IssueRepository issueRepository,
                        IssueHistoryRepository issueHistoryRepository,
                        WebSocketService webSocketService) {
        this.issueRepository = issueRepository;
        this.issueHistoryRepository = issueHistoryRepository;
        this.webSocketService = webSocketService;
    }

    @Scheduled(cron = "${sla.schedule.cron:0 0/15 * * * ?}")
    @Transactional
    public void checkSlaBreaches() {
        log.info("Starting SLA breach verification task...");
        runSlaCheck(LocalDateTime.now());
        log.info("SLA breach verification task completed.");
    }

    @Transactional
    public void runSlaCheck(LocalDateTime checkTime) {
        List<Issue> overdueIssues = issueRepository.findUnresolvedOverdueIssues(checkTime);
        if (overdueIssues.isEmpty()) {
            return;
        }

        log.warn("Found {} issues that breached their SLA deadlines. Escalating...", overdueIssues.size());

        for (Issue issue : overdueIssues) {
            String prevPriority = issue.getPriority().name();
            issue.setEscalated(true);
            issue.setPriority(IssuePriority.CRITICAL);
            issueRepository.save(issue);

            // Audit log
            IssueHistory history = IssueHistory.builder()
                    .issue(issue)
                    .statusFrom(issue.getStatus().name())
                    .statusTo(issue.getStatus().name())
                    .comments("System auto-escalated: SLA deadline breached. Priority raised from " + prevPriority + " to CRITICAL.")
                    .changedBy(null) // System operation
                    .build();
            issueHistoryRepository.save(history);

            // Push real-time notification
            String alertMessage = String.format("SLA BREACH: Issue #%d (%s) is overdue. Escalated to CRITICAL.", issue.getId(), issue.getTitle());
            webSocketService.notifySlaEscalation(issue.getId(), alertMessage);
        }
    }
}
