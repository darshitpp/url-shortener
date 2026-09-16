package dev.darshit.urlshortener.controller;

import dev.darshit.urlshortener.fetch.Fetcher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import java.net.IDN;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Locale;
import java.util.Optional;

@Controller
public class ResolveController {

    private final Fetcher fetcher;

    public ResolveController(Fetcher fetcher) {
        this.fetcher = fetcher;
    }

    @GetMapping("/{shortPath}")
    public ModelAndView resolve(
            @PathVariable String shortPath,
            HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-store");
        Optional<URI> destination = fetcher.getOriginalUrl(shortPath)
                .flatMap(this::toHttpUri);
        ModelAndView modelAndView = new ModelAndView();
        if (destination.isEmpty()) {
            response.setStatus(HttpStatus.NOT_FOUND.value());
            modelAndView.setViewName("not-found");
            return modelAndView;
        }
        URI uri = destination.get();
        modelAndView.setViewName("redirect-warning");
        modelAndView.addObject("shortPath", shortPath);
        modelAndView.addObject("destination", uri.toASCIIString());
        modelAndView.addObject("destinationScheme", uri.getScheme());
        modelAndView.addObject("destinationHost", uri.getHost());
        modelAndView.addObject("insecure",
                !"https".equalsIgnoreCase(uri.getScheme()));
        return modelAndView;
    }

    @PostMapping("/resolve/{shortPath}")
    public ResponseEntity<Void> continueResolving(@PathVariable String shortPath) {
        Optional<URI> destination = fetcher.getOriginalUrl(shortPath)
                .flatMap(this::toHttpUri);
        if (destination.isPresent()) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(destination.get())
                    .build();
        }
        return ResponseEntity.notFound().build();
    }

    private Optional<URI> toHttpUri(String value) {
        if (value == null) {
            return Optional.empty();
        }
        try {
            URL parsed = new URL(value);
            String scheme = parsed.getProtocol().toLowerCase(Locale.ROOT);
            if (!"http".equals(scheme) && !"https".equals(scheme)) {
                return Optional.empty();
            }
            if (parsed.getUserInfo() != null) {
                return Optional.empty();
            }
            String host = canonicalHost(parsed.getHost());
            if (host == null) {
                return Optional.empty();
            }
            String authority = host
                    + (parsed.getPort() < 0 ? "" : ":" + parsed.getPort());
            String destination = scheme + "://" + authority + parsed.getFile()
                    + (parsed.getRef() == null ? "" : "#" + parsed.getRef());
            return Optional.of(URI.create(destination));
        } catch (MalformedURLException | IllegalArgumentException exception) {
            return Optional.empty();
        }
    }

    private String canonicalHost(String host) {
        if (host == null) {
            return null;
        }
        if (host.startsWith("[") && host.endsWith("]")) {
            return host;
        }
        try {
            return IDN.toASCII(host, IDN.USE_STD3_ASCII_RULES);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }
}
