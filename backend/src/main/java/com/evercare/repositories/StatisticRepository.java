package com.evercare.repositories;

import com.evercare.dtos.response.statistics.DiseaseStatisticsResponse;
import com.evercare.dtos.response.statistics.PatientStatisticsResponse;
import com.evercare.dtos.response.statistics.RevenueStatisticsResponse;
import com.evercare.dtos.response.statistics.ServiceUsageStatisticsResponse;
import java.time.LocalDate;

public interface StatisticRepository {
    PatientStatisticsResponse getPatientStatistics(LocalDate fromDate, LocalDate toDate);

    ServiceUsageStatisticsResponse getServiceUsageStatistics(LocalDate fromDate, LocalDate toDate);

    DiseaseStatisticsResponse getDiseaseStatistics(LocalDate fromDate, LocalDate toDate);

    RevenueStatisticsResponse getRevenueStatistics(LocalDate fromDate, LocalDate toDate, String groupBy);
}
