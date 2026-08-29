package nl.templify.iceinsights.services;

import nl.templify.iceinsights.domain.Lap;
import nl.templify.iceinsights.domain.SessionStats;

import java.util.List;

public interface SessionAnalyticsService {

    /**
     * Fills best 1/2/5/13/25 consecutive active-lap totals on {@code stats}
     * and a moving average (window 5) plus rest-flag on each lap.
     * Call after laps are attached to the session.
     */
    void enrich(SessionStats stats, List<Lap> laps);
}
