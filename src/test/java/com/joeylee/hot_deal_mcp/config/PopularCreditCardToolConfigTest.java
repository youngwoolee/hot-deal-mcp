package com.joeylee.hot_deal_mcp.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.joeylee.hot_deal_mcp.service.CreditCardDataRepository;
import com.joeylee.hot_deal_mcp.service.PopularCreditCardService;
import com.joeylee.hot_deal_mcp.widget.PopularCreditCardWidgetFactory;
import com.joeylee.hot_deal_mcp.widget.PlayMcpWidgetResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springaicommunity.mcp.annotation.McpTool;

class PopularCreditCardToolConfigTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private PopularCreditCardToolConfig config;

    @BeforeEach
    void setUp() {
        config = new PopularCreditCardToolConfig(
                new PopularCreditCardService(new CreditCardDataRepository(objectMapper)),
                new PopularCreditCardWidgetFactory()
        );
    }

    @Test
    void toolHasNoParametersAndReadOnlyMetadata() throws NoSuchMethodException {
        Method method = PopularCreditCardToolConfig.class.getDeclaredMethod("getPopularCreditCards");
        McpTool tool = method.getAnnotation(McpTool.class);

        assertThat(method.getParameterCount()).isZero();
        assertThat(tool.name()).isEqualTo("getPopularCreditCards");
        assertThat(tool.description()).contains("top five", "requires no parameters");
        assertThat(tool.annotations().readOnlyHint()).isTrue();
        assertThat(tool.annotations().destructiveHint()).isFalse();
        assertThat(tool.annotations().idempotentHint()).isTrue();
    }

    @Test
    void returnsPromisedFiveCardsInRankingOrder() {
        PlayMcpWidgetResponse response = config.getPopularCreditCards();
        JsonNode json = objectMapper.valueToTree(response);
        JsonNode children = json.path("widget").path("children");

        assertThat(children).hasSize(4);
        assertThat(children.get(0).path("value").asText()).contains("인기 TOP5");
        JsonNode cardRows = children.get(2).path("children");
        assertThat(cardRows).hasSize(5);
        assertThat(cardName(cardRows.get(0))).isEqualTo("신한카드 Deep Oil");
        assertThat(cardName(cardRows.get(1))).isEqualTo("신한카드 Mr.Life");
        assertThat(cardName(cardRows.get(2))).isEqualTo("신한카드 Air One");
        assertThat(cardName(cardRows.get(3))).isEqualTo("신한카드 Point Plan");
        assertThat(cardName(cardRows.get(4))).isEqualTo("신한카드 SOL트래블 체크");
        assertThat(cardRows.get(0).path("children").get(0).path("value").asText()).isEqualTo("🥇");
        assertThat(cardRows.get(1).path("children").get(0).path("value").asText()).isEqualTo("🥈");
        assertThat(cardRows.get(2).path("children").get(0).path("value").asText()).isEqualTo("🥉");
        assertThat(cardRows.get(3).path("children").get(0).path("value").asText()).isEqualTo("4");
        assertThat(cardRows.get(4).path("children").get(0).path("value").asText()).isEqualTo("5");
        for (JsonNode cardRow : cardRows) {
            assertThat(cardRow.path("children").get(1).path("type").asText()).isEqualTo("Image");
            assertThat(cardRow.path("children").get(1).path("src").asText()).startsWith("https://");
            assertThat(cardRow.path("children").get(2).path("children").get(1)
                    .path("children").get(0).path("type").asText()).isEqualTo("Badge");
        }
        assertThat(cardRows.get(0).path("children").get(1).path("src").asText())
                .isEqualTo("https://cdn.www.shinhancard.com/pconts/static/images/card/plate/BIABE0_E5_v_f_s.webp");
        assertThat(children.get(3).path("type").asText()).isEqualTo("Button");
        assertThat(children.get(3).path("label").asText()).isEqualTo("신한카드 TOP10 차트 보러가기");
        assertThat(response.copyText()).contains(
                "1. **신한카드 Deep Oil**",
                "2. **신한카드 Mr.Life**",
                "3. **신한카드 Air One**",
                "4. **신한카드 Point Plan**",
                "5. **신한카드 SOL트래블 체크**"
        );
    }

    @Test
    void unexpectedFailureReturnsSanitizedMessage() {
        PopularCreditCardService failingService = mock(PopularCreditCardService.class);
        when(failingService.findPopularCards()).thenThrow(new IllegalStateException("internal details"));
        PopularCreditCardToolConfig failingConfig = new PopularCreditCardToolConfig(
                failingService,
                new PopularCreditCardWidgetFactory()
        );

        assertThatThrownBy(failingConfig::getPopularCreditCards)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("### 인기 카드 정보를 불러오지 못했습니다.\n\n잠시 후 다시 시도해 주세요.")
                .hasNoCause();
    }

    private String cardName(JsonNode row) {
        return row.path("children").get(2)
                .path("children").get(0)
                .path("value").asText();
    }
}
