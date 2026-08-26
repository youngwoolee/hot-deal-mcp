package com.joeylee.hot_deal_mcp.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.joeylee.hot_deal_mcp.service.CreditCardGuideService;
import com.joeylee.hot_deal_mcp.service.CreditCardName;
import com.joeylee.hot_deal_mcp.widget.PlayMcpWidgetFactory;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CreditCardDetailToolConfigTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private CreditCardDetailToolConfig config;

    @BeforeEach
    void setUp() {
        config = new CreditCardDetailToolConfig(
                new CreditCardGuideService(),
                new PlayMcpWidgetFactory(),
                objectMapper
        );
    }

    @Test
    void schemaUsesEnumDisplayNamesAsSingleSourceOfTruth() {
        McpSchema.Tool tool = config.creditCardDetailToolDefinition();
        Map<String, Object> cardNameProperty = property(tool.inputSchema(), "cardName");

        assertThat(tool.name()).isEqualTo("getCreditCardDetail");
        assertThat(tool.description())
                .hasSizeLessThanOrEqualTo(1_024)
                .contains("Shinhan Card(신한카드)");
        assertThat(tool.annotations().title()).isNotBlank();
        assertThat(tool.annotations().readOnlyHint()).isTrue();
        assertThat(tool.annotations().destructiveHint()).isFalse();
        assertThat(tool.annotations().openWorldHint()).isFalse();
        assertThat(tool.annotations().idempotentHint()).isTrue();
        assertThat(tool.inputSchema().required()).containsExactly("cardName");
        assertThat(cardNameProperty.get("type")).isEqualTo("string");
        assertThat(cardNameProperty.get("enum")).isEqualTo(CreditCardName.displayNames());
        assertThat(CreditCardName.values()).hasSize(81);

        String parameterDescription = (String) cardNameProperty.get("description");
        for (String displayName : CreditCardName.displayNames()) {
            assertThat(parameterDescription).contains(displayName);
        }
    }

    @Test
    void detailCallReturnsOnlyTheMatchedCard() throws Exception {
        McpSchema.CallToolResult result = call(Map.of(
                "cardName", "신한카드 SOL트래블 체크"
        ));
        JsonNode json = resultJson(result);

        assertThat(result.isError()).isFalse();
        assertThat(json.path("widget").path("type").asText()).isEqualTo("Card");
        assertThat(json.path("widget").path("children").get(0).path("value").asText())
                .isEqualTo("신한카드 SOL트래블 체크");
        assertThat(json.path("copy_text").asText())
                .contains("신한카드 SOL트래블 체크")
                .doesNotContain("신한 슈퍼SOL 체크");
    }

    @Test
    void missingCardNameReturnsClarificationWidget() throws Exception {
        McpSchema.CallToolResult result = call(Map.of());
        JsonNode json = resultJson(result);

        assertThat(result.isError()).isFalse();
        assertThat(json.path("widget").path("type").asText()).isEqualTo("Card");
        assertThat(json.path("copy_text").asText()).contains("정확히 말씀해 주세요");
    }

    @Test
    void enumUsesDisplayNameForJsonInputAndOutput() throws Exception {
        CreditCardName cardName = objectMapper.readValue(
                "\"신한카드 SOL트래블 체크\"",
                CreditCardName.class
        );

        assertThat(cardName).isEqualTo(CreditCardName.SOL_TRAVEL_CHECK);
        assertThat(objectMapper.writeValueAsString(cardName))
                .isEqualTo("\"신한카드 SOL트래블 체크\"");
    }

    private McpSchema.CallToolResult call(Map<String, Object> arguments) {
        List<McpServerFeatures.SyncToolSpecification> specifications =
                config.creditCardDetailToolSpecifications();
        McpSchema.CallToolRequest request = new McpSchema.CallToolRequest(
                CreditCardDetailToolConfig.TOOL_NAME,
                arguments
        );
        return specifications.get(0).callHandler().apply(null, request);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> property(McpSchema.JsonSchema schema, String name) {
        return (Map<String, Object>) schema.properties().get(name);
    }

    private JsonNode resultJson(McpSchema.CallToolResult result) throws Exception {
        McpSchema.TextContent content = (McpSchema.TextContent) result.content().get(0);
        return objectMapper.readTree(content.text());
    }
}
