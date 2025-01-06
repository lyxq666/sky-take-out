package com.sky.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionCommands;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@Slf4j
public class RedisConfiguration {

    @Bean//将方法返回的对象注册为 Spring 容器的 Bean。供其他组件注入和使用。
    public RedisTemplate redisTemplate(RedisConnectionFactory redisConnectionFactory)
    {
        log.info("开始创建redis模板对象。。。");
        RedisTemplate redisTemplate = new RedisTemplate();
        //设置redis的连接工厂对象
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        //设置Redis key的序列号器
//        redisTemplate.setKeySerializer(new StringRedisSerializer());
        return redisTemplate;
    }
}
