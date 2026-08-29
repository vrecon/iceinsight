package nl.templify.iceinsights.services.impl;

import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses Speedhive / Vinksite lap strings: {@code 50.839}, {@code 1:03.173}, {@code 00:00:32.100}.
 */
final class LapTime {

    private static final Pattern CLOCK = Pattern.compile(
            "^(?:([0-9]+):)?([0-9]+):([0-9]+)(?:[.,]([0-9]{1,3}))?$");
    private static final Pattern SECONDS = Pattern.compile(
            "^([0-9]+)(?:[.,]([0-9]{1,3}))$");

    private LapTime() {
    }

    static Optional<Long> toMillis(String raw) {
        if (raw == null || raw.isBlank()) {
            return Optional.empty();
        }
        String value = raw.trim();
        Matcher clock = CLOCK.matcher(value);
        if (clock.matches()) {
            long hours = clock.group(1) == null ? 0 : Long.parseLong(clock.group(1));
            long minutes = Long.parseLong(clock.group(2));
            long seconds = Long.parseLong(clock.group(3));
            long millis = padMillis(clock.group(4));
            return Optional.of(((hours * 60 + minutes) * 60 + seconds) * 1000 + millis);
        }
        Matcher secondsOnly = SECONDS.matcher(value);
        if (secondsOnly.matches()) {
            long seconds = Long.parseLong(secondsOnly.group(1));
            return Optional.of(seconds * 1000 + padMillis(secondsOnly.group(2)));
        }
        try {
            double asSeconds = Double.parseDouble(value.replace(',', '.'));
            return Optional.of(Math.round(asSeconds * 1000));
        } catch (NumberFormatException ex) {
            return Optional.empty();
        }
    }

    static String format(long millis) {
        if (millis < 0) {
            return null;
        }
        long totalSeconds = millis / 1000;
        long remainder = millis % 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        if (minutes == 0) {
            return String.format(Locale.US, "%d.%03d", seconds, remainder);
        }
        return String.format(Locale.US, "%d:%02d.%03d", minutes, seconds, remainder);
    }

    private static long padMillis(String fraction) {
        if (fraction == null) {
            return 0;
        }
        String padded = (fraction + "000").substring(0, 3);
        return Long.parseLong(padded);
    }
}
