package com.joeylee.hot_deal_mcp.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class IndustryTest {

    @Test
    void toolSearchSupportsAllIndustriesWhileWidgetShowsOnlySelectedCategories() {
        assertThat(Industry.values()).hasSize(24);
        assertThat(Industry.selectorDisplayNames()).containsExactly(
                "어디서나",
                "주유",
                "대형마트",
                "편의점",
                "쇼핑",
                "영화/공연",
                "외식/배달",
                "대중교통",
                "병원/약국",
                "통신",
                "교육/육아",
                "마일리지",
                "공항라운지",
                "여행/숙박"
        );
        assertThat(Industry.fromCode(8)).isEqualTo(Industry.CAFE);
        assertThat(Industry.fromCode(24)).isEqualTo(Industry.YOUTH);
    }
}
