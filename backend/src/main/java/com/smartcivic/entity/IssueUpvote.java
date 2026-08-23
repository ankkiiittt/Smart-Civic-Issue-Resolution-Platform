package com.smartcivic.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "issue_upvotes", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"issue_id", "user_id"})
})
public class IssueUpvote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issue_id", nullable = false)
    private Issue issue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public IssueUpvote() {}

    public IssueUpvote(Long id, Issue issue, User user, LocalDateTime createdAt) {
        this.id = id;
        this.issue = issue;
        this.user = user;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Issue getIssue() { return issue; }
    public void setIssue(Issue issue) { this.issue = issue; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public static IssueUpvoteBuilder builder() {
        return new IssueUpvoteBuilder();
    }

    public static class IssueUpvoteBuilder {
        private Long id;
        private Issue issue;
        private User user;
        private LocalDateTime createdAt;

        IssueUpvoteBuilder() {}

        public IssueUpvoteBuilder id(Long id) { this.id = id; return this; }
        public IssueUpvoteBuilder issue(Issue issue) { this.issue = issue; return this; }
        public IssueUpvoteBuilder user(User user) { this.user = user; return this; }
        public IssueUpvoteBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public IssueUpvote build() {
            return new IssueUpvote(id, issue, user, createdAt);
        }
    }
}
