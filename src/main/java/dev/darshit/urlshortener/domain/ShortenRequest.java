package dev.darshit.urlshortener.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import dev.darshit.urlshortener.validator.Validator;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;

public class ShortenRequest {

    @ApiModelProperty(notes = "The URL to be shortened", required = true)
    private final String url;

    @ApiModelProperty(notes = "URL shortening strategy", allowableValues = "custom, hash, word, wordHash")
    private final String strategy;

    @ApiModelProperty(notes = "URL shortening options")
    private final ShortenOptions options;


    @JsonCreator
    public ShortenRequest(@JsonProperty("url") String url, @JsonProperty("strategy") String strategy, @JsonProperty("options") ShortenOptions options) {
        if (!Validator.validateUrl(url)) {
            throw new IllegalArgumentException("Please pass a valid URL");
        }
        this.url = url;
        this.options = Objects.requireNonNullElseGet(options, () -> new ShortenOptions.Builder().build());
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
