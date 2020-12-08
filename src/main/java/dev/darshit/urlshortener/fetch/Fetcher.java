package dev.darshit.urlshortener.fetch;

import dev.darshit.urlshortener.controller.ShortenController;
import dev.darshit.urlshortener.redis.RedisUrlOperations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class Fetcher {

    private static final Logger logger = LoggerFactory.getLogger(Fetcher.class);

    private final RedisUrlOperations redisUrlOperations;

    public Fetcher(RedisUrlOperations redisUrlOperations) {
        this.redisUrlOperations = redisUrlOperations;
    }

    public Optional<String> getOriginalUrl(String urlPath) {
        return redisUrlOperations.get(urlPath);
    }
}
