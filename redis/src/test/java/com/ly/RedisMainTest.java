package com.ly;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ly.domain.User;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

/**
 * @Description
 * @Created by fun4ai
 * @Date 2020/11/18 18:51
 */
@SpringBootTest
public class RedisMainTest {
    @Resource
    private RedisTemplate redisTemplate;

    @Test
    public void test1() {
        redisTemplate.opsForValue().set("haha", "aaaaa");
        System.out.println(redisTemplate.opsForValue().get("haha"));
    }

    @Test
    public void test2() throws JsonProcessingException {
        User user = new User("liu", 18);
        //String s = new ObjectMapper().writeValueAsString(user);
        redisTemplate.opsForValue().set("user", user);
        System.out.println(redisTemplate.opsForValue().get("user"));
    }
}
