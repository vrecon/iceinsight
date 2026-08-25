package nl.templify.iceinsights.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;
import nl.templify.iceinsights.domain.LapStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class LapDto {
    private Integer nr;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss[.SSS][.SS][.S]XXX")  private ZonedDateTime dateTimeStart;
    private String duration;
    private SpeedDto speed;
    private String diffPrevLap;
    private String sessionDuration;
    private String status;
    private List<String> sections = new ArrayList<>();
    private List<String> dataAttributes = new ArrayList<>();
}