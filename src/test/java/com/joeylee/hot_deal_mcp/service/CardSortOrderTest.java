package com.joeylee.hot_deal_mcp.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class CardSortOrderTest {

    @Test
    void missingSortDefaultsToReleaseDate() {
        assertThat(CardSortOrder.fromString(null)).isEqualTo(CardSortOrder.RELEASE_DATE);
        assertThat(CardSortOrder.fromString(" ")).isEqualTo(CardSortOrder.RELEASE_DATE);
    }

    @Test
    void parsesSupportedSortNamesIgnoringSpaces() {
        assertThat(CardSortOrder.fromString("출시일 순")).isEqualTo(CardSortOrder.RELEASE_DATE);
        assertThat(CardSortOrder.fromString("연회비순")).isEqualTo(CardSortOrder.ANNUAL_FEE);
    }

    @Test
    void rejectsUnsupportedSortName() {
        assertThatThrownBy(() -> CardSortOrder.fromString("인기순"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
