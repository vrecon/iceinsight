package nl.templify.iceinsights.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivitySummaryDto {
    private Long id;
    private String name;
    private ZonedDateTime startTime;
    private ZonedDateTime endTime;
    private Long locationId;
    private Long chipId;
    private String best1Duration;
    private String best2Duration;
    private String best4Duration;
    private String best8Duration;
    private String best13Duration;
    private String best25Duration;
    private String best50Duration;
    private String best100Duration;
}
