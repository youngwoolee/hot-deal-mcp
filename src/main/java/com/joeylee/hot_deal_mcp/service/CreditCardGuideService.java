package com.joeylee.hot_deal_mcp.service;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;

/**
 * 사용자의 소비 업종과 연회비 조건을 카드 혜택 유형으로 변환한다.
 * 실제 카드 상품 데이터가 연결되기 전까지 특정 카드사나 상품명을 추천하지 않는다.
 */
@Service
public class CreditCardGuideService {

    public enum Industry {
        ONLINE_SHOPPING("온라인 쇼핑"),
        MART("마트"),
        CONVENIENCE_STORE("편의점"),
        RESTAURANT_CAFE("음식점/카페"),
        DELIVERY("배달"),
        TELECOM_UTILITIES("통신/공과금"),
        AUTO_FUEL("자동차/주유"),
        TRANSPORTATION("교통"),
        FASHION_BEAUTY("패션/뷰티"),
        EDUCATION("교육"),
        OVERSEAS("해외");

        private final String displayName;

        Industry(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }

        public static Industry fromString(String value) {
            if (value == null || value.isBlank()) {
                throw new IllegalArgumentException("업종을 입력해주세요.");
            }

            String normalized = value.trim().toLowerCase(Locale.ROOT).replace(" ", "");
            return Arrays.stream(values())
                    .filter(industry -> industry.displayName.toLowerCase(Locale.ROOT)
                            .replace(" ", "")
                            .equals(normalized))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException(
                            "지원하지 않는 업종입니다: " + value
                    ));
        }
    }

    public enum AnnualFeeBand {
        TEN_THOUSAND_RANGE("0~1만원대", 10_000),
        THIRTY_THOUSAND_RANGE("2~3만원대", 30_000),
        NO_LIMIT("제한없음", null);

        private final String displayName;
        private final Integer representativeFee;

        AnnualFeeBand(String displayName, Integer representativeFee) {
            this.displayName = displayName;
            this.representativeFee = representativeFee;
        }

        public String getDisplayName() {
            return displayName;
        }

        public Integer getRepresentativeFee() {
            return representativeFee;
        }

        public static AnnualFeeBand fromString(String value) {
            if (value == null || value.isBlank()) {
                return NO_LIMIT;
            }

            String normalized = value.trim().replace(" ", "");
            return Arrays.stream(values())
                    .filter(band -> band.displayName.replace(" ", "").equals(normalized))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException(
                            "지원하지 않는 연회비 구간입니다: " + value
                    ));
        }
    }

    public record CardGuide(
            String issuer,
            String name,
            String annualFee,
            List<String> benefits,
            String previousMonthRequirement,
            String notice
    ) {}

    public List<CardGuide> findGuides(Industry industry, AnnualFeeBand annualFeeBand) {
        if (annualFeeBand == AnnualFeeBand.NO_LIMIT) {
            return List.of(
                    guide(industry, AnnualFeeBand.TEN_THOUSAND_RANGE, "실속형"),
                    guide(industry, AnnualFeeBand.THIRTY_THOUSAND_RANGE, "균형형"),
                    guide(industry, AnnualFeeBand.THIRTY_THOUSAND_RANGE, "집중형")
            );
        }
        return List.of(
                guide(industry, annualFeeBand, "실속형"),
                guide(industry, annualFeeBand, "균형형"),
                guide(industry, annualFeeBand, "집중형")
        );
    }

    private CardGuide guide(Industry industry, AnnualFeeBand annualFeeBand, String tier) {
        return new CardGuide(
                "카드 상품 데이터 연동 필요",
                industry.getDisplayName() + " 혜택 " + tier + " 카드",
                annualFeeBand.getDisplayName(),
                List.of(
                        industry.getDisplayName() + " 이용 혜택을 우선 비교하는 유형",
                        "할인 한도와 제외 대상은 실제 상품 정보에서 확인"
                ),
                "상품별 확인 필요",
                "실제 상품명, 할인율, 전월 실적은 카드 상품 데이터 연동 후 제공됩니다."
        );
    }
}
