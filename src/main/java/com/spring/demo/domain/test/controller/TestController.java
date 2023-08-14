package com.spring.demo.domain.test.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api")
public class TestController {
    @GetMapping("/test")
    public String test(){
        log.info("call test() controller");
        return "test";
    }

    @GetMapping("/name-test")
    public String name(){
        log.info("call nameTest() by nothing");
        return "너의 이름은?";
    }

    @GetMapping("/name")
    public  String name(@RequestParam final String name){
        log.info("call name() by input : {}", name);
        return name;
    }
}
