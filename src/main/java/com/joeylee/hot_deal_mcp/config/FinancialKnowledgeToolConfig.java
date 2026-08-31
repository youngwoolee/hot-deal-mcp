package com.joeylee.hot_deal_mcp.config;

import com.joeylee.hot_deal_mcp.service.FinancialKnowledgeCategory;
import com.joeylee.hot_deal_mcp.service.FinancialKnowledgeService;
import com.joeylee.hot_deal_mcp.widget.PlayMcpWidgetFactory;
import com.joeylee.hot_deal_mcp.widget.PlayMcpWidgetResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class FinancialKnowledgeToolConfig {

    private static final String TOOL_NAME = "getFinancialLifeKnowledgeArticles";
    private static final String TOOL_ERROR_MESSAGE = "### 금융생활지식을 불러오지 못했습니다.\n\n"
            + "잠시 후 다시 시도해 주세요.";

    private final FinancialKnowledgeService financialKnowledgeService;
    private final PlayMcpWidgetFactory widgetFactory;

    @McpTool(
            name = TOOL_NAME,
            description = "Returns Shinhan financial-life articles grouped by intent. Use category 트렌드 for "
                    + "consumer trends, spending reports, consumption data, or general financial content; 금융 "
                    + "for practical money knowledge, financial issues, or Shinhan guidance; 카드연구소 for "
                    + "smart card usage, card tips, saving tips, or useful card information.",
            annotations = @McpTool.McpAnnotations(
                    title = "금융생활지식 콘텐츠",
                    readOnlyHint = true,
                    destructiveHint = false,
                    idempotentHint = true,
                    openWorldHint = false
            )
    )
    public PlayMcpWidgetResponse getFinancialLifeKnowledgeArticles(
            @McpToolParam(
                    description = "질문의 의도에 해당하는 콘텐츠 카테고리. 허용값: 트렌드, 금융, 카드연구소. '요즘 소비 트렌드', '금융 상식', '사람들 돈 어디에 써', '볼만한 금융 콘텐츠', '소비 리포트·데이터 분석'은 트렌드, '돈 관련 알아둘 정보', '금융생활에 도움되는 정보', '이슈되는 금융 이야기', '신한카드 안내'는 금융, '카드 똑똑하게 쓰는 법', '카드 꿀팁', '절약 팁'은 카드연구소를 사용합니다. 명확하지 않으면 트렌드를 사용합니다.",
                    required = false
            )
            String category
    ) {
        try {
            FinancialKnowledgeCategory parsedCategory =
                    FinancialKnowledgeCategory.fromString(category);
            PlayMcpWidgetResponse response = widgetFactory.financialKnowledgeList(
                    parsedCategory,
                    financialKnowledgeService.findArticles(parsedCategory)
            );
            log.info("MCP 툴 호출 완료 - tool={}, category={}", TOOL_NAME, parsedCategory.getDisplayName());
            return response;
        } catch (RuntimeException exception) {
            log.error("MCP 툴 처리 실패 - tool={}, category={}", TOOL_NAME, category, exception);
            throw new IllegalStateException(TOOL_ERROR_MESSAGE);
        }
    }
}
