package nl.templify.iceinsights.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChipDto {
    private Long id;
    @JsonProperty("chipCode")
    private String chipCode;
    @JsonProperty("code")
    public void setCode(String code) {
        if (this.chipCode == null) {
            this.chipCode = code;
        }
    }
    private String chipLabel;
    private String status;
    private LocalDateTime validUntil;
    private LocalDateTime createdAt;
    private Long userId;
}