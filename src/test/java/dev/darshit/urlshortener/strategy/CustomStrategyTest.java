package dev.darshit.urlshortener.strategy;

import dev.darshit.urlshortener.ShortenOptions;
import dev.darshit.urlshortener.fetch.Fetcher;
import dev.darshit.urlshortener.redis.RedisUrlOperations;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

@SpringBootTest
class CustomStrategyTest {

    @Autowired
    private CustomStrategy customStrategy;

    @Autowired
    private Fetcher fetcher;

    @Autowired
    private RedisUrlOperations redisUrlOperations;

    public static final String URL_TO_SHORTEN = "https://google.com";
    public static final String CUSTOM_PATH = "test-test-test";

    @Test
    @DisplayName("Shortens a URL using Custom Strategy")
    void shorten_with_custom_strategy() {
        ShortenOptions options = new ShortenOptions.Builder()
                .withCustomPath(CUSTOM_PATH)
                .withTtlInDays(1)
                .build();

        Optional<String> shorten = customStrategy.shorten(URL_TO_SHORTEN, options);
        Assertions.assertTrue(shorten.isPresent());
    }


    @Test
    @DisplayName("Fetches original Url")
    void fetch_original_url_after_shorten_with_custom_strategy() {
        ShortenOptions options = new ShortenOptions.Builder()
                .withCustomPath(CUSTOM_PATH)
                .withTtlInDays(1)
                .build();

        Optional<String> shorten = customStrategy.shorten(URL_TO_SHORTEN, options);
        Optional<String> originalUrl = shorten.isPresent() ? fetcher.getOriginalUrl(shorten.get()) : Optional.empty();

        Assertions.assertTrue(originalUrl.isPresent());
        Assertions.assertEquals(URL_TO_SHORTEN, originalUrl.get());
    }

    @AfterEach
    public void cleanUp() {
        redisUrlOperations.flushAll();
    }
}