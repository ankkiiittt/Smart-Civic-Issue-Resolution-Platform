package com.smartcivic.service;

import com.smartcivic.dto.IssueRequest;
import com.smartcivic.dto.IssueResponse;
import com.smartcivic.dto.IssueStatusUpdateRequest;
import com.smartcivic.dto.OfficerSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IssueService {
    IssueResponse reportIssue(IssueRequest request, List<MultipartFile> photos, String username);
    IssueResponse getIssueById(Long id, String currentUsername);
    Page<IssueResponse> getAllIssues(String status, Long categoryId, Long reporterId, Long assignedOfficerId, Pageable pageable, String currentUsername);
    List<IssueResponse> getNearbyIssues(double latitude, double longitude, double radiusInMeters, String currentUsername);
    void upvoteIssue(Long id, String username);
    void removeUpvote(Long id, String username);
    IssueResponse updateIssueStatus(Long id, IssueStatusUpdateRequest request, MultipartFile photo, String officerUsername);
    void deleteIssue(Long id, String username);
    IssueResponse assignIssue(Long id, Long officerId, String adminUsername);
    List<OfficerSummaryResponse> getAllOfficers();
}
