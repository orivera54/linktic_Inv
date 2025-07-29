package com.linktic.inventario.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Filtro de autenticación basado en API Key
 */
@Slf4j
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    private final String expectedApiKey;

    public ApiKeyAuthenticationFilter(String expectedApiKey) {
        this.expectedApiKey = expectedApiKey;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String apiKey = request.getHeader("X-API-Key");
        
        if (apiKey != null && apiKey.equals(expectedApiKey)) {
            // Autenticación exitosa
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                "service", null, new ArrayList<>());
            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.debug("Autenticación exitosa con API Key para: {}", request.getRequestURI());
        } else if (apiKey != null) {
            // API Key inválida
            log.warn("API Key inválida recibida para: {}", request.getRequestURI());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        } else {
            // No se proporcionó API Key
            log.warn("No se proporcionó API Key para: {}", request.getRequestURI());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        
        filterChain.doFilter(request, response);
    }
}