package com.lubover.singularity.order.feign;

import java.util.Map;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "singularity-product")
public interface ProductClient {

    @GetMapping("/api/product/{productId}")
    Map<String, Object> getProduct(@PathVariable("productId") String productId);
}
