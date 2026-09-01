package com.joeylee.hot_deal_mcp.widget;

import static com.joeylee.hot_deal_mcp.widget.PlayMcpWidgetActions.openUrl;
import static com.joeylee.hot_deal_mcp.widget.PlayMcpWidgetActions.sendUserMessage;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.joeylee.hot_deal_mcp.service.FinancialKnowledgeCategory;
import com.joeylee.hot_deal_mcp.service.FinancialKnowledgeService;
import org.springframework.stereotype.Component;

@Component
public class FinancialKnowledgeWidgetFactory {

    private static final int MAX_LIST_ITEMS = 10;

    public PlayMcpWidgetResponse financialKnowledgeList(
            FinancialKnowledgeCategory category,
            List<FinancialKnowledgeService.Article> articles
    ) {
        List<Map<String, Object>> listItems = new ArrayList<>();
        listItems.add(headerItem(category));
        for (int index = 0; index < articles.size(); index++) {
            listItems.add(articleItem(index + 1, articles.get(index)));
        }
        listItems.add(nextCategoryItem(category.next()));

        Map<String, Object> widget = new LinkedHashMap<>();
        widget.put("type", "ListView");
        widget.put("limit", MAX_LIST_ITEMS);
        widget.put("children", listItems);

        StringBuilder copyText = new StringBuilder("### 금융생활지식 · ")
                .append(category.getDisplayName())
                .append("\n\n");
        for (int index = 0; index < articles.size(); index++) {
            FinancialKnowledgeService.Article article = articles.get(index);
            copyText.append(index + 1)
                    .append(". [")
                    .append(article.title())
                    .append("](")
                    .append(article.url())
                    .append(")\n");
        }
        return new PlayMcpWidgetResponse(widget, copyText.toString());
    }

    private Map<String, Object> headerItem(FinancialKnowledgeCategory category) {
        return Map.of(
                "type", "ListViewItem",
                "children", List.of(Map.of(
                        "type", "Col",
                        "gap", 2,
                        "align", "start",
                        "padding", Map.of("x", 2, "y", 2),
                        "children", List.of(
                                Map.of(
                                        "type", "Text",
                                        "value", category.getWidgetTitle(),
                                        "size", "lg",
                                        "weight", "semibold",
                                        "textAlign", "start"
                                ),
                                Map.of(
                                        "type", "Caption",
                                        "value", "금융생활지식 (" + category.getDisplayName() + ")",
                                        "textAlign", "start"
                                )
                        )
                ))
        );
    }

    private Map<String, Object> articleItem(
            int index,
            FinancialKnowledgeService.Article article
    ) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("type", "ListViewItem");
        item.put("key", article.url());
        item.put("align", "start");
        item.put("gap", 3);
        item.put("onClickAction", openUrl(article.url()));
        item.put("children", List.of(
                Map.of(
                        "type", "Text",
                        "value", String.format("%02d", index),
                        "weight", "semibold",
                        "width", 32,
                        "textAlign", "start"
                ),
                Map.of(
                        "type", "Col",
                        "flex", 1,
                        "align", "start",
                        "padding", Map.of("x", 1, "y", 2),
                        "children", List.of(Map.of(
                                "type", "Text",
                                "value", article.title(),
                                "width", "100%",
                                "textAlign", "start",
                                "maxLines", 3
                        ))
                )
        ));
        return item;
    }

    private Map<String, Object> nextCategoryItem(FinancialKnowledgeCategory nextCategory) {
        Map<String, Object> button = new LinkedHashMap<>();
        button.put("type", "Button");
        button.put("label", "다른 카테고리 보기");
        button.put("variant", "outline");
        button.put("block", true);
        button.put(
                "onClickAction",
                sendUserMessage(nextCategory.getDisplayName() + " 금융생활지식 보여줘")
        );
        return Map.of(
                "type", "ListViewItem",
                "children", List.of(button)
        );
    }
}
