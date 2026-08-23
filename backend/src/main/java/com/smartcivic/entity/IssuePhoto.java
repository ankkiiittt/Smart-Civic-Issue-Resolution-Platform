package com.smartcivic.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "issue_photos")
public class IssuePhoto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issue_id", nullable = false)
    private Issue issue;

    @Column(name = "photo_url", nullable = false, length = 255)
    private String photoUrl;

    @Column(name = "is_before", nullable = false)
    private boolean isBefore;

    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt;

    @PrePersist
    protected void onCreate() {
        uploadedAt = LocalDateTime.now();
    }

    public IssuePhoto() {}

    public IssuePhoto(Long id, Issue issue, String photoUrl, boolean isBefore, LocalDateTime uploadedAt) {
        this.id = id;
        this.issue = issue;
        this.photoUrl = photoUrl;
        this.isBefore = isBefore;
        this.uploadedAt = uploadedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Issue getIssue() { return issue; }
    public void setIssue(Issue issue) { this.issue = issue; }

    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }

    public boolean isBefore() { return isBefore; }
    public void setBefore(boolean before) { isBefore = before; }

    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }

    public static IssuePhotoBuilder builder() {
        return new IssuePhotoBuilder();
    }

    public static class IssuePhotoBuilder {
        private Long id;
        private Issue issue;
        private String photoUrl;
        private boolean isBefore;
        private LocalDateTime uploadedAt;

        IssuePhotoBuilder() {}

        public IssuePhotoBuilder id(Long id) { this.id = id; return this; }
        public IssuePhotoBuilder issue(Issue issue) { this.issue = issue; return this; }
        public IssuePhotoBuilder photoUrl(String photoUrl) { this.photoUrl = photoUrl; return this; }
        public IssuePhotoBuilder isBefore(boolean isBefore) { this.isBefore = isBefore; return this; }
        public IssuePhotoBuilder uploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; return this; }

        public IssuePhoto build() {
            return new IssuePhoto(id, issue, photoUrl, isBefore, uploadedAt);
        }
    }
}
