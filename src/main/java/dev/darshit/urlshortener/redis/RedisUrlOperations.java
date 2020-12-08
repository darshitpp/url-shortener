package dev.darshit.urlshortener.redis;

import dev.darshit.urlshortener.configuration.RedisSerializationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
public class RedisUrlOperations {

    private static final Logger logger = LoggerFactory.getLogger(RedisUrlOperations.class);

    private final RedisTemplate<String, String> redisTemplate;
    private final ValueOperations<String, String> valueOperations;

    private static final long MAX_REDIS_VALUE = 9223372036854775807L;
    private static final String SHORT_LINK_COUNTER = "SHORT_LINK_COUNTER";
    private static final String DEFAULT_DOMAIN = "DEFAULT_DOMAIN";

    public RedisUrlOperations(LettuceConnectionFactory lettuceConnectionFactory) {
        this.redisTemplate = RedisSerializationBuilder.getRedisTemplate(lettuceConnectionFactory, String.class);
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
            resetKey(SHORT_LINK_COUNTER, String.valueOf(0));
        }
        return valueOperations.increment(SHORT_LINK_COUNTER);
    }

    private Optional<List<Object>> resetKey(final String key, final String value) {
        List<Object> transaction = redisTemplate.execute(new SessionCallback<>() {
            public List<Object> execute(RedisOperations operations) throws DataAccessException {
                operations.watch(key);
                operations.multi();
                operations.opsForValue().set(key, value);
                return operations.exec();
            }
        });
        return Optional.ofNullable(transaction);
    }

    public void flushAll() {
        Objects.requireNonNull(redisTemplate.getConnectionFactory())
                .getConnection().flushAll();
    }

    public void delete(String key) {
        redisTemplate.delete(key);
    }

    public void putDefaultDomain(String defaultDomain) {
        valueOperations.set(DEFAULT_DOMAIN, defaultDomain);
    }

    public void deleteDefaultDomain() {
        delete(DEFAULT_DOMAIN);
    }

    public Optional<String> getDefaultDomain() {
        return Optional.ofNullable(valueOperations.get(DEFAULT_DOMAIN));
    }
}
