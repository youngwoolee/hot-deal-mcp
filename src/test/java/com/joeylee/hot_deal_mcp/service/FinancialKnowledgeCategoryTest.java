package com.joeylee.hot_deal_mcp.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class FinancialKnowledgeCategoryTest {

    @Test
    void missingCategoryDefaultsToTrend() {
        assertThat(FinancialKnowledgeCategory.fromString(null))
                .isEqualTo(FinancialKnowledgeCategory.TREND);
        assertThat(FinancialKnowledgeCategory.fromString(" "))
                .isEqualTo(FinancialKnowledgeCategory.TREND);
    }

    @Test
    void parsesSupportedCategoriesIgnoringSpaces() {
        assertThat(FinancialKnowledgeCategory.fromString("트렌드"))
                .isEqualTo(FinancialKnowledgeCategory.TREND);
        assertThat(FinancialKnowledgeCategory.fromString("금융"))
                .isEqualTo(FinancialKnowledgeCategory.FINANCE);
        assertThat(FinancialKnowledgeCategory.fromString("카드 연구소"))
                .isEqualTo(FinancialKnowledgeCategory.CARD_LAB);
    }

    @Test
    void cyclesThroughAllCategories() {
        assertThat(FinancialKnowledgeCategory.TREND.next())
                .isEqualTo(FinancialKnowledgeCategory.FINANCE);
        assertThat(FinancialKnowledgeCategory.FINANCE.next())
                .isEqualTo(FinancialKnowledgeCategory.CARD_LAB);
        assertThat(FinancialKnowledgeCategory.CARD_LAB.next())
                .isEqualTo(FinancialKnowledgeCategory.TREND);
    }

    @Test
    void rejectsUnsupportedCategory() {
        assertThatThrownBy(() -> FinancialKnowledgeCategory.fromString("보험"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
