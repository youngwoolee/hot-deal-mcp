package com.joeylee.hot_deal_mcp.service;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreditCardGuideService {

    private static final int MAX_SEARCH_RESULTS = 3;

    private final CreditCardDataRepository cardRepository;

    public record CardGuide(
            String issuer,
            String name,
            String annualFee,
            String benefitCategory,
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
                        industry.isWidgetVisible() ? industry.getWidgetDisplayName() : null,
                        card.benefits(),
                        card.imageUrl()
                ))
                .toList();
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
