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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 *
 * @author huu-thanhduong
 */
public class JwtFilter implements Filter{

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        
        String contextPath = httpRequest.getContextPath();
        String secureApiPrefix = String.format("%s/api/secure", contextPath);
        String doctorApiPrefix = String.format("%s/api/doctor", contextPath);
        String staffApiPrefix = String.format("%s/api/staff", contextPath);
        String pharmacistApiPrefix = String.format("%s/api/pharmacist", contextPath);

        if (httpRequest.getRequestURI().startsWith(secureApiPrefix)
                || httpRequest.getRequestURI().startsWith(doctorApiPrefix)
                || httpRequest.getRequestURI().startsWith(staffApiPrefix)
                || httpRequest.getRequestURI().startsWith(pharmacistApiPrefix)) {
        
           
            String header = httpRequest.getHeader("Authorization");
            
            if (header == null || !header.startsWith("Bearer ")) {
                ((HttpServletResponse) response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing or invalid Authorization header.");
                return;
            }
            else {
                String token = header.substring(7);
                try {
                    String username = JwtUtils.validateTokenAndGetUsername(token);
                    if (username != null) {
                        httpRequest.setAttribute("username", username);
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(username, null, null);
                        SecurityContextHolder.getContext().setAuthentication(authentication);

                        HttpServletRequest authenticatedRequest = new HttpServletRequestWrapper(httpRequest) {
                            @Override
                            public Principal getUserPrincipal() {
                                return authentication;
                            }
                        };

                        chain.doFilter(authenticatedRequest, response);
                        return;
                    }
                } catch (Exception e) {
                    // Log lỗi
                }
            }

            ((HttpServletResponse) response).sendError(HttpServletResponse.SC_UNAUTHORIZED, 
                    "Token không hợp lệ hoặc hết hạn");
        }
        
        chain.doFilter(request, response);
    }
    
}
