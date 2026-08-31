package com.joeylee.hot_deal_mcp.config;

import java.util.List;

import com.joeylee.hot_deal_mcp.service.PopularCreditCardService;
import com.joeylee.hot_deal_mcp.widget.PlayMcpWidgetFactory;
import com.joeylee.hot_deal_mcp.widget.PlayMcpWidgetResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class PopularCreditCardToolConfig {

    private static final String TOOL_NAME = "getPopularCreditCards";
    private static final String TOOL_ERROR_MESSAGE = "### 인기 카드 정보를 불러오지 못했습니다.\n\n"
            + "잠시 후 다시 시도해 주세요.";

    private final PopularCreditCardService popularCreditCardService;
    private final PlayMcpWidgetFactory widgetFactory;

    @McpTool(
            name = TOOL_NAME,
            description = "Returns the current top five popular Shinhan Card(신한카드) products. Use when the "
                    + "user asks for popular, trending, best-selling, or most-issued cards. This tool requires no "
                    + "parameters and returns the ranking in popularity order.",
            annotations = @McpTool.McpAnnotations(
                    title = "신한카드 인기 TOP5",
                    readOnlyHint = true,
                    destructiveHint = false,
                    idempotentHint = true,
                    openWorldHint = false
            )
    )
    public PlayMcpWidgetResponse getPopularCreditCards() {
        try {
            List<PopularCreditCardService.PopularCard> popularCards =
                    popularCreditCardService.findPopularCards();
            PlayMcpWidgetResponse response =
                    widgetFactory.popularCreditCardList(popularCards);
            log.info("MCP 툴 호출 완료 - tool={}, count={}", TOOL_NAME, popularCards.size());
            return response;
        } catch (RuntimeException exception) {
            log.error("MCP 툴 처리 실패 - tool={}", TOOL_NAME, exception);
            throw new IllegalStateException(TOOL_ERROR_MESSAGE);
        }
    }
}
