package com.itheima.reggie.config;

import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig extends CachingConfigurerSupport {

    @Bean
    public RedisTemplate<Object , Object> redisTemplate(RedisConnectionFactory connectionFactory) {

        RedisTemplate<Object, Object> redisTemplate = new RedisTemplate<>();

        // 使用Jackson2JsonRedisSerializer来序列化和反序列化redis的value值
   //     redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
      //  redisTemplate.setValueSerializer(new StringRedisSerializer());
        //使用GenericJackson2JsonRedisSerializer map的value值会加 "", 值不正确
//        redisTemplate.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
    //    redisTemplate.setHashValueSerializer(new StringRedisSerializer(Object.class));

        // 使用StringRedisSerializer来序列化和反序列化redis的key
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());

        redisTemplate.setConnectionFactory(connectionFactory);
      //  redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }
}
