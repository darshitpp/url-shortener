package dev.darshit.urlshortener.strategy;

import dev.darshit.urlshortener.ShortenOptions;
import dev.darshit.urlshortener.redis.RedisUrlOperations;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service("words")
public class WordStrategy implements ShorteningStrategy {

    private final List<String> wordSet;
    private final Random random = new Random();

    @Value("${number.of.words:3}")
    private int numberOfWords;

    private final RedisUrlOperations redisUrlOperations;

    public WordStrategy(@Value("classpath:${dictionary.path}") String dictionaryPath, RedisUrlOperations redisUrlOperations) throws IOException {
        this.wordSet = Files.readAllLines(Path.of(ResourceUtils.getFile(dictionaryPath).getPath()));
        this.redisUrlOperations = redisUrlOperations;
    }


    @Override
    public Optional<String> shorten(String originalUrl, ShortenOptions options) {
        String path;
        Optional<Boolean> valueStored;
        do {
            path = String.join("-", fetchRandomWords());
            valueStored = redisUrlOperations.putIfAbsent(path, originalUrl, options.getTtlInDays());
        } while (valueStored.isEmpty() || !valueStored.get());
        return Optional.of(path);
    }

    private List<String> fetchRandomWords() {
        return IntStream.range(0, numberOfWords)
                .map(i -> random.nextInt(wordSet.size()))
                .mapToObj(wordSet::get)
                .collect(Collectors.toList());
    }
}
