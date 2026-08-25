package nl.templify.iceinsights.mapper;

import nl.templify.iceinsights.domain.Activity;
import nl.templify.iceinsights.dto.ActivityDto;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ActivityMapper {
    Activity toEntity(ActivityDto dto);
    ActivityDto toDto(Activity entity);
    
    List<Activity> toEntities(List<ActivityDto> dtos);
    List<ActivityDto> toDtos(List<Activity> entities);
}