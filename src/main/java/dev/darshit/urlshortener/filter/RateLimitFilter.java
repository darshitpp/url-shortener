package dev.darshit.urlshortener.filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class RateLimitFilter extends OncePerRequestFilter {

    private final boolean enabled;
    private final int requestsPerMinute;
    private final int maxIdentities;
    private final Clock clock;

    // ponytail: one JVM only; replace with a shared store if the service scales horizontally.
    private final Map<String, Window> windows = new HashMap<>();

    @Autowired
    public RateLimitFilter(
            @Value("${shortener.rate-limit.enabled:true}") boolean enabled,
            @Value("${shortener.rate-limit.requests-per-minute:30}") int requestsPerMinute,
            @Value("${shortener.rate-limit.max-identities:10000}") int maxIdentities) {
        this(enabled, requestsPerMinute, maxIdentities, Clock.systemUTC());
    }

    RateLimitFilter(
            boolean enabled,
            int requestsPerMinute,
            int maxIdentities,
            Clock clock) {
        if (requestsPerMinute <= 0) {
            throw new IllegalArgumentException("requestsPerMinute must be positive");
        }
        if (maxIdentities <= 0) {
            throw new IllegalArgumentException("maxIdentities must be positive");
        }
        this.enabled = enabled;
        this.requestsPerMinute = requestsPerMinute;
        this.maxIdentities = maxIdentities;
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !("POST".equals(request.getMethod())
                && ShortenEndpointMatcher.matches(request));
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        if (enabled) {
            long currentMinute = clock.millis() / 60_000L;
            if (!allow(clientIdentity(request), currentMinute)) {
                reject(response);
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    private synchronized boolean allow(String identity, long currentMinute) {
        Window window = windows.get(identity);
        if (window == null && windows.size() >= maxIdentities) {
            windows.entrySet().removeIf(entry ->
                    entry.getValue().minute < currentMinute);
            if (windows.size() >= maxIdentities) {
                return false;
            }
        }

        window = windows.get(identity);
        if (window == null || window.minute != currentMinute) {
            window = new Window(currentMinute);
            windows.put(identity, window);
        }
        if (window.count >= requestsPerMinute) {
            return false;
        }
        window.count++;
        return true;
    }

    private String clientIdentity(HttpServletRequest request) {
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }
        String remoteAddress = request.getRemoteAddr();
        return remoteAddress == null || remoteAddress.isBlank()
                ? "unknown"
                : remoteAddress;
    }

    private void reject(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setHeader("Retry-After", "60");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write("{\"error\":\"Too many requests\"}");
    }

    private static final class Window {

        private final long minute;
        private int count;

        private Window(long minute) {
            this.minute = minute;
        }
    }
}
