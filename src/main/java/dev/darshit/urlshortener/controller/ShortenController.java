package dev.darshit.urlshortener.controller;

import dev.darshit.urlshortener.domain.ShortenOptions;
import dev.darshit.urlshortener.domain.ShortenRequest;
import dev.darshit.urlshortener.domain.ShortenResponse;
import dev.darshit.urlshortener.strategy.ShorteningStrategy;
import dev.darshit.urlshortener.strategy.StrategyFactory;
import dev.darshit.urlshortener.utils.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
public class ShortenController {

    private final StrategyFactory strategyFactory;

    @Value("${default.domain:@null}")
    private String defaultDomain;

    public ShortenController(StrategyFactory strategyFactory) {
        this.strategyFactory = strategyFactory;
    }

    @PostMapping("/shorten")
    public ShortenResponse shortenUrl(@RequestBody ShortenRequest shortenRequest) {
        ShorteningStrategy shorteningStrategy = strategyFactory.get(shortenRequest.getStrategy());
        ShortenOptions options = shortenRequest.getOptions();
        Optional<String> shortUrl = shorteningStrategy.shorten(shortenRequest.getUrl(), options);

        String domain = StringUtils.isEmpty(options.getDomain()) ? defaultDomain : options.getDomain();
        return new ShortenResponse.Builder()
                .withDomain(domain)
                .withShortUrl(shortUrl.orElse(null), options.getTtlInDays())
                .build();
    }
}
