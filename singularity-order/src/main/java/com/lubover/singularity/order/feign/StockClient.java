package com.lubover.singularity.order.feign;

import java.util.Map;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "singularity-stock")
public interface StockClient {

    @PostMapping("/api/stock/internal/deduct-for-order")
    Map<String, Object> deductForOrder(@RequestBody Map<String, Object> body);
}
