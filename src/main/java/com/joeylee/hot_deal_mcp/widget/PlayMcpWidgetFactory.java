package com.joeylee.hot_deal_mcp.widget;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.joeylee.hot_deal_mcp.service.AnnualFeeBand;
import com.joeylee.hot_deal_mcp.service.CreditCardGuideService;
import com.joeylee.hot_deal_mcp.service.Industry;
import org.springframework.stereotype.Component;

/**
 * 도메인 데이터를 PlayMCP ChatKit 위젯 페이로드로 변환한다.
 * status는 PlayMCP가 자동으로 추가하므로 widget에는 포함하지 않는다.
 */
@Component
public class PlayMcpWidgetFactory {

    private static final int MAX_WIDGET_ITEMS = 100;
    private static final int MAX_COPY_TEXT_ITEMS = 10;
    private static final String HOT_DEAL_RANK_URL = "https://www.algumon.com/deal/rank";
    private static final String MORE_CARDS_URL =
            "https://www.shinhancard.com/mob/MOBFM039N/MOBFM039C01.shc?crustMenuId=ms467";

    public PlayMcpWidgetResponse clarificationNeeded(String message) {
        return new PlayMcpWidgetResponse(null, message);
    }

    public PlayMcpWidgetResponse industrySelector() {
        List<Map<String, Object>> children = new java.util.ArrayList<>();
        children.add(Map.of(
                "type", "Text",
                "value", "어떤 업종의 카드 혜택을 원하시나요?"
        ));
        children.addAll(selectorButtonRows(Industry.widgetDisplayNames()));
        // 버튼 클릭 시 라벨 텍스트가 재발화로 전달되므로 업종명을 그대로 라벨로 사용한다.

        Map<String, Object> widget = new LinkedHashMap<>();
        widget.put("type", "Card");
        widget.put("children", children);

        return new PlayMcpWidgetResponse(widget, "원하시는 업종을 선택해 주세요.");
    }

    public PlayMcpWidgetResponse annualFeeSelector() {
        List<Map<String, Object>> children = new java.util.ArrayList<>();
        children.add(Map.of(
                "type", "Text",
                "value", "원하시는 연회비 구간을 선택해주세요"
        ));
        children.addAll(selectorButtonRows(AnnualFeeBand.displayNames()));

        Map<String, Object> widget = new LinkedHashMap<>();
        widget.put("type", "Card");
        widget.put("children", children);

        return new PlayMcpWidgetResponse(widget, "원하시는 연회비 구간을 선택해 주세요.");
    }

    private List<Map<String, Object>> selectorButtonRows(List<String> labels) {
        List<Map<String, Object>> rows = new java.util.ArrayList<>();
        for (int index = 0; index < labels.size(); index += 2) {
            List<Map<String, Object>> buttons = labels.subList(
                            index,
                            Math.min(index + 2, labels.size())
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

    public PlayMcpWidgetResponse creditCardGuideList(
            List<CreditCardGuideService.CardGuide> guides,
            Industry industry,
            AnnualFeeBand annualFeeBand
    ) {
        List<Map<String, Object>> cardRows = guides.stream()
                .map(this::toCreditCardListRow)
                .toList();

        Map<String, Object> moreCardsButton = new LinkedHashMap<>();
        moreCardsButton.put("type", "Button");
        moreCardsButton.put("label", "더 많은 카드 보기");
        moreCardsButton.put("variant", "outline");
        moreCardsButton.put("pill", true);
        moreCardsButton.put("block", true);
        moreCardsButton.put("onClickAction", openUrlAction(MORE_CARDS_URL));

        Map<String, Object> cardList = new LinkedHashMap<>();
        cardList.put("type", "Col");
        cardList.put("gap", 4);
        cardList.put("padding", Map.of(
                "x", 2,
                "y", 3
        ));
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
        copyText.append("\n[더 많은 카드 보기](").append(MORE_CARDS_URL).append(")")
                .append("\n\n_연회비와 혜택 조건은 카드 상세 페이지에서 최종 확인해 주세요._");

        return new PlayMcpWidgetResponse(widget, copyText.toString());
    }

    public PlayMcpWidgetResponse creditCardDetail(CreditCardGuideService.CardDetail detail) {
        List<Map<String, Object>> children = new java.util.ArrayList<>();
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
                .append("\n\n- 카드사: **").append(detail.issuer()).append("**")
                .append("\n- 연회비: **").append(detail.annualFee()).append("**")
                .append("\n\n#### 주요 혜택\n");
        detail.benefits().forEach(benefit -> copyText.append("- ").append(benefit).append("\n"));
        copyText.append("\n[자세히 보기](").append(detail.detailPageUrl()).append(")");
        return new PlayMcpWidgetResponse(widget, copyText.toString());
    }

    private Map<String, Object> cardDetailHeader(CreditCardGuideService.CardDetail detail) {
        Map<String, Object> cardImage = Map.of(
                "type", "Image",
                "src", detail.imageUrl(),
                "alt", detail.name() + " 카드 이미지",
                "width", 112,
                "height", 72,
                "fit", "contain",
                "radius", "sm"
        );
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
                        Map.of(
                                "type", "Caption",
                                "value", detail.issuer(),
                                "color", "secondary"
                        ),
                        Map.of(
                                "type", "Badge",
                                "label", "연회비 " + detail.annualFee(),
                                "color", "info",
                                "variant", "soft"
                        )
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
        button.put("onClickAction", openUrlAction(detailPageUrl));
        return button;
    }

    public PlayMcpWidgetResponse cardNameClarification() {
        String message = "조회할 신한카드 상품명을 정확히 말씀해 주세요.";
        Map<String, Object> widget = Map.of(
                "type", "Card",
                "children", List.of(Map.of(
                        "type", "Text",
                        "value", message
                ))
        );
        return new PlayMcpWidgetResponse(widget, message);
    }

    private Map<String, Object> toCreditCardListRow(CreditCardGuideService.CardGuide guide) {
        Map<String, Object> cardImage = Map.of(
                "type", "Image",
                "src", guide.imageUrl(),
                "alt", guide.name() + " 카드 이미지",
                "width", 112,
                "height", 72,
                "fit", "contain",
                "radius", "sm"
        );

        Map<String, Object> annualFeeRow = Map.of(
                "type", "Row",
                "gap", 2,
                "children", List.of(Map.of(
                        "type", "Badge",
                        "label", "연회비 " + guide.annualFee(),
                        "color", "info",
                        "variant", "soft"
                ))
        );

        List<Map<String, Object>> detailChildren = new java.util.ArrayList<>();
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
        Map<String, Object> clickIndicator = new LinkedHashMap<>();
        clickIndicator.put("type", "Button");
        clickIndicator.put("label", ">");
        clickIndicator.put("variant", "ghost");
        clickIndicator.put("uniform", true);
        clickIndicator.put("size", "xl");
        clickIndicator.put("onClickAction", sendUserMessageAction(guide.name() + " 혜택 알려줘"));

        Map<String, Object> item = new LinkedHashMap<>();
        item.put("type", "Row");
        item.put("key", guide.name());
        item.put("gap", 4);
        item.put("align", "center");
        item.put("padding", Map.of("y", 2));
        item.put("children", List.of(cardImage, cardDetails, clickIndicator));
        return item;
    }

    private Map<String, Object> benefitCategoryBadge(String category) {
        return Map.of(
                "type", "Badge",
                "label", category,
                "color", "success",
                "variant", "soft"
        );
    }

    private Map<String, Object> selectorButton(String label) {
        Map<String, Object> button = new LinkedHashMap<>();
        button.put("type", "Button");
        button.put("label", label);
        button.put("onClickAction", sendUserMessageAction(label));
        return button;
    }

    private Map<String, Object> sendUserMessageAction(String text) {
        Map<String, Object> action = new LinkedHashMap<>();
        action.put("payload", Map.of(
                "target", Map.of(
                        "type", "sendUserMessage",
                        "properties", Map.of("text", text)
                )
        ));
        return action;
    }

    private Map<String, Object> openUrlAction(String url) {
        Map<String, Object> action = new LinkedHashMap<>();
        action.put("payload", Map.of(
                "target", Map.of(
                        "url", url,
                        "pcUrl", url
                )
        ));
        return action;
    }

}
