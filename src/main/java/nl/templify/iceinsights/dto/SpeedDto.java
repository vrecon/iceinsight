package nl.templify.iceinsights.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SpeedDto {
    private Double kph;
    private Double mps;
}