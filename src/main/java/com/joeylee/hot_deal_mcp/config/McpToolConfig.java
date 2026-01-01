package com.joeylee.hot_deal_mcp.config;

import java.util.ArrayList;
import java.util.List;

import com.joeylee.hot_deal_mcp.service.HotDealService;
import lombok.RequiredArgsConstructor;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.context.annotation.Configuration;


@Configuration
@RequiredArgsConstructor
public class McpToolConfig {

    private final HotDealService hotDealService;

    // 1. 입력 파라미터 (LLM이 채워줄 값)
    public record HotDealRequest(String keyword) {}

    // 2. 출력 결과 (Markdown 문자열로 리턴)
    @McpTool(
            name = "searchHotDeals",
            description = "현재 진행 중인 핫딜 정보를 검색합니다. 'keyword'와 'category'를 입력하여 알구몬에서 관련 상품을 검색합니다. 카테고리: ALL(전체), IT(전자/IT), FOOD(식품/영양제), BEAUTY(뷰티/패션), EVENT(이벤트/상품권), GAME(게임/앱), ETC(기타)"
        )
    public String searchHotDeals(
            @McpToolParam(description = "검색할 상품명 키워드", required = true) String keyword,
            @McpToolParam(description = "카테고리 (ALL, IT, FOOD, BEAUTY, EVENT, GAME, ETC 중 선택, 기본값: ALL)", required = false) String category
    ) {
        // 1. 알구몬에서 핫딜 데이터 가져오기 (카테고리별)
        List<HotDealService.DealInfo> allDeals = hotDealService.fetchHotDeals(category);

        // 2. 키워드 필터링
        List<HotDealService.DealInfo> filteredDeals = allDeals.stream()
                .filter(d -> isMatch(d.getTitle(), keyword))
                .limit(20)
                .toList();

        // 3. 결과 포맷팅 (Markdown)
        if (filteredDeals.isEmpty()) {
            return "현재 검색된 핫딜 정보가 없습니다. (키워드를 바꿔보세요!)";
        }

        HotDealService.Category cat = HotDealService.Category.fromString(category);
        return formatToMarkdown(filteredDeals, "🔍 " + keyword + " 검색 결과 (" + cat.getDisplayName() + ")");
    }

    @McpTool(
            name = "getHotDealRanking",
            description = "현재 실시간 핫딜 랭킹을 조회합니다. 카테고리별로 조회 가능합니다. 카테고리: ALL(전체), IT(전자/IT), FOOD(식품/영양제), BEAUTY(뷰티/패션), EVENT(이벤트/상품권), GAME(게임/앱), ETC(기타)"
    )
    public String getHotDealRanking(
            @McpToolParam(description = "카테고리 (ALL, IT, FOOD, BEAUTY, EVENT, GAME, ETC 중 선택, 기본값: ALL)", required = false) String category
    ) {
        List<HotDealService.DealInfo> allDeals = hotDealService.fetchHotDeals(category);

        if (allDeals.isEmpty()) {
            return "현재 조회 가능한 핫딜이 없습니다. 잠시 후 다시 시도해주세요.";
        }

        HotDealService.Category cat = HotDealService.Category.fromString(category);
        return formatToMarkdown(allDeals, "🔥 실시간 핫딜 랭킹 - " + cat.getDisplayName());
    }

    /**
     * 공통 로직: Markdown 변환 (중복 제거)
     */
    private String formatToMarkdown(List<HotDealService.DealInfo> deals, String titleHeader) {
        if (deals.isEmpty()) {
            return "검색된 핫딜 정보가 없습니다.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("### ").append(titleHeader).append("\n\n");
        sb.append("| 쇼핑몰 | 상품명 | 가격 | 링크 |\n");
        sb.append("| :---: | :--- | :---: | :---: |\n");

        for (HotDealService.DealInfo d : deals) {
            sb.append(String.format("| %s | %s | **%s** | [보기](%s) |\n",
                                    d.getMall(), d.getTitle(), d.getPrice(), d.getLink()));
        }
        sb.append(String.format("\n_총 %d개 핫딜 • 데이터 출처: [알구몬](https://www.algumon.com)_", deals.size()));
        return sb.toString();
    }

    private boolean isMatch(String title, String keyword) {
        if (keyword == null || keyword.isBlank()) return true;

        String cleanTitle = title.toLowerCase().replace(" ", "");
        String cleanKeyword = keyword.toLowerCase().replace(" ", "");

        // 1. 단순 포함 여부 (띄어쓰기 무시)
        if (cleanTitle.contains(cleanKeyword)) {
            return true;
        }

        // 2. 오타 허용 (Fuzzy Search)
        // 제목을 단어별로 쪼개서 키워드와 비슷한 단어가 있는지 확인
        // 예: 제목의 "체다치즈" vs 키워드 "체즈치즈"
        String[] titleWords = title.split(" ");
        for (String word : titleWords) {
            // 단어와 키워드의 '편집 거리' 계산
            int distance = calculateLevenshteinDistance(word, keyword);

            // 글자 수에 따라 허용 오차 범위 설정 (짧은 단어는 1글자, 긴 단어는 2글자까지)
            int threshold = (keyword.length() <= 3) ? 1 : 2;

            if (distance <= threshold) {
                return true; // 유사한 단어 발견!
            }
        }

        return false;
    }

    /**
     * 레벤슈타인 거리 계산 알고리즘 (두 문자열이 얼마나 다른지 계산)
     * 0이면 완전 일치, 숫자가 클수록 많이 다름.
     */
    private int calculateLevenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];

        for (int i = 0; i <= s1.length(); i++) dp[i][0] = i;
        for (int j = 0; j <= s2.length(); j++) dp[0][j] = j;

        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                int cost = (s1.charAt(i - 1) == s2.charAt(j - 1)) ? 0 : 1;
                dp[i][j] = Math.min(
                        Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                        dp[i - 1][j - 1] + cost
                );
            }
        }
        return dp[s1.length()][s2.length()];
    }

}
