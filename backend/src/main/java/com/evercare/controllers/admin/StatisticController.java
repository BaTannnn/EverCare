package com.evercare.controllers.admin;

import com.evercare.dtos.response.statistics.DiseaseStatisticsResponse;
import com.evercare.dtos.response.statistics.PatientStatisticsResponse;
import com.evercare.dtos.response.statistics.RevenueStatisticsResponse;
import com.evercare.dtos.response.statistics.ServiceUsageStatisticsResponse;
import com.evercare.services.StatisticService;
import com.evercare.utils.AuthSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin/statistics")
public class StatisticController {
    @Autowired
    private StatisticService statisticService;

    @Autowired
    private AuthSupport authSupport;

    @GetMapping
    public String index(
            @RequestParam(value = "fromDate", required = false) String fromDate,
            @RequestParam(value = "toDate", required = false) String toDate,
            @RequestParam(value = "groupBy", required = false) String groupBy,
            Model model
    ) {
        try {
            this.authSupport.requireReceptionistUser();
        } catch (AccessDeniedException | SecurityException ex) {
            return "redirect:/admin/login";
        }

        StatisticService.DateRange range;
        try {
            range = this.statisticService.resolveDateRange(fromDate, toDate);
        } catch (RuntimeException ex) {
            range = this.statisticService.resolveDateRange(null, null);
        }

        model.addAttribute("fromDate", range.fromDate().toString());
        model.addAttribute("toDate", range.toDate().toString());
        model.addAttribute("groupBy", groupBy != null && !groupBy.isBlank() ? groupBy.trim().toUpperCase() : "DAY");

        return "statistics/statistics";
    }

    @GetMapping("/api/patients")
    public ResponseEntity<PatientStatisticsResponse> patients(
            @RequestParam(value = "fromDate", required = false) String fromDate,
            @RequestParam(value = "toDate", required = false) String toDate
    ) {
        return ResponseEntity.ok(this.statisticService.getPatientStatistics(fromDate, toDate));
    }

    @GetMapping("/api/services")
    public ResponseEntity<ServiceUsageStatisticsResponse> services(
            @RequestParam(value = "fromDate", required = false) String fromDate,
            @RequestParam(value = "toDate", required = false) String toDate
    ) {
        return ResponseEntity.ok(this.statisticService.getServiceUsageStatistics(fromDate, toDate));
    }

    @GetMapping("/api/diseases")
    public ResponseEntity<DiseaseStatisticsResponse> diseases(
            @RequestParam(value = "fromDate", required = false) String fromDate,
            @RequestParam(value = "toDate", required = false) String toDate
    ) {
        return ResponseEntity.ok(this.statisticService.getDiseaseStatistics(fromDate, toDate));
    }

    @GetMapping("/api/revenue")
    public ResponseEntity<RevenueStatisticsResponse> revenue(
            @RequestParam(value = "fromDate", required = false) String fromDate,
            @RequestParam(value = "toDate", required = false) String toDate,
            @RequestParam(value = "groupBy", required = false, defaultValue = "DAY") String groupBy
    ) {
        return ResponseEntity.ok(this.statisticService.getRevenueStatistics(fromDate, toDate, groupBy));
    }
}
