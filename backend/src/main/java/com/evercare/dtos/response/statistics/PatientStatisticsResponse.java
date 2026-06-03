package com.evercare.dtos.response.statistics;

import java.util.List;

public class PatientStatisticsResponse {
    private List<StatisticItemResponse> byAgeGroups;
    private List<StatisticItemResponse> byGender;
    private List<StatisticItemResponse> byDepartment;

    public List<StatisticItemResponse> getByAgeGroups() {
        return byAgeGroups;
    }

    public void setByAgeGroups(List<StatisticItemResponse> byAgeGroups) {
        this.byAgeGroups = byAgeGroups;
    }

    public List<StatisticItemResponse> getByGender() {
        return byGender;
    }

    public void setByGender(List<StatisticItemResponse> byGender) {
        this.byGender = byGender;
    }

    public List<StatisticItemResponse> getByDepartment() {
        return byDepartment;
    }

    public void setByDepartment(List<StatisticItemResponse> byDepartment) {
        this.byDepartment = byDepartment;
    }
}
