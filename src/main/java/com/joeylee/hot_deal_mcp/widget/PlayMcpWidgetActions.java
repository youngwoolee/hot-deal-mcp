package com.joeylee.hot_deal_mcp.widget;

import java.util.Map;

final class PlayMcpWidgetActions {

    private PlayMcpWidgetActions() {}

    static Map<String, Object> sendUserMessage(String text) {
        return Map.of(
                "payload", Map.of(
                        "target", Map.of(
                                "type", "sendUserMessage",
                                "properties", Map.of("text", text)
                        )
                )
        );
    }

    static Map<String, Object> openUrl(String url) {
        return Map.of(
                "payload", Map.of(
                        "target", Map.of(
                                "url", url,
                                "pcUrl", url
                        )
                )
        );
    }
}
