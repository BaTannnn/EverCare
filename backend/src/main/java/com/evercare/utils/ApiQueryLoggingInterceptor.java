package com.evercare.utils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.logging.Logger;
import org.springframework.web.servlet.HandlerInterceptor;

public class ApiQueryLoggingInterceptor implements HandlerInterceptor {
    private static final Logger LOGGER = Logger.getLogger(ApiQueryLoggingInterceptor.class.getName());
    private static final String START_TIME_ATTRIBUTE = ApiQueryLoggingInterceptor.class.getName() + ".startTime";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        request.setAttribute(START_TIME_ATTRIBUTE, System.nanoTime());
        HibernateQueryCounter.startRequest();
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return;
        }

        long startTime = attributeAsLong(request, START_TIME_ATTRIBUTE, System.nanoTime());
        long durationMs = (System.nanoTime() - startTime) / 1_000_000L;
        long queryCount = HibernateQueryCounter.getRequestQueryCount();
        HibernateQueryCounter.finishRequest();

        String message = String.format(
                "[API_QUERY] sql=%d ms=%d status=%d %s %s",
                queryCount,
                durationMs,
                response.getStatus(),
                request.getMethod(),
                request.getRequestURI()
        );

        if (ex != null) {
            LOGGER.warning(message + " error=" + ex.getClass().getSimpleName() + ": " + ex.getMessage());
            return;
        }

        LOGGER.info(message);
    }

    private long attributeAsLong(HttpServletRequest request, String name, long fallback) {
        Object value = request.getAttribute(name);
        return value instanceof Long longValue ? longValue : fallback;
    }
}
