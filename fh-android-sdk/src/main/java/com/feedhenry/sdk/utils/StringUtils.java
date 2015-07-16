package com.feedhenry.sdk.utils;

import java.util.regex.Pattern;

public class StringUtils {
    private static final Pattern TRAILING_SLASH = Pattern.compile("/$");

    public static String removeTrailingSlash(String input) {
        return TRAILING_SLASH.matcher(input).replaceAll("");
    }
}
