package dev.darshit.urlshortener.strategy;

import dev.darshit.urlshortener.domain.ShortenOptions;
import dev.darshit.urlshortener.redis.RedisUrlOperations;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service("wordHashCombo")
public class WordHashComboStrategy implements ShorteningStrategy {

    private final RedisUrlOperations redisUrlOperations;
    private final WordStrategy wordStrategy;
    private final HashStrategy hashStrategy;

    public WordHashComboStrategy(RedisUrlOperations redisUrlOperations, WordStrategy wordStrategy, HashStrategy hashStrategy) {
        this.redisUrlOperations = redisUrlOperations;
        this.wordStrategy = wordStrategy;
        this.hashStrategy = hashStrategy;
    }


    @Override
    public Optional<String> shorten(String originalUrl, ShortenOptions options) {
        String path;
        Optional<Boolean> valueStored;
        do {
            String word = wordStrategy.fetchRandomWords(1).get(0);
            String hash = hashStrategy.getHashedValue(originalUrl, options.isLiberalHash());
            path = hash.length() < 4 ? word + "-" + hash : word + "-" + hash.substring(0, 4);
            valueStored = redisUrlOperations.putIfAbsent(path, originalUrl, options.getTtlInDays());
        } while (valueStored.isEmpty() || !valueStored.get());
        return Optional.of(path);
    }

}
