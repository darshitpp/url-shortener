package dev.darshit.urlshortener.strategy;

import dev.darshit.urlshortener.domain.ShortenOptions;
import dev.darshit.urlshortener.redis.RedisUrlOperations;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service("custom")
public class CustomStrategy implements ShorteningStrategy {

    private final RedisUrlOperations redisUrlOperations;

    public CustomStrategy(RedisUrlOperations redisUrlOperations) {
        this.redisUrlOperations = redisUrlOperations;
    }


    @Override
    public Optional<String> shorten(String originalUrl, ShortenOptions options) {
        Optional<Boolean> valueStored = redisUrlOperations.putIfAbsent(options.getCustomPath(), originalUrl, options.getTtlInDays());
        String storedPath = valueStored.isPresent() && valueStored.get() ? options.getCustomPath() : null;
        return Optional.ofNullable(storedPath);
    }
}
