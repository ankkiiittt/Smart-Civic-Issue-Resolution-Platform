package com.smartcivic.dto;

import jakarta.validation.constraints.NotBlank;

public class IssueStatusUpdateRequest {

    @NotBlank(message = "Status is required")
    private String status;

    private String comments;

    public IssueStatusUpdateRequest() {}

    public IssueStatusUpdateRequest(String status, String comments) {
        this.status = status;
        this.comments = comments;
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }
}
