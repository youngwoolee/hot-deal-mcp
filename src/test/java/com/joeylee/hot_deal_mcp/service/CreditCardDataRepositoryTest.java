package com.joeylee.hot_deal_mcp.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CreditCardDataRepositoryTest {

    private CreditCardDataRepository repository;

    @BeforeEach
    void setUp() {
        repository = new CreditCardDataRepository(new ObjectMapper());
    }

    @Test
    void everyEnumDisplayNameHasAnExactDataMatch() {
        assertThat(CreditCardName.values()).hasSize(81);
        for (CreditCardName cardName : CreditCardName.values()) {
            assertThat(repository.findByExactTitle(cardName.getDisplayName()).title())
                    .isEqualTo(cardName.getDisplayName());
        }
    }

    @Test
    void titleLookupDoesNotUseFuzzyMatching() {
        assertThatThrownBy(() -> repository.findByExactTitle("신한카드 sol트래블 체크"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void searchFiltersByBenefitCodeAndAnnualFeeBand() {
        List<CreditCardDataRepository.CardData> cards =
                repository.search(5, AnnualFeeBand.TEN_THOUSAND_RANGE, 3);

        assertThat(cards).hasSize(3);
        assertThat(cards).allSatisfy(card -> {
            assertThat(card.benefitCodes()).contains("5");
            assertThat(card.annualFee()).isBetween(0, 19_999);
        });
    }
}
