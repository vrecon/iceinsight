package nl.templify.iceinsights.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BestLap {
    private Long sessionId;
    private Integer lapNr;
    private String duration;
    private SpeedDto speed;
}