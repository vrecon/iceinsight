package nl.templify.iceinsights.services.impl;

import nl.templify.iceinsights.domain.Lap;
import nl.templify.iceinsights.domain.SessionStats;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SessionAnalyticsServiceImplTest {

    private final SessionAnalyticsServiceImpl service = new SessionAnalyticsServiceImpl();

    @Test
    void enrich_marksRestLapsAndStoresBestConsecutiveAndMovingAverage() {
        List<Lap> laps = List.of(
                lap(1, "50.000"),
                lap(2, "51.000"),
                lap(3, "120.000"),
                lap(4, "52.000"),
                lap(5, "49.000")
        );
        SessionStats stats = new SessionStats();

        service.enrich(stats, laps);

        assertFalse(laps.get(0).getRest());
        assertTrue(laps.get(2).getRest());
        assertEquals("49.000", stats.getBest1Duration());
        assertEquals("1:41.000", stats.getBest2Duration());
        assertNull(stats.getBest5Duration());
        assertEquals(5, stats.getMovingAvgWindow());
        assertEquals("50.000", laps.get(0).getMovingAvgDuration());
        assertEquals("50.500", laps.get(1).getMovingAvgDuration());
        assertNull(laps.get(2).getMovingAvgDuration());
    }

    @Test
    void lapTime_parsesClockAndPlainSeconds() {
        assertEquals(50839L, LapTime.toMillis("50.839").orElseThrow());
        assertEquals(63173L, LapTime.toMillis("1:03.173").orElseThrow());
        assertEquals("1:03.173", LapTime.format(63173));
        assertEquals("50.839", LapTime.format(50839));
    }

    private static Lap lap(int nr, String duration) {
        return Lap.builder().lapNr(nr).duration(duration).build();
    }
}
