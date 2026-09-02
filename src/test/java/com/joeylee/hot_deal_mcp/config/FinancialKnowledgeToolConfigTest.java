package com.joeylee.hot_deal_mcp.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.joeylee.hot_deal_mcp.service.FinancialKnowledgeService;
import com.joeylee.hot_deal_mcp.widget.FinancialKnowledgeWidgetFactory;
import com.joeylee.hot_deal_mcp.widget.PlayMcpWidgetResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springaicommunity.mcp.annotation.McpTool;

class FinancialKnowledgeToolConfigTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private FinancialKnowledgeToolConfig config;

    @BeforeEach
    void setUp() {
        config = new FinancialKnowledgeToolConfig(
                new FinancialKnowledgeService(),
                new FinancialKnowledgeWidgetFactory()
        );
    }

    @Test
    void toolMetadataDefinesIntentCategoryParameter() throws NoSuchMethodException {
        Method method = FinancialKnowledgeToolConfig.class.getDeclaredMethod(
                "getFinancialLifeKnowledgeArticles",
                String.class
        );
        McpTool tool = method.getAnnotation(McpTool.class);

        assertThat(tool.name()).isEqualTo("getFinancialLifeKnowledgeArticles");
        assertThat(tool.description())
                .hasSizeLessThanOrEqualTo(1_024)
                .contains(
                        "Shinhan Card(신한카드)",
                        "consumer trends",
                        "practical money knowledge",
                        "smart card usage"
                );
        assertThat(tool.annotations().readOnlyHint()).isTrue();
        assertThat(tool.annotations().destructiveHint()).isFalse();
        assertThat(tool.annotations().idempotentHint()).isTrue();
    }

    @Test
    void trendIntentReturnsFiveClickableTrendArticles() {
        PlayMcpWidgetResponse response = config.getFinancialLifeKnowledgeArticles("트렌드");
        JsonNode json = objectMapper.valueToTree(response);
        JsonNode children = json.path("widget").path("children");

        assertThat(json.path("widget").path("type").asText()).isEqualTo("ListView");
        assertThat(children).hasSize(7);
        assertThat(children.get(0).path("children").get(0)
                .path("children").get(0).path("value").asText()).isEqualTo("TREND");
        assertArticle(
                children.get(1),
                "데이터로 살펴본 2026 여행 트렌드",
                "https://www.shinhancardblog.com/1432"
        );
        assertThat(children.get(6).path("children").get(0)
                .path("onClickAction").path("payload").path("target")
                .path("properties").path("text").asText())
                .isEqualTo("금융 금융생활지식 보여줘");
    }

    @Test
    void financeIntentReturnsThreeFinanceArticles() {
        PlayMcpWidgetResponse response = config.getFinancialLifeKnowledgeArticles("금융");
        JsonNode json = objectMapper.valueToTree(response);
        JsonNode children = json.path("widget").path("children");

        assertThat(children.get(0).path("children").get(0)
                .path("children").get(0).path("value").asText())
                .isEqualTo("FINANCE");
        assertThat(children).hasSize(5);
        assertArticle(
                children.get(3),
                "새로워진 신한 슈퍼 SOL! 가입하고 런칭 이벤트 혜택 받는 방법",
                "https://www.shinhancardblog.com/1434"
        );
    }

    @Test
    void cardTipsIntentReturnsFiveCardLabArticles() {
        PlayMcpWidgetResponse response = config.getFinancialLifeKnowledgeArticles("카드 연구소");
        JsonNode json = objectMapper.valueToTree(response);
        JsonNode children = json.path("widget").path("children");

        assertThat(children.get(0).path("children").get(0)
                .path("children").get(0).path("value").asText())
                .isEqualTo("CARD LAB");
        assertThat(children).hasSize(7);
        assertArticle(
                children.get(5),
                "[쏠깃한 카드 연구소] 사장님 지갑 쏠쏠해지는 Npay biz 신한카드",
                "https://www.shinhancardblog.com/1407"
        );
    }

    @Test
    void unexpectedFailureReturnsSanitizedMessage() {
        FinancialKnowledgeService failingService = mock(FinancialKnowledgeService.class);
        when(failingService.findArticles(any())).thenThrow(new IllegalStateException("internal details"));
        FinancialKnowledgeToolConfig failingConfig = new FinancialKnowledgeToolConfig(
                failingService,
                new FinancialKnowledgeWidgetFactory()
        );

        assertThatThrownBy(() -> failingConfig.getFinancialLifeKnowledgeArticles("트렌드"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("### 금융생활지식을 불러오지 못했습니다.\n\n잠시 후 다시 시도해 주세요.")
                .hasNoCause();
    }

    private void assertArticle(JsonNode row, String title, String url) {
        JsonNode rankText = row.path("children").get(0);
        JsonNode titleContainer = row.path("children").get(1);
        JsonNode titleText = titleContainer.path("children").get(0);
        assertThat(row.path("type").asText()).isEqualTo("ListViewItem");
        assertThat(row.path("align").asText()).isEqualTo("start");
        assertThat(rankText.path("type").asText()).isEqualTo("Text");
        assertThat(titleContainer.path("align").asText()).isEqualTo("start");
        assertThat(titleContainer.path("padding").has("y")).isFalse();
        assertThat(titleText.path("value").asText()).isEqualTo(title);
        assertThat(titleText.path("textAlign").asText()).isEqualTo("start");
        assertThat(titleText.path("maxLines").asInt()).isEqualTo(3);
        assertThat(row.path("onClickAction").path("payload").path("target")
                .path("url").asText()).isEqualTo(url);
    }
}
