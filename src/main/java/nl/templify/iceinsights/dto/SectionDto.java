package nl.templify.iceinsights.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SectionDto {
    private String name;
    private Integer length;
    private Boolean speedTrap;
}