package com.joeylee.hot_deal_mcp.service;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

public enum Industry {
    ANYWHERE(1, "어디서나"),
    FUEL(2, "주유"),
    LARGE_MART(3, "대형마트"),
    CONVENIENCE_STORE(4, "편의점"),
    SHOPPING(5, "쇼핑"),
    MOVIE_PERFORMANCE(6, "영화/공연"),
    DINING_DELIVERY(7, "외식/배달"),
    CAFE(8, "카페"),
    PUBLIC_TRANSPORT(9, "대중교통"),
    HOSPITAL_PHARMACY(10, "병원/약국"),
    UTILITIES(11, "공과금"),
    TELECOM(12, "통신"),
    EDUCATION_CHILDCARE(13, "교육/육아"),
    LEISURE(14, "레저"),
    AIRLINE(15, "항공"),
    AIRPORT(16, "공항"),
    BEAUTY(17, "뷰티"),
    SIMPLE_PAYMENT(18, "간편결제"),
    SUBSCRIPTION(19, "구독"),
    TRAVEL_STAY(20, "여행/숙박"),
    FINANCE(21, "금융"),
    DISCOUNT(22, "할인"),
    POINTS(23, "적립"),
    YOUTH(24, "청소년");

    private static final Set<Integer> SELECTOR_VISIBLE_CODES = Set.of(
            1, 2, 3, 4, 5, 6, 7, 9, 10, 12, 13, 15, 16, 20
    );

    private final int code;
    private final String displayName;

    Industry(int code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    public int getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getWidgetDisplayName() {
        return switch (this) {
            case AIRLINE -> "마일리지";
            case AIRPORT -> "공항라운지";
            default -> displayName;
        };
    }

    public String getSelectorDisplayName() {
        return switch (this) {
            case PUBLIC_TRANSPORT -> "교통";
            case AIRPORT -> "공항라운지";
            default -> displayName;
        };
    }

    public boolean isSelectorVisible() {
        return SELECTOR_VISIBLE_CODES.contains(code);
    }

    public static List<String> displayNames() {
        return Arrays.stream(values())
                .map(Industry::getDisplayName)
                .toList();
    }

    public static List<String> selectorDisplayNames() {
        return Arrays.stream(values())
                .filter(Industry::isSelectorVisible)
                .map(Industry::getSelectorDisplayName)
                .toList();
    }

    public static Industry fromCode(int code) {
        return Arrays.stream(values())
                .filter(industry -> industry.code == code)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "지원하지 않는 업종 코드입니다: " + code
                ));
    }
}
