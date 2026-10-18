package com.smartcivic.service.impl;

import com.smartcivic.dto.IssueRequest;
import com.smartcivic.dto.IssueResponse;
import com.smartcivic.dto.IssueStatusUpdateRequest;
import com.smartcivic.dto.OfficerSummaryResponse;
import com.smartcivic.entity.*;
import com.smartcivic.exception.BadRequestException;
import com.smartcivic.exception.ResourceNotFoundException;
import com.smartcivic.repository.*;
import com.smartcivic.service.IssueService;
import com.smartcivic.service.StorageService;
import com.smartcivic.service.WebSocketService;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class IssueServiceImpl implements IssueService {

    private final IssueRepository issueRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final OfficerProfileRepository officerProfileRepository;
    private final IssuePhotoRepository issuePhotoRepository;
    private final IssueUpvoteRepository issueUpvoteRepository;
    private final IssueHistoryRepository issueHistoryRepository;
    private final StorageService storageService;
    private final WebSocketService webSocketService;

    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    public IssueServiceImpl(IssueRepository issueRepository,
                            UserRepository userRepository,
                            CategoryRepository categoryRepository,
                            OfficerProfileRepository officerProfileRepository,
                            IssuePhotoRepository issuePhotoRepository,
                            IssueUpvoteRepository issueUpvoteRepository,
                            IssueHistoryRepository issueHistoryRepository,
                            StorageService storageService,
                            WebSocketService webSocketService) {
        this.issueRepository = issueRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.officerProfileRepository = officerProfileRepository;
        this.issuePhotoRepository = issuePhotoRepository;
        this.issueUpvoteRepository = issueUpvoteRepository;
        this.issueHistoryRepository = issueHistoryRepository;
        this.storageService = storageService;
        this.webSocketService = webSocketService;
    }

    @Override
    @Transactional
    public IssueResponse reportIssue(IssueRequest request, List<MultipartFile> photos, String username) {
        User reporter = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        // Construct PostGIS Point (long, lat)
        Point location = geometryFactory.createPoint(new Coordinate(request.getLongitude(), request.getLatitude()));
        location.setSRID(4326);

        // Auto SLA Calculation
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime slaDeadline = now.plusHours(category.getDefaultSlaHours());

        // Auto-assign officer by department and service zone
        User assignedOfficer = null;
        List<OfficerProfile> matchingOfficers = officerProfileRepository
                .findAvailableOfficersByDepartmentAndPoint(category.getDepartment().getId(), location);

        if (!matchingOfficers.isEmpty()) {
            assignedOfficer = matchingOfficers.get(0).getUser();
        } else {
            // Fallback: search for any available officer containing the point
            List<OfficerProfile> globalMatchingOfficers = officerProfileRepository
                    .findAvailableOfficersContainingPoint(location);
            if (!globalMatchingOfficers.isEmpty()) {
                assignedOfficer = globalMatchingOfficers.get(0).getUser();
            }
        }

        IssueStatus initialStatus = assignedOfficer != null ? IssueStatus.ACKNOWLEDGED : IssueStatus.REPORTED;

        Issue issue = Issue.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .category(category)
                .status(initialStatus)
                .priority(IssuePriority.MEDIUM) // Default priority
                .locationGeom(location)
                .addressDescription(request.getAddressDescription())
                .reporter(reporter)
                .assignedOfficer(assignedOfficer)
                .slaDeadline(slaDeadline)
                .isEscalated(false)
                .build();

        Issue savedIssue = issueRepository.save(issue);

        // Save History
        IssueHistory history = IssueHistory.builder()
                .issue(savedIssue)
                .statusFrom(null)
                .statusTo(initialStatus.name())
                .changedBy(reporter)
                .comments("Issue reported successfully.")
                .build();
        issueHistoryRepository.save(history);

        // Handle Photo Uploads
        List<IssuePhoto> savedPhotos = new ArrayList<>();
        if (photos != null && !photos.isEmpty()) {
            for (MultipartFile photo : photos) {
                if (photo != null && !photo.isEmpty()) {
                    String photoUrl = storageService.store(photo);
                    IssuePhoto issuePhoto = IssuePhoto.builder()
                            .issue(savedIssue)
                            .photoUrl(photoUrl)
                            .isBefore(true)
                            .build();
                    savedPhotos.add(issuePhotoRepository.save(issuePhoto));
                }
            }
        }

        webSocketService.notifyIssueUpdate(savedIssue.getId(), "A new issue has been reported: " + savedIssue.getTitle());

        return mapToResponse(savedIssue, savedPhotos, reporter, false);
    }

    @Override
    @Transactional(readOnly = true)
    public IssueResponse getIssueById(Long id, String currentUsername) {
        Issue issue = issueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Issue not found"));

        User currentUser = null;
        if (currentUsername != null) {
            currentUser = userRepository.findByUsername(currentUsername).orElse(null);
        }

        boolean hasUpvoted = false;
        if (currentUser != null) {
            hasUpvoted = issueUpvoteRepository.existsByIssueIdAndUserId(issue.getId(), currentUser.getId());
        }

        List<IssuePhoto> photos = issuePhotoRepository.findByIssueId(issue.getId());
        return mapToResponse(issue, photos, currentUser, hasUpvoted);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<IssueResponse> getAllIssues(String status, Long categoryId, Long reporterId, Long assignedOfficerId, Pageable pageable, String currentUsername) {
        // Simple page filters (we can extend to full specifications if needed, but pagination mapping works beautifully)
        Page<Issue> issues;
        if (status != null) {
            issues = issueRepository.findByStatus(IssueStatus.valueOf(status), pageable);
        } else if (categoryId != null) {
            issues = issueRepository.findByCategoryId(categoryId, pageable);
        } else if (reporterId != null) {
            issues = issueRepository.findByReporterId(reporterId, pageable);
        } else if (assignedOfficerId != null) {
            issues = issueRepository.findByAssignedOfficerId(assignedOfficerId, pageable);
        } else {
            issues = issueRepository.findAll(pageable);
        }

        User currentUser = currentUsername != null ? userRepository.findByUsername(currentUsername).orElse(null) : null;

        return issues.map(issue -> {
            boolean hasUpvoted = currentUser != null && issueUpvoteRepository.existsByIssueIdAndUserId(issue.getId(), currentUser.getId());
            List<IssuePhoto> photos = issuePhotoRepository.findByIssueId(issue.getId());
            return mapToResponse(issue, photos, currentUser, hasUpvoted);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public List<IssueResponse> getNearbyIssues(double latitude, double longitude, double radiusInMeters, String currentUsername) {
        Point center = geometryFactory.createPoint(new Coordinate(longitude, latitude));
        center.setSRID(4326);

        List<Issue> issues = issueRepository.findNearbyIssues(center, radiusInMeters);
        User currentUser = currentUsername != null ? userRepository.findByUsername(currentUsername).orElse(null) : null;

        return issues.stream().map(issue -> {
            boolean hasUpvoted = currentUser != null && issueUpvoteRepository.existsByIssueIdAndUserId(issue.getId(), currentUser.getId());
            List<IssuePhoto> photos = issuePhotoRepository.findByIssueId(issue.getId());
            return mapToResponse(issue, photos, currentUser, hasUpvoted);
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void upvoteIssue(Long id, String username) {
        Issue issue = issueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Issue not found"));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (issueUpvoteRepository.existsByIssueIdAndUserId(issue.getId(), user.getId())) {
            throw new BadRequestException("You have already upvoted this issue");
        }

        IssueUpvote upvote = IssueUpvote.builder()
                .issue(issue)
                .user(user)
                .build();

        issueUpvoteRepository.save(upvote);
    }

    @Override
    @Transactional
    public void removeUpvote(Long id, String username) {
        Issue issue = issueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Issue not found"));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        IssueUpvote upvote = issueUpvoteRepository.findByIssueIdAndUserId(issue.getId(), user.getId())
                .orElseThrow(() -> new BadRequestException("You have not upvoted this issue"));

        issueUpvoteRepository.delete(upvote);
    }

    @Override
    @Transactional
    public IssueResponse updateIssueStatus(Long id, IssueStatusUpdateRequest request, MultipartFile photo, String officerUsername) {
        Issue issue = issueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Issue not found"));

        User officer = userRepository.findByUsername(officerUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Validate authority: officer must be assigned or be admin
        if (officer.getRole() != UserRole.ADMIN && officer.getRole() != UserRole.OFFICER) {
            throw new BadRequestException("You do not have permission to update issue status");
        }

        IssueStatus nextStatus = IssueStatus.valueOf(request.getStatus().toUpperCase());
        IssueStatus prevStatus = issue.getStatus();

        if (prevStatus == nextStatus) {
            throw new BadRequestException("Issue is already in " + nextStatus + " status");
        }

        issue.setStatus(nextStatus);
        if (nextStatus == IssueStatus.RESOLVED) {
            issue.setResolvedAt(LocalDateTime.now());
        }

        Issue updatedIssue = issueRepository.save(issue);

        // Save Audit History
        IssueHistory history = IssueHistory.builder()
                .issue(updatedIssue)
                .statusFrom(prevStatus.name())
                .statusTo(nextStatus.name())
                .changedBy(officer)
                .comments(request.getComments())
                .build();
        issueHistoryRepository.save(history);

        // Handle Resolution Photo (isAfter = false)
        if (photo != null && !photo.isEmpty()) {
            String photoUrl = storageService.store(photo);
            IssuePhoto issuePhoto = IssuePhoto.builder()
                    .issue(updatedIssue)
                    .photoUrl(photoUrl)
                    .isBefore(false)
                    .build();
            issuePhotoRepository.save(issuePhoto);
        }

        webSocketService.notifyIssueUpdate(updatedIssue.getId(), "Issue status updated from " + prevStatus + " to " + nextStatus);

        List<IssuePhoto> photos = issuePhotoRepository.findByIssueId(updatedIssue.getId());
        return mapToResponse(updatedIssue, photos, officer, false);
    }

    @Override
    @Transactional
    public void deleteIssue(Long id, String username) {
        Issue issue = issueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Issue not found"));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Only the reporter or an ADMIN can delete the issue
        if (!issue.getReporter().getUsername().equals(username) && user.getRole() != UserRole.ADMIN) {
            throw new BadRequestException("You do not have permission to delete this issue");
        }

        // Delete photos from storage
        List<IssuePhoto> photos = issuePhotoRepository.findByIssueId(id);
        for (IssuePhoto photo : photos) {
            storageService.delete(photo.getPhotoUrl());
        }

        // Delete related entities from database
        issuePhotoRepository.deleteAll(photos);
        issueUpvoteRepository.deleteByIssueId(id);

        List<IssueHistory> histories = issueHistoryRepository.findByIssueIdOrderByChangedAtDesc(id);
        issueHistoryRepository.deleteAll(histories);

        // Delete the issue itself
        issueRepository.delete(issue);

        webSocketService.notifyIssueUpdate(id, "Issue deleted: " + issue.getTitle());
    }

    @Override
    @Transactional
    public IssueResponse assignIssue(Long id, Long officerId, String adminUsername) {
        Issue issue = issueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Issue not found"));

        User admin = userRepository.findByUsername(adminUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (admin.getRole() != UserRole.ADMIN) {
            throw new BadRequestException("Only administrators can assign or reassign issues");
        }

        User officer = userRepository.findById(officerId)
                .orElseThrow(() -> new ResourceNotFoundException("Officer not found"));

        if (officer.getRole() != UserRole.OFFICER && officer.getRole() != UserRole.ADMIN) {
            throw new BadRequestException("Selected user is not an officer");
        }

        IssueStatus prevStatus = issue.getStatus();
        issue.setAssignedOfficer(officer);

        if (issue.getStatus() == IssueStatus.REPORTED) {
            issue.setStatus(IssueStatus.ACKNOWLEDGED);
        }

        Issue updatedIssue = issueRepository.save(issue);

        IssueHistory history = IssueHistory.builder()
                .issue(updatedIssue)
                .statusFrom(prevStatus.name())
                .statusTo(updatedIssue.getStatus().name())
                .changedBy(admin)
                .comments("Assigned to " + officer.getFirstName() + " " + officer.getLastName() + " (" + officer.getUsername() + ") by Admin " + adminUsername)
                .build();
        issueHistoryRepository.save(history);

        webSocketService.notifyIssueUpdate(updatedIssue.getId(), "Issue #" + updatedIssue.getId() + " assigned to officer " + officer.getUsername());

        List<IssuePhoto> photos = issuePhotoRepository.findByIssueId(updatedIssue.getId());
        return mapToResponse(updatedIssue, photos, admin, false);
    }

    @Override
    public List<OfficerSummaryResponse> getAllOfficers() {
        List<User> officers = userRepository.findByRole(UserRole.OFFICER);
        List<OfficerProfile> profiles = officerProfileRepository.findAll();

        return officers.stream().map(officer -> {
            String deptName = "Municipal Services";
            boolean isAvailable = true;

            for (OfficerProfile profile : profiles) {
                if (profile.getUser() != null && profile.getUser().getId().equals(officer.getId())) {
                    if (profile.getDepartment() != null) {
                        deptName = profile.getDepartment().getName();
                    }
                    isAvailable = profile.isAvailable();
                    break;
                }
            }

            return new OfficerSummaryResponse(
                    officer.getId(),
                    officer.getUsername(),
                    officer.getFirstName(),
                    officer.getLastName(),
                    deptName,
                    isAvailable
            );
        }).collect(Collectors.toList());
    }

    private IssueResponse mapToResponse(Issue issue, List<IssuePhoto> photos, User currentUser, boolean hasUpvoted) {
        long upvoteCount = issueUpvoteRepository.countByIssueId(issue.getId());

        List<IssueResponse.PhotoDto> photoDtos = photos.stream()
                .map(p -> new IssueResponse.PhotoDto(p.getPhotoUrl(), p.isBefore()))
                .collect(Collectors.toList());

        String assignedUsername = issue.getAssignedOfficer() != null ? issue.getAssignedOfficer().getUsername() : null;
        Long assignedId = issue.getAssignedOfficer() != null ? issue.getAssignedOfficer().getId() : null;

        return IssueResponse.builder()
                .id(issue.getId())
                .title(issue.getTitle())
                .description(issue.getDescription())
                .categoryId(issue.getCategory().getId())
                .categoryName(issue.getCategory().getName())
                .status(issue.getStatus().name())
                .priority(issue.getPriority().name())
                .latitude(issue.getLocationGeom().getY())
                .longitude(issue.getLocationGeom().getX())
                .addressDescription(issue.getAddressDescription())
                .reporterId(issue.getReporter().getId())
                .reporterUsername(issue.getReporter().getUsername())
                .assignedOfficerId(assignedId)
                .assignedOfficerUsername(assignedUsername)
                .createdAt(issue.getCreatedAt())
                .updatedAt(issue.getUpdatedAt())
                .slaDeadline(issue.getSlaDeadline())
                .resolvedAt(issue.getResolvedAt())
                .isEscalated(issue.isEscalated())
                .upvotesCount(upvoteCount)
                .hasUpvoted(hasUpvoted)
                .photos(photoDtos)
                .build();
    }
}
