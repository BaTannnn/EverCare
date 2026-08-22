package com.evercare.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

class QueryPagingSupportTest {
    @Test
    void capsRequestedPageSizeAtOneHundred() {
        MockEnvironment environment = new MockEnvironment().withProperty("items.pageSize", "20");

        int size = QueryPagingSupport.resolvePageSize(
                environment,
                "items.pageSize",
                Map.of("size", "1000000"),
                20
        );

        assertEquals(100, size);
    }

    @Test
    void fallsBackForInvalidPageSize() {
        MockEnvironment environment = new MockEnvironment().withProperty("items.pageSize", "20");

        assertEquals(20, QueryPagingSupport.resolvePageSize(
                environment,
                "items.pageSize",
                Map.of("size", "invalid"),
                20
        ));
    }
}
