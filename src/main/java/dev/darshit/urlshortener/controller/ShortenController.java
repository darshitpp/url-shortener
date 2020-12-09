package dev.darshit.urlshortener.controller;

import dev.darshit.urlshortener.domain.ShortenOptions;
import dev.darshit.urlshortener.domain.ShortenRequest;
import dev.darshit.urlshortener.domain.ShortenResponse;
import dev.darshit.urlshortener.redis.RedisUrlOperations;
import dev.darshit.urlshortener.strategy.ShorteningStrategy;
import dev.darshit.urlshortener.strategy.StrategyFactory;
import dev.darshit.urlshortener.utils.JsonUtils;
import dev.darshit.urlshortener.utils.StringUtils;
import dev.darshit.urlshortener.validator.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
public class ShortenController {

    private static final Logger logger = LoggerFactory.getLogger(ShortenController.class);

    private final StrategyFactory strategyFactory;
    private final RedisUrlOperations redisUrlOperations;

    public ShortenController(StrategyFactory strategyFactory, RedisUrlOperations redisUrlOperations) {
        this.strategyFactory = strategyFactory;
        this.redisUrlOperations = redisUrlOperations;
    }

    @PostMapping("/shorten")
    public ShortenResponse shortenUrl(@RequestBody ShortenRequest shortenRequest) {
        logger.debug("Received request: {}", JsonUtils.json(shortenRequest));
        ShorteningStrategy shorteningStrategy = strategyFactory.get(shortenRequest.getStrategy());
        ShortenOptions options = shortenRequest.getOptions();
        Optional<String> shortUrl = shorteningStrategy.shorten(shortenRequest.getUrl(), options);

        String domain = options.getDomain();
        Optional<String> defaultDomain = redisUrlOperations.getDefaultDomain();
        if (StringUtils.isEmpty(options.getDomain()) && defaultDomain.isPresent()) {
            domain = defaultDomain.get();
        }

        return new ShortenResponse.Builder()
                .withDomain(domain)
                .withShortUrl(shortUrl.orElse(null), options.getTtlInDays())
                .build();
    }

    @PutMapping("/update/defaultDomain")
    public void updateDefaultDomain(@RequestParam("value") String defaultDomain) {
        if (!Validator.validateUrl(defaultDomain)) {
            throw new IllegalArgumentException("Please pass a valid domain starting with http/https");
        } else {
            redisUrlOperations.putDefaultDomain(defaultDomain);
        }
    }

    @PutMapping("/delete/defaultDomain")
    public void deleteDefaultDomain() {
        redisUrlOperations.deleteDefaultDomain();
    }
}
