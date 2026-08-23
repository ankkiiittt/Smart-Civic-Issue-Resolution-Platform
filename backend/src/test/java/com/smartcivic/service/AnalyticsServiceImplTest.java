package com.smartcivic.service;

import com.smartcivic.dto.AnalyticsResponse;
import com.smartcivic.entity.IssueStatus;
import com.smartcivic.repository.IssueRepository;
import com.smartcivic.service.impl.AnalyticsServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AnalyticsServiceImplTest {

    @Mock
    private IssueRepository issueRepository;

    @InjectMocks
    private AnalyticsServiceImpl analyticsService;

    @Test
    void getSystemAnalytics_CalculatesCorrectMetrics() {
        // Setup mocks
        when(issueRepository.count()).thenReturn(10L);

        List<Object[]> statusCounts = Arrays.asList(
                new Object[]{IssueStatus.RESOLVED, 4L},
                new Object[]{IssueStatus.REPORTED, 6L}
        );
        when(issueRepository.countIssuesByStatus()).thenReturn(statusCounts);

        List<Object[]> categoryCounts = Arrays.asList(
                new Object[]{"Pothole Repair", 7L},
                new Object[]{"Garbage Overflow", 3L}
        );
        when(issueRepository.countIssuesByCategory()).thenReturn(categoryCounts);

        List<Object[]> avgTimes = Arrays.asList(
                new Object[]{"Pothole Repair", 24.5},
                new Object[]{"Garbage Overflow", 12.0}
        );
        when(issueRepository.getAverageResolutionTimeByCategory()).thenReturn(avgTimes);

        List<Object[]> complianceStats = Collections.singletonList(
                new Object[]{2L, 10L} // 2 breached out of 10 total
        );
        when(issueRepository.getSlaComplianceStats()).thenReturn(complianceStats);

        List<Object[]> weeklyTrends = Arrays.asList(
                new Object[]{"2026-08-10", 5L},
                new Object[]{"2026-08-17", 5L}
        );
        when(issueRepository.getWeeklyIssueVolumeTrends()).thenReturn(weeklyTrends);

        // Run service
        AnalyticsResponse response = analyticsService.getSystemAnalytics();

        // Verify values
        assertNotNull(response);
        assertEquals(10, response.getTotalIssues());
        assertEquals(4, response.getResolvedIssues());
        assertEquals(6, response.getPendingIssues());
        assertEquals(20.0, response.getSlaBreachRate());

        assertEquals(7L, response.getIssuesByCategory().get("Pothole Repair"));
        assertEquals(3L, response.getIssuesByCategory().get("Garbage Overflow"));

        assertEquals(24.5, response.getAverageResolutionTimeByCategory().get("Pothole Repair"));
        assertEquals(12.0, response.getAverageResolutionTimeByCategory().get("Garbage Overflow"));

        assertEquals(2, response.getWeeklyVolumeTrends().size());
        assertEquals("2026-08-10", response.getWeeklyVolumeTrends().get(0).getWeekStartDate());
        assertEquals(5L, response.getWeeklyVolumeTrends().get(0).getCount());

        verify(issueRepository, times(1)).count();
        verify(issueRepository, times(1)).countIssuesByStatus();
        verify(issueRepository, times(1)).countIssuesByCategory();
        verify(issueRepository, times(1)).getAverageResolutionTimeByCategory();
        verify(issueRepository, times(1)).getSlaComplianceStats();
        verify(issueRepository, times(1)).getWeeklyIssueVolumeTrends();
    }
}
