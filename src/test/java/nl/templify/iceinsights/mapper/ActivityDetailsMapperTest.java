package nl.templify.iceinsights.mapper;

import nl.templify.iceinsights.domain.Activity;
import nl.templify.iceinsights.services.impl.SessionAnalyticsServiceImpl;
import nl.templify.iceinsights.domain.LapStatus;
import nl.templify.iceinsights.domain.Session;
import nl.templify.iceinsights.dto.ActivityDetailsResponse;
import nl.templify.iceinsights.dto.BestLapInfo;
import nl.templify.iceinsights.dto.LapDto;
import nl.templify.iceinsights.dto.SessionDto;
import nl.templify.iceinsights.dto.SpeedDto;
import nl.templify.iceinsights.dto.StatsDto;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class ActivityDetailsMapperTest {

    private final ActivityDetailsMapper mapper = new ActivityDetailsMapperImpl(new SessionAnalyticsServiceImpl());

    @Test
    void mapToEntities_persistsActivityStatsOnTheSession() {
        SpeedDto avg = new SpeedDto();
        avg.setKph(32.1);
        avg.setMps(8.9);
        SpeedDto fastest = new SpeedDto();
        fastest.setKph(36.4);
        fastest.setMps(10.1);

        StatsDto stats = new StatsDto();
        stats.setLapCount(12);
        stats.setFastestTime("00:00:32.100");
        stats.setAverageTime("00:00:34.000");
        stats.setMedianTime("00:00:33.500");
        stats.setTotalTrainingTime("00:08:00.000");
        stats.setActiveTrainingTime("00:07:10.000");
        stats.setAverageSpeed(avg);
        stats.setFastestSpeed(fastest);

        SpeedDto lapSpeed = new SpeedDto();
        lapSpeed.setKph(36.4);
        lapSpeed.setMps(10.1);
        BestLapInfo bestLap = new BestLapInfo();
        bestLap.setNr(3);
        bestLap.setDuration("00:00:32.100");
        bestLap.setSpeed(lapSpeed);

        LapDto lap = new LapDto();
        lap.setNr(1);
        lap.setDateTimeStart(ZonedDateTime.parse("2026-08-20T09:33:43+02:00"));
        lap.setDuration("00:00:34.000");
        lap.setSpeed(lapSpeed);
        lap.setStatus("FASTER");

        SessionDto sessionDto = new SessionDto();
        sessionDto.setId(99L);
        sessionDto.setChipId(7L);
        sessionDto.setDateTimeStart(ZonedDateTime.parse("2026-08-20T09:33:43+02:00"));
        sessionDto.setBestLap(bestLap);
        sessionDto.setAveLapDuration("00:00:34.000");
        sessionDto.setMedianLapDuration("00:00:33.500");
        sessionDto.setDuration("00:08:00.000");
        sessionDto.setLaps(List.of(lap));

        ActivityDetailsResponse response = new ActivityDetailsResponse();
        response.setStats(stats);
        response.setSessions(List.of(sessionDto));

        Activity activity = Activity.builder().id(8190779733L).name("Practice").build();

        List<Session> sessions = mapper.mapToEntities(response, activity);
        assertEquals(1, sessions.size());
        Session session = sessions.get(0);
        assertNotNull(session.getStats());
        assertSame(session, session.getStats().getSession());
        assertEquals(1, session.getStats().getLapCount());
        assertEquals("00:00:32.100", session.getStats().getFastestTime());
        assertEquals("00:00:34.000", session.getStats().getAverageTime());
        assertEquals("00:08:00.000", session.getStats().getTotalTrainingTime());
        assertEquals(0, session.getStats().getAverageSpeedKph().compareTo(BigDecimal.valueOf(32.1)));
        assertEquals(1, session.getLaps().size());
        assertEquals(LapStatus.FASTER, session.getLaps().get(0).getStatus());
        assertEquals("34.000", session.getStats().getBest1Duration());
    }
}
