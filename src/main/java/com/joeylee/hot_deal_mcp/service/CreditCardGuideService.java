package com.joeylee.hot_deal_mcp.service;

import java.util.List;

import org.springframework.stereotype.Service;

/**
 * 사용자의 소비 업종과 연회비 조건을 카드 혜택 유형으로 변환한다.
 * 실제 카드 상품 데이터가 연결되기 전까지 특정 카드사나 상품명을 추천하지 않는다.
 */
@Service
public class CreditCardGuideService {

    private static final String MOCK_CARD_DETAIL_URL =
            "https://www.shinhancard.com/pconts/html/card/apply/credit/2013775_2207.html";

    public record CardGuide(
            String issuer,
            String name,
            String annualFee,
            String benefitCategory,
            List<String> benefits,
            String previousMonthRequirement,
            String notice
    ) {}

    public record CardDetail(
            String issuer,
            String name,
            String annualFee,
            List<String> benefits,
            String detailPageUrl
    ) {}

    public List<CardGuide> findGuides(int industryCode, AnnualFeeBand annualFeeBand) {
        Industry industry = Industry.fromCode(industryCode);
        List<CreditCardName> mockCards = mockCardsFor(industry);
        if (annualFeeBand == AnnualFeeBand.NO_LIMIT) {
            return List.of(
                    guide(mockCards.get(0), industry, AnnualFeeBand.TEN_THOUSAND_RANGE),
                    guide(mockCards.get(1), industry, AnnualFeeBand.THIRTY_THOUSAND_RANGE),
                    guide(mockCards.get(2), industry, AnnualFeeBand.THIRTY_THOUSAND_RANGE)
            );
        }
        return List.of(
                guide(mockCards.get(0), industry, annualFeeBand),
                guide(mockCards.get(1), industry, annualFeeBand),
                guide(mockCards.get(2), industry, annualFeeBand)
        );
    }

    public CardDetail findCardDetail(String cardName) {
        CreditCardName matchedCard = CreditCardName.fromDisplayName(cardName);
        return new CardDetail(
                "신한카드",
                matchedCard.getDisplayName(),
                "18,000원",
                List.of(
                        "공과금 10% 할인",
                        "편의점·병원·약국 10% 할인",
                        "야간 온라인 쇼핑 10% 할인"
                ),
                MOCK_CARD_DETAIL_URL
        );
    }

    private CardGuide guide(
            CreditCardName cardName,
            Industry industry,
            AnnualFeeBand annualFeeBand
    ) {
        return new CardGuide(
                "신한카드",
                cardName.getDisplayName(),
                annualFeeBand.getDisplayName(),
                industry.getDisplayName(),
                List.of(
                        industry.getDisplayName() + " 이용 혜택을 우선 비교하는 유형",
                        "할인 한도와 제외 대상은 실제 상품 정보에서 확인"
                ),
                "상품별 확인 필요",
                "실제 상품명, 할인율, 전월 실적은 카드 상품 데이터 연동 후 제공됩니다."
        );
    }

    private List<CreditCardName> mockCardsFor(Industry industry) {
        return switch (industry) {
            case FUEL -> List.of(
                    CreditCardName.DEEP_OIL,
                    CreditCardName.EVERYWHERE,
                    CreditCardName.DISCOUNT_PLAN
            );
            case LARGE_MART, CONVENIENCE_STORE, SHOPPING, SIMPLE_PAYMENT -> List.of(
                    CreditCardName.SHOPPING,
                    CreditCardName.DEEP_STORE,
                    CreditCardName.POINT_PLAN
            );
            case MOVIE_PERFORMANCE, LEISURE, SUBSCRIPTION -> List.of(
                    CreditCardName.PLY,
                    CreditCardName.DISCOUNT_PLAN,
                    CreditCardName.POINT_PLAN_PLUS
            );
            case DINING_DELIVERY, CAFE -> List.of(
                    CreditCardName.EATS_MORE,
                    CreditCardName.BAEMIN_BAPCHINGU,
                    CreditCardName.MR_LIFE
            );
            case PUBLIC_TRANSPORT -> List.of(
                    CreditCardName.B_BIG,
                    CreditCardName.K_PASS,
                    CreditCardName.POSTPAID_CLIMATE_COMPANION
            );
            case HOSPITAL_PHARMACY, UTILITIES, TELECOM -> List.of(
                    CreditCardName.MR_LIFE,
                    CreditCardName.SIMPLE_PLAN,
                    CreditCardName.DISCOUNT_PLAN
            );
            case EDUCATION_CHILDCARE, YOUTH -> List.of(
                    CreditCardName.EDU,
                    CreditCardName.EDU_PLAN_PLUS,
                    CreditCardName.NATIONAL_HAPPINESS
            );
            case AIRLINE, AIRPORT, TRAVEL_STAY -> List.of(
                    CreditCardName.AIR_ONE,
                    CreditCardName.AIR_PLATINUM_SHARP,
                    CreditCardName.SOL_TRAVEL_CHECK
            );
            case BEAUTY -> List.of(
                    CreditCardName.DEEP_STORE,
                    CreditCardName.POINT_PLAN,
                    CreditCardName.PLY
            );
            case ANYWHERE, FINANCE, DISCOUNT, POINTS -> List.of(
                    CreditCardName.POINT_PLAN,
                    CreditCardName.SOL_PLAN,
                    CreditCardName.SIMPLE_PLAN
            );
        };
    }
}
