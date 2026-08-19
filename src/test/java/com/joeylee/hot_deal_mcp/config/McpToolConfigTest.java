package com.joeylee.hot_deal_mcp.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import com.joeylee.hot_deal_mcp.service.CreditCardGuideService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.joeylee.hot_deal_mcp.widget.PlayMcpWidgetFactory;
import com.joeylee.hot_deal_mcp.widget.PlayMcpWidgetResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class McpToolConfigTest {

    @Mock
    private HotDealService hotDealService;

    private McpToolConfig mcpToolConfig;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mcpToolConfig = new McpToolConfig(
                hotDealService,
                new CreditCardGuideService(),
                new PlayMcpWidgetFactory(),
                objectMapper
        );
    }

    @Test
    void rankingUsesPlayMcpListViewEnvelope() {
        HotDealService.DealInfo deal = HotDealService.DealInfo.builder()
                .title("테스트 상품")
                .price("10,000원")
                .mall("테스트몰")
                .link("https://example.com/deal")
                .category("전자/IT")
                .build();
        when(hotDealService.fetchHotDeals(HotDealService.Category.IT)).thenReturn(List.of(deal));

        PlayMcpWidgetResponse response = mcpToolConfig.getHotDealRanking("IT");
        JsonNode json = objectMapper.valueToTree(response);

        assertThat(json.has("widget")).isTrue();
        assertThat(json.has("copy_text")).isTrue();
        assertThat(json.path("widget").path("type").asText()).isEqualTo("ListView");
        assertThat(json.path("widget").path("limit").asInt()).isEqualTo(100);
        assertThat(json.path("widget").has("status")).isFalse();
        assertThat(json.path("widget").path("children").get(0).path("type").asText())
                .isEqualTo("ListViewItem");
        assertThat(json.path("copy_text").asText()).contains("테스트 상품", "10,000원");
    }

    @Test
    void categorySelectorUsesKakaoCompatibleCardButtons() {
        PlayMcpWidgetResponse response =
                mcpToolConfig.getHotDealCategorySelector("it, food");
        JsonNode json = objectMapper.valueToTree(response);
        JsonNode widget = json.path("widget");
        JsonNode children = widget.path("children");

        assertThat(widget.path("type").asText()).isEqualTo("Card");
        assertThat(widget.has("status")).isFalse();
        assertThat(widget.has("size")).isFalse();
        assertThat(children.get(0).path("type").asText()).isEqualTo("Text");
        assertThat(children.get(1).path("type").asText()).isEqualTo("Button");
        assertThat(children.get(2).path("label").asText()).isEqualTo("✓ 전자/IT");
        assertThat(children.get(2).has("variant")).isFalse();
        assertThat(children.get(2).has("pill")).isFalse();
        assertThat(children.get(2).path("onClickAction").has("type")).isFalse();
        assertThat(children.get(2).path("onClickAction").path("payload")
                .path("target").path("url").asText())
                .isEqualTo("https://www.algumon.com/deal/rank?category=it");
        assertThat(children.get(2).path("onClickAction").path("payload")
                .path("target").path("pcUrl").asText())
                .isEqualTo("https://www.algumon.com/deal/rank?category=it");
        assertThat(children.get(3).path("label").asText()).isEqualTo("✓ 식품/영양제");
        assertThat(children.get(4).path("label").asText()).isEqualTo("뷰티/패션");
        assertThat(json.path("copy_text").asText()).contains("전자/IT", "식품/영양제");
    }

    @Test
    void creditCardRecommendationsRenderIndustryAndAnnualFeeAsListView() {
        PlayMcpWidgetResponse response =
                mcpToolConfig.getCreditCardRecommendations("온라인 쇼핑", "0~1만원대");
        JsonNode json = objectMapper.valueToTree(response);
        JsonNode widget = json.path("widget");
        JsonNode firstItem = widget.path("children").get(0);

        assertThat(widget.path("type").asText()).isEqualTo("ListView");
        assertThat(widget.has("status")).isFalse();
        assertThat(widget.path("children")).hasSize(3);
        assertThat(firstItem.path("type").asText()).isEqualTo("ListViewItem");
        assertThat(firstItem.path("gap").asInt()).isEqualTo(3);
        assertThat(firstItem.path("align").asText()).isEqualTo("start");
        assertThat(firstItem.path("children")).hasSize(2);
        assertThat(firstItem.path("children").get(0).path("type").asText())
                .isEqualTo("Image");
        assertThat(firstItem.path("children").get(0).path("src").asText())
                .isEqualTo("https://a77b-110-12-100-12.ngrok-free.app"
                        + "/images/cards/shinhan-hi-point-portrait.webp");
        assertThat(firstItem.path("children").get(0).path("width").asInt()).isEqualTo(72);
        assertThat(firstItem.path("children").get(0).path("height").asInt()).isEqualTo(112);
        assertThat(firstItem.path("children").get(0).path("fit").asText())
                .isEqualTo("contain");

        JsonNode details = firstItem.path("children").get(1);
        assertThat(details.path("type").asText()).isEqualTo("Col");
        assertThat(details.path("children").get(0).path("value").asText())
                .isEqualTo("온라인 쇼핑 혜택 실속형 카드");
        assertThat(details.path("children").get(1).path("value").asText())
                .isEqualTo("카드 상품 데이터 연동 필요");

        JsonNode feeAndRequirement = details.path("children").get(2);
        assertThat(feeAndRequirement.path("type").asText()).isEqualTo("Row");
        assertThat(feeAndRequirement.path("children").get(0).path("label").asText())
                .isEqualTo("연회비 0~1만원대");
        assertThat(feeAndRequirement.path("children").get(0).path("variant").asText())
                .isEqualTo("soft");
        assertThat(feeAndRequirement.path("children").get(1).path("value").asText())
                .startsWith("전월 실적 ");
        assertThat(details.path("children").get(3).path("type").asText())
                .isEqualTo("Text");
        assertThat(details.path("children").get(3).path("value").asText())
                .contains("온라인 쇼핑", "할인 한도");
        assertThat(widget.path("children").get(1).path("children").get(0).path("src").asText())
                .isEqualTo(firstItem.path("children").get(0).path("src").asText());
        assertThat(widget.path("children").get(2).path("children").get(0).path("src").asText())
                .isEqualTo(firstItem.path("children").get(0).path("src").asText());
        String applyUrl = "https://www.shinhancard.com/pconts/html/card/apply/credit/"
                + "2013775_2207.html";
        for (JsonNode item : widget.path("children")) {
            assertThat(item.path("onClickAction").has("type")).isFalse();
            assertThat(item.path("onClickAction").path("payload")
                    .path("target").path("url").asText()).isEqualTo(applyUrl);
            assertThat(item.path("onClickAction").path("payload")
                    .path("target").path("pcUrl").asText()).isEqualTo(applyUrl);
        }
        assertThat(widget.path("children").get(1).path("children").get(1)
                .path("children").get(0).path("value").asText()).contains("균형형");
        assertThat(widget.path("children").get(2).path("children").get(1)
                .path("children").get(0).path("value").asText()).contains("집중형");
        assertThat(json.path("copy_text").asText()).contains("온라인 쇼핑", "0~1만원대");
    }

    @Test
    void creditCardRecommendationsDefaultToNoAnnualFeeLimit() {
        PlayMcpWidgetResponse response =
                mcpToolConfig.getCreditCardRecommendations("해외", null);
        JsonNode json = objectMapper.valueToTree(response);

        assertThat(json.path("widget").path("children")).hasSize(3);
        assertThat(json.path("copy_text").asText()).contains("해외", "제한없음");
    }
}
