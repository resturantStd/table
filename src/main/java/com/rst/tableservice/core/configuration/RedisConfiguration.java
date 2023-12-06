package com.rst.tableservice.core.configuration;

import io.lettuce.core.api.StatefulConnection;
import io.lettuce.core.dynamic.RedisCommandFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

//@Configuration
public class RedisConfiguration {

/*    @Bean
    public RedisCommandFactory redisCommandFactory(StatefulConnection<String, String> connection) {
        return new RedisCommandFactory(connection);
    }
    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisCommandFactory redisConnectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        return template;
    }*/


}
