package com.joeylee.hot_deal_mcp.config;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.joeylee.hot_deal_mcp.service.CreditCardGuideService;
import com.joeylee.hot_deal_mcp.service.CreditCardName;
import com.joeylee.hot_deal_mcp.widget.PlayMcpWidgetFactory;
import com.joeylee.hot_deal_mcp.widget.PlayMcpWidgetResponse;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class CreditCardDetailToolConfig {

    static final String TOOL_NAME = "getCreditCardDetail";
    private static final String TOOL_DESCRIPTION =
            "Retrieves details for a specific Shinhan Card(신한카드) product. Use when the user "
                    + "asks about the benefits, annual fee, eligibility, or other details of a named card. "
                    + "Resolve the user's wording to one supported cardName value before calling.";
    private static final String TOOL_ERROR_MESSAGE = "### 카드 정보를 불러오지 못했습니다.\n\n"
            + "잠시 후 다시 시도해 주세요.";

    private final CreditCardGuideService creditCardGuideService;
    private final PlayMcpWidgetFactory widgetFactory;
    private final ObjectMapper objectMapper;

    @Bean
    public List<McpServerFeatures.SyncToolSpecification> creditCardDetailToolSpecifications() {
        McpServerFeatures.SyncToolSpecification specification =
                McpServerFeatures.SyncToolSpecification.builder()
                        .tool(creditCardDetailToolDefinition())
                        .callHandler((exchange, request) -> callTool(request.arguments()))
                        .build();
        return List.of(specification);
    }

    McpSchema.Tool creditCardDetailToolDefinition() {
        Map<String, Object> cardNameProperty = new LinkedHashMap<>();
        cardNameProperty.put("type", "string");
        cardNameProperty.put("description", CreditCardName.parameterDescription());
        cardNameProperty.put("enum", CreditCardName.displayNames());

        McpSchema.JsonSchema inputSchema = new McpSchema.JsonSchema(
                "object",
                Map.of("cardName", cardNameProperty),
                List.of("cardName"),
                false,
                null,
                null
        );
        McpSchema.ToolAnnotations annotations = new McpSchema.ToolAnnotations(
                "신한카드 상품 상세 조회",
                true,
                false,
                true,
                false,
                false
        );

        return McpSchema.Tool.builder()
                .name(TOOL_NAME)
                .description(TOOL_DESCRIPTION)
                .inputSchema(inputSchema)
                .annotations(annotations)
                .build();
    }

    private McpSchema.CallToolResult callTool(Map<String, Object> arguments) {
        Object rawCardName = arguments.get("cardName");
        String cardName = rawCardName instanceof String value ? value : null;
        log.info("MCP 툴 파라미터 - tool={}, cardName={}", TOOL_NAME, cardName);

        try {
            PlayMcpWidgetResponse response;
            try {
                CreditCardGuideService.CardDetail detail =
                        creditCardGuideService.findCardDetail(cardName);
                response = widgetFactory.creditCardDetail(detail);
            } catch (IllegalArgumentException exception) {
                response = widgetFactory.cardNameClarification();
            }
            return textResult(response);
        } catch (RuntimeException | JsonProcessingException exception) {
            log.error("MCP 툴 처리 실패 - tool={}, cardName={}", TOOL_NAME, cardName, exception);
            return McpSchema.CallToolResult.builder()
                    .isError(true)
                    .addTextContent(TOOL_ERROR_MESSAGE)
                    .build();
        }
    }

    private McpSchema.CallToolResult textResult(PlayMcpWidgetResponse response)
            throws JsonProcessingException {
        return McpSchema.CallToolResult.builder()
                .isError(false)
                .addTextContent(objectMapper.writeValueAsString(response))
                .build();
    }
}
