package dev.darshit.urlshortener;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.darshit.urlshortener.utils.StringUtils;

import java.util.Objects;

public class ShortenOptions {

    @JsonProperty("customPath")
    private String customPath;

    @JsonProperty("urlSize")
    private int pathSize;

    @JsonProperty("ttl")
    private int ttlInDays;

    @JsonProperty("liberalHash")
    private boolean liberalHash;

    public String getCustomPath() {
        return customPath;
    }

    public int getPathSize() {
        return pathSize;
    }

    public int getTtlInDays() {
        return ttlInDays;
    }

    public boolean isLiberalHash() {
        return liberalHash;
    }

    public static final class Builder {
        private String customPath;
        private int pathSize;
        private int ttlInDays;
        private boolean liberalHash;

        public Builder() {
            this.pathSize = 8;
            this.ttlInDays = 7;
            this.liberalHash = false;
        }

        public Builder withCustomPath(String customPath) throws IllegalArgumentException {
            if (StringUtils.isEmpty(customPath)) {
                throw new IllegalArgumentException("CustomPath cannot be empty");
            }
            this.customPath = customPath;
            return this;
        }


        public Builder withPathSize(int pathSize) throws IllegalArgumentException {
            if (pathSize < 1 || pathSize > 18) {
                throw new IllegalArgumentException("Size must be between 1 and 18 characters");
            }
            this.pathSize = pathSize;
            return this;
        }

        public Builder withTtlInDays(int ttl) throws IllegalArgumentException {
            if (ttl < 1 || ttl > 30) {
                throw new IllegalArgumentException("TTL must be between 1 and 30 days");
            }
            this.ttlInDays = ttl;
            return this;
        }

        public Builder withLiberalHash(boolean liberalHash) {
            this.liberalHash = liberalHash;
            return this;
        }

        public ShortenOptions build() {
            ShortenOptions shortenOptions = new ShortenOptions();
            shortenOptions.customPath = getCustomPath();
            shortenOptions.pathSize = getPathSize();
            shortenOptions.ttlInDays = getTtlInDays();
            shortenOptions.liberalHash = isLiberalHash();
            return shortenOptions;
        }

        public String getCustomPath() {
            return customPath;
        }

        public int getPathSize() {
            return pathSize;
        }

        public int getTtlInDays() {
            return ttlInDays;
        }

        public boolean isLiberalHash() {
            return liberalHash;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ShortenOptions)) return false;
        ShortenOptions options = (ShortenOptions) o;
        return pathSize == options.pathSize &&
                ttlInDays == options.ttlInDays &&
                liberalHash == options.liberalHash &&
                Objects.equals(customPath, options.customPath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(customPath, pathSize, ttlInDays, liberalHash);
    }

    @Override
    public String toString() {
        return "ShortenOptions{" +
                "customPath='" + customPath + '\'' +
                ", pathSize=" + pathSize +
                ", ttlInDays=" + ttlInDays +
                ", liberalHash=" + liberalHash +
                '}';
    }
}
