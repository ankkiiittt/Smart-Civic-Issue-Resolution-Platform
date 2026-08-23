package com.smartcivic.dto;

import java.time.LocalDateTime;
import java.util.List;

public class IssueResponse {
    private Long id;
    private String title;
    private String description;
    private Long categoryId;
    private String categoryName;
    private String status;
    private String priority;
    private Double latitude;
    private Double longitude;
    private String addressDescription;
    private Long reporterId;
    private String reporterUsername;
    private Long assignedOfficerId;
    private String assignedOfficerUsername;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime slaDeadline;
    private LocalDateTime resolvedAt;
    private boolean isEscalated;
    private long upvotesCount;
    private boolean hasUpvoted;
    private List<PhotoDto> photos;

    public static class PhotoDto {
        private String photoUrl;
        private boolean isBefore;

        public PhotoDto() {}

        public PhotoDto(String photoUrl, boolean isBefore) {
            this.photoUrl = photoUrl;
            this.isBefore = isBefore;
        }

        public String getPhotoUrl() { return photoUrl; }
        public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }

        public boolean isBefore() { return isBefore; }
        public void setBefore(boolean before) { isBefore = before; }
    }

    public IssueResponse() {}

    public IssueResponse(Long id, String title, String description, Long categoryId, String categoryName, String status, String priority, Double latitude, Double longitude, String addressDescription, Long reporterId, String reporterUsername, Long assignedOfficerId, String assignedOfficerUsername, LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime slaDeadline, LocalDateTime resolvedAt, boolean isEscalated, long upvotesCount, boolean hasUpvoted, List<PhotoDto> photos) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.status = status;
        this.priority = priority;
        this.latitude = latitude;
        this.longitude = longitude;
        this.addressDescription = addressDescription;
        this.reporterId = reporterId;
        this.reporterUsername = reporterUsername;
        this.assignedOfficerId = assignedOfficerId;
        this.assignedOfficerUsername = assignedOfficerUsername;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.slaDeadline = slaDeadline;
        this.resolvedAt = resolvedAt;
        this.isEscalated = isEscalated;
        this.upvotesCount = upvotesCount;
        this.hasUpvoted = hasUpvoted;
        this.photos = photos;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public String getAddressDescription() { return addressDescription; }
    public void setAddressDescription(String addressDescription) { this.addressDescription = addressDescription; }

    public Long getReporterId() { return reporterId; }
    public void setReporterId(Long reporterId) { this.reporterId = reporterId; }

    public String getReporterUsername() { return reporterUsername; }
    public void setReporterUsername(String reporterUsername) { this.reporterUsername = reporterUsername; }

    public Long getAssignedOfficerId() { return assignedOfficerId; }
    public void setAssignedOfficerId(Long assignedOfficerId) { this.assignedOfficerId = assignedOfficerId; }

    public String getAssignedOfficerUsername() { return assignedOfficerUsername; }
    public void setAssignedOfficerUsername(String assignedOfficerUsername) { this.assignedOfficerUsername = assignedOfficerUsername; }

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

    public long getUpvotesCount() { return upvotesCount; }
    public void setUpvotesCount(long upvotesCount) { this.upvotesCount = upvotesCount; }

    public boolean isHasUpvoted() { return hasUpvoted; }
    public void setHasUpvoted(boolean hasUpvoted) { this.hasUpvoted = hasUpvoted; }

    public List<PhotoDto> getPhotos() { return photos; }
    public void setPhotos(List<PhotoDto> photos) { this.photos = photos; }

    public static IssueResponseBuilder builder() {
        return new IssueResponseBuilder();
    }

    public static class IssueResponseBuilder {
        private Long id;
        private String title;
        private String description;
        private Long categoryId;
        private String categoryName;
        private String status;
        private String priority;
        private Double latitude;
        private Double longitude;
        private String addressDescription;
        private Long reporterId;
        private String reporterUsername;
        private Long assignedOfficerId;
        private String assignedOfficerUsername;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private LocalDateTime slaDeadline;
        private LocalDateTime resolvedAt;
        private boolean isEscalated;
        private long upvotesCount;
        private boolean hasUpvoted;
        private List<PhotoDto> photos;

        IssueResponseBuilder() {}

        public IssueResponseBuilder id(Long id) { this.id = id; return this; }
        public IssueResponseBuilder title(String title) { this.title = title; return this; }
        public IssueResponseBuilder description(String description) { this.description = description; return this; }
        public IssueResponseBuilder categoryId(Long categoryId) { this.categoryId = categoryId; return this; }
        public IssueResponseBuilder categoryName(String categoryName) { this.categoryName = categoryName; return this; }
        public IssueResponseBuilder status(String status) { this.status = status; return this; }
        public IssueResponseBuilder priority(String priority) { this.priority = priority; return this; }
        public IssueResponseBuilder latitude(Double latitude) { this.latitude = latitude; return this; }
        public IssueResponseBuilder longitude(Double longitude) { this.longitude = longitude; return this; }
        public IssueResponseBuilder addressDescription(String addressDescription) { this.addressDescription = addressDescription; return this; }
        public IssueResponseBuilder reporterId(Long reporterId) { this.reporterId = reporterId; return this; }
        public IssueResponseBuilder reporterUsername(String reporterUsername) { this.reporterUsername = reporterUsername; return this; }
        public IssueResponseBuilder assignedOfficerId(Long assignedOfficerId) { this.assignedOfficerId = assignedOfficerId; return this; }
        public IssueResponseBuilder assignedOfficerUsername(String assignedOfficerUsername) { this.assignedOfficerUsername = assignedOfficerUsername; return this; }
        public IssueResponseBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public IssueResponseBuilder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }
        public IssueResponseBuilder slaDeadline(LocalDateTime slaDeadline) { this.slaDeadline = slaDeadline; return this; }
        public IssueResponseBuilder resolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; return this; }
        public IssueResponseBuilder isEscalated(boolean isEscalated) { this.isEscalated = isEscalated; return this; }
        public IssueResponseBuilder upvotesCount(long upvotesCount) { this.upvotesCount = upvotesCount; return this; }
        public IssueResponseBuilder hasUpvoted(boolean hasUpvoted) { this.hasUpvoted = hasUpvoted; return this; }
        public IssueResponseBuilder photos(List<PhotoDto> photos) { this.photos = photos; return this; }

        public IssueResponse build() {
            return new IssueResponse(id, title, description, categoryId, categoryName, status, priority, latitude, longitude, addressDescription, reporterId, reporterUsername, assignedOfficerId, assignedOfficerUsername, createdAt, updatedAt, slaDeadline, resolvedAt, isEscalated, upvotesCount, hasUpvoted, photos);
        }
    }
}
