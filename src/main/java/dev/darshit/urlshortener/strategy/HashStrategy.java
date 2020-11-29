package dev.darshit.urlshortener.strategy;

import dev.darshit.urlshortener.ShortenOptions;
import dev.darshit.urlshortener.redis.RedisUrlOperations;
import dev.darshit.urlshortener.utils.HashUtils;
import dev.darshit.urlshortener.utils.StringUtils;
import dev.darshit.urlshortener.utils.UUIDUtils;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Service("hash")
public class HashStrategy implements UrlShorteningStrategy {

    private final RedisUrlOperations redisUrlOperations;
    private static final AtomicLong atomicLong = new AtomicLong();

    public HashStrategy(RedisUrlOperations redisUrlOperations) {
        this.redisUrlOperations = redisUrlOperations;
    }


    @Override
    public Optional<String> shorten(String originalUrl, ShortenOptions options) {
        String path;
        Optional<Boolean> valueStored;
        do {
            String numberString = getUniqueNumberString(originalUrl);
            path = HashUtils.generateHash(numberString, options.isLiberalHash()).substring(0, options.getPathSize());
            valueStored = redisUrlOperations.putIfAbsent(path, originalUrl, options.getTtlInDays());
        } while (valueStored.isEmpty() || !valueStored.get());
        return Optional.of(path);
    }

    public static String getUniqueNumberString(String originalUrl) {
        int uuidHashCode = UUIDUtils.uuid().hashCode();
        long atomicLongValue = atomicLong.incrementAndGet();
        long currentTime = System.currentTimeMillis();
        long originalUrlHashCode = originalUrl.hashCode();

        String uniqueString = String.valueOf(uuidHashCode) + currentTime + atomicLongValue + originalUrlHashCode;
        return StringUtils.replace(uniqueString, "-", "");
    }
}
