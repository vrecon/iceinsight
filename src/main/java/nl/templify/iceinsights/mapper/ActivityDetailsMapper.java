package nl.templify.iceinsights.mapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.templify.iceinsights.domain.*;
import nl.templify.iceinsights.dto.ActivityDetailsResponse;
import nl.templify.iceinsights.dto.LapDto;
import nl.templify.iceinsights.dto.SessionDto;
import nl.templify.iceinsights.dto.StatsDto;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ActivityDetailsMapper {
    
    public List<Session> mapToEntities(ActivityDetailsResponse response, Activity activity) {

        SessionStats sessionStats = SessionStats.builder()
               .lapCount(response.getStats().getLapCount())
                .fastestTime(response.getStats().getFastestTime())
                .averageTime(response.getStats().getAverageTime())
                .medianTime(response.getStats().getMedianTime())
                .totalTrainingTime(response.getStats().getTotalTrainingTime())
                .activeTrainingTime(response.getStats().getActiveTrainingTime())
                .averageSpeedKph(BigDecimal.valueOf(response.getStats().getAverageSpeed().getKph()))
                .averageSpeedMps(BigDecimal.valueOf(response.getStats().getAverageSpeed().getMps()))
                .fastestSpeedKph(BigDecimal.valueOf(response.getStats().getFastestSpeed().getKph()))
                .fastestSpeedMps(BigDecimal.valueOf(response.getStats().getFastestSpeed().getMps()))
               .build();

        return response.getSessions().stream()
            .map(sessionDto -> mapSession(sessionDto, activity))
            .collect(Collectors.toList());
    }
    
    private Session mapSession(SessionDto dto, Activity activity) {
        Session session = Session.builder()
            .id(dto.getId())
            .activity(activity)
            .chipId(dto.getChipId())
            .datetimeStart(dto.getDateTimeStart())
            .bestLapNr(dto.getBestLap().getNr())
            .bestLapDuration(dto.getBestLap().getDuration())
            .bestLapSpeedKph(BigDecimal.valueOf(dto.getBestLap().getSpeed().getKph()))
            .bestLapSpeedMps(BigDecimal.valueOf(dto.getBestLap().getSpeed().getMps()))
            .aveLapDuration(dto.getAveLapDuration())
            .medianLapDuration(dto.getMedianLapDuration())
            .duration(dto.getDuration())
            .build();

        // Map laps
        List<Lap> laps = dto.getLaps().stream()
            .map(lapDto -> mapLap(lapDto, session))
            .collect(Collectors.toList());
            session.setLaps(laps);

        // Map stats
       // SessionStats stats = mapSessionStats(dto, session);
        return session;
    }

    private Lap mapLap(LapDto dto, Session session) {
        return Lap.builder()
            .session(session)
            .lapNr(dto.getNr())
            .datetimeStart(dto.getDateTimeStart())
            .duration(dto.getDuration())
            .speedKph(BigDecimal.valueOf(dto.getSpeed().getKph()))
            .speedMps(BigDecimal.valueOf(dto.getSpeed().getMps()))
            .diffPrevLap(dto.getDiffPrevLap())
            .sessionDuration(dto.getSessionDuration())
            .status(LapStatus.valueOf(dto.getStatus()))
            .build();
    }

    private SessionStats mapSessionStats(StatsDto dto, Session session) {
        return SessionStats.builder()
            .session(session)
            .lapCount(dto.getLapCount())
            .fastestTime(dto.getFastestTime())
            .averageTime(dto.getAverageTime())
            .medianTime(dto.getMedianTime())
            .totalTrainingTime(dto.getTotalTrainingTime())
            .activeTrainingTime(dto.getActiveTrainingTime())
            .averageSpeedKph(BigDecimal.valueOf(dto.getAverageSpeed().getKph()))
            .averageSpeedMps(BigDecimal.valueOf(dto.getAverageSpeed().getMps()))
            .fastestSpeedKph(BigDecimal.valueOf(dto.getFastestSpeed().getKph()))
            .fastestSpeedMps(BigDecimal.valueOf(dto.getFastestSpeed().getMps()))
            .build();
    }
}
