package nl.templify.iceinsights.mapper;

import lombok.extern.slf4j.Slf4j;
import nl.templify.iceinsights.domain.Activity;
import nl.templify.iceinsights.domain.Lap;
import nl.templify.iceinsights.domain.LapStatus;
import nl.templify.iceinsights.domain.Session;
import nl.templify.iceinsights.domain.SessionStats;
import nl.templify.iceinsights.dto.ActivityDetailsResponse;
import nl.templify.iceinsights.dto.LapDto;
import nl.templify.iceinsights.dto.SessionDto;
import nl.templify.iceinsights.dto.SpeedDto;
import nl.templify.iceinsights.dto.StatsDto;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class ActivityDetailsMapper {

    public List<Session> mapToEntities(ActivityDetailsResponse response, Activity activity) {
        if (response == null || response.getSessions() == null) {
            return List.of();
        }

        StatsDto activityStats = response.getStats();
        return response.getSessions().stream()
                .map(sessionDto -> mapSession(sessionDto, activity, activityStats))
                .toList();
    }

    private Session mapSession(SessionDto dto, Activity activity, StatsDto activityStats) {
        Session session = Session.builder()
                .id(dto.getId())
                .activity(activity)
                .chipId(dto.getChipId())
                .datetimeStart(dto.getDateTimeStart())
                .bestLapNr(dto.getBestLap() != null ? dto.getBestLap().getNr() : null)
                .bestLapDuration(dto.getBestLap() != null ? dto.getBestLap().getDuration() : null)
                .bestLapSpeedKph(speedKph(dto.getBestLap() != null ? dto.getBestLap().getSpeed() : null))
                .bestLapSpeedMps(speedMps(dto.getBestLap() != null ? dto.getBestLap().getSpeed() : null))
                .aveLapDuration(dto.getAveLapDuration())
                .medianLapDuration(dto.getMedianLapDuration())
                .duration(dto.getDuration())
                .build();

        List<LapDto> lapDtos = dto.getLaps() != null ? dto.getLaps() : Collections.emptyList();
        List<Lap> laps = lapDtos.stream()
                .map(lapDto -> mapLap(lapDto, session))
                .toList();
        session.setLaps(laps);

        SessionStats stats = mapSessionStats(dto, activityStats, session, laps.size());
        session.setStats(stats);
        return session;
    }

    private SessionStats mapSessionStats(SessionDto sessionDto, StatsDto activityStats,
                                         Session session, int mappedLapCount) {
        SessionStats.SessionStatsBuilder builder = SessionStats.builder().session(session);

        if (activityStats != null) {
            builder
                    .lapCount(activityStats.getLapCount())
                    .fastestTime(activityStats.getFastestTime())
                    .averageTime(activityStats.getAverageTime())
                    .medianTime(activityStats.getMedianTime())
                    .totalTrainingTime(activityStats.getTotalTrainingTime())
                    .activeTrainingTime(activityStats.getActiveTrainingTime())
                    .averageSpeedKph(speedKph(activityStats.getAverageSpeed()))
                    .averageSpeedMps(speedMps(activityStats.getAverageSpeed()))
                    .fastestSpeedKph(speedKph(activityStats.getFastestSpeed()))
                    .fastestSpeedMps(speedMps(activityStats.getFastestSpeed()));
        }

        // Session-level fields win when present so each session_stats row matches that session.
        if (mappedLapCount > 0) {
            builder.lapCount(mappedLapCount);
        }
        if (sessionDto.getBestLap() != null && sessionDto.getBestLap().getDuration() != null) {
            builder.fastestTime(sessionDto.getBestLap().getDuration());
            builder.fastestSpeedKph(speedKph(sessionDto.getBestLap().getSpeed()));
            builder.fastestSpeedMps(speedMps(sessionDto.getBestLap().getSpeed()));
        }
        if (sessionDto.getAveLapDuration() != null) {
            builder.averageTime(sessionDto.getAveLapDuration());
        }
        if (sessionDto.getMedianLapDuration() != null) {
            builder.medianTime(sessionDto.getMedianLapDuration());
        }
        if (sessionDto.getDuration() != null) {
            builder.totalTrainingTime(sessionDto.getDuration());
        }

        return builder.build();
    }

    private Lap mapLap(LapDto dto, Session session) {
        return Lap.builder()
                .session(session)
                .lapNr(dto.getNr())
                .datetimeStart(dto.getDateTimeStart())
                .duration(dto.getDuration())
                .speedKph(speedKph(dto.getSpeed()))
                .speedMps(speedMps(dto.getSpeed()))
                .diffPrevLap(dto.getDiffPrevLap())
                .sessionDuration(dto.getSessionDuration())
                .status(parseLapStatus(dto.getStatus()))
                .build();
    }

    private static LapStatus parseLapStatus(String status) {
        if (status == null || status.isBlank()) {
            return LapStatus.NONE;
        }
        try {
            return LapStatus.valueOf(status);
        } catch (IllegalArgumentException ex) {
            return LapStatus.NONE;
        }
    }

    private static BigDecimal speedKph(SpeedDto speed) {
        return speed != null && speed.getKph() != null ? BigDecimal.valueOf(speed.getKph()) : null;
    }

    private static BigDecimal speedMps(SpeedDto speed) {
        return speed != null && speed.getMps() != null ? BigDecimal.valueOf(speed.getMps()) : null;
    }
}
