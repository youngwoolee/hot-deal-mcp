package com.joeylee.hot_deal_mcp.widget;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.joeylee.hot_deal_mcp.service.CreditCardGuideService;
import org.springframework.beans.factory.annotation.Value;
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
    private static final String DEFAULT_PUBLIC_BASE_URL =
            "https://a77b-110-12-100-12.ngrok-free.app";
    private static final String CREDIT_CARD_APPLY_URL =
            "https://www.shinhancard.com/pconts/html/card/apply/credit/2013775_2207.html";
    // Rotated portrait derivative of the Shinhan Card plate supplied for this widget.
    // Source: https://cdn.www.shinhancard.com/pconts/static/images/card/plate/POLE1N_E5_v_f_d.webp
    private static final String CREDIT_CARD_IMAGE_PATH =
            "/images/cards/shinhan-hi-point-portrait.webp";

    private String publicBaseUrl = DEFAULT_PUBLIC_BASE_URL;

    @Value("${app.public-base-url:https://a77b-110-12-100-12.ngrok-free.app}")
    void setPublicBaseUrl(String publicBaseUrl) {
        this.publicBaseUrl = publicBaseUrl.replaceAll("/+$", "");
    }

    public PlayMcpWidgetResponse clarificationNeeded(String message) {
        return new PlayMcpWidgetResponse(null, message);
    }

    public PlayMcpWidgetResponse industrySelector() {
        List<Map<String, Object>> children = new java.util.ArrayList<>();
        children.add(Map.of(
                "type", "Text",
                "value", "어떤 업종의 카드 혜택을 원하시나요?"
        ));
        children.addAll(selectorButtonRows(Arrays.stream(CreditCardGuideService.Industry.values())
                .map(CreditCardGuideService.Industry::getDisplayName)
                .toList()));
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
        children.addAll(selectorButtonRows(Arrays.stream(CreditCardGuideService.AnnualFeeBand.values())
                .map(CreditCardGuideService.AnnualFeeBand::getDisplayName)
                .toList()));

        Map<String, Object> widget = new LinkedHashMap<>();
        widget.put("type", "Card");
        widget.put("children", children);

        return new PlayMcpWidgetResponse(widget, "원하시는 연회비 구간을 선택해 주세요.");
    }

    private List<Map<String, Object>> selectorButtonRows(List<String> labels) {
        return labels.stream().map(this::selectorButton).toList();
    }

    public PlayMcpWidgetResponse creditCardGuideList(
            List<CreditCardGuideService.CardGuide> guides,
            CreditCardGuideService.Industry industry,
            CreditCardGuideService.AnnualFeeBand annualFeeBand
    ) {
        List<Map<String, Object>> children = guides.stream()
                .map(this::toCreditCardListViewItem)
                .toList();

        Map<String, Object> widget = new LinkedHashMap<>();
        widget.put("type", "ListView");
        widget.put("children", children);
        widget.put("limit", 20);

        StringBuilder copyText = new StringBuilder()
                .append("### 카드 안내\n\n")
                .append("- 업종: **").append(industry.getDisplayName()).append("**\n")
                .append("- 연회비: **").append(annualFeeBand.getDisplayName()).append("**\n\n");
        guides.forEach(guide -> copyText
                .append("- ").append(guide.name())
                .append(" (`").append(guide.annualFee()).append("`)\n"));
        copyText.append("\n_실제 카드 상품 데이터 연동 전 혜택 유형 안내입니다._");

        return new PlayMcpWidgetResponse(widget, copyText.toString());
    }

    private Map<String, Object> toCreditCardListViewItem(CreditCardGuideService.CardGuide guide) {
        String primaryBenefit = String.join(" · ", guide.benefits());

        Map<String, Object> cardImage = Map.of(
                "type", "Image",
                "src", publicBaseUrl + CREDIT_CARD_IMAGE_PATH,
                "alt", guide.name() + " 카드 이미지",
                "width", 72,
                "height", 112,
                "fit", "contain",
                "radius", "sm"
        );

        Map<String, Object> feeAndRequirement = Map.of(
                "type", "Row",
                "gap", 2,
                "children", List.of(
                        Map.of(
                                "type", "Badge",
                                "label", "연회비 " + guide.annualFee(),
                                "color", "info",
                                "variant", "soft"
                        ),
                        Map.of(
                                "type", "Caption",
                                "value", "전월 실적 " + guide.previousMonthRequirement(),
                                "color", "secondary"
                        )
                )
        );

        Map<String, Object> cardDetails = Map.of(
                "type", "Col",
                "gap", 1,
                "flex", 1,
                "children", List.of(
                        Map.of(
                                "type", "Text",
                                "value", guide.name(),
                                "size", "sm",
                                "weight", "semibold",
                                "maxLines", 2
                        ),
                        Map.of(
                                "type", "Caption",
                                "value", guide.issuer(),
                                "color", "secondary"
                        ),
                        feeAndRequirement,
                        Map.of(
                                "type", "Text",
                                "value", primaryBenefit,
                                "size", "sm",
                                "color", "secondary",
                                "maxLines", 2
                        )
                )
        );

        Map<String, Object> item = new LinkedHashMap<>();
        item.put("type", "ListViewItem");
        item.put("key", guide.name());
        item.put("gap", 3);
        item.put("align", "start");
        item.put("children", List.of(cardImage, cardDetails));
        item.put("onClickAction", openUrlAction(CREDIT_CARD_APPLY_URL));
        return item;
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
