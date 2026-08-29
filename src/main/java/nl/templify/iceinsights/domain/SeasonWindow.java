package nl.templify.iceinsights.domain;

import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * Skating season: 1 May through 30 April. Label is {@code startYear/endYear}, e.g. 2025/2026.
 */
public final class SeasonWindow {

    public static final ZoneId DEFAULT_ZONE = ZoneId.of("Europe/Amsterdam");

    private SeasonWindow() {
    }

    public static LocalDate startDate(ZonedDateTime instant) {
        LocalDate local = toLocalDate(instant);
        int year = local.getMonthValue() >= 5 ? local.getYear() : local.getYear() - 1;
        return LocalDate.of(year, Month.MAY, 1);
    }

    public static LocalDate endDate(LocalDate startDate) {
        return startDate.plusYears(1).minusDays(1);
    }

    public static String label(LocalDate startDate) {
        int startYear = startDate.getYear();
        return startYear + "/" + (startYear + 1);
    }

    static LocalDate toLocalDate(ZonedDateTime instant) {
        ZoneId zone = instant.getZone() != null ? instant.getZone() : DEFAULT_ZONE;
        return instant.withZoneSameInstant(zone).toLocalDate();
    }
}
