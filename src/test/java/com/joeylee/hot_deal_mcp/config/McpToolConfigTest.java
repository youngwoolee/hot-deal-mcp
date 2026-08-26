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
import com.joeylee.hot_deal_mcp.service.CreditCardGuideService;
import com.joeylee.hot_deal_mcp.widget.PlayMcpWidgetFactory;
import com.joeylee.hot_deal_mcp.widget.PlayMcpWidgetResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springaicommunity.mcp.annotation.McpTool;

class McpToolConfigTest {

    private McpToolConfig mcpToolConfig;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mcpToolConfig = new McpToolConfig(
                new CreditCardGuideService(),
                new PlayMcpWidgetFactory(),
                objectMapper
        );
    }

    @Test
    void toolMetadataFollowsKakaoRequirements() throws NoSuchMethodException {
        Method method = McpToolConfig.class.getDeclaredMethod(
                "getCreditCardRecommendationsWithSelector",
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
                mcpToolConfig.getCreditCardRecommendationsWithSelector(5, "0~1만원대");
        JsonNode json = objectMapper.valueToTree(response);

        assertThat(json.path("widget").path("type").asText()).isEqualTo("ListView");
        assertThat(json.path("widget").path("children")).hasSize(3);
        assertThat(json.path("copy_text").asText()).contains("쇼핑", "0~1만원대");
    }

    @Test
    void missingIndustryReturnsSelectorWidget() {
        PlayMcpWidgetResponse response =
                mcpToolConfig.getCreditCardRecommendationsWithSelector(null, "제한없음");
        JsonNode json = objectMapper.valueToTree(response);

        assertThat(json.path("widget").path("type").asText()).isEqualTo("Card");
        assertThat(json.path("widget").path("children").get(0).path("value").asText())
                .contains("업종");
        JsonNode firstButtonRow = json.path("widget").path("children").get(1);
        assertThat(firstButtonRow.path("type").asText()).isEqualTo("Row");
        assertThat(firstButtonRow.path("children")).hasSize(2);
        assertThat(firstButtonRow.path("children").get(0).path("label").asText())
                .isEqualTo("어디서나");
        assertThat(firstButtonRow.path("children").get(1).path("label").asText())
                .isEqualTo("주유");
        assertThat(json.path("copy_text").asText()).isEqualTo("원하시는 업종을 선택해 주세요.");
    }

    @Test
    void annualFeeSelectorPlacesTwoButtonsPerRow() {
        PlayMcpWidgetResponse response =
                mcpToolConfig.getCreditCardRecommendationsWithSelector(5, "지원하지 않는 구간");
        JsonNode children = objectMapper.valueToTree(response).path("widget").path("children");

        assertThat(children.get(1).path("type").asText()).isEqualTo("Row");
        assertThat(children.get(1).path("children")).hasSize(2);
        assertThat(children.get(2).path("type").asText()).isEqualTo("Row");
        assertThat(children.get(2).path("children")).hasSize(1);
    }

    @Test
    void missingAnnualFeeDefaultsToNoLimit() {
        PlayMcpWidgetResponse response =
                mcpToolConfig.getCreditCardRecommendationsWithSelector(15, null);
        JsonNode json = objectMapper.valueToTree(response);

        assertThat(json.path("widget").path("children")).hasSize(3);
        assertThat(json.path("copy_text").asText()).contains("항공", "제한없음");
    }

    @Test
    void unexpectedFailureReturnsOnlySanitizedMessage() {
        CreditCardGuideService failingService = mock(CreditCardGuideService.class);
        when(failingService.findGuides(anyInt(), any()))
                .thenThrow(new IllegalStateException("internal database details"));
        McpToolConfig failingConfig = new McpToolConfig(
                failingService,
                new PlayMcpWidgetFactory(),
                objectMapper
        );

        assertThatThrownBy(() ->
                failingConfig.getCreditCardRecommendationsWithSelector(5, "제한없음"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("### 카드 정보를 불러오지 못했습니다.\n\n잠시 후 다시 시도해 주세요.")
                .hasNoCause();
    }
}
