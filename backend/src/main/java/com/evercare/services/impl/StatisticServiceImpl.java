package com.evercare.services.impl;

import com.evercare.dtos.response.statistics.DiseaseStatisticsResponse;
import com.evercare.dtos.response.statistics.PatientStatisticsResponse;
import com.evercare.dtos.response.statistics.RevenueStatisticsResponse;
import com.evercare.dtos.response.statistics.ServiceUsageStatisticsResponse;
import com.evercare.dtos.response.statistics.StatisticItemResponse;
import com.evercare.enums.InvoiceStatus;
import com.evercare.repositories.StatisticRepository;
import com.evercare.services.StatisticService;
import com.evercare.utils.AuthSupport;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class StatisticServiceImpl implements StatisticService {
    private static final List<String> AGE_GROUP_LABELS = List.of("0-5", "6-17", "18-35", "36-60", "60+", "Unknown");
    private static final List<String> GENDER_LABELS = List.of("Nam", "Nữ", "Khác", "Chưa xác định");

    @Autowired
    private StatisticRepository statisticRepository;

    @Autowired
    private AuthSupport authSupport;

    @Override
    public DateRange resolveDateRange(String fromDate, String toDate) {
        LocalDate from = parseDate(fromDate);
        LocalDate to = parseDate(toDate);

        if (from == null && to == null) {
            to = LocalDate.now();
            from = to.withDayOfMonth(1);
        } else {
            if (from == null) {
                from = LocalDate.now().withDayOfMonth(1);
            }

            if (to == null) {
                to = LocalDate.now();
            }
        }

        if (from.isAfter(to)) {
            throw new IllegalArgumentException("fromDate không được lớn hơn toDate");
        }

        return new DateRange(from, to);
    }

    @Override
    public PatientStatisticsResponse getPatientStatistics(String fromDate, String toDate) {
        requireAccess();

        DateRange range = resolveDateRange(fromDate, toDate);
        PatientStatisticsResponse response = this.statisticRepository.getPatientStatistics(range.fromDate(), range.toDate());

        PatientStatisticsResponse result = new PatientStatisticsResponse();
        result.setByAgeGroups(normalizeAgeGroups(response != null ? response.getByAgeGroups() : null));
        result.setByGender(normalizeGenderGroups(response != null ? response.getByGender() : null));
        result.setByDepartment(response != null && response.getByDepartment() != null ? response.getByDepartment() : List.of());

        return result;
    }

    @Override
    public ServiceUsageStatisticsResponse getServiceUsageStatistics(String fromDate, String toDate) {
        requireAccess();

        DateRange range = resolveDateRange(fromDate, toDate);
        ServiceUsageStatisticsResponse response = this.statisticRepository.getServiceUsageStatistics(range.fromDate(), range.toDate());
        if (response == null || response.getItems() == null) {
            ServiceUsageStatisticsResponse empty = new ServiceUsageStatisticsResponse();
            empty.setItems(List.of());
            return empty;
        }
        return response;
    }

    @Override
    public DiseaseStatisticsResponse getDiseaseStatistics(String fromDate, String toDate) {
        requireAccess();

        DateRange range = resolveDateRange(fromDate, toDate);
        DiseaseStatisticsResponse response = this.statisticRepository.getDiseaseStatistics(range.fromDate(), range.toDate());
        if (response == null || response.getItems() == null) {
            DiseaseStatisticsResponse empty = new DiseaseStatisticsResponse();
            empty.setItems(List.of());
            return empty;
        }
        return response;
    }

    @Override
    public RevenueStatisticsResponse getRevenueStatistics(String fromDate, String toDate, String groupBy) {
        requireAccess();

        DateRange range = resolveDateRange(fromDate, toDate);
        String normalizedGroupBy = groupBy == null || groupBy.isBlank() ? "DAY" : groupBy.trim().toUpperCase();
        if (!"DAY".equals(normalizedGroupBy) && !"MONTH".equals(normalizedGroupBy)) {
            throw new IllegalArgumentException("groupBy chỉ chấp nhận DAY hoặc MONTH");
        }
        RevenueStatisticsResponse response = this.statisticRepository.getRevenueStatistics(range.fromDate(), range.toDate(), normalizedGroupBy);
        if (response == null) {
            response = new RevenueStatisticsResponse();
        }

        response.setTotalRevenue(response.getTotalRevenue() != null ? response.getTotalRevenue() : BigDecimal.ZERO);
        response.setPaidInvoiceCount(response.getPaidInvoiceCount() != null ? response.getPaidInvoiceCount() : 0L);
        response.setUnpaidInvoiceCount(response.getUnpaidInvoiceCount() != null ? response.getUnpaidInvoiceCount() : 0L);
        response.setRefundedInvoiceCount(response.getRefundedInvoiceCount() != null ? response.getRefundedInvoiceCount() : 0L);
        response.setSeries(response.getSeries() != null ? response.getSeries() : List.of());
        response.setDetails(response.getDetails() != null ? response.getDetails() : List.of());

        return response;
    }

    private void requireAccess() {
        this.authSupport.requireReceptionistUser();
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return LocalDate.parse(value.trim());
    }

    private List<StatisticItemResponse> normalizeAgeGroups(List<StatisticItemResponse> items) {
        Map<String, Long> countMap = new LinkedHashMap<>();
        if (items != null) {
            for (StatisticItemResponse item : items) {
                if (item != null && item.getLabel() != null) {
                    countMap.merge(item.getLabel(), item.getCount() != null ? item.getCount() : 0L, Long::sum);
                }
            }
        }

        return AGE_GROUP_LABELS.stream()
                .map(label -> {
                    StatisticItemResponse item = new StatisticItemResponse();
                    item.setLabel(label);
                    item.setCount(countMap.getOrDefault(label, 0L));
                    return item;
                })
                .toList();
    }

    private List<StatisticItemResponse> normalizeGenderGroups(List<StatisticItemResponse> items) {
        Map<String, Long> countMap = new LinkedHashMap<>();
        if (items != null) {
            for (StatisticItemResponse item : items) {
                String label = normalizeGenderLabel(item != null ? item.getLabel() : null);
                countMap.merge(label, item != null && item.getCount() != null ? item.getCount() : 0L, Long::sum);
            }
        }

        return GENDER_LABELS.stream()
                .map(label -> {
                    StatisticItemResponse item = new StatisticItemResponse();
                    item.setLabel(label);
                    item.setCount(countMap.getOrDefault(label, 0L));
                    return item;
                })
                .toList();
    }

    private String normalizeGenderLabel(String gender) {
        if (gender == null || gender.isBlank()) {
            return "Chưa xác định";
        }

        String normalized = gender.trim().toUpperCase();
        return switch (normalized) {
            case "MALE", "M" -> "Nam";
            case "FEMALE", "F" -> "Nữ";
            case "OTHER", "KHAC", "KHÁC" -> "Khác";
            case "NAM" -> "Nam";
            case "NU", "NỮ" -> "Nữ";
            default -> "Chưa xác định";
        };
    }
}
