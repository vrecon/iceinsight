package nl.templify.iceinsights.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityLapDto {
    private Integer lapNr;
    private Integer sessionNr;
    private ZonedDateTime datetimeStart;
    private String duration;
    private Boolean rest;
    private String movingAvgDuration;
    private BigDecimal speedKph;
}
