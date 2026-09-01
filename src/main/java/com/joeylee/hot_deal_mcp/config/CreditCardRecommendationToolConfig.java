package com.joeylee.hot_deal_mcp.config;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.joeylee.hot_deal_mcp.service.AnnualFeeBand;
import com.joeylee.hot_deal_mcp.service.CardSortOrder;
import com.joeylee.hot_deal_mcp.service.CreditCardGuideService;
import com.joeylee.hot_deal_mcp.service.Industry;
import com.joeylee.hot_deal_mcp.widget.CreditCardRecommendationWidgetFactory;
import com.joeylee.hot_deal_mcp.widget.PlayMcpWidgetResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class CreditCardRecommendationToolConfig {

    private static final String TOOL_NAME = "getCreditCardRecommendationsWithSelector";
    private static final int CREDIT_CARD_TYPE = 1;
    private static final int CHECK_CARD_TYPE = 2;
    private static final String TOOL_ERROR_MESSAGE = "### 카드 정보를 불러오지 못했습니다.\n\n"
            + "잠시 후 다시 시도해 주세요.";

    private final CreditCardGuideService creditCardGuideService;
    private final CreditCardRecommendationWidgetFactory widgetFactory;
    private final ObjectMapper objectMapper;

    @McpTool(
            name = TOOL_NAME,
            description = "Recommends Shinhan Card(신한카드) credit card benefit types based on the user's "
                    + "preferred spending category and annual-fee range. Use for credit card recommendations, "
                    + "comparisons, or category-specific benefits. Pass the closest supported category code as "
                    + "industry; omit it when unclear to show a selector widget. Set annualFee to 0~1만원대, "
                    + "2~3만원대, or 제한없음; use 제한없음 when the user does not specify a range. Recommend "
                    + "credit cards by default. Set cardType to 2 only when the user explicitly asks for a check "
                    + "card; youth category requests automatically return check cards. Set sort to 출시일순 or "
                    + "연회비순; default to 출시일순 when the user does not specify sorting.",
            annotations = @McpTool.McpAnnotations(
                    title = "소비 업종별 카드 안내 (선택 위젯)",
                    readOnlyHint = true,
                    destructiveHint = false,
                    idempotentHint = true,
                    openWorldHint = false
            )
    )
    public PlayMcpWidgetResponse getCreditCardRecommendationsWithSelector(
            @McpToolParam(
                    description = "사용자가 할인이나 혜택을 원하는 업종에 해당하는 번호(1~24). "
                            + "허용값: 1 어디서나, 2 주유, 3 대형마트, 4 편의점, 5 쇼핑, 6 영화/공연, 7 외식/배달, 8 카페, "
                            + "9 대중교통, 10 병원/약국, 11 공과금, 12 통신, 13 교육/육아, 14 레저, 15 항공/마일리지, 16 공항/공항라운지, "
                            + "17 뷰티, 18 간편결제, 19 구독, 20 여행/숙박, 21 금융, 22 할인, 23 적립, 24 청소년",
                    required = false
            )
            Integer industry,
            @McpToolParam(
                    description = "사용자가 원하는 연회비 구간. '3만원대'는 2~3만원대로 변환합니다. 허용값: 0~1만원대, 2~3만원대, 제한없음. 언급이 없거나 상관없으면 제한없음",
                    required = false
            )
            String annualFee,
            @McpToolParam(
                    description = "카드 종류. 1은 신용카드, 2는 체크카드입니다. 사용자가 체크카드를 명시적으로 요청한 경우에만 2를 사용하고, 그 외에는 생략하거나 1을 사용합니다. 청소년 업종(24)은 값과 관계없이 체크카드로 조회됩니다.",
                    required = false
            )
            Integer cardType,
            @McpToolParam(
                    description = "정렬 기준. 허용값: 출시일순, 연회비순. 언급이 없으면 출시일순을 사용합니다. 출시일순은 최신 출시 카드부터, 연회비순은 낮은 연회비부터 정렬합니다.",
                    required = false
            )
            String sort
    ) {
        try {
            logToolParameters(
                    TOOL_NAME,
                    "industry", industry,
                    "annualFee", annualFee,
                    "cardType", cardType,
                    "sort", sort
            );

            return recommendCreditCards(
                    industry,
                    annualFee,
                    cardType,
                    sort
            );
        } catch (RuntimeException exception) {
            log.error(
                    "MCP 툴 처리 실패 - tool={}, industry={}, annualFee={}, cardType={}, sort={}",
                    TOOL_NAME,
                    industry,
                    annualFee,
                    cardType,
                    sort,
                    exception
            );
            // The MCP annotation adapter converts this message into isError=true text content.
            // Do not attach the original cause because the adapter exposes the deepest cause message.
            throw new IllegalStateException(TOOL_ERROR_MESSAGE);
        }
    }

    private PlayMcpWidgetResponse recommendCreditCards(
            Integer industry,
            String annualFee,
            Integer cardType,
            String sort
    ) {
        Industry parsedIndustry;
        try {
            if (industry == null) {
                throw new IllegalArgumentException("업종 코드를 입력해주세요.");
            }
            parsedIndustry = Industry.fromCode(industry);
        } catch (IllegalArgumentException exception) {
            return logAndReturn(widgetFactory.industrySelector());
        }

        AnnualFeeBand parsedAnnualFee;
        try {
            parsedAnnualFee = AnnualFeeBand.fromString(annualFee);
        } catch (IllegalArgumentException exception) {
            return logAndReturn(widgetFactory.annualFeeSelector());
        }

        CardSortOrder parsedSortOrder = CardSortOrder.fromString(sort);

        List<CreditCardGuideService.CardGuide> guides =
                creditCardGuideService.findGuides(
                        parsedIndustry.getCode(),
                        parsedAnnualFee,
                        resolveCardType(parsedIndustry, cardType),
                        parsedSortOrder
                );
        PlayMcpWidgetResponse response =
                widgetFactory.creditCardGuideList(guides, parsedIndustry, parsedAnnualFee);
        return logAndReturn(response);
    }

    private int resolveCardType(Industry industry, Integer requestedCardType) {
        if (industry == Industry.YOUTH || Integer.valueOf(CHECK_CARD_TYPE).equals(requestedCardType)) {
            return CHECK_CARD_TYPE;
        }
        return CREDIT_CARD_TYPE;
    }

    private void logToolParameters(String toolName, Object... keyValues) {
        Map<String, Object> parameters = new LinkedHashMap<>();
        for (int index = 0; index < keyValues.length; index += 2) {
            parameters.put((String) keyValues[index], keyValues[index + 1]);
        }
        logJson("MCP 툴 파라미터", toolName, parameters);
    }

    private PlayMcpWidgetResponse logAndReturn(PlayMcpWidgetResponse response) {
        logJson("MCP 툴 JSON 응답", TOOL_NAME, response);
        return response;
    }

    private void logJson(String message, String toolName, Object value) {
        try {
            log.info("{} - tool={}, json={}", message, toolName, objectMapper.writeValueAsString(value));
        } catch (JsonProcessingException exception) {
            log.warn("{} 직렬화 실패 - tool={}, error={}", message, toolName, exception.getMessage());
        }
    }
}
