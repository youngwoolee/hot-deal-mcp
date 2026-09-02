package com.joeylee.hot_deal_mcp.widget;

import static com.joeylee.hot_deal_mcp.widget.CreditCardWidgetLinks.CARD_RANKING_URL;
import static com.joeylee.hot_deal_mcp.widget.CreditCardWidgetComponents.badge;
import static com.joeylee.hot_deal_mcp.widget.CreditCardWidgetComponents.cardImage;
import static com.joeylee.hot_deal_mcp.widget.PlayMcpWidgetActions.openUrl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.joeylee.hot_deal_mcp.service.PopularCreditCardService;
import org.springframework.stereotype.Component;

@Component
public class PopularCreditCardWidgetFactory {

    public PlayMcpWidgetResponse popularCreditCardList(
            List<PopularCreditCardService.PopularCard> popularCards
    ) {
        List<Map<String, Object>> children = new ArrayList<>();
        children.add(Map.of(
                "type", "Text",
                "value", "추천 TOP5",
                "size", "lg",
                "weight", "semibold"
        ));
        children.add(Map.of(
                "type", "Caption",
                "value", "신한카드에서 추천해드리는 카드예요!"
        ));
        children.add(Map.of(
                "type", "Col",
                "gap", 4,
                "padding", Map.of("x", 2, "y", 3),
                "children", popularCards.stream()
                        .map(this::popularCreditCardRow)
                        .toList()
        ));

        Map<String, Object> rankButton = new LinkedHashMap<>();
        rankButton.put("type", "Button");
        rankButton.put("label", "신한카드 TOP10 차트 보러가기");
        rankButton.put("variant", "outline");
        rankButton.put("block", true);
        rankButton.put("onClickAction", openUrl(CARD_RANKING_URL));
        children.add(rankButton);

        Map<String, Object> widget = new LinkedHashMap<>();
        widget.put("type", "Card");
        widget.put("children", children);

        StringBuilder copyText = new StringBuilder("### 추천 TOP5\n\n")
                .append("신한카드에서 추천해드리는 카드예요!\n\n");
        popularCards.forEach(card -> copyText
                .append(card.rank()).append(". **").append(card.name()).append("**")
                .append(" · ").append(card.category()).append("\n"));
        copyText.append("\n[신한카드 TOP10 차트 보러가기](")
                .append(CARD_RANKING_URL)
                .append(")");

        return new PlayMcpWidgetResponse(widget, copyText.toString());
    }

    private Map<String, Object> popularCreditCardRow(
            PopularCreditCardService.PopularCard card
    ) {
        String rankLabel = switch (card.rank()) {
            case 1 -> "🥇";
            case 2 -> "🥈";
            case 3 -> "🥉";
            default -> String.valueOf(card.rank());
        };
        String rankSize = card.rank() <= 3 ? "xl" : "lg";
        Map<String, Object> cardImage = cardImage(card.name(), card.imageUrl());
        Map<String, Object> cardDetails = Map.of(
                "type", "Col",
                "gap", 2,
                "flex", "auto",
                "children", List.of(
                        Map.of(
                                "type", "Text",
                                "value", card.name(),
                                "size", "sm",
                                "weight", "semibold",
                                "maxLines", 2
                        ),
                        Map.of(
                                "type", "Row",
                                "gap", 2,
                                "children", List.of(badge(card.category(), "success"))
                        )
                )
        );
        return Map.of(
                "type", "Row",
                "key", card.name(),
                "gap", 4,
                "align", "center",
                "padding", Map.of("y", 2),
                "children", List.of(
                        Map.of(
                                "type", "Text",
                                "value", rankLabel,
                                "size", rankSize,
                                "weight", "semibold",
                                "width", 32,
                                "textAlign", "center"
                        ),
                        cardImage,
                        cardDetails
                )
        );
    }
}
