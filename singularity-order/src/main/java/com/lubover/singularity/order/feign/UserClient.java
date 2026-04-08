package com.lubover.singularity.order.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@FeignClient(name = "singularity-user")
public interface UserClient {

    @GetMapping("/api/user/{id}")
    Map<String, Object> getUserById(@PathVariable("id") Long id);
}
