package ru.practicum.ewm.service;

import jakarta.servlet.http.HttpServletRequest;
import ru.practicum.ewm.dto.EventFullDto;
import ru.practicum.ewm.dto.EventShortDto;

import java.util.List;

public interface EventService {

    List<EventShortDto> getPublicEvents(
            String text,
            List<Long> categories,
            Boolean paid,
            String rangeStart,
            String rangeEnd,
            Boolean onlyAvailable,
            String sort,
            int from,
            int size,
            HttpServletRequest request
    );

    EventFullDto getPublicEvent(Long eventId, HttpServletRequest request);
}