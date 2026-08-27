package com.joeylee.hot_deal_mcp.config;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.joeylee.hot_deal_mcp.service.AnnualFeeBand;
import com.joeylee.hot_deal_mcp.service.CreditCardGuideService;
import com.joeylee.hot_deal_mcp.service.Industry;
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
public class McpToolConfig {

    private static final String TOOL_NAME = "getCreditCardRecommendationsWithSelector";
    private static final String TOOL_ERROR_MESSAGE = "### 카드 정보를 불러오지 못했습니다.\n\n"
            + "잠시 후 다시 시도해 주세요.";

    private final CreditCardGuideService creditCardGuideService;
    private final PlayMcpWidgetFactory widgetFactory;
    private final ObjectMapper objectMapper;

//    @McpTool(
//            name = "getCreditCardRecommendations",
//            description = "사용자가 신용카드 추천, 카드 비교, 업종별 할인 카드 또는 혜택 카드를 요청할 때 호출합니다. "
//                    + "사용자 발화에서 주요 소비 업종과 선호 연회비를 추출하여 조건에 맞는 카드 혜택 유형을 안내합니다. "
//                    + "예를 들어 '3만원대 교육 할인되는 카드 추천해줘'라는 요청은 업종을 교육, 연회비를 2~3만원대로 설정합니다. "
//                    + "업종은 온라인 쇼핑, 마트, 편의점, 음식점/카페, 배달, 통신/공과금, 자동차/주유, 교통, 패션/뷰티, 교육, 해외 중 하나입니다. "
//                    + "연회비는 0~1만원대, 2~3만원대, 제한없음 중 하나입니다. 무료, 1만원 이하, 1만원대는 0~1만원대로, "
//                    + "2만원대, 3만원대, 3만원 이하는 2~3만원대로 변환합니다. 연회비를 말하지 않거나 상관없다고 하면 제한없음을 사용합니다.",
//            annotations = @McpTool.McpAnnotations(
//                    title = "소비 업종별 카드 안내",
//                    readOnlyHint = true,
//                    destructiveHint = false,
//                    idempotentHint = true,
//                    openWorldHint = false
//            )
//    )
//    public PlayMcpWidgetResponse getCreditCardRecommendations(
//            @McpToolParam(
//                    description = "사용자가 할인이나 혜택을 원하는 업종. '교육 할인'은 교육으로 추출합니다. 허용값: 온라인 쇼핑, 마트, 편의점, 음식점/카페, 배달, 통신/공과금, 자동차/주유, 교통, 패션/뷰티, 교육, 해외",
//                    required = false
//            )
//            String industry,
//            @McpToolParam(
//                    description = "사용자가 원하는 연회비 구간. '3만원대'는 2~3만원대로 변환합니다. 허용값: 0~1만원대, 2~3만원대, 제한없음. 언급이 없거나 상관없으면 제한없음",
//                    required = false
//            )
//            String annualFee
//    ) {
//        logToolParameters(
//                "getCreditCardRecommendations",
//                "industry", industry,
//                "annualFee", annualFee
//        );
//
//        return recommendCreditCards(
//                "getCreditCardRecommendationsWithSelector",
//                industry,
//                annualFee,
//                widgetFactory.clarificationNeeded(industryClarificationText()),
//                widgetFactory.clarificationNeeded(annualFeeClarificationText())
//        );
//    }

    @McpTool(
            name = TOOL_NAME,
            description = "Recommends Shinhan Card(신한카드) credit card benefit types based on the user's "
                    + "preferred spending category and annual-fee range. Use for credit card recommendations, "
                    + "comparisons, or category-specific benefits. Pass the closest supported category code as "
                    + "industry; omit it when unclear to show a selector widget. Set annualFee to 0~1만원대, "
                    + "2~3만원대, or 제한없음; use 제한없음 when the user does not specify a range.",
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
            String annualFee
    ) {
        try {
            logToolParameters(
                    TOOL_NAME,
                    "industry", industry,
                    "annualFee", annualFee
            );

            return recommendCreditCards(
                    TOOL_NAME,
                    industry,
                    annualFee,
                    widgetFactory.industrySelector(),
                    widgetFactory.annualFeeSelector()
            );
        } catch (RuntimeException exception) {
            log.error(
                    "MCP 툴 처리 실패 - tool={}, industry={}, annualFee={}",
                    TOOL_NAME,
                    industry,
                    annualFee,
                    exception
            );
            // The MCP annotation adapter converts this message into isError=true text content.
            // Do not attach the original cause because the adapter exposes the deepest cause message.
            throw new IllegalStateException(TOOL_ERROR_MESSAGE);
        }
    }

    private PlayMcpWidgetResponse recommendCreditCards(
            String toolName,
            Integer industry,
            String annualFee,
            PlayMcpWidgetResponse industryClarification,
            PlayMcpWidgetResponse annualFeeClarification
    ) {
        Industry parsedIndustry;
        try {
            if (industry == null) {
                throw new IllegalArgumentException("업종 코드를 입력해주세요.");
            }
            parsedIndustry = Industry.fromCode(industry);
        } catch (IllegalArgumentException exception) {
            return logAndReturn(toolName, industryClarification);
        }

        AnnualFeeBand parsedAnnualFee;
        try {
            parsedAnnualFee = AnnualFeeBand.fromString(annualFee);
        } catch (IllegalArgumentException exception) {
            return logAndReturn(toolName, annualFeeClarification);
        }

        List<CreditCardGuideService.CardGuide> guides =
                creditCardGuideService.findGuides(parsedIndustry.getCode(), parsedAnnualFee);
        PlayMcpWidgetResponse response =
                widgetFactory.creditCardGuideList(guides, parsedIndustry, parsedAnnualFee);
        return logAndReturn(toolName, response);
    }

    private static String industryClarificationText() {
        String options = String.join(", ", Industry.displayNames());
        return "어떤 업종의 카드 혜택을 원하시는지 다시 말씀해 주세요.\n\n**선택 가능한 업종**: " + options
                + "\n\n버튼을 눌러 선택하실 수도 있습니다.";
    }

    private static String annualFeeClarificationText() {
        String options = String.join(", ", AnnualFeeBand.displayNames());
        return "원하시는 연회비 구간을 다시 말씀해 주세요.\n\n**선택 가능한 구간**: " + options;
    }

    private void logToolParameters(String toolName, Object... keyValues) {
        Map<String, Object> parameters = new LinkedHashMap<>();
        for (int index = 0; index < keyValues.length; index += 2) {
            parameters.put((String) keyValues[index], keyValues[index + 1]);
        }
        logJson("MCP 툴 파라미터", toolName, parameters);
    }

    private PlayMcpWidgetResponse logAndReturn(
            String toolName,
            PlayMcpWidgetResponse response
    ) {
        logJson("MCP 툴 JSON 응답", toolName, response);
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
