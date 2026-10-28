package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.DashboardDto;
import com.example.demo.service.DashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public ApiResponse<DashboardDto> dashboard() {
        return ApiResponse.ok(dashboardService.getDashboard());
    }
}