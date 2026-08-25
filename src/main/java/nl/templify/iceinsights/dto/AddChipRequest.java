package nl.templify.iceinsights.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class AddChipRequest {
    @NotBlank(message = "Chip code is required")
    private String chipCode;
    
    @NotBlank(message = "Chip label is required")
    private String chipLabel;
}