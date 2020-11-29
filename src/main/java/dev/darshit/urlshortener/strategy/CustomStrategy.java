package dev.darshit.urlshortener.strategy;

import dev.darshit.urlshortener.redis.RedisUrlOperations;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service("custom")
public class CustomStrategy implements UrlShorteningStrategy {

    private final RedisUrlOperations redisUrlOperations;

    public CustomStrategy(RedisUrlOperations redisUrlOperations) {
        this.redisUrlOperations = redisUrlOperations;
    }


    @Override
    public Optional<String> shorten(String originalUrl, String customPath, int ttlInDays) {
        Optional<Boolean> valueStored = redisUrlOperations.putIfAbsent(customPath, originalUrl, ttlInDays);
        String storedUrl = valueStored.isPresent() && valueStored.get() ? customPath : null;
        return Optional.ofNullable(storedUrl);
    }
}
