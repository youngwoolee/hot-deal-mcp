package com.joeylee.hot_deal_mcp.config;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * MCP 호출 시 전달된 요청 헤더를 기록한다.
 * 인증 정보가 로그에 그대로 노출되지 않도록 민감한 값은 항상 마스킹한다.
 */
@Component
@Slf4j
public class McpRequestHeaderLoggingFilter extends OncePerRequestFilter {

    private static final int MAX_LOG_VALUE_LENGTH = 200;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        return !(requestUri.equals("/mcp") || requestUri.startsWith("/mcp/"));
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        log.info(
                "MCP incoming request - method={}, uri={}, remoteAddress={}, headers={}",
                request.getMethod(),
                request.getRequestURI(),
                request.getRemoteAddr(),
                sanitizedHeaders(request)
        );
        filterChain.doFilter(request, response);
    }

    private Map<String, String> sanitizedHeaders(HttpServletRequest request) {
        Map<String, String> headers = new LinkedHashMap<>();
        Collections.list(request.getHeaderNames()).forEach(name -> {
            String value = String.join(", ", Collections.list(request.getHeaders(name)));
            headers.put(name, isSensitive(name) ? mask(value) : sanitize(value));
        });
        return headers;
    }

    private boolean isSensitive(String headerName) {
        String normalized = headerName.toLowerCase(Locale.ROOT);
        return normalized.contains("authorization")
                || normalized.contains("cookie")
                || normalized.contains("token")
                || normalized.contains("secret")
                || normalized.contains("api-key")
                || normalized.contains("apikey");
    }

    private String mask(String value) {
        String sanitized = sanitize(value);
        if (sanitized.length() <= 4) {
            return "****";
        }
        return sanitized.substring(0, 2) + "***" + sanitized.substring(sanitized.length() - 2);
    }

    private String sanitize(String value) {
        String sanitized = value.replace('\r', '_').replace('\n', '_');
        return sanitized.length() <= MAX_LOG_VALUE_LENGTH
                ? sanitized
                : sanitized.substring(0, MAX_LOG_VALUE_LENGTH) + "...";
    }
}
