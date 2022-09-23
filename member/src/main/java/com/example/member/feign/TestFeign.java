package com.example.member.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "member", contextId = "testFeign", path = "test")
public interface TestFeign {

    @GetMapping("get")
    String getString(@RequestParam String str);
}
