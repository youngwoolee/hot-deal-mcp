package com.joeylee.hot_deal_mcp.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.joeylee.hot_deal_mcp.service.CreditCardDataRepository;
import com.joeylee.hot_deal_mcp.service.CreditCardGuideService;
import com.joeylee.hot_deal_mcp.widget.CreditCardRecommendationWidgetFactory;
import com.joeylee.hot_deal_mcp.widget.PlayMcpWidgetResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springaicommunity.mcp.annotation.McpTool;

class CreditCardRecommendationToolConfigTest {

    private CreditCardRecommendationToolConfig mcpToolConfig;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mcpToolConfig = new CreditCardRecommendationToolConfig(
                new CreditCardGuideService(new CreditCardDataRepository(objectMapper)),
                new CreditCardRecommendationWidgetFactory(),
                objectMapper
        );
    }

    @Test
    void toolMetadataFollowsKakaoRequirements() throws NoSuchMethodException {
        Method method = CreditCardRecommendationToolConfig.class.getDeclaredMethod(
                "getCreditCardRecommendationsWithSelector",
                Integer.class,
                String.class,
                Integer.class,
                String.class
        );
        McpTool tool = method.getAnnotation(McpTool.class);

        assertThat(tool).isNotNull();
        assertThat(tool.name()).isEqualTo("getCreditCardRecommendationsWithSelector");
        assertThat(tool.description())
                .hasSizeLessThanOrEqualTo(1_024)
                .contains("Shinhan Card(신한카드)");
        assertThat(tool.annotations().title()).isNotBlank();
        assertThat(tool.annotations().readOnlyHint()).isTrue();
        assertThat(tool.annotations().destructiveHint()).isFalse();
        assertThat(tool.annotations().openWorldHint()).isFalse();
        assertThat(tool.annotations().idempotentHint()).isTrue();
    }

    @Test
    void recommendationReturnsCuratedWidgetAndMarkdownCopyText() {
        PlayMcpWidgetResponse response =
                mcpToolConfig.getCreditCardRecommendationsWithSelector(5, "0~1만원대", null, null);
        JsonNode json = objectMapper.valueToTree(response);

        assertThat(json.path("widget").path("type").asText()).isEqualTo("Card");
        assertThat(json.path("widget").has("size")).isFalse();
        assertThat(json.path("widget").path("children")).hasSize(2);
        JsonNode cardList = json.path("widget").path("children").get(0);
        assertThat(cardList.path("type").asText()).isEqualTo("Col");
        assertThat(cardList.path("gap").asInt()).isEqualTo(4);
        assertThat(cardList.path("children")).hasSize(5);
        JsonNode firstCard = cardList.path("children").get(0);
        assertThat(firstCard.path("type").asText()).isEqualTo("Row");
        assertThat(firstCard.path("gap").asInt()).isEqualTo(4);
        assertThat(firstCard.path("padding").path("y").asInt()).isEqualTo(2);
        assertThat(firstCard.path("align").asText()).isEqualTo("center");
        assertThat(firstCard.path("children")).hasSize(3);
        JsonNode clickIndicator = firstCard.path("children").get(2);
        assertThat(clickIndicator.path("type").asText()).isEqualTo("Button");
        assertThat(clickIndicator.path("label").asText()).isEqualTo(">");
        assertThat(clickIndicator.path("variant").asText()).isEqualTo("ghost");
        String firstCardName = firstCard.path("children").get(1)
                .path("children").get(0).path("value").asText();
        assertThat(firstCardName).isNotBlank();
        JsonNode annualFeeRow = firstCard.path("children").get(1)
                .path("children").get(1);
        assertThat(annualFeeRow.path("type").asText()).isEqualTo("Row");
        assertThat(annualFeeRow.path("children")).hasSize(1);
        assertThat(annualFeeRow.path("children").get(0).path("label").asText())
                .startsWith("연회비 ")
                .endsWith("원");
        JsonNode benefitCategoryRow = firstCard.path("children").get(1)
                .path("children").get(2);
        assertThat(benefitCategoryRow.path("type").asText()).isEqualTo("Row");
        assertThat(benefitCategoryRow.path("children").get(0).path("label").asText())
                .isEqualTo("쇼핑");
        JsonNode cardDetailRows = firstCard.path("children").get(1).path("children");
        assertThat(cardDetailRows).hasSize(3);
        assertThat(benefitCategoryRow.path("children")).hasSizeBetween(1, 3);
        for (JsonNode row : cardDetailRows) {
            assertThat(row.toString()).doesNotContain("특화");
        }
        assertThat(clickIndicator.path("onClickAction").path("payload").path("target")
                .path("type").asText()).isEqualTo("sendUserMessage");
        assertThat(clickIndicator.path("onClickAction").path("payload").path("target")
                .path("properties").path("text").asText())
                .isEqualTo(firstCardName + " 혜택 알려줘");
        JsonNode moreCardsButton = json.path("widget").path("children").get(1);
        assertThat(moreCardsButton.path("type").asText()).isEqualTo("Button");
        assertThat(moreCardsButton.path("label").asText()).isEqualTo("더 많은 카드 보기");
        assertThat(moreCardsButton.path("onClickAction").path("payload").path("target")
                .path("url").asText()).isEqualTo(
                "https://www.shinhancard.com/mob/MOBFM039N/MOBFM039C01.shc?crustMenuId=ms467"
        );
        assertThat(moreCardsButton.path("onClickAction").path("payload").path("target")
                .path("pcUrl").asText()).isEqualTo(
                "https://www.shinhancard.com/mob/MOBFM039N/MOBFM039C01.shc?crustMenuId=ms467"
        );
        assertThat(json.path("copy_text").asText()).contains("쇼핑", "0~1만원대");
        assertThat(json.path("copy_text").asText()).doesNotContain("체크");
    }

    @Test
    void explicitCheckCardRequestReturnsCheckCards() {
        PlayMcpWidgetResponse response =
                mcpToolConfig.getCreditCardRecommendationsWithSelector(5, "제한없음", 2, null);
        JsonNode cardRows = objectMapper.valueToTree(response)
                .path("widget").path("children").get(0).path("children");

        assertThat(cardRows).isNotEmpty();
        for (JsonNode cardRow : cardRows) {
            String cardName = cardRow.path("children").get(1)
                    .path("children").get(0).path("value").asText();
            assertThat(cardName).contains("체크");
        }
    }

    @Test
    void youthCategoryReturnsCheckCardsEvenWhenCreditCardTypeIsRequested() {
        PlayMcpWidgetResponse response =
                mcpToolConfig.getCreditCardRecommendationsWithSelector(24, "제한없음", 1, null);

        assertThat(response.copyText()).contains("신한카드 처음 체크");
    }

    @Test
    void missingIndustryReturnsSelectorWidget() {
        PlayMcpWidgetResponse response =
                mcpToolConfig.getCreditCardRecommendationsWithSelector(null, "제한없음", null, null);
        JsonNode json = objectMapper.valueToTree(response);

        assertThat(json.path("widget").path("type").asText()).isEqualTo("Card");
        assertThat(json.path("widget").path("children").get(0).path("value").asText())
                .contains("업종");
        JsonNode firstButtonRow = json.path("widget").path("children").get(1);
        assertThat(firstButtonRow.path("type").asText()).isEqualTo("Row");
        assertThat(firstButtonRow.path("children")).hasSize(4);
        assertThat(firstButtonRow.path("children").get(0).path("label").asText())
                .isEqualTo("어디서나");
        assertThat(firstButtonRow.path("children").get(1).path("label").asText())
                .isEqualTo("주유");
        assertThat(firstButtonRow.path("children").get(2).path("label").asText())
                .isEqualTo("대형마트");
        assertThat(firstButtonRow.path("children").get(3).path("label").asText())
                .isEqualTo("편의점");
        assertThat(json.path("widget").path("children").get(2)
                .path("children").get(3).path("label").asText()).isEqualTo("교통");
        assertThat(json.path("widget").path("children").get(3)
                .path("children").get(3).path("label").asText()).isEqualTo("항공");
        assertThat(json.path("widget").path("children")).hasSize(5);
        JsonNode lastButtonRow = json.path("widget").path("children").get(4);
        assertThat(lastButtonRow.path("children").get(0).path("label").asText())
                .isEqualTo("공항라운지");
        assertThat(lastButtonRow.path("children").get(1).path("label").asText())
                .isEqualTo("여행/숙박");
        assertThat(json.path("copy_text").asText()).isEqualTo("원하시는 업종을 선택해 주세요.");
    }

    @Test
    void annualFeeSelectorPlacesUpToFourButtonsPerRow() {
        PlayMcpWidgetResponse response =
                mcpToolConfig.getCreditCardRecommendationsWithSelector(5, "지원하지 않는 구간", null, null);
        JsonNode children = objectMapper.valueToTree(response).path("widget").path("children");

        assertThat(children.get(1).path("type").asText()).isEqualTo("Row");
        assertThat(children.get(1).path("children")).hasSize(3);
        assertThat(children).hasSize(2);
    }

    @Test
    void missingAnnualFeeDefaultsToNoLimit() {
        PlayMcpWidgetResponse response =
                mcpToolConfig.getCreditCardRecommendationsWithSelector(15, null, null, null);
        JsonNode json = objectMapper.valueToTree(response);

        JsonNode cardList = json.path("widget").path("children").get(0);
        assertThat(cardList.path("children")).hasSizeBetween(1, 5);
        assertThat(json.path("copy_text").asText()).contains("항공", "제한없음");
        JsonNode categoryBadge = cardList.path("children").get(0)
                .path("children").get(1).path("children").get(2)
                .path("children").get(0);
        assertThat(categoryBadge.path("label").asText()).isEqualTo("마일리지");
    }

    @Test
    void categoryHiddenFromSelectorIsRenderedAsRecommendationBadge() {
        PlayMcpWidgetResponse response =
                mcpToolConfig.getCreditCardRecommendationsWithSelector(11, "제한없음", null, null);
        JsonNode firstCardBadges = objectMapper.valueToTree(response)
                .path("widget").path("children").get(0).path("children").get(0)
                .path("children").get(1).path("children").get(2)
                .path("children");

        assertThat(firstCardBadges).isNotEmpty();
        assertThat(firstCardBadges.get(0).path("label").asText()).isEqualTo("공과금");
    }

    @Test
    void unexpectedFailureReturnsOnlySanitizedMessage() {
        CreditCardGuideService failingService = mock(CreditCardGuideService.class);
        when(failingService.findGuides(anyInt(), any(), anyInt(), any()))
                .thenThrow(new IllegalStateException("internal database details"));
        CreditCardRecommendationToolConfig failingConfig = new CreditCardRecommendationToolConfig(
                failingService,
                new CreditCardRecommendationWidgetFactory(),
                objectMapper
        );

        assertThatThrownBy(() ->
                failingConfig.getCreditCardRecommendationsWithSelector(5, "제한없음", null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("### 카드 정보를 불러오지 못했습니다.\n\n잠시 후 다시 시도해 주세요.")
                .hasNoCause();
    }
}
