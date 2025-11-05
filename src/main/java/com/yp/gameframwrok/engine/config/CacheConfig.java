package com.yp.gameframwrok.engine.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.yp.gameframwrok.engine.config.FastJson2JsonRedisSerializer;
import com.yp.gameframwrok.model.cache.UserCache;
import lombok.extern.log4j.Log4j2;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.concurrent.TimeUnit;

/**
 * @author yyp
 */
@Log4j2
@Configuration
//@EnableCaching
public class CacheConfig {

//    @Bean("caffeineCache")
//    @Primary
//    public CacheManager caffeineCacheManager() {
//        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
//        cacheManager.setCaffeine(Caffeine.newBuilder()
//                // 设置最后一次写入或访问后经过固定时间过期
//                .expireAfterWrite(2, TimeUnit.HOURS)
//                // 初始的缓存空间大小
//                .initialCapacity(100)
//                // 缓存的最大条数
//                .maximumSize(2000)
//                .removalListener((key, value, cause) -> {
//                    log.info("caffeine cache key: {} value: {} cause: {}", key, value, cause);
//                }));
//        return cacheManager;
//    }

//    @Bean("redisCache")
//    public RedisCacheManager redisCacheManager(RedisConnectionFactory connectionFactory){
//        RedisCacheConfiguration config =
//                RedisCacheConfiguration.defaultCacheConfig()
//                                        .disableCachingNullValues(); // 不缓存空值
//        return RedisCacheManager.builder(connectionFactory).cacheDefaults(config).build();
//    }

    @Bean
    public StringRedisSerializer stringRedisSerializer() {
        return new StringRedisSerializer();
    }

    @Bean
    public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<Object, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        FastJson2JsonRedisSerializer<Object> serializer = new FastJson2JsonRedisSerializer<>(Object.class);

//        ObjectMapper om = new ObjectMapper();
//        // 指定要序列化的域，field,get和set,以及修饰符范围，ANY是都有包括private和public
//        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
//        // 指定序列化输入的类型，类必须是非final修饰的，final修饰的类，比如String,Integer等会跑出异常
//        om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
//        serializer.setObjectMapper(om);

        // 使用StringRedisSerializer来序列化和反序列化redis的key值
        template.setKeySerializer(stringRedisSerializer());
        template.setValueSerializer(serializer);

        // Hash的key也采用StringRedisSerializer的序列化方式
        template.setHashKeySerializer(stringRedisSerializer());
        template.setHashValueSerializer(serializer);

        template.setDefaultSerializer(serializer);

        template.afterPropertiesSet();
        return template;
    }


    // 手动创建 Caffeine Cache 实例，用于更精细的控制
    @Bean(name = "userCache")
    public Cache<Integer, UserCache> userCache() {
        return Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(2, TimeUnit.HOURS) // 从配置文件中读取
                .build();
    }
}
