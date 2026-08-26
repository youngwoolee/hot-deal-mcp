package com.joeylee.hot_deal_mcp.service;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;

/**
 * 사용자의 소비 업종과 연회비 조건을 카드 혜택 유형으로 변환한다.
 * 실제 카드 상품 데이터가 연결되기 전까지 특정 카드사나 상품명을 추천하지 않는다.
 */
@Service
public class CreditCardGuideService {

    public enum Industry {
        ANYWHERE(1, "어디서나"),
        FUEL(2, "주유"),
        LARGE_MART(3, "대형마트"),
        CONVENIENCE_STORE(4, "편의점"),
        SHOPPING(5, "쇼핑"),
        MOVIE_PERFORMANCE(6, "영화/공연"),
        DINING_DELIVERY(7, "외식/배달"),
        CAFE(8, "카페"),
        PUBLIC_TRANSPORT(9, "대중교통"),
        HOSPITAL_PHARMACY(10, "병원/약국"),
        UTILITIES(11, "공과금"),
        TELECOM(12, "통신"),
        EDUCATION_CHILDCARE(13, "교육/육아"),
        LEISURE(14, "레저"),
        AIRLINE(15, "항공"),
        AIRPORT(16, "공항"),
        BEAUTY(17, "뷰티"),
        SIMPLE_PAYMENT(18, "간편결제"),
        SUBSCRIPTION(19, "구독"),
        TRAVEL_STAY(20, "여행/숙박"),
        FINANCE(21, "금융"),
        DISCOUNT(22, "할인"),
        POINTS(23, "적립"),
        YOUTH(24, "청소년");

        private final int code;
        private final String displayName;

        Industry(int code, String displayName) {
            this.code = code;
            this.displayName = displayName;
        }

        public int getCode() {
            return code;
        }

        public String getDisplayName() {
            return displayName;
        }

        public static Industry fromCode(int code) {
            return Arrays.stream(values())
                    .filter(industry -> industry.code == code)
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException(
                            "지원하지 않는 업종 코드입니다: " + code
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

    public record CardDetail(
            String issuer,
            String name,
            String summary
    ) {}

    public List<CardGuide> findGuides(int industryCode, AnnualFeeBand annualFeeBand) {
        Industry industry = Industry.fromCode(industryCode);
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

    public CardDetail findCardDetail(String cardName) {
        CreditCardName matchedCard = CreditCardName.fromDisplayName(cardName);
        return new CardDetail(
                "신한카드",
                matchedCard.getDisplayName(),
                "카드 상세 데이터 연동 필요"
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
