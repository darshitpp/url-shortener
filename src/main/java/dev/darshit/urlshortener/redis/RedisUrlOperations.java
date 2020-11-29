package dev.darshit.urlshortener.redis;

import dev.darshit.urlshortener.configuration.RedisSerializationBuilder;
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

    public void flushAll() {
        Objects.requireNonNull(redisTemplate.getConnectionFactory())
                .getConnection().flushAll();
    }
}
