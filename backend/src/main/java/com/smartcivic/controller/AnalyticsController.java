package com.smartcivic.controller;

import com.smartcivic.dto.ApiResponse;
import com.smartcivic.dto.AnalyticsResponse;
import com.smartcivic.service.AnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OFFICER')")
    public ResponseEntity<ApiResponse<AnalyticsResponse>> getSystemAnalytics() {
        AnalyticsResponse response = analyticsService.getSystemAnalytics();
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
