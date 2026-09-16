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
    private static final String URL_PREFIX = "url:";
    private static final String DEFAULT_DOMAIN_KEY = "config:default-domain";
    private static final String COUNTER_KEY = "counter:short-link";
    private static final String LEGACY_DEFAULT_DOMAIN = "DEFAULT_DOMAIN";
    private static final String LEGACY_COUNTER = "SHORT_LINK_COUNTER";

    public RedisUrlOperations(LettuceConnectionFactory lettuceConnectionFactory) {
        this.redisTemplate = RedisSerializationBuilder.getRedisTemplate(lettuceConnectionFactory, String.class);
        this.valueOperations = redisTemplate.opsForValue();
    }

    private String urlKey(String shortPath) {
        return URL_PREFIX + shortPath;
    }

    private boolean isLegacyMetadata(String key) {
        return LEGACY_DEFAULT_DOMAIN.equals(key) || LEGACY_COUNTER.equals(key);
    }

    private void migrateLegacyString(String legacyKey, String namespacedKey) {
        String value = valueOperations.get(legacyKey);
        if (value == null) {
            return;
        }
        Long ttlSeconds = redisTemplate.getExpire(legacyKey, TimeUnit.SECONDS);
        if (ttlSeconds != null && ttlSeconds > 0) {
            valueOperations.setIfAbsent(namespacedKey, value, ttlSeconds, TimeUnit.SECONDS);
        } else if (ttlSeconds == null || ttlSeconds == -1) {
            valueOperations.setIfAbsent(namespacedKey, value);
        }
    }

    public Optional<Boolean> putIfAbsent(String key, String value, int ttlInDays) {
        if (!isLegacyMetadata(key) && valueOperations.get(key) != null) {
            return Optional.of(false);
        }
        return Optional.ofNullable(
                valueOperations.setIfAbsent(urlKey(key), value, ttlInDays, TimeUnit.DAYS)
        );
    }

    public Optional<String> get(String key) {
        if (isLegacyMetadata(key)) {
            return Optional.empty();
        }
        String namespaced = valueOperations.get(urlKey(key));
        if (namespaced != null) {
            return Optional.of(namespaced);
        }
        String legacy = valueOperations.get(key);
        if (legacy == null) {
            return Optional.empty();
        }
        migrateLegacyString(key, urlKey(key));
        return Optional.of(legacy);
    }

    public Long incrementCounterForShortUrl() {
        String current = valueOperations.get(COUNTER_KEY);
        if (current == null) {
            String legacy = valueOperations.get(LEGACY_COUNTER);
            valueOperations.setIfAbsent(COUNTER_KEY, legacy == null ? "0" : legacy);
            current = valueOperations.get(COUNTER_KEY);
        }
        if (current != null && Long.parseLong(current) == MAX_REDIS_VALUE) {
            resetKey(COUNTER_KEY, "0");
        }
        return valueOperations.increment(COUNTER_KEY);
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
        redisTemplate.delete(List.of(urlKey(key), key));
    }

    public void putDefaultDomain(String defaultDomain) {
        valueOperations.set(DEFAULT_DOMAIN_KEY, defaultDomain);
    }

    public void deleteDefaultDomain() {
        redisTemplate.delete(List.of(DEFAULT_DOMAIN_KEY, LEGACY_DEFAULT_DOMAIN));
    }

    public Optional<String> getDefaultDomain() {
        String current = valueOperations.get(DEFAULT_DOMAIN_KEY);
        if (current != null) {
            return Optional.of(current);
        }
        migrateLegacyString(LEGACY_DEFAULT_DOMAIN, DEFAULT_DOMAIN_KEY);
        return Optional.ofNullable(valueOperations.get(DEFAULT_DOMAIN_KEY));
    }
}
