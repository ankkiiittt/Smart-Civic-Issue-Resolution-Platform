package com.smartcivic.controller;

import com.smartcivic.dto.ApiResponse;
import com.smartcivic.dto.IssueRequest;
import com.smartcivic.dto.IssueResponse;
import com.smartcivic.dto.IssueStatusUpdateRequest;
import com.smartcivic.dto.OfficerSummaryResponse;
import com.smartcivic.service.IssueService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/issues")
public class IssueController {

    private final IssueService issueService;

    public IssueController(IssueService issueService) {
        this.issueService = issueService;
    }

    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<ApiResponse<IssueResponse>> reportIssue(
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("categoryId") Long categoryId,
            @RequestParam("latitude") Double latitude,
            @RequestParam("longitude") Double longitude,
            @RequestParam(value = "addressDescription", required = false) String addressDescription,
            @RequestParam(value = "photos", required = false) List<MultipartFile> photos,
            Authentication authentication) {

        IssueRequest request = new IssueRequest(title, description, categoryId, latitude, longitude, addressDescription);
        IssueResponse response = issueService.reportIssue(request, photos, authentication.getName());
        return new ResponseEntity<>(ApiResponse.success(response), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<IssueResponse>> getIssueById(
            @PathVariable Long id,
            Authentication authentication) {
        String username = authentication != null ? authentication.getName() : null;
        IssueResponse response = issueService.getIssueById(id, username);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<IssueResponse>>> getAllIssues(
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "reporterId", required = false) Long reporterId,
            @RequestParam(value = "assignedOfficerId", required = false) Long assignedOfficerId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "createdAt") String sortBy,
            @RequestParam(value = "direction", defaultValue = "DESC") String direction,
            Authentication authentication) {

        String username = authentication != null ? authentication.getName() : null;
        Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<IssueResponse> response = issueService.getAllIssues(status, categoryId, reporterId, assignedOfficerId, pageable, username);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/nearby")
    public ResponseEntity<ApiResponse<List<IssueResponse>>> getNearbyIssues(
            @RequestParam("latitude") double latitude,
            @RequestParam("longitude") double longitude,
            @RequestParam("radius") double radiusInMeters,
            Authentication authentication) {

        String username = authentication != null ? authentication.getName() : null;
        List<IssueResponse> response = issueService.getNearbyIssues(latitude, longitude, radiusInMeters, username);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{id}/upvote")
    public ResponseEntity<ApiResponse<String>> upvoteIssue(
            @PathVariable Long id,
            Authentication authentication) {
        issueService.upvoteIssue(id, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Issue upvoted successfully"));
    }

    @DeleteMapping("/{id}/upvote")
    public ResponseEntity<ApiResponse<String>> removeUpvote(
            @PathVariable Long id,
            Authentication authentication) {
        issueService.removeUpvote(id, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Upvote removed successfully"));
    }

    @PutMapping(value = "/{id}/status", consumes = {"multipart/form-data"})
    public ResponseEntity<ApiResponse<IssueResponse>> updateIssueStatus(
            @PathVariable Long id,
            @RequestParam("status") String status,
            @RequestParam(value = "comments", required = false) String comments,
            @RequestParam(value = "photo", required = false) MultipartFile photo,
            Authentication authentication) {

        IssueStatusUpdateRequest request = new IssueStatusUpdateRequest(status, comments);
        IssueResponse response = issueService.updateIssueStatus(id, request, photo, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteIssue(
            @PathVariable Long id,
            Authentication authentication) {
        issueService.deleteIssue(id, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Issue deleted successfully"));
    }

    @GetMapping("/officers")
    public ResponseEntity<ApiResponse<List<OfficerSummaryResponse>>> getOfficers() {
        List<OfficerSummaryResponse> officers = issueService.getAllOfficers();
        return ResponseEntity.ok(ApiResponse.success(officers));
    }

    @PutMapping("/{id}/assign")
    public ResponseEntity<ApiResponse<IssueResponse>> assignIssue(
            @PathVariable Long id,
            @RequestParam("officerId") Long officerId,
            Authentication authentication) {
        IssueResponse response = issueService.assignIssue(id, officerId, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
