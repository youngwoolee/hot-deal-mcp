package com.joeylee.hot_deal_mcp.service;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PopularCreditCardService {

    private static final List<PopularCardSpec> MOCK_POPULAR_CARD_SPECS = List.of(
            new PopularCardSpec(1, "신한카드 Deep Oil", "주유"),
            new PopularCardSpec(2, "신한카드 Mr.Life", "어디서나"),
            new PopularCardSpec(3, "신한카드 Air One", "마일리지"),
            new PopularCardSpec(4, "신한카드 Point Plan", "어디서나"),
            new PopularCardSpec(5, "신한카드 SOL트래블 체크", "여행")
    );

    private final CreditCardDataRepository cardRepository;

    public List<PopularCard> findPopularCards() {
        return MOCK_POPULAR_CARD_SPECS.stream()
                .map(spec -> {
                    CreditCardDataRepository.CardData card =
                            cardRepository.findByExactTitle(spec.cardName());
                    return new PopularCard(
                            spec.rank(),
                            card.title(),
                            spec.category(),
                            card.imageUrl()
                    );
                })
                .toList();
    }

    private record PopularCardSpec(int rank, String cardName, String category) {}

    public record PopularCard(
            int rank,
            String name,
            String category,
            String imageUrl
    ) {}
}
