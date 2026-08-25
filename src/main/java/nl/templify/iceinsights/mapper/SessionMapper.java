package nl.templify.iceinsights.mapper;

import nl.templify.iceinsights.domain.Session;
import nl.templify.iceinsights.dto.SessionDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SessionMapper {
    @Mapping(target = "activity", ignore = true)
    Session toEntity(SessionDto dto);
    
    SessionDto toDto(Session entity);
    
    List<Session> toEntities(List<SessionDto> dtos);
    List<SessionDto> toDtos(List<Session> entities);
}