package com.joeylee.hot_deal_mcp.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;


@Service
@Slf4j
public class HotDealService {

    public enum Category {
        ALL("전체", ""),
        IT("전자/IT", "it"),
        FOOD("식품/영양제", "food"),
        BEAUTY("뷰티/패션", "beauty"),
        EVENT("이벤트/상품권", "event"),
        GAME("게임/앱", "game"),
        ETC("기타", "etc");

        private final String displayName;
        private final String urlParam;

        Category(String displayName, String urlParam) {
            this.displayName = displayName;
            this.urlParam = urlParam;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getUrlParam() {
            return urlParam;
        }

        public static Category fromString(String category) {
            if (category == null || category.isBlank()) {
                return ALL;
            }

            for (Category c : Category.values()) {
                if (c.name().equalsIgnoreCase(category) ||
                    c.urlParam.equalsIgnoreCase(category) ||
                    c.displayName.equals(category)) {
                    return c;
                }
            }
            return ALL;
        }
    }

    @Data
    @Builder
    public static class DealInfo {
        private String title;
        private String price;
        private String link;
        private String mall;
        private String category;
    }

    @Cacheable(value = "hotDeals", key = "#category.name()", unless = "#result == null || #result.isEmpty()")
    public List<DealInfo> fetchHotDeals(Category category) {
        if (category == null) {
            category = Category.ALL;
        }

        log.info("알구몬 핫딜 크롤링 시작 - 카테고리: {} (Cache Miss)", category.getDisplayName());
        List<DealInfo> deals = new ArrayList<>();

        try {
            // 알구몬 랭킹 페이지 - 카테고리별 URL
            String url = category.getUrlParam().isEmpty()
                ? "https://www.algumon.com/deal/rank"
                : "https://www.algumon.com/deal/rank?category=" + category.getUrlParam();

            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36")
                    .header("Referer", "https://www.algumon.com/") // 리퍼러 헤더 추가 (차단 방지)
                    .timeout(5000)
                    .get();

            // 알구몬 리스트 아이템 선택자 (.post-li)
            for (Element row : doc.select(".post-li")) {
                // 1. 제목 & 링크
                Element linkEl = row.selectFirst(".product-link");
                if (linkEl == null) continue;

                String title = linkEl.text().trim();
                String link = "https://www.algumon.com" + linkEl.attr("href"); // 상대 경로이므로 도메인 붙이기

                // 2. 가격
                String price = "가격정보없음";
                Element priceEl = row.selectFirst(".product-price");
                if (priceEl != null) {
                    price = priceEl.text().trim();
                }

                // 3. 쇼핑몰 이름 (Badge)
                String mall = "기타";
                Element shopEl = row.selectFirst(".label.shop");
                if (shopEl != null) {
                    mall = shopEl.text().trim();
                } else {
                    Element siteEl = row.selectFirst(".label.site");
                    if (siteEl != null) mall = siteEl.text().trim();
                }

                deals.add(DealInfo.builder()
                                  .title(title)
                                  .price(price)
                                  .link(link)
                                  .mall(mall)
                                  .category(category.getDisplayName())
                                  .build());
            }
        } catch (Exception e) {
            log.error("알구몬 크롤링 에러: {}", e.getMessage());
            return Collections.emptyList();
        }

        log.info("알구몬에서 {}개의 핫딜 수집 완료 (카테고리: {})", deals.size(), category.getDisplayName());
        return deals;
    }

    // 편의 메서드: 카테고리 없이 호출 시 전체 카테고리
    public List<DealInfo> fetchHotDeals() {
        return fetchHotDeals(Category.ALL);
    }

    // 편의 메서드: 문자열로 카테고리 지정
    public List<DealInfo> fetchHotDeals(String categoryStr) {
        Category category = Category.fromString(categoryStr);
        return fetchHotDeals(category);
    }
}
