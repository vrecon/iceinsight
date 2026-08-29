package nl.templify.iceinsights.services.impl;

import nl.templify.iceinsights.domain.Activity;
import nl.templify.iceinsights.domain.Lap;
import nl.templify.iceinsights.domain.Session;
import nl.templify.iceinsights.domain.SessionStats;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
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

    @Test
    void applyActivityBests_twoSessionsOfEightActiveLaps_best13IsNull() {
        List<Lap> firstLaps = nLaps(8, "50.000");
        List<Lap> secondLaps = nLaps(8, "51.000");
        SessionStats firstStats = new SessionStats();
        SessionStats secondStats = new SessionStats();
        service.enrich(firstStats, firstLaps);
        service.enrich(secondStats, secondLaps);

        assertNull(firstStats.getBest13Duration());
        assertNull(secondStats.getBest13Duration());
        assertEquals("4:10.000", firstStats.getBest5Duration());
        assertEquals("4:15.000", secondStats.getBest5Duration());

        Activity activity = new Activity();
        service.applyActivityBests(activity, List.of(
                sessionWith(firstStats, firstLaps),
                sessionWith(secondStats, secondLaps)));

        assertNull(activity.getBest13Duration());
        assertEquals("50.000", activity.getBest1Duration());
        assertEquals("4:10.000", activity.getBest5Duration());
        assertEquals("4:10.000", firstStats.getBest5Duration());
        assertEquals("4:15.000", secondStats.getBest5Duration());
        assertFalse(firstLaps.get(0).getRest());
        assertEquals("50.000", firstLaps.get(0).getMovingAvgDuration());
    }

    @Test
    void applyActivityBests_sessionWith13ActiveLaps_copiesThatSessionsBest13() {
        List<Lap> laps = nLaps(13, "50.000");
        SessionStats stats = new SessionStats();
        service.enrich(stats, laps);

        Activity activity = new Activity();
        service.applyActivityBests(activity, List.of(sessionWith(stats, laps)));

        assertEquals(stats.getBest13Duration(), activity.getBest13Duration());
        assertEquals("10:50.000", activity.getBest13Duration());
    }

    @Test
    void applyActivityBests_twoSessionsWith13_picksTheFasterBest13() {
        List<Lap> slowerLaps = nLaps(13, "50.000");
        List<Lap> fasterLaps = nLaps(13, "49.000");
        SessionStats slower = new SessionStats();
        SessionStats faster = new SessionStats();
        service.enrich(slower, slowerLaps);
        service.enrich(faster, fasterLaps);

        Activity activity = new Activity();
        service.applyActivityBests(activity, List.of(
                sessionWith(slower, slowerLaps),
                sessionWith(faster, fasterLaps)));

        assertEquals(faster.getBest13Duration(), activity.getBest13Duration());
        assertEquals("10:37.000", activity.getBest13Duration());
        assertEquals("10:50.000", slower.getBest13Duration());
    }

    @Test
    void applyActivityBests_ignoresNullSessionBestsAndDoesNotMutateStats() {
        SessionStats withBest = SessionStats.builder()
                .best1Duration("49.000")
                .best2Duration("1:40.000")
                .best5Duration(null)
                .best13Duration(null)
                .best25Duration(null)
                .build();
        SessionStats empty = new SessionStats();

        Activity activity = new Activity();
        service.applyActivityBests(activity, List.of(
                sessionWith(withBest, List.of()),
                sessionWith(empty, List.of())));

        assertEquals("49.000", activity.getBest1Duration());
        assertEquals("1:40.000", activity.getBest2Duration());
        assertNull(activity.getBest5Duration());
        assertEquals("49.000", withBest.getBest1Duration());
        assertNull(empty.getBest1Duration());
    }

    private static Session sessionWith(SessionStats stats, List<Lap> laps) {
        Session session = Session.builder().stats(stats).laps(laps).build();
        stats.setSession(session);
        return session;
    }

    private static List<Lap> nLaps(int count, String duration) {
        List<Lap> laps = new ArrayList<>(count);
        for (int i = 1; i <= count; i++) {
            laps.add(lap(i, duration));
        }
        return laps;
    }

    private static Lap lap(int nr, String duration) {
        return Lap.builder().lapNr(nr).duration(duration).build();
    }
}
