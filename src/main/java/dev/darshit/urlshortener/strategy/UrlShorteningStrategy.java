package dev.darshit.urlshortener.strategy;

import java.util.Optional;

public interface UrlShorteningStrategy {

    Optional<String> shorten(String originalUrl, String customPath, int ttlInDays);

}
