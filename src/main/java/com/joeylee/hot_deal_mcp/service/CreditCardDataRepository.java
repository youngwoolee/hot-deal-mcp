package com.joeylee.hot_deal_mcp.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Repository;

@Repository
public class CreditCardDataRepository {

    private static final String DATA_FILE = "data.json";
    private static final String CARD_HOST = "https://www.shinhancard.com";
    private static final String IMAGE_HOST = "https://cdn.www.shinhancard.com";

    private final List<CardData> cards;
    private final Map<String, CardData> cardsByExactTitle;

    public CreditCardDataRepository(ObjectMapper objectMapper) {
        this.cards = loadCards(objectMapper);
        this.cardsByExactTitle = indexByExactTitle(cards);
    }

    public List<CardData> search(int industryCode, AnnualFeeBand annualFeeBand, int limit) {
        String categoryCode = String.valueOf(industryCode);
        return cards.stream()
                .filter(card -> card.benefitCodes().contains(categoryCode))
                .filter(card -> annualFeeBand.matches(card.annualFee()))
                .sorted(Comparator
                        .comparingInt((CardData card) -> card.benefitCodes().size())
                        .thenComparingInt(CardData::annualFee)
                        .thenComparing(CardData::title))
                .limit(limit)
                .toList();
    }

    public CardData findByExactTitle(String title) {
        if (title == null) {
            throw new IllegalArgumentException("카드 이름을 입력해주세요.");
        }
        CardData card = cardsByExactTitle.get(title.trim());
        if (card == null) {
            throw new IllegalArgumentException("지원하지 않는 카드 이름입니다: " + title);
        }
        return card;
    }

    private List<CardData> loadCards(ObjectMapper objectMapper) {
        try (InputStream inputStream = new ClassPathResource(DATA_FILE).getInputStream()) {
            JsonNode hits = objectMapper.readTree(inputStream).path("hits").path("hits");
            if (!hits.isArray()) {
                throw new IllegalStateException("data.json의 hits.hits가 배열이 아닙니다.");
            }

            List<CardData> loadedCards = new ArrayList<>();
            for (JsonNode hit : hits) {
                loadedCards.add(toCardData(hit.path("_source")));
            }
            return List.copyOf(loadedCards);
        } catch (IOException exception) {
            throw new IllegalStateException("data.json을 불러오지 못했습니다.", exception);
        }
    }

    private Map<String, CardData> indexByExactTitle(List<CardData> loadedCards) {
        Map<String, CardData> index = new LinkedHashMap<>();
        for (CardData card : loadedCards) {
            CardData previous = index.put(card.title(), card);
            if (previous != null) {
                throw new IllegalStateException("중복 카드명입니다: " + card.title());
            }
        }
        return Map.copyOf(index);
    }

    private CardData toCardData(JsonNode source) {
        String title = requiredText(source, "pagetitle").trim();
        List<String> benefits = new ArrayList<>();
        for (int index = 1; index <= 3; index++) {
            String name = text(source, "svtpnm" + index);
            String description = text(source, "svtptt" + index);
            if (!name.isBlank() && !description.isBlank()) {
                benefits.add(name + " · " + description);
            } else if (!name.isBlank()) {
                benefits.add(name);
            } else if (!description.isBlank()) {
                benefits.add(description);
            }
        }

        return new CardData(
                title,
                source.path("pvafeat").asInt(),
                stringList(source.path("svtcd")),
                List.copyOf(benefits),
                text(source, "pagecont"),
                absoluteUrl(CARD_HOST, requiredText(source, "pageurl")),
                absoluteUrl(IMAGE_HOST, requiredText(source, "thumbimgurl"))
        );
    }

    private List<String> stringList(JsonNode node) {
        if (!node.isArray()) {
            return List.of();
        }
        List<String> values = new ArrayList<>();
        node.forEach(value -> values.add(value.asText()));
        return List.copyOf(values);
    }

    private String requiredText(JsonNode source, String fieldName) {
        String value = text(source, fieldName);
        if (value.isBlank()) {
            throw new IllegalStateException("data.json 필수 필드가 비어 있습니다: " + fieldName);
        }
        return value;
    }

    private String text(JsonNode source, String fieldName) {
        return source.path(fieldName).asText("").trim();
    }

    private String absoluteUrl(String host, String path) {
        if (path.startsWith("http://") || path.startsWith("https://")) {
            return path;
        }
        return host + (path.startsWith("/") ? path : "/" + path);
    }

    public record CardData(
            String title,
            int annualFee,
            List<String> benefitCodes,
            List<String> benefits,
            String summary,
            String detailPageUrl,
            String imageUrl
    ) {}
}
