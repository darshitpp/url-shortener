package dev.darshit.urlshortener.controller;

import dev.darshit.urlshortener.fetch.Fetcher;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Optional;

@Controller
public class Resolver {

    private final Fetcher fetcher;

    public Resolver(Fetcher fetcher) {
        this.fetcher = fetcher;
    }

    @GetMapping("/{shortPath}")
    public ResponseEntity<String> resolve(@PathVariable String shortPath) {
        Optional<String> originalUrl = fetcher.getOriginalUrl(shortPath);
        return originalUrl
                .map(url -> {
                    HttpHeaders headers = new HttpHeaders();
                    headers.add("Location", url);
                    return new ResponseEntity<>("", headers, HttpStatus.FOUND);
                })
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
}
