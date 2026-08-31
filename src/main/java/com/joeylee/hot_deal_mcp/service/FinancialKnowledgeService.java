package com.joeylee.hot_deal_mcp.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

@Service
public class FinancialKnowledgeService {

    private static final Map<FinancialKnowledgeCategory, List<Article>> MOCK_ARTICLES = Map.of(
            FinancialKnowledgeCategory.TREND,
            List.of(
                    new Article(
                            "데이터로 살펴본 2026 여행 트렌드",
                            "https://www.shinhancardblog.com/1432"
                    ),
                    new Article(
                            "데이터로 살펴본 2026년 웰니스 트렌드",
                            "https://www.shinhancardblog.com/1425"
                    ),
                    new Article(
                            "데이터로 살펴본 외식 트렌드 : 경험 콘텐츠가 된 파인 다이닝",
                            "https://www.shinhancardblog.com/1417"
                    ),
                    new Article(
                            "2026년 주목할 만한 소비 트렌드 ‘WISE UP’",
                            "https://www.shinhancardblog.com/1410"
                    ),
                    new Article(
                            "경험소비가 늘어나는 이유, 사람들은 왜 물건보다 경험에 돈을 쓸까요?",
                            "https://shinhangroup.com/kr/archive/insight/extend/detail/33245"
                    )
            ),
            FinancialKnowledgeCategory.FINANCE,
            List.of(
                    new Article(
                            "직장인이 점심값에 민감해진 이유 : 런치플레이션 시대의 생존법",
                            "https://shinhangroup.com/kr/archive/insight/extend/detail/33233"
                    ),
                    new Article(
                            "노후 생활비 계산, 은퇴 후 실제 필요한 생활비는 어떻게 계산할까요?",
                            "https://shinhangroup.com/kr/archive/insight/extend/detail/33235"
                    ),
                    new Article(
                            "새로워진 신한 슈퍼 SOL! 가입하고 런칭 이벤트 혜택 받는 방법",
                            "https://www.shinhancardblog.com/1434"
                    )
            ),
            FinancialKnowledgeCategory.CARD_LAB,
            List.of(
                    new Article(
                            "[쏠깃한 카드 연구소] 지구와 나를 위한 더 나은 Plan 신한카드 ECO Plan",
                            "https://www.shinhancardblog.com/1424"
                    ),
                    new Article(
                            "[쏠깃한 카드 연구소] 조건 없이 한층 더 강해진 혜택, 신한카드 Simple Plan+",
                            "https://www.shinhancardblog.com/1421"
                    ),
                    new Article(
                            "[쏠깃한 카드 연구소] 복잡한 건 딱 질색일 땐? 신한카드 Simple Plan",
                            "https://www.shinhancardblog.com/1415"
                    ),
                    new Article(
                            "[쏠깃한 카드 연구소] 장병들을 위한 ‘진짜’ 카드! 신한카드 나라사랑카드",
                            "https://www.shinhancardblog.com/1412"
                    ),
                    new Article(
                            "[쏠깃한 카드 연구소] 사장님 지갑 쏠쏠해지는 Npay biz 신한카드",
                            "https://www.shinhancardblog.com/1407"
                    )
            )
    );

    public List<Article> findArticles(FinancialKnowledgeCategory category) {
        return MOCK_ARTICLES.get(category);
    }

    public record Article(String title, String url) {}
}
