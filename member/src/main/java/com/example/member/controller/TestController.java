package com.example.member.controller;

import com.example.member.feign.TestFeign;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("test")
public class TestController implements TestFeign {

    @Override
    public String getString(@RequestParam String str){
        return str;
    }

}
