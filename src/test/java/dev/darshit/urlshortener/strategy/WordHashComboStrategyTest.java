package dev.darshit.urlshortener.strategy;

import dev.darshit.urlshortener.configuration.LettuceTestConfiguration;
import dev.darshit.urlshortener.domain.ShortenOptions;
import dev.darshit.urlshortener.fetch.Fetcher;
import dev.darshit.urlshortener.redis.RedisUrlOperations;
import dev.darshit.urlshortener.utils.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Optional;

@SpringBootTest
@Import(LettuceTestConfiguration.class)
class WordHashComboStrategyTest {

    @Autowired
    private WordHashComboStrategy wordHashComboStrategy;

    @Autowired
    private Fetcher fetcher;

    @Autowired
    private RedisUrlOperations redisUrlOperations;
    public static final String URL_TO_SHORTEN = "https://google.com";

    @Test
    @DisplayName("Shortens a URL using WordHashCombo Strategy")
    void shorten_with_word_hash_combo_strategy() {
        ShortenOptions option = new ShortenOptions.Builder()
                .withTtlInDays(1)
                .build();
        Optional<String> shorten = wordHashComboStrategy.shorten(URL_TO_SHORTEN, option);
        Assertions.assertTrue(shorten.isPresent());
        List<String> splitLink = StringUtils.split(shorten.get(), "-");
        Assertions.assertFalse(CollectionUtils.isEmpty(splitLink));
        Assertions.assertTrue(splitLink.size() <= 2);
        redisUrlOperations.delete(shorten.get());
    }


    @Test
    @DisplayName("Fetches original Url")
    void fetch_original_url_after_shorten() {
        ShortenOptions options = new ShortenOptions.Builder()
                .withTtlInDays(1)
                .build();

        Optional<String> shorten = wordHashComboStrategy.shorten(URL_TO_SHORTEN, options);
        Optional<String> originalUrl = shorten.isPresent() ? fetcher.getOriginalUrl(shorten.get()) : Optional.empty();

        Assertions.assertTrue(originalUrl.isPresent());
        Assertions.assertEquals(URL_TO_SHORTEN, originalUrl.get());
        redisUrlOperations.delete(shorten.get());
    }
}