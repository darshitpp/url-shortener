package dev.darshit.urlshortener.strategy;

import dev.darshit.urlshortener.ShortenOptions;
import dev.darshit.urlshortener.fetch.Fetcher;
import dev.darshit.urlshortener.redis.RedisUrlOperations;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

@SpringBootTest
class HashStrategyTest {

    private final HashStrategy hashStrategy;
    private final Fetcher fetcher;
    private final RedisUrlOperations redisUrlOperations;
    public static final String URL_TO_SHORTEN = "https://google.com";

    HashStrategyTest(HashStrategy hashStrategy, Fetcher fetcher, RedisUrlOperations redisUrlOperations) {
        this.hashStrategy = hashStrategy;
        this.fetcher = fetcher;
        this.redisUrlOperations = redisUrlOperations;
    }

    @Test
    void shorten_with_normal_hash() {
        ShortenOptions option = new ShortenOptions.Builder()
                .withTtlInDays(1)
                .build();
        Optional<String> shorten = hashStrategy.shorten(URL_TO_SHORTEN, option);
        Assertions.assertTrue(shorten.isPresent());
    }

    @Test
    void shorten_with_liberal_hash() {
        ShortenOptions option = new ShortenOptions.Builder()
                .withLiberalHash(true)
                .withTtlInDays(1)
                .build();
        Optional<String> shorten = hashStrategy.shorten(URL_TO_SHORTEN, option);
        Assertions.assertTrue(shorten.isPresent());
    }

    @Test
    @DisplayName("Fetches original Url with Normal Hash")
    void fetch_original_url_after_shorten_with_normal_hash() {
        ShortenOptions options = new ShortenOptions.Builder()
                .withTtlInDays(1)
                .build();

        Optional<String> shorten = hashStrategy.shorten(URL_TO_SHORTEN, options);
        Optional<String> originalUrl = shorten.isPresent() ? fetcher.getOriginalUrl(shorten.get()) : Optional.empty();

        Assertions.assertTrue(originalUrl.isPresent());
        Assertions.assertEquals(URL_TO_SHORTEN, originalUrl.get());
    }

    @Test
    @DisplayName("Fetches original Url with Liberal Hash")
    void fetch_original_url_after_shorten_with_liberal_hash() {
        ShortenOptions options = new ShortenOptions.Builder()
                .withLiberalHash(true)
                .withTtlInDays(1)
                .build();

        Optional<String> shorten = hashStrategy.shorten(URL_TO_SHORTEN, options);
        Optional<String> originalUrl = shorten.isPresent() ? fetcher.getOriginalUrl(shorten.get()) : Optional.empty();

        Assertions.assertTrue(originalUrl.isPresent());
        Assertions.assertEquals(URL_TO_SHORTEN, originalUrl.get());
    }

    @Test
    void shorten_with_normal_hash_full_path_length() {
        ShortenOptions option = new ShortenOptions.Builder()
                .withPathSize(18)
                .withTtlInDays(1)
                .build();
        Optional<String> shorten = hashStrategy.shorten(URL_TO_SHORTEN, option);
        Assertions.assertTrue(shorten.isPresent());
    }

    @Test
    void shorten_with_liberal_hash_full_path_length() {
        ShortenOptions option = new ShortenOptions.Builder()
                .withLiberalHash(true)
                .withPathSize(18)
                .withTtlInDays(1)
                .build();
        Optional<String> shorten = hashStrategy.shorten(URL_TO_SHORTEN, option);
        Assertions.assertTrue(shorten.isPresent());
    }

    @AfterEach
    public void cleanUp() {
        redisUrlOperations.flushAll();
    }
}