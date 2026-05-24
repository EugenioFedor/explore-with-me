package ru.practicum.stats.server.mapper;

import org.mapstruct.Mapper;
import ru.practicum.stats.server.model.EndpointHit;
import ru.practicum.stats.dto.EndpointHitDto;

@Mapper(componentModel = "spring")
public interface EndpointHitMapper {

    EndpointHit toEntity(EndpointHitDto endpointHitDto);
}
