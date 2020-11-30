package dev.darshit.urlshortener;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class ShortenResponse {

    @JsonProperty("shortUrl")
    private String shortUrl;

    @JsonProperty("error")
    private String error;


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

    public static final class Builder {
        private String shortUrl;
        private String error;

        public Builder() {
        }


        public Builder withShortUrl(String shortUrl) {
            this.shortUrl = shortUrl;
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
            return shortenResponse;
        }

        public String getShortUrl() {
            return shortUrl;
        }

        public String getError() {
            return error;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ShortenResponse)) return false;
        ShortenResponse that = (ShortenResponse) o;
        return Objects.equals(shortUrl, that.shortUrl) &&
                Objects.equals(error, that.error);
    }

    @Override
    public int hashCode() {
        return Objects.hash(shortUrl, error);
    }

    @Override
    public String toString() {
        return "ShortenResponse{" +
                "shortUrl='" + shortUrl + '\'' +
                ", error='" + error + '\'' +
                '}';
    }
}
