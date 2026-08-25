package nl.templify.iceinsights.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ActivityDetailsResponse {
    private BestLap bestLap;
    private StatsDto stats;
    private List<SessionDto> sessions;
    private List<SectionDto> sections;
}