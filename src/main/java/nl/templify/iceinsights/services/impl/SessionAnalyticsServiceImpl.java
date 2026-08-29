package nl.templify.iceinsights.services.impl;

import nl.templify.iceinsights.domain.Lap;
import nl.templify.iceinsights.domain.SessionStats;
import nl.templify.iceinsights.services.SessionAnalyticsService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class SessionAnalyticsServiceImpl implements SessionAnalyticsService {

    static final int MOVING_AVG_WINDOW = 5;
    private static final int[] BEST_NS = {1, 2, 5, 13, 25};
    private static final double REST_MEDIAN_FACTOR = 1.5;

    @Override
    public void enrich(SessionStats stats, List<Lap> laps) {
        if (stats == null || laps == null || laps.isEmpty()) {
            return;
        }

        List<Lap> ordered = laps.stream()
                .sorted(Comparator.comparing(lap -> lap.getLapNr() == null ? 0 : lap.getLapNr()))
                .toList();

        List<Long> millis = new ArrayList<>(ordered.size());
        List<Long> parsedActive = new ArrayList<>();
        for (Lap lap : ordered) {
            Long ms = LapTime.toMillis(lap.getDuration()).orElse(null);
            millis.add(ms);
            if (ms != null) {
                parsedActive.add(ms);
            }
        }

        long median = median(parsedActive);
        long restThreshold = median <= 0 ? Long.MAX_VALUE : Math.round(median * REST_MEDIAN_FACTOR);

        List<Long> activeMillis = new ArrayList<>();
        for (int i = 0; i < ordered.size(); i++) {
            Long ms = millis.get(i);
            boolean rest = ms != null && ms > restThreshold;
            ordered.get(i).setRest(rest);
            if (ms != null && !rest) {
                activeMillis.add(ms);
            }
        }

        stats.setMovingAvgWindow(MOVING_AVG_WINDOW);
        stats.setBest1Duration(bestConsecutive(activeMillis, 1));
        stats.setBest2Duration(bestConsecutive(activeMillis, 2));
        stats.setBest5Duration(bestConsecutive(activeMillis, 5));
        stats.setBest13Duration(bestConsecutive(activeMillis, 13));
        stats.setBest25Duration(bestConsecutive(activeMillis, 25));
        if (stats.getFastestTime() == null) {
            stats.setFastestTime(stats.getBest1Duration());
        }

        List<Long> window = new ArrayList<>();
        for (int i = 0; i < ordered.size(); i++) {
            Lap lap = ordered.get(i);
            Long ms = millis.get(i);
            if (ms == null || Boolean.TRUE.equals(lap.getRest())) {
                lap.setMovingAvgDuration(null);
                continue;
            }
            window.add(ms);
            if (window.size() > MOVING_AVG_WINDOW) {
                window.remove(0);
            }
            long sum = 0;
            for (Long value : window) {
                sum += value;
            }
            lap.setMovingAvgDuration(LapTime.format(Math.round(sum / (double) window.size())));
        }
    }

    private static String bestConsecutive(List<Long> activeMillis, int n) {
        if (activeMillis.size() < n) {
            return null;
        }
        long windowSum = 0;
        for (int i = 0; i < n; i++) {
            windowSum += activeMillis.get(i);
        }
        long best = windowSum;
        for (int i = n; i < activeMillis.size(); i++) {
            windowSum += activeMillis.get(i) - activeMillis.get(i - n);
            if (windowSum < best) {
                best = windowSum;
            }
        }
        return LapTime.format(best);
    }

    private static long median(List<Long> values) {
        if (values.isEmpty()) {
            return 0;
        }
        List<Long> sorted = new ArrayList<>(values);
        sorted.sort(Long::compareTo);
        int mid = sorted.size() / 2;
        if (sorted.size() % 2 == 1) {
            return sorted.get(mid);
        }
        return (sorted.get(mid - 1) + sorted.get(mid)) / 2;
    }
}
