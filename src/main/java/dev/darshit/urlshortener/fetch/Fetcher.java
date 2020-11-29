package dev.darshit.urlshortener.fetch;

import dev.darshit.urlshortener.configuration.RedisSerializationBuilder;
import dev.darshit.urlshortener.redis.RedisUrlOperations;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
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
