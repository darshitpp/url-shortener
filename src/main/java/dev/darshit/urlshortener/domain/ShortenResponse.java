package dev.darshit.urlshortener.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class ShortenResponse {

    @JsonProperty("shortUrl")
    private String shortUrl;

    @JsonProperty("error")
    private String error;

    @JsonProperty("ttl")
    private Integer ttlInDays;

    public String getShortUrl() {
        return shortUrl;
    }

    public void setShortUrl(String shortUrl) {
        this.shortUrl = shortUrl;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public Integer getTtlInDays() {
        return ttlInDays;
    }

    public static final class Builder {
        private String shortUrl;
        private String error;
        private Integer ttlInDays;

        public Builder withShortUrl(String shortUrl, Integer ttlInDays) {
            this.shortUrl = shortUrl;
            this.ttlInDays = ttlInDays;
            return this;
        }

        public Builder withError(String error) {
            this.error = error;
            return this;
        }

        public ShortenResponse build() {
            ShortenResponse shortenResponse = new ShortenResponse();
            shortenResponse.shortUrl = getShortUrl();
            shortenResponse.error = getError();
            shortenResponse.ttlInDays = getTtlInDays();
            return shortenResponse;
        }

        public String getShortUrl() {
            return shortUrl;
        }

        public String getError() {
            return error;
        }

        public Integer getTtlInDays() {
            return ttlInDays;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ShortenResponse)) return false;
        ShortenResponse that = (ShortenResponse) o;
        return Objects.equals(shortUrl, that.shortUrl) &&
                Objects.equals(error, that.error) &&
                Objects.equals(ttlInDays, that.ttlInDays);
    }

    @Override
    public int hashCode() {
        return Objects.hash(shortUrl, error, ttlInDays);
    }

    @Override
    public String toString() {
        return "ShortenResponse{" +
                "shortUrl='" + shortUrl + '\'' +
                ", error='" + error + '\'' +
                ", ttlInDays=" + ttlInDays +
                '}';
    }
}
