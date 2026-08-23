package com.smartcivic.service.impl;

import com.smartcivic.dto.AnalyticsResponse;
import com.smartcivic.entity.IssueStatus;
import com.smartcivic.repository.IssueRepository;
import com.smartcivic.service.AnalyticsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AnalyticsServiceImpl implements AnalyticsService {

    private final IssueRepository issueRepository;

    public AnalyticsServiceImpl(IssueRepository issueRepository) {
        this.issueRepository = issueRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public AnalyticsResponse getSystemAnalytics() {
        long totalIssues = issueRepository.count();

        // 1. Map issues by status
        List<Object[]> statusCounts = issueRepository.countIssuesByStatus();
        Map<String, Long> issuesByStatus = new HashMap<>();
        long resolvedIssues = 0;

        for (Object[] row : statusCounts) {
            if (row[0] != null) {
                String statusName = ((IssueStatus) row[0]).name();
                long count = ((Number) row[1]).longValue();
                issuesByStatus.put(statusName, count);
                if (statusName.equals(IssueStatus.RESOLVED.name())) {
                    resolvedIssues = count;
                }
            }
        }

        long pendingIssues = totalIssues - resolvedIssues;

        // 2. Map issues by category
        List<Object[]> categoryCounts = issueRepository.countIssuesByCategory();
        Map<String, Long> issuesByCategory = categoryCounts.stream()
                .collect(Collectors.toMap(
                        row -> row[0] != null ? (String) row[0] : "Uncategorized",
                        row -> ((Number) row[1]).longValue(),
                        (existing, replacement) -> existing
                ));

        // 3. Map average resolution times by category
        List<Object[]> avgTimes = issueRepository.getAverageResolutionTimeByCategory();
        Map<String, Double> averageResolutionTimeByCategory = avgTimes.stream()
                .collect(Collectors.toMap(
                        row -> row[0] != null ? (String) row[0] : "Uncategorized",
                        row -> row[1] != null ? ((Number) row[1]).doubleValue() : 0.0,
                        (existing, replacement) -> existing
                ));

        // 4. Calculate SLA breach rates
        List<Object[]> complianceStats = issueRepository.getSlaComplianceStats();
        long breachedCount = 0;
        if (complianceStats != null && !complianceStats.isEmpty()) {
            Object[] row = complianceStats.get(0);
            breachedCount = row[0] != null ? ((Number) row[0]).longValue() : 0;
        }
        Double slaBreachRate = totalIssues > 0 ? (double) breachedCount * 100.0 / totalIssues : 0.0;

        // 5. Weekly volume trends
        List<Object[]> weeklyTrends = issueRepository.getWeeklyIssueVolumeTrends();
        List<AnalyticsResponse.WeeklyTrendDto> weeklyVolumeTrends = weeklyTrends.stream()
                .map(row -> new AnalyticsResponse.WeeklyTrendDto(
                        row[0] != null ? row[0].toString() : "Unknown",
                        row[1] != null ? ((Number) row[1]).longValue() : 0L
                ))
                .collect(Collectors.toList());

        return AnalyticsResponse.builder()
                .totalIssues(totalIssues)
                .resolvedIssues(resolvedIssues)
                .pendingIssues(pendingIssues)
                .slaBreachRate(slaBreachRate)
                .issuesByCategory(issuesByCategory)
                .issuesByStatus(issuesByStatus)
                .averageResolutionTimeByCategory(averageResolutionTimeByCategory)
                .weeklyVolumeTrends(weeklyVolumeTrends)
                .build();
    }
}
