package nl.templify.iceinsights.dto;

import lombok.Data;

import java.time.ZonedDateTime;

@Data
public class ActivityDto {
    private Long id;
    private String name;
    private ZonedDateTime startTime;
    private ZonedDateTime endTime;
    private String chipCode;
    private String chipLabel;
    private Long chipId;
    private Long locationId;
}