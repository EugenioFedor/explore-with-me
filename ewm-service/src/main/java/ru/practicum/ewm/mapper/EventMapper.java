package ru.practicum.ewm.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import ru.practicum.ewm.dto.EventShortDto;
import ru.practicum.ewm.model.Event;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = {CategoryMapper.class, UserMapper.class})
public interface EventMapper {

    EventShortDto toShortDto(Event event);
}