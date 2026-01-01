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
        return hotDealService.fetchHotDeals(category);
    }

    @GetMapping("/search")
    public List<HotDealService.DealInfo> searchDeals(
            @RequestParam String keyword,
            @RequestParam(required = false, defaultValue = "ALL") String category
    ) {
        return hotDealService.fetchHotDeals(category).stream()
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
