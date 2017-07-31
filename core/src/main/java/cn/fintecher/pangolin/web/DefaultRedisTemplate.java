package cn.fintecher.pangolin.web;

import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Created by ChenChang on 2017/3/30.
 */
public class DefaultRedisTemplate extends RedisTemplate<Object,Object> {
    public DefaultRedisTemplate(JedisConnectionFactory jedisConnectionFactory) {
        setConnectionFactory(jedisConnectionFactory);
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        setKeySerializer(stringRedisSerializer);
        setHashKeySerializer(stringRedisSerializer);
    }

}
