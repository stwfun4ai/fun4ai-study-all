package com.ly.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @Description
 * @Created by fun4ai
 * @Date 2020/11/18 15:01
 */
@RestController
@Slf4j
public class HelloController {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @GetMapping("/hello")
    public String hello(){
        Long views = stringRedisTemplate.opsForValue().increment("views");
        return "hello views="+ views;
    }

}
