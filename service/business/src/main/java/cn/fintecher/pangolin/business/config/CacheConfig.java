package cn.fintecher.pangolin.business.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * Created by qijigui on 2017/5/15.
 */

@Configuration
@EnableCaching
public class CacheConfig {
    @Autowired
    private JedisConnectionFactory jedisConnectionFactory;

    @Bean
    public RedisCacheManager cacheManager() {
        return new RedisCacheManager(redisTemplate());
    }

    @Bean
    public RedisTemplate<Object, Object> redisTemplate() {
        return new DefaultRedisTemplate(jedisConnectionFactory);
    }

}
