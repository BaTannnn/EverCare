package com.evercare.utils;

import java.util.Map;

public final class PaginationUtils {

    private PaginationUtils() {
    }

    public static int getPage(Map<String, String> params) {
        try {
            if (params == null || params.get("page") == null || params.get("page").isBlank()) {
                return 1;
            }

            return Math.max(1, Integer.parseInt(params.get("page")));
        } catch (NumberFormatException ex) {
            return 1;
        }
    }

    public static int normalizePage(int page, long totalElements, int pageSize) {
        if (totalElements <= 0) {
            return 1;
        }

        int totalPages = (int) Math.ceil((double) totalElements / pageSize);

        if (page > totalPages) {
            return totalPages;
        }

        return Math.max(1, page);
    }
}