package nl.templify.iceinsights.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChipDto {
    private Long id;
    private String chipCode;
    private String chipLabel;
    private String status;
    private LocalDateTime validUntil;
    private LocalDateTime createdAt;
    private Long userId;
}