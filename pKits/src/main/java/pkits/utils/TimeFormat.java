package pkits.utils;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TimeFormat {

    private static final Pattern TOKEN = Pattern.compile("(\\d+)([dhms])");

    private TimeFormat() {
    }

    public static String format(long totalSeconds, String days, String hours, String minutes, String seconds) {
        long d = totalSeconds / 86400;
        long h = (totalSeconds % 86400) / 3600;
        long m = (totalSeconds % 3600) / 60;
        long s = totalSeconds % 60;
        StringBuilder builder = new StringBuilder();
        if (d > 0) {
            builder.append(d).append(days).append(' ');
        }
        if (h > 0) {
            builder.append(h).append(hours).append(' ');
        }
        if (m > 0) {
            builder.append(m).append(minutes).append(' ');
        }
        if (s > 0 || builder.isEmpty()) {
            builder.append(s).append(seconds);
        }
        return builder.toString().trim();
    }

    public static Long parse(String input) {
        if (input == null) {
            return null;
        }
        String value = input.trim().toLowerCase(Locale.ROOT);
        if (value.isEmpty()) {
            return null;
        }
        if (value.matches("-?\\d{1,15}")) {
            return Long.parseLong(value);
        }
        Matcher matcher = TOKEN.matcher(value);
        long total = 0;
        int end = 0;
        while (matcher.find()) {
            if (matcher.start() != end) {
                return null;
            }
            long amount = Long.parseLong(matcher.group(1));
            long multiplier = switch (matcher.group(2)) {
                case "d" -> 86400L;
                case "h" -> 3600L;
                case "m" -> 60L;
                default -> 1L;
            };
            total += amount * multiplier;
            end = matcher.end();
        }
        if (end == 0 || end != value.length()) {
            return null;
        }
        return total;
    }
}
