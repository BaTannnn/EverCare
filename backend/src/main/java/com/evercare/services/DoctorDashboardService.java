package com.evercare.services;

import com.evercare.dtos.response.DoctorDashboardSummaryResponse;

public interface DoctorDashboardService {
    DoctorDashboardSummaryResponse getTodaySummary(String username);
}
