package com.yp.gameframwrok.utils;

/**
 * @author yyp
 */

import java.util.Collections;
import java.util.concurrent.TimeUnit;

import lombok.extern.log4j.Log4j2;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

@Log4j2
@Component
public class RedisRateLimiter {

    private final RedisTemplate<String, Long> redisTemplate;

    private final DefaultRedisScript<Long> script;

    public RedisRateLimiter(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.script = new DefaultRedisScript<>(LUA_SCRIPT, Long.class);
    }

    private static final String LUA_SCRIPT =
            "local current_time = tonumber(redis.call('time')[1])\n" +
                    "local last_time = tonumber(redis.call('hget', KEYS[1], 'last_time')) or current_time\n" +
                    "local current_tokens = tonumber(redis.call('hget', KEYS[1], 'tokens'))\n" +
                    "local max_tokens = tonumber(ARGV[1]) - 1\n" +
                    "local interval = tonumber(ARGV[2])\n" +
                    "\n" +
                    "-- 检查 max_tokens 和 interval 是否为 nil\n" +
                    "if max_tokens == nil or interval == nil then\n" +
                    "    return redis.error_reply(\"max_tokens and interval must be provided\")\n" +
                    "end\n" +
                    "\n" +
                    "-- 初始化令牌桶\n" +
                    "if current_tokens == nil or last_time == nil then\n" +
                    "    current_tokens = max_tokens\n" +
                    "    last_time = current_time\n" +
                    "    redis.call('hset', KEYS[1], 'tokens', current_tokens, 'last_time', last_time)\n" +
                    "    redis.call('expire', KEYS[1], interval)\n" +
                    "    return current_tokens\n" +
                    "end\n" +
                    "\n" +
                    "-- 计算补充的令牌\n" +
                    "local elapsed_time = current_time - last_time\n" +
                    "local new_tokens = current_tokens + math.floor(elapsed_time * max_tokens / interval)\n" +
                    "if new_tokens > max_tokens then\n" +
                    "    new_tokens = max_tokens\n" +
                    "end\n" +
                    "\n" +
                    "-- 尝试获取令牌\n" +
                    "if new_tokens >= 1 then\n" +
                    "    current_tokens = new_tokens - 1\n" +
                    "    redis.call('hset', KEYS[1], 'tokens', current_tokens, 'last_time', current_time)\n" +
                    "    return current_tokens\n" +
                    "else\n" +
                    "    return -1\n" +
                    "end";

    /**
     * 创建时候默认减少一次
     *
     * @param key
     * @param rate
     * @param rateInterval
     * @return
     */
    public Long tryAcquire(String key, long rate, long rateInterval) {
        try {
            return redisTemplate.execute(script, Collections.singletonList(key), Long.valueOf(rate).intValue(), Long.valueOf(rateInterval).intValue());
        } catch (Exception e) {
            // 记录日志
            log.error("Failed to execute rate limiting script for key: {}", key, e);
            return -1L; // 发生异常时，默认接受
        }
    }

    public long rateLimiter(String key, int rate, int rateInterval) {
        ValueOperations<String, Long> ops = redisTemplate.opsForValue();
        Long currentRate = ops.get(key);
        if (currentRate == null) {
            ops.set(key, (long) rate - 1, rateInterval, TimeUnit.SECONDS);
            return rate;
        } else if (currentRate > 0) {
            ops.set(key, currentRate - 1, rateInterval, TimeUnit.SECONDS);
            return Long.parseLong(currentRate.toString()) - 1;
        } else {
            return -1L;
        }
    }
}