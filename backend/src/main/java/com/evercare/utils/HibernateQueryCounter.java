package com.evercare.utils;

import org.hibernate.resource.jdbc.spi.StatementInspector;

public class HibernateQueryCounter implements StatementInspector {
    private static final ThreadLocal<Long> REQUEST_QUERY_COUNT = new ThreadLocal<>();

    public static void startRequest() {
        REQUEST_QUERY_COUNT.set(0L);
    }

    public static long getRequestQueryCount() {
        Long count = REQUEST_QUERY_COUNT.get();
        return count == null ? 0L : count;
    }

    public static void finishRequest() {
        REQUEST_QUERY_COUNT.remove();
    }

    @Override
    public String inspect(String sql) {
        Long count = REQUEST_QUERY_COUNT.get();

        if (count != null) {
            REQUEST_QUERY_COUNT.set(count + 1L);
        }

        return sql;
    }
}
