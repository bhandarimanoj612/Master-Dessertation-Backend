package com.example.sajilo_tayaar.controller;

import com.example.sajilo_tayaar.dto.response.ShopCustomerSummaryResponse;
import com.example.sajilo_tayaar.services.CustomerInsightService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerInsightService customerInsightService;

    @GetMapping("/shop/{shopId}")
    public List<ShopCustomerSummaryResponse> listShopCustomers(@PathVariable Long shopId) {
        return customerInsightService.getCustomersForShop(shopId);
    }
}
