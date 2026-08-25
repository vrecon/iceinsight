package nl.templify.iceinsights.mapper;

import nl.templify.iceinsights.domain.User;
import nl.templify.iceinsights.dto.UserDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = {ChipMapper.class})
public interface UserMapper {
    UserDto toDto(User user);
    
    @Mapping(target = "chips", ignore = true)  // We handelen chips apart af
    User toEntity(UserDto dto);
    
    List<UserDto> toDtos(List<User> users);
    List<User> toEntities(List<UserDto> dtos);
}