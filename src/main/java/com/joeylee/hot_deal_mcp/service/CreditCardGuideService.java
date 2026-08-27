package com.joeylee.hot_deal_mcp.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreditCardGuideService {

    private static final int MAX_SEARCH_RESULTS = 5;

    private final CreditCardDataRepository cardRepository;

    public record CardGuide(
            String issuer,
            String name,
            String annualFee,
            List<String> benefitCategories,
            List<String> benefits,
            String imageUrl
    ) {}

    public record CardDetail(
            String issuer,
            String name,
            String annualFee,
            List<String> benefits,
            String detailPageUrl,
            String imageUrl
    ) {}

    public List<CardGuide> findGuides(int industryCode, AnnualFeeBand annualFeeBand) {
        Industry industry = Industry.fromCode(industryCode);
        return cardRepository.search(industryCode, annualFeeBand, MAX_SEARCH_RESULTS).stream()
                .map(card -> new CardGuide(
                        "신한카드",
                        card.title(),
                        formatAnnualFee(card.annualFee()),
                        benefitCategories(industry, card.benefitCodes()),
                        card.benefits(),
                        card.imageUrl()
                ))
                .toList();
    }

    private List<String> benefitCategories(
            Industry searchedIndustry,
            List<String> cardBenefitCodes
    ) {
        if (!searchedIndustry.isWidgetVisible()) {
            return List.of();
        }

        Set<String> otherCategories = new LinkedHashSet<>();
        for (String benefitCode : cardBenefitCodes) {
            try {
                Industry industry = Industry.fromCode(Integer.parseInt(benefitCode));
                if (industry.isWidgetVisible() && industry != searchedIndustry) {
                    otherCategories.add(industry.getWidgetDisplayName());
                }
            } catch (IllegalArgumentException ignored) {
                // 알 수 없는 코드는 위젯에 노출하지 않는다.
            }
        }

        List<String> randomizedCategories = new ArrayList<>(otherCategories);
        Collections.shuffle(randomizedCategories);

        List<String> result = new ArrayList<>();
        result.add(searchedIndustry.getWidgetDisplayName());
        randomizedCategories.stream().limit(2).forEach(result::add);
        return List.copyOf(result);
    }

    public CardDetail findCardDetail(String cardName) {
        CreditCardName exactCardName = CreditCardName.fromDisplayName(cardName);
        CreditCardDataRepository.CardData card =
                cardRepository.findByExactTitle(exactCardName.getDisplayName());
        return new CardDetail(
                "신한카드",
                card.title(),
                formatAnnualFee(card.annualFee()),
                card.benefits(),
                card.detailPageUrl(),
                card.imageUrl()
        );
    }

    private String formatAnnualFee(int annualFee) {
        return String.format("%,d원", annualFee);
    }
}
