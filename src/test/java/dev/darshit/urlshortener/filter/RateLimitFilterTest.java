package dev.darshit.urlshortener.filter;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RateLimitFilterTest {

    private static final Clock FIXED_CLOCK =
            Clock.fixed(Instant.parse("2026-09-14T00:00:00Z"), ZoneOffset.UTC);

    @Test
    void rejectsRequestThirtyOneAndDocumentsRetry() throws Exception {
        RateLimitFilter filter = new RateLimitFilter(true, 30, 10_000, FIXED_CLOCK);

        for (int request = 1; request <= 30; request++) {
            assertEquals(200, perform(filter, request("203.0.113.10", null)).getStatus());
        }

        MockHttpServletResponse rejected =
                perform(filter, request("203.0.113.10", null));
        assertEquals(429, rejected.getStatus());
        assertEquals("60", rejected.getHeader("Retry-After"));
        assertTrue(rejected.getContentType().startsWith("application/json"));
        assertTrue(rejected.getContentAsString()
                .contains("\"error\":\"Too many requests\""));
    }

    @Test
    void tracksDistinctClientAddressesIndependently() throws Exception {
        RateLimitFilter filter = new RateLimitFilter(true, 1, 10_000, FIXED_CLOCK);

        assertEquals(200, perform(filter, request("203.0.113.10", null)).getStatus());
        assertEquals(429, perform(filter, request("203.0.113.10", null)).getStatus());
        assertEquals(200, perform(filter, request("203.0.113.11", null)).getStatus());
    }

    @Test
    void ignoresForwardedForButUsesRealIp() throws Exception {
        RateLimitFilter filter = new RateLimitFilter(true, 1, 10_000, FIXED_CLOCK);

        MockHttpServletRequest first = request("203.0.113.10", "198.51.100.1");
        first.addHeader("X-Forwarded-For", "192.0.2.1");
        assertEquals(200, perform(filter, first).getStatus());

        MockHttpServletRequest second = request("203.0.113.10", "198.51.100.1");
        second.addHeader("X-Forwarded-For", "192.0.2.2");
        assertEquals(429, perform(filter, second).getStatus());

        assertEquals(200,
                perform(filter, request("203.0.113.10", "198.51.100.2")).getStatus());
    }

    @Test
    void treatsNormalizedPathsAsSameEndpoint() throws Exception {
        for (String path : java.util.List.of(
                "/shorten/", "/shorten;untrusted", "//shorten", "/shorten//", "/%73horten")) {
            RateLimitFilter filter = new RateLimitFilter(true, 1, 10_000, FIXED_CLOCK);

            assertEquals(200,
                    perform(filter, request(path, "203.0.113.10", null)).getStatus());
            assertEquals(429,
                    perform(filter, request(path, "203.0.113.10", null)).getStatus());
        }
    }

    @Test
    void rejectsTenThousandFirstIdentityWithoutGrowingPastCapacity() throws Exception {
        RateLimitFilter filter = new RateLimitFilter(true, 1, 10_000, FIXED_CLOCK);

        for (int identity = 0; identity < 10_000; identity++) {
            MockHttpServletRequest request =
                    request("203.0." + (identity / 256) + "." + (identity % 256), null);
            assertEquals(200, perform(filter, request).getStatus());
        }

        assertEquals(429,
                perform(filter, request("198.51.100.200", null)).getStatus());
    }

    @Test
    void disabledFilterBypassesTracking() throws Exception {
        RateLimitFilter filter = new RateLimitFilter(false, 1, 1, FIXED_CLOCK);
        assertEquals(200, perform(filter, request("203.0.113.10", null)).getStatus());
        assertEquals(200, perform(filter, request("203.0.113.10", null)).getStatus());
        assertEquals(200, perform(filter, request("203.0.113.11", null)).getStatus());
    }

    private MockHttpServletResponse perform(
            RateLimitFilter filter,
            MockHttpServletRequest request) throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, new MockFilterChain());
        return response;
    }

    private MockHttpServletRequest request(String remoteAddress, String realIp) {
        return request("/shorten", remoteAddress, realIp);
    }

    private MockHttpServletRequest request(
            String path,
            String remoteAddress,
            String realIp) {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", path);
        request.setRemoteAddr(remoteAddress);
        if (realIp != null) {
            request.addHeader("X-Real-IP", realIp);
        }
        return request;
    }
}
