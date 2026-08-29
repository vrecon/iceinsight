package nl.templify.iceinsights.services;

import nl.templify.iceinsights.domain.Activity;
import nl.templify.iceinsights.domain.Lap;
import nl.templify.iceinsights.domain.Session;
import nl.templify.iceinsights.domain.SessionStats;

import java.util.List;

public interface SessionAnalyticsService {

    /**
     * Fills best 1/2/4/8/13/25/50/100 consecutive active-lap totals on {@code stats}
     * and a moving average (window 5) plus rest-flag on each lap.
     * Call after laps are attached to the session.
     */
    void enrich(SessionStats stats, List<Lap> laps);

    /**
     * Copies activity-level best 1/2/4/8/13/25/50/100 from the fastest non-null
     * {@code session_stats} value for each N. Does not mutate session stats or laps.
     */
    void applyActivityBests(Activity activity, List<Session> sessions);
}
