package nl.templify.iceinsights.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import nl.templify.iceinsights.domain.Chip;

import java.math.BigDecimal;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class StatsDto {
    private Integer lapCount;
    private String fastestTime;
    private String averageTime;
    private String medianTime;
    private String totalTrainingTime;
    private String activeTrainingTime;
    private SpeedDto averageSpeed;
    private SpeedDto fastestSpeed;
    private ChipDto chip;
}