package com.joeylee.hot_deal_mcp.service;

import java.util.Arrays;
import java.util.List;

public enum AnnualFeeBand {
    TEN_THOUSAND_RANGE("0~1만원대", 10_000),
    THIRTY_THOUSAND_RANGE("2~3만원대", 30_000),
    NO_LIMIT("제한없음", null);

    private final String displayName;
    private final Integer representativeFee;

    AnnualFeeBand(String displayName, Integer representativeFee) {
        this.displayName = displayName;
        this.representativeFee = representativeFee;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Integer getRepresentativeFee() {
        return representativeFee;
    }

    public boolean matches(int annualFee) {
        return switch (this) {
            case TEN_THOUSAND_RANGE -> annualFee >= 0 && annualFee < 20_000;
            case THIRTY_THOUSAND_RANGE -> annualFee >= 20_000 && annualFee < 40_000;
            case NO_LIMIT -> true;
        };
    }

    public static List<String> displayNames() {
        return Arrays.stream(values())
                .map(AnnualFeeBand::getDisplayName)
                .toList();
    }

    public static AnnualFeeBand fromString(String value) {
        if (value == null || value.isBlank()) {
            return NO_LIMIT;
        }

        String normalized = value.trim().replace(" ", "");
        return Arrays.stream(values())
                .filter(band -> band.displayName.replace(" ", "").equals(normalized))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "지원하지 않는 연회비 구간입니다: " + value
                ));
    }
}
