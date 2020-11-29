package dev.darshit.urlshortener.utils;

import java.math.BigInteger;

public class HashUtils {

    private static final char[] BASE62_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();

    private static final char[] BASE68_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789$-_.:=".toCharArray();
    public static final int SIXTY_EIGHT = 68;
    public static final int SIXTY_TWO = 62;

    public static String generateHash(final String stringNumber, boolean isLiberal) {
        char[] chars = isLiberal ? BASE68_CHARS : BASE62_CHARS;
        int baseValue = isLiberal ? SIXTY_EIGHT : SIXTY_TWO;

        int charPos = stringNumber.length() - 1;
        char[] buffer = new char[stringNumber.length()];
        BigInteger number = new BigInteger(stringNumber);
        BigInteger base = BigInteger.valueOf(baseValue);
        while (number.compareTo(base) >= 0) {
            buffer[charPos--] = chars[number.mod(base).intValue()];
            number = number.divide(base);
        }
        buffer[charPos] = chars[number.intValue()];

        return new String(buffer, charPos, stringNumber.length() - charPos);
    }

}
