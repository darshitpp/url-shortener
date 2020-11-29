package dev.darshit.urlshortener.utils;

import java.util.UUID;
import java.util.regex.Pattern;

public final class UUIDUtils {
    
    private static final Pattern pattern = Pattern.compile("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
    
    public static boolean isValidUuid(final String uuid) {
        return !StringUtils.isEmpty(uuid) && uuid.trim().length() == 36 && pattern.matcher(uuid).matches();
    }
    
    public static String uuid() {
        return UUID.randomUUID().toString();
    }
    
    public static String uuid32() {
        return StringUtils.delete(uuid(), "-");
    }

}