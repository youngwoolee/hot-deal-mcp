package com.joeylee.hot_deal_mcp.config;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

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
    private static final Set<String> SENSITIVE_HEADERS = Set.of(
            "authorization",
            "proxy-authorization",
            "cookie",
            "set-cookie",
            "x-api-key",
            "api-key",
            "infer-auth-key",
            "client-secret"
    );

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
            headers.put(name, sanitizeHeaderValue(name, value));
        });
        return headers;
    }

    private String sanitizeHeaderValue(String name, String value) {
        if (SENSITIVE_HEADERS.contains(name.toLowerCase(Locale.ROOT))) {
            return mask(value);
        }
        if (value.length() <= MAX_LOG_VALUE_LENGTH) {
            return value;
        }
        return value.substring(0, MAX_LOG_VALUE_LENGTH) + "...";
    }

    private String mask(String value) {
        if (value.length() <= 4) {
            return "***";
        }
        return value.substring(0, 2) + "***" + value.substring(value.length() - 2);
    }

}
