package com.joeylee.hot_deal_mcp.widget;

import static com.joeylee.hot_deal_mcp.widget.CreditCardWidgetLinks.CARD_RANKING_URL;
import static com.joeylee.hot_deal_mcp.widget.CreditCardWidgetComponents.badge;
import static com.joeylee.hot_deal_mcp.widget.CreditCardWidgetComponents.cardImage;
import static com.joeylee.hot_deal_mcp.widget.PlayMcpWidgetActions.openUrl;
import static com.joeylee.hot_deal_mcp.widget.PlayMcpWidgetActions.sendUserMessage;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.joeylee.hot_deal_mcp.service.AnnualFeeBand;
import com.joeylee.hot_deal_mcp.service.CreditCardGuideService;
import com.joeylee.hot_deal_mcp.service.Industry;
import org.springframework.stereotype.Component;

@Component
public class CreditCardRecommendationWidgetFactory {

    private static final int SELECTOR_BUTTONS_PER_ROW = 4;

    public PlayMcpWidgetResponse industrySelector() {
        List<Map<String, Object>> children = new ArrayList<>();
        children.add(Map.of(
                "type", "Text",
                "value", "어떤 업종의 카드 혜택을 원하시나요?"
        ));
        children.addAll(selectorButtonRows(Industry.selectorDisplayNames()));

        return new PlayMcpWidgetResponse(
                Map.of("type", "Card", "children", children),
                "원하시는 업종을 선택해 주세요."
        );
    }

    public PlayMcpWidgetResponse annualFeeSelector() {
        List<Map<String, Object>> children = new ArrayList<>();
        children.add(Map.of(
                "type", "Text",
                "value", "원하시는 연회비 구간을 선택해주세요"
        ));
        children.addAll(selectorButtonRows(AnnualFeeBand.displayNames()));

        return new PlayMcpWidgetResponse(
                Map.of("type", "Card", "children", children),
                "원하시는 연회비 구간을 선택해 주세요."
        );
    }

    public PlayMcpWidgetResponse creditCardGuideList(
            List<CreditCardGuideService.CardGuide> guides,
            Industry industry,
            AnnualFeeBand annualFeeBand
    ) {
        List<Map<String, Object>> cardRows = guides.stream()
                .map(this::creditCardRow)
                .toList();

        Map<String, Object> moreCardsButton = new LinkedHashMap<>();
        moreCardsButton.put("type", "Button");
        moreCardsButton.put("label", "더 많은 카드 보기");
        moreCardsButton.put("variant", "outline");
        moreCardsButton.put("pill", true);
        moreCardsButton.put("block", true);
        moreCardsButton.put("onClickAction", openUrl(CARD_RANKING_URL));

        Map<String, Object> cardList = new LinkedHashMap<>();
        cardList.put("type", "Col");
        cardList.put("gap", 4);
        cardList.put("padding", Map.of("x", 2, "y", 3));
        cardList.put("children", cardRows);

        Map<String, Object> widget = new LinkedHashMap<>();
        widget.put("type", "Card");
        widget.put("children", List.of(cardList, moreCardsButton));

        StringBuilder copyText = new StringBuilder()
                .append("### 카드 안내\n\n")
                .append("- 업종: **").append(industry.getDisplayName()).append("**\n")
                .append("- 연회비: **").append(annualFeeBand.getDisplayName()).append("**\n\n");
        guides.forEach(guide -> copyText
                .append("- ").append(guide.name())
                .append(" (`").append(guide.annualFee()).append("`)\n"));
        copyText.append("\n[더 많은 카드 보기](").append(CARD_RANKING_URL).append(")")
                .append("\n\n_연회비와 혜택 조건은 카드 상세 페이지에서 최종 확인해 주세요._");

        return new PlayMcpWidgetResponse(widget, copyText.toString());
    }

    private List<Map<String, Object>> selectorButtonRows(List<String> labels) {
        List<Map<String, Object>> rows = new ArrayList<>();
        for (int index = 0; index < labels.size(); index += SELECTOR_BUTTONS_PER_ROW) {
            List<Map<String, Object>> buttons = labels.subList(
                            index,
                            Math.min(index + SELECTOR_BUTTONS_PER_ROW, labels.size())
                    ).stream()
                    .map(this::selectorButton)
                    .toList();
            rows.add(Map.of(
                    "type", "Row",
                    "gap", 2,
                    "children", buttons
            ));
        }
        return rows;
    }

    private Map<String, Object> selectorButton(String label) {
        Map<String, Object> button = new LinkedHashMap<>();
        button.put("type", "Button");
        button.put("label", label);
        button.put("onClickAction", sendUserMessage(label));
        return button;
    }

    private Map<String, Object> creditCardRow(CreditCardGuideService.CardGuide guide) {
        Map<String, Object> cardImage = cardImage(guide.name(), guide.imageUrl());
        Map<String, Object> annualFeeRow = Map.of(
                "type", "Row",
                "gap", 2,
                "children", List.of(badge("연회비 " + guide.annualFee(), "info"))
        );

        List<Map<String, Object>> detailChildren = new ArrayList<>();
        detailChildren.add(Map.of(
                "type", "Text",
                "value", guide.name(),
                "size", "sm",
                "weight", "semibold",
                "maxLines", 2
        ));
        detailChildren.add(annualFeeRow);
        if (!guide.benefitCategories().isEmpty()) {
            detailChildren.add(Map.of(
                    "type", "Row",
                    "gap", 2,
                    "children", guide.benefitCategories().stream()
                            .map(this::benefitCategoryBadge)
                            .toList()
            ));
        }

        Map<String, Object> cardDetails = Map.of(
                "type", "Col",
                "gap", 2,
                "flex", "auto",
                "children", detailChildren
        );
        Map<String, Object> detailButton = new LinkedHashMap<>();
        detailButton.put("type", "Button");
        detailButton.put("label", ">");
        detailButton.put("variant", "ghost");
        detailButton.put("uniform", true);
        detailButton.put("size", "xl");
        detailButton.put("onClickAction", sendUserMessage(guide.name() + " 혜택 알려줘"));

        Map<String, Object> item = new LinkedHashMap<>();
        item.put("type", "Row");
        item.put("key", guide.name());
        item.put("gap", 4);
        item.put("align", "center");
        item.put("padding", Map.of("y", 2));
        item.put("children", List.of(cardImage, cardDetails, detailButton));
        return item;
    }

    private Map<String, Object> benefitCategoryBadge(String category) {
        return badge(category, "success");
    }
}
