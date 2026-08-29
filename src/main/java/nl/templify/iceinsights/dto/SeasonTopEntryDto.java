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
public class SeasonTopEntryDto {
    private Long activityId;
    private ZonedDateTime startTime;
    private Long locationId;
    private Long chipId;
    private Integer n;
    private String duration;
}
