package nl.templify.iceinsights.mapper;

import nl.templify.iceinsights.domain.Chip;
import nl.templify.iceinsights.dto.ChipDto;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ChipMapper {
    ChipDto toDto(Chip chip);
    Chip toEntity(ChipDto dto);
    
    List<ChipDto> toDtos(List<Chip> chips);
    List<Chip> toEntities(List<ChipDto> dtos);
}
