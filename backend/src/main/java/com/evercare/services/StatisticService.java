package com.evercare.services;

import com.evercare.dtos.response.statistics.DiseaseStatisticsResponse;
import com.evercare.dtos.response.statistics.PatientStatisticsResponse;
import com.evercare.dtos.response.statistics.RevenueStatisticsResponse;
import com.evercare.dtos.response.statistics.ServiceUsageStatisticsResponse;
import java.time.LocalDate;

public interface StatisticService {
    record DateRange(LocalDate fromDate, LocalDate toDate) {
    }

    DateRange resolveDateRange(String fromDate, String toDate);

    PatientStatisticsResponse getPatientStatistics(String fromDate, String toDate);

    ServiceUsageStatisticsResponse getServiceUsageStatistics(String fromDate, String toDate);

    DiseaseStatisticsResponse getDiseaseStatistics(String fromDate, String toDate);

    RevenueStatisticsResponse getRevenueStatistics(String fromDate, String toDate, String groupBy);
}
