/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.evercare.filters;

import com.evercare.utils.JwtUtils;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.Principal;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 *
 *
 */
public class JwtFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        if ("OPTIONS".equalsIgnoreCase(httpRequest.getMethod())) {
            chain.doFilter(request, response);
            return;
        }

        String contextPath = httpRequest.getContextPath();
        String uri = httpRequest.getRequestURI();

        String secureApiPrefix = contextPath + "/api/secure";
        String doctorApiPrefix = contextPath + "/api/doctor";
        String staffApiPrefix = contextPath + "/api/staff";
        String pharmacistApiPrefix = contextPath + "/api/pharmacist";

        boolean protectedApi =
                uri.startsWith(secureApiPrefix)
                        || uri.startsWith(doctorApiPrefix)
                        || uri.startsWith(staffApiPrefix)
                        || uri.startsWith(pharmacistApiPrefix);

        if (!protectedApi) {
            chain.doFilter(request, response);
            return;
        }

        String header = httpRequest.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing or invalid Authorization header.");
            return;
        }

        String token = header.substring(7);

        try {
            String username = JwtUtils.validateTokenAndGetUsername(token);

            if (username == null) {
                httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token không hợp lệ hoặc hết hạn");
                return;
            }

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(username, null, null);

            SecurityContextHolder.getContext().setAuthentication(authentication);

            HttpServletRequest authenticatedRequest = new HttpServletRequestWrapper(httpRequest) {
                @Override
                public Principal getUserPrincipal() {
                    return authentication;
                }
            };

            chain.doFilter(authenticatedRequest, response);

        } catch (Exception e) {
            httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token không hợp lệ hoặc hết hạn");
        }
    }
}
