package nl.templify.iceinsights.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SessionDto {
    private Long id;
    private Long chipId;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss[.SSS][.SS][.S]XXX")
    private ZonedDateTime dateTimeStart;
    private BestLapInfo bestLap;  // Inner class voor session best lap info
    private String aveLapDuration;
    private String medianLapDuration;
    private String duration;
    private List<LapDto> laps;
}