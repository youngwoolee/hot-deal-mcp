package com.joeylee.hot_deal_mcp.service;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum CreditCardName {
    SOL_TRAVEL_CHECK("신한카드 SOL트래블 체크"),
    SUPER_SOL_CHECK("신한 슈퍼SOL 체크"),
    HEY_YOUNG_CHECK("신한카드 Hey Young 체크"),
    POINT_PLAN_CHECK("신한카드 Point Plan 체크"),
    SOL_GLOBAL_CHECK("신한카드 SOL글로벌 체크"),
    ON_CHECK("신한카드 On 체크"),
    WAY_CHECK("신한카드 Way 체크"),
    PICK_E_CHECK("신한카드 Pick E 체크"),
    HIPASS_CHECK("신한카드 하이패스(전용) 체크"),
    COMPACT_CAR_LOVE_LIFE("신한카드 경차사랑 Life"),
    PICK_I_CHECK("신한카드 Pick I 체크"),
    PLY_CHECK("신한카드 플리(체크)"),
    TRADITIONAL_MARKET_LOVE_CHECK("전통시장사랑 체크 신한카드"),
    SOL_TRAVEL_J_CHECK("신한카드 SOL트래블J 체크"),
    FIRST_CHECK("신한카드 처음 체크"),
    SOL_TRIP_AND_SHOP_CHECK("신한카드 SOL트립앤샵 체크"),
    FIRST("신한카드 처음"),
    SOL_MATE_SOL_PLAN_CHECK("SOL 메이트 신한카드 SOL Plan 체크"),
    NARASARANG_CHECK("신한카드 나라사랑카드 체크"),
    NARASARANG_BATTLEGROUNDS_CHECK("신한카드 나라사랑카드 체크(배틀그라운드 에디션)"),
    EVERYWHERE("신한카드 EVerywhere"),
    FIRST_ANNIVERSE("신한카드 처음 (ANNIVERSE)"),
    EDU_PLAN_PLUS("신한카드 Edu Plan+"),
    NATIONAL_HAPPINESS("신한카드 국민행복"),
    MAIN_TRANSACTION_CHECK("신한카드 주거래 체크"),
    NATIONAL_TOMORROW_LEARNING_SIMPLE("국민내일배움 신한카드 Simple"),
    ELEVEN_STREET("11번가 신한카드"),
    K_PASS("K-패스 신한카드"),
    POSTPAID_CLIMATE_COMPANION("신한 후불 기후동행 신용카드"),
    DEEP_OIL("신한카드 Deep Oil"),
    B_BIG("신한카드 B.Big(삑)"),
    SHINSEGAE("신세계 신한카드"),
    BAEMIN_BAPCHINGU("배민 신한카드 밥친구"),
    DEEP_STORE("신한카드 Deep Store"),
    EATS_MORE("신한카드 Eats More(이츠모아)"),
    TMONEY_PAY_AND_GO("티머니 Pay & GO 신한카드"),
    GS_ALL("GS ALL 신한카드"),
    NEXEN_TIRE("넥센타이어 신한카드"),
    E9PAY_FIRST("E9pay 신한카드 처음"),
    PLY("신한카드 플리"),
    DISCOUNT_PLAN("신한카드 Discount Plan"),
    MR_LIFE("신한카드 Mr.Life"),
    KT_FAMILY_SATISFACTION_DC("KT 가족만족 DC 신한카드"),
    SIMPLE_PLAN("신한카드 Simple Plan"),
    CU_NPAY("CU Npay 카드"),
    ALIEXPRESS("알리익스프레스 신한카드"),
    AFFORDABLE_MORE("신한카드 알뜰More(알뜰모아)"),
    HYDROGEN_CHARGING_DISCOUNT("수소차 충전할인 신한카드"),
    EDU("신한카드 Edu"),
    SHOPPING("신한카드 Shopping"),
    POINT_PLAN_SANRIO("신한카드 Point Plan(산리오캐릭터즈)"),
    ECO_PLAN("신한카드 ECO Plan"),
    POINT_PLAN("신한카드 Point Plan"),
    HI_POINT_PLAN("신한카드 Hi-Point Plan"),
    KAKAOBANK_CHACKCHAK("카카오뱅크 착붙 신한카드"),
    LGU_BORA_BIG_PLUS("LG U+ Bora 신한카드 Big Plus"),
    LGU_SMART_PLAN_PLUS("LG U+ 스마트플랜 Plus 신한카드"),
    LG_ELECTRONICS_SUBSCRIPTION_CARE("LG전자 The 구독케어 신한카드"),
    SKT_T_LIGHT("SKT T라이트 신한카드"),
    LGE_COM("LGE.COM 신한카드"),
    TOSS_ONE("Toss One 신한카드"),
    SKT_T_AND_LIFE("SKT T&Life 신한카드"),
    COWAY("코웨이 신한카드"),
    SK_BROADBAND("SK브로드밴드 신한카드"),
    WOONGJIN_PREED("웅진프리드 신한카드"),
    CARRIER("캐리어 신한카드"),
    HARU_HOSHINO_RESORTS("신한카드 Haru(Hoshino Resorts)"),
    DEEP_ON_PLATINUM_PLUS("신한카드 Deep On Platinum+"),
    BIZ_PLAN("신한카드 Biz Plan"),
    SOL_PLAN("신한카드 SOL Plan"),
    POINT_PLAN_PLUS("신한카드 Point Plan+"),
    AIR_PLATINUM_SHARP("신한카드 Air Platinum#"),
    AIR_ONE("신한카드 Air One"),
    SOL_PLAN_PLUS("신한카드 SOL Plan+"),
    SOL_MATE_SOL_PLAN_PLUS("SOL메이트 신한카드 SOL Plan+"),
    DISCOUNT_PLAN_PLUS("신한카드 Discount Plan+"),
    HI_POINT_PLAN_PLUS("신한카드 Hi-Point Plan+"),
    SIMPLE_PLAN_PLUS("신한카드 Simple Plan+"),
    THE_CLASSIC_Y("신한카드 The CLASSIC-Y"),
    KRISFLYER_THE_BEST("싱가포르항공 크리스플라이어 더 베스트 신한카드"),
    MARRIOTT_BONVOY_THE_BEST("메리어트 본보이™ 더 베스트 신한카드");

    private static final String PARAMETER_DESCRIPTION_PREFIX =
            "Exact Shinhan Card(신한카드) product name. Choose the closest matching value from: ";

    private final String displayName;

    CreditCardName(String displayName) {
        this.displayName = displayName;
    }

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }

    public static List<String> displayNames() {
        return Arrays.stream(values())
                .map(CreditCardName::getDisplayName)
                .toList();
    }

    public static String parameterDescription() {
        return PARAMETER_DESCRIPTION_PREFIX + String.join(", ", displayNames());
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static CreditCardName fromDisplayName(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("카드 이름을 입력해주세요.");
        }

        String normalized = normalize(value);
        return Arrays.stream(values())
                .filter(cardName -> normalize(cardName.displayName).equals(normalized))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "지원하지 않는 카드 이름입니다: " + value
                ));
    }

    private static String normalize(String value) {
        return value.replaceAll("\\s+", "").toLowerCase(Locale.ROOT);
    }
}
