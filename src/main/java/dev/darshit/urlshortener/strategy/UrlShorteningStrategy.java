package dev.darshit.urlshortener.strategy;

import dev.darshit.urlshortener.ShortenOptions;

import java.util.Optional;

public interface UrlShorteningStrategy {

    Optional<String> shorten(String originalUrl, ShortenOptions options);

}
