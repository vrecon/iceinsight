package nl.templify.iceinsights.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class LapSectionDto {
    private String name;
    private String duration;
    private String diffPrevLap;
    private SpeedDto speed;
}
