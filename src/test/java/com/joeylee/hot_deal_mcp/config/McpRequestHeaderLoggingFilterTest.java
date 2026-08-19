package com.joeylee.hot_deal_mcp.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

@ExtendWith(OutputCaptureExtension.class)
class McpRequestHeaderLoggingFilterTest {

    @Test
    void logsAllHeadersWithoutExposingSensitiveValues(CapturedOutput output) throws Exception {
        McpRequestHeaderLoggingFilter filter = new McpRequestHeaderLoggingFilter();
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/mcp");
        request.setRemoteAddr("10.0.0.1");
        request.addHeader("Content-Type", "application/json");
        request.addHeader("X-Kakao-Trace-Id", "trace-1234");
        request.addHeader("Authorization", "Bearer private-token");

        filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

        assertThat(output).contains(
                "MCP incoming request",
                "method=POST",
                "uri=/mcp",
                "remoteAddress=10.0.0.1",
                "Content-Type=application/json",
                "X-Kakao-Trace-Id=trace-1234",
                "Authorization=Be***en"
        );
        assertThat(output).doesNotContain("Bearer private-token");
    }

    @Test
    void doesNotLogNonMcpRequests(CapturedOutput output) throws Exception {
        McpRequestHeaderLoggingFilter filter = new McpRequestHeaderLoggingFilter();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/actuator/health");

        filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

        assertThat(output).doesNotContain("MCP incoming request");
    }
}
