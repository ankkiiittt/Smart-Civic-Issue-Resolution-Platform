package com.smartcivic.dto;

import java.util.List;
import java.util.Map;

public class AnalyticsResponse {
    private long totalIssues;
    private long resolvedIssues;
    private long pendingIssues;
    private Double slaBreachRate;
    private Map<String, Long> issuesByCategory;
    private Map<String, Long> issuesByStatus;
    private Map<String, Double> averageResolutionTimeByCategory;
    private List<WeeklyTrendDto> weeklyVolumeTrends;

    public static class WeeklyTrendDto {
        private String weekStartDate;
        private long count;

        public WeeklyTrendDto() {}

        public WeeklyTrendDto(String weekStartDate, long count) {
            this.weekStartDate = weekStartDate;
            this.count = count;
        }

        public String getWeekStartDate() { return weekStartDate; }
        public void setWeekStartDate(String weekStartDate) { this.weekStartDate = weekStartDate; }

        public long getCount() { return count; }
        public void setCount(long count) { this.count = count; }
    }

    public AnalyticsResponse() {}

    public AnalyticsResponse(long totalIssues, long resolvedIssues, long pendingIssues, Double slaBreachRate, Map<String, Long> issuesByCategory, Map<String, Long> issuesByStatus, Map<String, Double> averageResolutionTimeByCategory, List<WeeklyTrendDto> weeklyVolumeTrends) {
        this.totalIssues = totalIssues;
        this.resolvedIssues = resolvedIssues;
        this.pendingIssues = pendingIssues;
        this.slaBreachRate = slaBreachRate;
        this.issuesByCategory = issuesByCategory;
        this.issuesByStatus = issuesByStatus;
        this.averageResolutionTimeByCategory = averageResolutionTimeByCategory;
        this.weeklyVolumeTrends = weeklyVolumeTrends;
    }

    // Getters and Setters
    public long getTotalIssues() { return totalIssues; }
    public void setTotalIssues(long totalIssues) { this.totalIssues = totalIssues; }

    public long getResolvedIssues() { return resolvedIssues; }
    public void setResolvedIssues(long resolvedIssues) { this.resolvedIssues = resolvedIssues; }

    public long getPendingIssues() { return pendingIssues; }
    public void setPendingIssues(long pendingIssues) { this.pendingIssues = pendingIssues; }

    public Double getSlaBreachRate() { return slaBreachRate; }
    public void setSlaBreachRate(Double slaBreachRate) { this.slaBreachRate = slaBreachRate; }

    public Map<String, Long> getIssuesByCategory() { return issuesByCategory; }
    public void setIssuesByCategory(Map<String, Long> issuesByCategory) { this.issuesByCategory = issuesByCategory; }

    public Map<String, Long> getIssuesByStatus() { return issuesByStatus; }
    public void setIssuesByStatus(Map<String, Long> issuesByStatus) { this.issuesByStatus = issuesByStatus; }

    public Map<String, Double> getAverageResolutionTimeByCategory() { return averageResolutionTimeByCategory; }
    public void setAverageResolutionTimeByCategory(Map<String, Double> averageResolutionTimeByCategory) { this.averageResolutionTimeByCategory = averageResolutionTimeByCategory; }

    public List<WeeklyTrendDto> getWeeklyVolumeTrends() { return weeklyVolumeTrends; }
    public void setWeeklyVolumeTrends(List<WeeklyTrendDto> weeklyVolumeTrends) { this.weeklyVolumeTrends = weeklyVolumeTrends; }

    public static AnalyticsResponseBuilder builder() {
        return new AnalyticsResponseBuilder();
    }

    public static class AnalyticsResponseBuilder {
        private long totalIssues;
        private long resolvedIssues;
        private long pendingIssues;
        private Double slaBreachRate;
        private Map<String, Long> issuesByCategory;
        private Map<String, Long> issuesByStatus;
        private Map<String, Double> averageResolutionTimeByCategory;
        private List<WeeklyTrendDto> weeklyVolumeTrends;

        AnalyticsResponseBuilder() {}

        public AnalyticsResponseBuilder totalIssues(long totalIssues) { this.totalIssues = totalIssues; return this; }
        public AnalyticsResponseBuilder resolvedIssues(long resolvedIssues) { this.resolvedIssues = resolvedIssues; return this; }
        public AnalyticsResponseBuilder pendingIssues(long pendingIssues) { this.pendingIssues = pendingIssues; return this; }
        public AnalyticsResponseBuilder slaBreachRate(Double slaBreachRate) { this.slaBreachRate = slaBreachRate; return this; }
        public AnalyticsResponseBuilder issuesByCategory(Map<String, Long> issuesByCategory) { this.issuesByCategory = issuesByCategory; return this; }
        public AnalyticsResponseBuilder issuesByStatus(Map<String, Long> issuesByStatus) { this.issuesByStatus = issuesByStatus; return this; }
        public AnalyticsResponseBuilder averageResolutionTimeByCategory(Map<String, Double> averageResolutionTimeByCategory) { this.averageResolutionTimeByCategory = averageResolutionTimeByCategory; return this; }
        public AnalyticsResponseBuilder weeklyVolumeTrends(List<WeeklyTrendDto> weeklyVolumeTrends) { this.weeklyVolumeTrends = weeklyVolumeTrends; return this; }

        public AnalyticsResponse build() {
            return new AnalyticsResponse(totalIssues, resolvedIssues, pendingIssues, slaBreachRate, issuesByCategory, issuesByStatus, averageResolutionTimeByCategory, weeklyVolumeTrends);
        }
    }
}
