package com.joeylee.hot_deal_mcp.service;

import java.util.Arrays;

public enum CardSortOrder {
    RELEASE_DATE("출시일순"),
    ANNUAL_FEE("연회비순");

    private final String displayName;

    CardSortOrder(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static CardSortOrder fromString(String value) {
        if (value == null || value.isBlank()) {
            return RELEASE_DATE;
        }

        String normalized = value.trim().replace(" ", "");
        return Arrays.stream(values())
                .filter(order -> order.displayName.equals(normalized))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "지원하지 않는 정렬 기준입니다: " + value
                ));
    }
}
