package dev.darshit.urlshortener.strategy;

import dev.darshit.urlshortener.configuration.LettuceTestConfiguration;
import dev.darshit.urlshortener.domain.ShortenOptions;
import dev.darshit.urlshortener.fetch.Fetcher;
import dev.darshit.urlshortener.redis.RedisUrlOperations;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.util.Optional;


@SpringBootTest
@Import(LettuceTestConfiguration.class)
class WordStrategyTest {

    @Autowired
    private WordStrategy wordStrategy;

    @Autowired
    private Fetcher fetcher;

    @Autowired
    private RedisUrlOperations redisUrlOperations;
    public static final String URL_TO_SHORTEN = "https://google.com";

    @Test
    @DisplayName("Shortens a URL using Word Strategy")
    void shorten_with_word_strategy() {
        ShortenOptions option = new ShortenOptions.Builder()
                .withTtlInDays(1)
                .build();
        Optional<String> shorten = wordStrategy.shorten(URL_TO_SHORTEN, option);
        Assertions.assertTrue(shorten.isPresent());
        redisUrlOperations.delete(shorten.get());
    }


    @Test
    @DisplayName("Fetches original Url")
    void fetch_original_url_after_shorten() {
        ShortenOptions options = new ShortenOptions.Builder()
                .withTtlInDays(1)
                .build();

        Optional<String> shorten = wordStrategy.shorten(URL_TO_SHORTEN, options);
        Optional<String> originalUrl = shorten.isPresent() ? fetcher.getOriginalUrl(shorten.get()) : Optional.empty();

        Assertions.assertTrue(originalUrl.isPresent());
        Assertions.assertEquals(URL_TO_SHORTEN, originalUrl.get());
        redisUrlOperations.delete(shorten.get());
    }
}