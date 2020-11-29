package dev.darshit.urlshortener.redis;

import dev.darshit.urlshortener.configuration.RedisSerializationBuilder;
import dev.darshit.urlshortener.utils.StringUtils;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
public class RedisUrlOperations {

    private final RedisTemplate<String, String> redisTemplate;
    private final ValueOperations<String, String> valueOperations;

    private static final long MAX_REDIS_VALUE = 9223372036854775807L;
    private static final String SHORT_LINK_COUNTER = "SHORT_LINK_COUNTER";

    public RedisUrlOperations(LettuceConnectionFactory redisConnectionFactory) {
        this.redisTemplate = RedisSerializationBuilder.getRedisTemplate(redisConnectionFactory, String.class);
        this.valueOperations = redisTemplate.opsForValue();
    }

    public Optional<Boolean> putIfAbsent(String key, String value, int ttlInDays) {
        return Optional.ofNullable(
                valueOperations.setIfAbsent(key, value, ttlInDays, TimeUnit.DAYS)
        );
    }

    public Optional<String> get(String key) {
        return Optional.ofNullable(valueOperations.get(key));
    }

    public Long incrementCounterForShortUrl() {
        Optional<String> counter = get(SHORT_LINK_COUNTER);
        if (counter.isEmpty() || Long.parseLong(counter.get()) == MAX_REDIS_VALUE) {
            valueOperations.setIfAbsent(SHORT_LINK_COUNTER, String.valueOf(0));
        }
        return valueOperations.increment(SHORT_LINK_COUNTER);
    }

    public void flushAll() {
        Objects.requireNonNull(redisTemplate.getConnectionFactory())
                .getConnection().flushAll();
    }
}
