package dev.darshit.urlshortener;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class ShortenRequest {

    @JsonProperty("url")
    private final String url;

    @JsonProperty("strategy")
    private final String strategy;

    @JsonProperty("options")
    private final ShortenOptions options;


    public ShortenRequest(String url, String strategy, ShortenOptions options) {
        this.url = url;
        this.options = options;
        this.strategy = strategy;
    }

    public String getUrl() {
        return url;
    }

    public String getStrategy() {
        return strategy;
    }

    public ShortenOptions getOptions() {
        return options;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ShortenRequest)) return false;
        ShortenRequest that = (ShortenRequest) o;
        return Objects.equals(url, that.url) &&
                Objects.equals(options, that.options) &&
                Objects.equals(strategy, that.strategy);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, options, strategy);
    }

    @Override
    public String toString() {
        return "ShortenRequest{" +
                "url='" + url + '\'' +
                ", strategy='" + strategy + '\'' +
                ", options=" + options +
                '}';
    }
}
