package com.joeylee.hot_deal_mcp.widget;

import static com.joeylee.hot_deal_mcp.widget.CreditCardWidgetComponents.badge;
import static com.joeylee.hot_deal_mcp.widget.CreditCardWidgetComponents.cardImage;
import static com.joeylee.hot_deal_mcp.widget.PlayMcpWidgetActions.openUrl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.joeylee.hot_deal_mcp.service.CreditCardGuideService;
import org.springframework.stereotype.Component;

@Component
public class CreditCardDetailWidgetFactory {

    public PlayMcpWidgetResponse creditCardDetail(CreditCardGuideService.CardDetail detail) {
        List<Map<String, Object>> children = new ArrayList<>();
        children.add(cardDetailHeader(detail));
        children.add(Map.of("type", "Divider"));
        children.add(Map.of(
                "type", "Text",
                "value", "주요 혜택",
                "weight", "semibold"
        ));
        detail.benefits().stream()
                .map(this::cardBenefitRow)
                .forEach(children::add);
        children.add(detailPageButton(detail.detailPageUrl()));

        Map<String, Object> widget = new LinkedHashMap<>();
        widget.put("type", "Card");
        widget.put("children", children);

        StringBuilder copyText = new StringBuilder("### ")
                .append(detail.name())
                .append("\n- 연회비: **").append(detail.annualFee()).append("**")
                .append("\n\n#### 주요 혜택\n");
        detail.benefits().forEach(benefit -> copyText.append("- ").append(benefit).append("\n"));
        copyText.append("\n[자세히 보기](").append(detail.detailPageUrl()).append(")");
        return new PlayMcpWidgetResponse(widget, copyText.toString());
    }

    public PlayMcpWidgetResponse cardNameClarification() {
        String message = "조회할 신한카드 상품명을 정확히 말씀해 주세요.";
        return new PlayMcpWidgetResponse(
                Map.of(
                        "type", "Card",
                        "children", List.of(Map.of(
                                "type", "Text",
                                "value", message
                        ))
                ),
                message
        );
    }

    private Map<String, Object> cardDetailHeader(CreditCardGuideService.CardDetail detail) {
        Map<String, Object> cardImage = cardImage(detail.name(), detail.imageUrl());
        Map<String, Object> cardSummary = Map.of(
                "type", "Col",
                "gap", 2,
                "flex", 1,
                "children", List.of(
                        Map.of(
                                "type", "Text",
                                "value", detail.name(),
                                "size", "lg",
                                "weight", "semibold",
                                "maxLines", 2
                        ),
                        badge("연회비 " + detail.annualFee(), "info")
                )
        );
        return Map.of(
                "type", "Row",
                "gap", 3,
                "align", "start",
                "children", List.of(cardImage, cardSummary)
        );
    }

    private Map<String, Object> cardBenefitRow(String benefit) {
        return Map.of(
                "type", "Row",
                "gap", 2,
                "children", List.of(
                        Map.of(
                                "type", "Text",
                                "value", "✓",
                                "weight", "semibold"
                        ),
                        Map.of(
                                "type", "Text",
                                "value", benefit,
                                "flex", 1
                        )
                )
        );
    }

    private Map<String, Object> detailPageButton(String detailPageUrl) {
        Map<String, Object> button = new LinkedHashMap<>();
        button.put("type", "Button");
        button.put("label", "자세히 보기");
        button.put("onClickAction", openUrl(detailPageUrl));
        return button;
    }
}
