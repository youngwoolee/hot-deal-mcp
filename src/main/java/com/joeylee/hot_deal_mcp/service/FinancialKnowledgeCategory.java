package com.joeylee.hot_deal_mcp.service;

import java.util.Arrays;

public enum FinancialKnowledgeCategory {
    TREND("트렌드", "TREND"),
    FINANCE("금융", "FINANCE"),
    CARD_LAB("카드연구소", "CARD LAB");

    private final String displayName;
    private final String widgetTitle;

    FinancialKnowledgeCategory(String displayName, String widgetTitle) {
        this.displayName = displayName;
        this.widgetTitle = widgetTitle;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getWidgetTitle() {
        return widgetTitle;
    }

    public FinancialKnowledgeCategory next() {
        return switch (this) {
            case TREND -> FINANCE;
            case FINANCE -> CARD_LAB;
            case CARD_LAB -> TREND;
        };
    }

    public static FinancialKnowledgeCategory fromString(String value) {
        if (value == null || value.isBlank()) {
            return TREND;
        }

        String normalized = value.trim().replace(" ", "");
        return Arrays.stream(values())
                .filter(category -> category.displayName.equals(normalized))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "지원하지 않는 금융생활지식 카테고리입니다: " + value
                ));
    }
}
