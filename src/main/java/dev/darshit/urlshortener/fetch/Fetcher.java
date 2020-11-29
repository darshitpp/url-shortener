package dev.darshit.urlshortener.fetch;

import dev.darshit.urlshortener.redis.RedisUrlOperations;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class Fetcher {

    private final RedisUrlOperations redisUrlOperations;

    public Fetcher(RedisUrlOperations redisUrlOperations) {
        this.redisUrlOperations = redisUrlOperations;
    }

    public Optional<String> getOriginalUrl(String urlPath) {
        return redisUrlOperations.get(urlPath);
    }
}
