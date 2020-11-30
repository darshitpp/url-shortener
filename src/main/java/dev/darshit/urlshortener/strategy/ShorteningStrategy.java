package dev.darshit.urlshortener.strategy;

import dev.darshit.urlshortener.domain.ShortenOptions;

import java.util.Optional;

public interface ShorteningStrategy {

    Optional<String> shorten(String originalUrl, ShortenOptions options);

}
