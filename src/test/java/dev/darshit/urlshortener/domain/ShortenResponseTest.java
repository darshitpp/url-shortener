package dev.darshit.urlshortener.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ShortenResponseTest {

    @Test
    void prefixesDomainWhenShortUrlPresent() {
        assertEquals("https://just.darshit.dev/abc",
                new ShortenResponse.Builder().withDomain("https://just.darshit.dev")
                        .withShortUrl("abc", 7).build().getShortUrl());
        assertEquals("https://just.darshit.dev/abc",
                new ShortenResponse.Builder().withDomain("https://just.darshit.dev/")
                        .withShortUrl("abc", 7).build().getShortUrl());
    }

    @Test
    void takenPathStaysNullEvenWithDomain() {
        assertNull(new ShortenResponse.Builder().withDomain("https://just.darshit.dev")
                .withShortUrl(null, 7).build().getShortUrl());
    }
}
