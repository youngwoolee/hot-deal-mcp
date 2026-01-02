package com.joeylee.hot_deal_mcp.controller;

import com.joeylee.hot_deal_mcp.service.HotDealService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
public class TestController {

    private final HotDealService hotDealService;

    @GetMapping("/hot-deals")
    public List<HotDealService.DealInfo> getHotDeals(
            @RequestParam(required = false, defaultValue = "ALL") String category
    ) {
        // String을 Category로 변환 후 직접 호출 (캐시 작동을 위해)
        HotDealService.Category cat = HotDealService.Category.fromString(category);
        return hotDealService.fetchHotDeals(cat);
    }

    @GetMapping("/search")
    public List<HotDealService.DealInfo> searchDeals(
            @RequestParam String keyword,
            @RequestParam(required = false, defaultValue = "ALL") String category
    ) {
        // String을 Category로 변환 후 직접 호출 (캐시 작동을 위해)
        HotDealService.Category cat = HotDealService.Category.fromString(category);
        return hotDealService.fetchHotDeals(cat).stream()
                .filter(deal -> deal.getTitle().toLowerCase().contains(keyword.toLowerCase()))
                .limit(10)
                .toList();
    }

    @GetMapping("/categories")
    public List<String> getCategories() {
        return java.util.Arrays.stream(HotDealService.Category.values())
                .map(c -> c.name() + " (" + c.getDisplayName() + ")")
                .toList();
    }
}
