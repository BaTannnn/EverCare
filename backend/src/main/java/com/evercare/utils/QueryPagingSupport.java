package com.evercare.utils;

import java.util.Map;
import org.hibernate.query.Query;
import org.springframework.core.env.Environment;

public final class QueryPagingSupport {
    private static final int MAX_PAGE_SIZE = 100;

    private QueryPagingSupport() {
    }

    public static int resolvePageSize(Environment env, String propertyName, Map<String, String> params, int defaultValue) {
        int defaultPageSize = Math.min(env.getProperty(propertyName, Integer.class, defaultValue), MAX_PAGE_SIZE);
        if (params == null) {
            return defaultPageSize;
        }

        String sizeValue = params.get("size");
        if (sizeValue == null || sizeValue.isBlank()) {
            return defaultPageSize;
        }

        try {
            int size = Integer.parseInt(sizeValue.trim());
            return size > 0 ? Math.min(size, MAX_PAGE_SIZE) : defaultPageSize;
        } catch (NumberFormatException ex) {
            return defaultPageSize;
        }
    }

    public static void applyPaging(Query<?> query, Map<String, String> params, long count, int pageSize) {
        int page = PaginationUtils.normalizePage(PaginationUtils.getPage(params), count, pageSize);
        query.setFirstResult((page - 1) * pageSize);
        query.setMaxResults(pageSize);
    }
}
