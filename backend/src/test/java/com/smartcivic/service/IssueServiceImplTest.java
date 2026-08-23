package com.smartcivic.service;

import com.smartcivic.dto.IssueRequest;
import com.smartcivic.dto.IssueResponse;
import com.smartcivic.dto.IssueStatusUpdateRequest;
import com.smartcivic.entity.*;
import com.smartcivic.repository.*;
import com.smartcivic.service.impl.IssueServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class IssueServiceImplTest {

    @Mock
    private IssueRepository issueRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private OfficerProfileRepository officerProfileRepository;

    @Mock
    private IssuePhotoRepository issuePhotoRepository;

    @Mock
    private IssueUpvoteRepository issueUpvoteRepository;

    @Mock
    private IssueHistoryRepository issueHistoryRepository;

    @Mock
    private StorageService storageService;

    @Mock
    private WebSocketService webSocketService;

    @InjectMocks
    private IssueServiceImpl issueService;

    private User citizen;
    private User officer;
    private Department department;
    private Category category;
    private IssueRequest issueRequest;
    private Issue testIssue;
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    @BeforeEach
    void setUp() {
        citizen = User.builder()
                .id(1L)
                .username("citizenjoe")
                .role(UserRole.CITIZEN)
                .firstName("Joe")
                .lastName("Citizen")
                .build();

        officer = User.builder()
                .id(2L)
                .username("officersmith")
                .role(UserRole.OFFICER)
                .firstName("Agent")
                .lastName("Smith")
                .build();

        department = Department.builder()
                .id(1L)
                .name("Sanitation")
                .build();

        category = Category.builder()
                .id(1L)
                .name("Garbage Overflow")
                .defaultSlaHours(24)
                .department(department)
                .build();

        issueRequest = new IssueRequest("Garbage on 5th Ave", "Huge pile of trash left outside.", 1L, 40.7128, -74.0060, "5th Avenue");

        Point point = geometryFactory.createPoint(new Coordinate(-74.0060, 40.7128));
        point.setSRID(4326);

        testIssue = Issue.builder()
                .id(100L)
                .title("Garbage on 5th Ave")
                .description("Huge pile of trash left outside.")
                .category(category)
                .status(IssueStatus.REPORTED)
                .priority(IssuePriority.MEDIUM)
                .locationGeom(point)
                .reporter(citizen)
                .assignedOfficer(null)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .slaDeadline(LocalDateTime.now().plusHours(24))
                .isEscalated(false)
                .build();
    }

    @Test
    void reportIssue_WithoutPhotos_UnassignedSuccess() {
        when(userRepository.findByUsername("citizenjoe")).thenReturn(Optional.of(citizen));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(officerProfileRepository.findAvailableOfficersByDepartmentAndPoint(anyLong(), any(Point.class)))
                .thenReturn(Collections.emptyList());
        when(officerProfileRepository.findAvailableOfficersContainingPoint(any(Point.class)))
                .thenReturn(Collections.emptyList());
        when(issueRepository.save(any(Issue.class))).thenReturn(testIssue);

        IssueResponse response = issueService.reportIssue(issueRequest, null, "citizenjoe");

        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertEquals("Garbage on 5th Ave", response.getTitle());
        assertEquals("REPORTED", response.getStatus());
        assertNull(response.getAssignedOfficerUsername());

        verify(issueRepository, times(1)).save(any(Issue.class));
        verify(issueHistoryRepository, times(1)).save(any(IssueHistory.class));
        verify(webSocketService, times(1)).notifyIssueUpdate(eq(100L), anyString());
    }

    @Test
    void reportIssue_WithOfficerAutoAssignSuccess() {
        OfficerProfile profile = OfficerProfile.builder()
                .userId(officer.getId())
                .user(officer)
                .department(department)
                .isAvailable(true)
                .build();

        testIssue.setAssignedOfficer(officer);
        testIssue.setStatus(IssueStatus.ACKNOWLEDGED);

        when(userRepository.findByUsername("citizenjoe")).thenReturn(Optional.of(citizen));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(officerProfileRepository.findAvailableOfficersByDepartmentAndPoint(anyLong(), any(Point.class)))
                .thenReturn(List.of(profile));
        when(issueRepository.save(any(Issue.class))).thenReturn(testIssue);

        IssueResponse response = issueService.reportIssue(issueRequest, null, "citizenjoe");

        assertNotNull(response);
        assertEquals("ACKNOWLEDGED", response.getStatus());
        assertEquals("officersmith", response.getAssignedOfficerUsername());
    }

    @Test
    void upvoteIssue_Success() {
        when(issueRepository.findById(100L)).thenReturn(Optional.of(testIssue));
        when(userRepository.findByUsername("citizenjoe")).thenReturn(Optional.of(citizen));
        when(issueUpvoteRepository.existsByIssueIdAndUserId(100L, 1L)).thenReturn(false);

        issueService.upvoteIssue(100L, "citizenjoe");

        verify(issueUpvoteRepository, times(1)).save(any(IssueUpvote.class));
    }

    @Test
    void updateIssueStatus_ToResolvedSuccess() {
        testIssue.setAssignedOfficer(officer);
        testIssue.setStatus(IssueStatus.ACKNOWLEDGED);

        when(issueRepository.findById(100L)).thenReturn(Optional.of(testIssue));
        when(userRepository.findByUsername("officersmith")).thenReturn(Optional.of(officer));
        when(issueRepository.save(any(Issue.class))).thenReturn(testIssue);

        IssueStatusUpdateRequest statusRequest = new IssueStatusUpdateRequest("RESOLVED", "Cleaned up the waste pile.");
        IssueResponse response = issueService.updateIssueStatus(100L, statusRequest, null, "officersmith");

        assertNotNull(response);
        assertEquals("RESOLVED", response.getStatus());
        verify(issueHistoryRepository, times(1)).save(any(IssueHistory.class));
        verify(webSocketService, times(1)).notifyIssueUpdate(eq(100L), anyString());
    }
}
