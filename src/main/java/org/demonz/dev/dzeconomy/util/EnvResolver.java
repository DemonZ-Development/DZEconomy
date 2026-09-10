package org.demonz.dev.dzeconomy.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class EnvResolver {

    private static final Pattern PLACEHOLDER = Pattern.compile("\\$\\{([A-Za-z0-9_.]+)}");

    private EnvResolver() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static String resolve(String raw) {
        if (raw == null || !raw.contains("${")) {
            return raw;
        }
        Matcher matcher = PLACEHOLDER.matcher(raw);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            String value = System.getenv(matcher.group(1));
            matcher.appendReplacement(buffer,
                    Matcher.quoteReplacement(value != null ? value : matcher.group(0)));
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    public static int resolveInt(String raw, int fallback) {
        String resolved = resolve(raw);
        if (resolved == null) return fallback;
        try {
            return Integer.parseInt(resolved.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    public static boolean resolveBoolean(String raw, boolean fallback) {
        String resolved = resolve(raw);
        if (resolved == null) return fallback;
        return Boolean.parseBoolean(resolved.trim());
    }
}
