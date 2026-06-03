package com.evercare.dtos.response.statistics;

import java.util.List;

public class DiseaseStatisticsResponse {
    private List<StatisticItemResponse> items;

    public List<StatisticItemResponse> getItems() {
        return items;
    }

    public void setItems(List<StatisticItemResponse> items) {
        this.items = items;
    }
}
