package nl.templify.iceinsights.dto;

import lombok.Data;

import java.util.List;

@Data
public class ActivityResponseDto {
    private List<ActivityDto> activities;
    private Integer activityCount;
}
