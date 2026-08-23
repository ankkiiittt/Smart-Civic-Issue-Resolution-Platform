package com.smartcivic.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "issue_history")
public class IssueHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issue_id", nullable = false)
    private Issue issue;

    @Column(name = "status_from", length = 50)
    private String statusFrom;

    @Column(name = "status_to", nullable = false, length = 50)
    private String statusTo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by_id", nullable = false)
    private User changedBy;

    @Column(columnDefinition = "TEXT")
    private String comments;

    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;

    @PrePersist
    protected void onCreate() {
        changedAt = LocalDateTime.now();
    }

    public IssueHistory() {}

    public IssueHistory(Long id, Issue issue, String statusFrom, String statusTo, User changedBy, String comments, LocalDateTime changedAt) {
        this.id = id;
        this.issue = issue;
        this.statusFrom = statusFrom;
        this.statusTo = statusTo;
        this.changedBy = changedBy;
        this.comments = comments;
        this.changedAt = changedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Issue getIssue() { return issue; }
    public void setIssue(Issue issue) { this.issue = issue; }

    public String getStatusFrom() { return statusFrom; }
    public void setStatusFrom(String statusFrom) { this.statusFrom = statusFrom; }

    public String getStatusTo() { return statusTo; }
    public void setStatusTo(String statusTo) { this.statusTo = statusTo; }

    public User getChangedBy() { return changedBy; }
    public void setChangedBy(User changedBy) { this.changedBy = changedBy; }

    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }

    public LocalDateTime getChangedAt() { return changedAt; }
    public void setChangedAt(LocalDateTime changedAt) { this.changedAt = changedAt; }

    public static IssueHistoryBuilder builder() {
        return new IssueHistoryBuilder();
    }

    public static class IssueHistoryBuilder {
        private Long id;
        private Issue issue;
        private String statusFrom;
        private String statusTo;
        private User changedBy;
        private String comments;
        private LocalDateTime changedAt;

        IssueHistoryBuilder() {}

        public IssueHistoryBuilder id(Long id) { this.id = id; return this; }
        public IssueHistoryBuilder issue(Issue issue) { this.issue = issue; return this; }
        public IssueHistoryBuilder statusFrom(String statusFrom) { this.statusFrom = statusFrom; return this; }
        public IssueHistoryBuilder statusTo(String statusTo) { this.statusTo = statusTo; return this; }
        public IssueHistoryBuilder changedBy(User changedBy) { this.changedBy = changedBy; return this; }
        public IssueHistoryBuilder comments(String comments) { this.comments = comments; return this; }
        public IssueHistoryBuilder changedAt(LocalDateTime changedAt) { this.changedAt = changedAt; return this; }

        public IssueHistory build() {
            return new IssueHistory(id, issue, statusFrom, statusTo, changedBy, comments, changedAt);
        }
    }
}
