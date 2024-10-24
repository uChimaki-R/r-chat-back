package com.r.chat.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {
    // 获取配置的redis服务端地址
    @Value("${spring.redis.host}")
    private String redisHost;
    @Value("${spring.redis.port}")
    private int redisPort;
    @Value("${spring.redis.password}")
    private String redisPassword;

    /**
     * 集群开发需要使用redis服务器进行消息的广播，在这里注册RedissonClient，连接redis服务器
     */
    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient() {
        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://" + redisHost + ":" + redisPort)
                .setPassword(redisPassword);  // 注意要传密码否则报错空指针
        return Redisson.create(config);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        // 使用jackson来进行值序列化
        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(Object.class);
        // 配置信息
        ObjectMapper om = new ObjectMapper();
        // 所有属性可见
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        // 设置在反序列化时遇到未知属性不抛出异常
        om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // 激活默认的类型解析，允许Jackson处理非最终类的对象。
        om.activateDefaultTyping(om.getPolymorphicTypeValidator(), ObjectMapper.DefaultTyping.NON_FINAL);
        jackson2JsonRedisSerializer.setObjectMapper(om);

        // 键用string序列化
        RedisSerializer<?> stringSerializer = new StringRedisSerializer();

        // 配置redisTemplate
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        // 连接工厂，用于连接redis服务器
        redisTemplate.setConnectionFactory(factory);
        redisTemplate.setKeySerializer(stringSerializer);  // key序列化
        redisTemplate.setValueSerializer(jackson2JsonRedisSerializer);  // value序列化
        redisTemplate.setHashKeySerializer(stringSerializer);  // Hash key序列化
        redisTemplate.setHashValueSerializer(jackson2JsonRedisSerializer);  // Hash value序列化
        // 调用afterPropertiesSet来初始化RedisTemplate，确保所有的属性都已经被设置
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }
}
