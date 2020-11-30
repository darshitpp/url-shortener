package dev.darshit.urlshortener.validator;

import org.apache.commons.validator.routines.UrlValidator;

public class Validator {

    private static final UrlValidator URL_VALIDATOR = new UrlValidator(new String[]{"http", "https"});

    public static boolean validateUrl(String url) {
        return URL_VALIDATOR.isValid(url);
    }
}
