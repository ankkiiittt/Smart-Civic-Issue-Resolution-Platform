package com.smartcivic.entity;

import jakarta.persistence.*;
import org.locationtech.jts.geom.Point;
import java.time.LocalDateTime;

@Entity
@Table(name = "issues")
public class Issue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private IssueStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private IssuePriority priority;

    @Column(name = "location_geom", columnDefinition = "geometry(Point, 4326)", nullable = false)
    private Point locationGeom;

    @Column(name = "address_description", length = 255)
    private String addressDescription;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_officer_id")
    private User assignedOfficer;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "sla_deadline")
    private LocalDateTime slaDeadline;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "is_escalated", nullable = false)
    private boolean isEscalated;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Issue() {}

    public Issue(Long id, String title, String description, Category category, IssueStatus status, IssuePriority priority, Point locationGeom, String addressDescription, User reporter, User assignedOfficer, LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime slaDeadline, LocalDateTime resolvedAt, boolean isEscalated) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.category = category;
        this.status = status;
        this.priority = priority;
        this.locationGeom = locationGeom;
        this.addressDescription = addressDescription;
        this.reporter = reporter;
        this.assignedOfficer = assignedOfficer;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.slaDeadline = slaDeadline;
        this.resolvedAt = resolvedAt;
        this.isEscalated = isEscalated;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public IssueStatus getStatus() { return status; }
    public void setStatus(IssueStatus status) { this.status = status; }

    public IssuePriority getPriority() { return priority; }
    public void setPriority(IssuePriority priority) { this.priority = priority; }

    public Point getLocationGeom() { return locationGeom; }
    public void setLocationGeom(Point locationGeom) { this.locationGeom = locationGeom; }

    public String getAddressDescription() { return addressDescription; }
    public void setAddressDescription(String addressDescription) { this.addressDescription = addressDescription; }

    public User getReporter() { return reporter; }
    public void setReporter(User reporter) { this.reporter = reporter; }

    public User getAssignedOfficer() { return assignedOfficer; }
    public void setAssignedOfficer(User assignedOfficer) { this.assignedOfficer = assignedOfficer; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public LocalDateTime getSlaDeadline() { return slaDeadline; }
    public void setSlaDeadline(LocalDateTime slaDeadline) { this.slaDeadline = slaDeadline; }

    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }

    public boolean isEscalated() { return isEscalated; }
    public void setEscalated(boolean escalated) { isEscalated = escalated; }

    public static IssueBuilder builder() {
        return new IssueBuilder();
    }

    public static class IssueBuilder {
        private Long id;
        private String title;
        private String description;
        private Category category;
        private IssueStatus status;
        private IssuePriority priority;
        private Point locationGeom;
        private String addressDescription;
        private User reporter;
        private User assignedOfficer;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private LocalDateTime slaDeadline;
        private LocalDateTime resolvedAt;
        private boolean isEscalated;

        IssueBuilder() {}

        public IssueBuilder id(Long id) { this.id = id; return this; }
        public IssueBuilder title(String title) { this.title = title; return this; }
        public IssueBuilder description(String description) { this.description = description; return this; }
        public IssueBuilder category(Category category) { this.category = category; return this; }
        public IssueBuilder status(IssueStatus status) { this.status = status; return this; }
        public IssueBuilder priority(IssuePriority priority) { this.priority = priority; return this; }
        public IssueBuilder locationGeom(Point locationGeom) { this.locationGeom = locationGeom; return this; }
        public IssueBuilder addressDescription(String addressDescription) { this.addressDescription = addressDescription; return this; }
        public IssueBuilder reporter(User reporter) { this.reporter = reporter; return this; }
        public IssueBuilder assignedOfficer(User assignedOfficer) { this.assignedOfficer = assignedOfficer; return this; }
        public IssueBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public IssueBuilder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }
        public IssueBuilder slaDeadline(LocalDateTime slaDeadline) { this.slaDeadline = slaDeadline; return this; }
        public IssueBuilder resolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; return this; }
        public IssueBuilder isEscalated(boolean isEscalated) { this.isEscalated = isEscalated; return this; }

        public Issue build() {
            return new Issue(id, title, description, category, status, priority, locationGeom, addressDescription, reporter, assignedOfficer, createdAt, updatedAt, slaDeadline, resolvedAt, isEscalated);
        }
    }
}
