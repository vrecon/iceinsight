package nl.templify.iceinsights.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SeasonWindowTest {

    private static final ZoneId AMS = ZoneId.of("Europe/Amsterdam");

    @Test
    void jan15_2026_isSeason2025_2026() {
        assertWindow("2026-01-15T12:00:00+01:00[Europe/Amsterdam]",
                "2025-05-01", "2026-04-30", "2025/2026");
    }

    @Test
    void aug29_2026_isSeason2026_2027() {
        assertWindow("2026-08-29T12:00:00+02:00[Europe/Amsterdam]",
                "2026-05-01", "2027-04-30", "2026/2027");
    }

    @Test
    void may1_2026_isSeason2026_2027() {
        assertWindow("2026-05-01T00:00:00+02:00[Europe/Amsterdam]",
                "2026-05-01", "2027-04-30", "2026/2027");
    }

    @Test
    void apr30_2026_isSeason2025_2026() {
        assertWindow("2026-04-30T23:59:59+02:00[Europe/Amsterdam]",
                "2025-05-01", "2026-04-30", "2025/2026");
    }

    @Test
    void usesInstantZoneNotAlwaysAmsterdam() {
        ZonedDateTime utc = ZonedDateTime.parse("2026-04-30T22:30:00Z");
        // 30 Apr 22:30 UTC is still April in the instant's own zone
        assertEquals(LocalDate.of(2025, 5, 1), SeasonWindow.startDate(utc));
        assertEquals("2025/2026", SeasonWindow.label(SeasonWindow.startDate(utc)));
    }

    private static void assertWindow(String instant, String start, String end, String label) {
        ZonedDateTime zoned = ZonedDateTime.parse(instant);
        LocalDate startDate = SeasonWindow.startDate(zoned);
        assertEquals(LocalDate.parse(start), startDate);
        assertEquals(LocalDate.parse(end), SeasonWindow.endDate(startDate));
        assertEquals(label, SeasonWindow.label(startDate));
        assertEquals(AMS, zoned.getZone());
    }
}
