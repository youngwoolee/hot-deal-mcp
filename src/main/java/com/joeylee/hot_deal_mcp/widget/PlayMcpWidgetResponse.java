package com.joeylee.hot_deal_mcp.widget;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * PlayMCP가 인식하는 최상위 응답 형식.
 */
public record PlayMcpWidgetResponse(
        Object widget,
        @JsonProperty("copy_text") String copyText
) {}
