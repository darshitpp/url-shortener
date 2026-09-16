package dev.darshit.urlshortener.filter;

import org.springframework.web.util.UrlPathHelper;

import javax.servlet.http.HttpServletRequest;

final class ShortenEndpointMatcher {

    private static final UrlPathHelper PATH_HELPER = new UrlPathHelper();

    private ShortenEndpointMatcher() {
    }

    static boolean matches(HttpServletRequest request) {
        String path = PATH_HELPER.getPathWithinApplication(request);
        return "/shorten".equals(path) || "/shorten/".equals(path);
    }
}
