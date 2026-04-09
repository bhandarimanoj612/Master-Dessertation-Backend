package com.example.sajilo_tayaar.controller;

import com.example.sajilo_tayaar.dto.response.DashboardSummaryResponse;
import com.example.sajilo_tayaar.services.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/shop/{shopId}/summary")
    public DashboardSummaryResponse getShopSummary(@PathVariable Long shopId) {
        return dashboardService.getSummary(shopId);
    }
}
