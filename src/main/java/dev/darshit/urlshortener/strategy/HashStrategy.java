package dev.darshit.urlshortener.strategy;

import dev.darshit.urlshortener.domain.ShortenOptions;
import dev.darshit.urlshortener.redis.RedisUrlOperations;
import dev.darshit.urlshortener.utils.HashUtils;
import dev.darshit.urlshortener.utils.StringUtils;
import dev.darshit.urlshortener.utils.UUIDUtils;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service("hash")
public class HashStrategy implements ShorteningStrategy {

    private final RedisUrlOperations redisUrlOperations;

    public HashStrategy(RedisUrlOperations redisUrlOperations) {
        this.redisUrlOperations = redisUrlOperations;
    }


    @Override
    public Optional<String> shorten(String originalUrl, ShortenOptions options) {
        String path;
        Optional<Boolean> valueStored;
        do {
            path = getHashedValue(originalUrl, options.isLiberalHash());
            if (path.length() > options.getPathSize()) {
                path = path.substring(0, options.getPathSize());
            }
            valueStored = redisUrlOperations.putIfAbsent(path, originalUrl, options.getTtlInDays());
        } while (valueStored.isEmpty() || !valueStored.get());
        return Optional.of(path);
    }

    public String getHashedValue(String originalUrl, boolean liberalHash) {
        String numberString = getUniqueNumberString(originalUrl);
        return HashUtils.generateHash(numberString, liberalHash);
    }

    private String getUniqueNumberString(String originalUrl) {
        int uuidHashCode = UUIDUtils.uuid().hashCode();
        long counter = redisUrlOperations.incrementCounterForShortUrl();
        long currentTime = System.currentTimeMillis();
        long originalUrlHashCode = originalUrl.hashCode();

        String uniqueString = String.valueOf(uuidHashCode) + currentTime + counter + originalUrlHashCode;
        return StringUtils.replace(uniqueString, "-", "");
    }
}
