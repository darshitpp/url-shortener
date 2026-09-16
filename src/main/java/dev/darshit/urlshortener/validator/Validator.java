package dev.darshit.urlshortener.validator;

import org.apache.commons.validator.routines.UrlValidator;

import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

public final class Validator {

    private static final UrlValidator URL_VALIDATOR = new UrlValidator(new String[]{"http", "https"});

    private static final Pattern INVALID_STRING_VALIDATOR = Pattern.compile("[\\w\\d\\-_]+");

    public static final int MAX_URL_LENGTH = 2048;
    public static final int MAX_CUSTOM_PATH_LENGTH = 64;
    public static final int MAX_DOMAIN_LENGTH = 253;
    public static final int MAX_STRATEGY_LENGTH = 32;

    private static final Set<String> RESERVED_PATHS = Set.of(
            "ui", "shorten", "resolve", "update", "delete", "links",
            "default_domain", "short_link_counter",
            "v2", "swagger-resources", "swagger-ui"
    );

    public static boolean validateUrl(String url) {
        return url != null
                && url.length() <= MAX_URL_LENGTH
                && URL_VALIDATOR.isValid(url);
    }

    public static boolean validateCustomPath(String customPath) {
        return customPath != null
                && customPath.length() <= MAX_CUSTOM_PATH_LENGTH
                && INVALID_STRING_VALIDATOR.matcher(customPath).matches()
                && !RESERVED_PATHS.contains(customPath.toLowerCase(Locale.ROOT));
    }

    public static boolean validateDomain(String domain) {
        return domain != null
                && domain.length() <= MAX_DOMAIN_LENGTH
                && URL_VALIDATOR.isValid(domain);
    }

    public static boolean validateStrategy(String strategy) {
        return strategy == null || strategy.length() <= MAX_STRATEGY_LENGTH;
    }
}
