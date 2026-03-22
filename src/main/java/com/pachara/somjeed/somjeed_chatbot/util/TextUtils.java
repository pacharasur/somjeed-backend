package com.pachara.somjeed.somjeed_chatbot.util;

import java.util.Locale;
import java.util.Optional;

public class TextUtils {
    private TextUtils() {}
    public static String normalize(String message) {
        return Optional.ofNullable(message)
                .map(m -> m.trim().toLowerCase(Locale.ROOT))
                .orElse("");
    }
}