package com.joeylee.hot_deal_mcp.widget;

import java.util.Map;

final class CreditCardWidgetComponents {

    private CreditCardWidgetComponents() {}

    static Map<String, Object> cardImage(String cardName, String imageUrl) {
        return Map.of(
                "type", "Image",
                "src", imageUrl,
                "alt", cardName + " 카드 이미지",
                "width", 112,
                "height", 72,
                "fit", "contain",
                "radius", "sm"
        );
    }

    static Map<String, Object> badge(String label, String color) {
        return Map.of(
                "type", "Badge",
                "label", label,
                "color", color,
                "variant", "soft"
        );
    }
}
