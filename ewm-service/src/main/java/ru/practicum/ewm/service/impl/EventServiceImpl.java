package ru.practicum.ewm.service.impl;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.dto.EventFullDto;
import ru.practicum.ewm.dto.EventShortDto;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.mapper.EventMapper;
import ru.practicum.ewm.model.Event;
import ru.practicum.ewm.model.EventState;
import ru.practicum.ewm.model.RequestStatus;
import ru.practicum.ewm.repository.EventRepository;
import ru.practicum.ewm.repository.RequestRepository;
import ru.practicum.ewm.service.EventService;
import ru.practicum.stats.client.StatsClient;
import ru.practicum.stats.dto.EndpointHitDto;
import ru.practicum.stats.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private static final String APP_NAME = "ewm-main-service";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final EventRepository eventRepository;
    private final RequestRepository requestRepository;
    private final EventMapper eventMapper;
    private final StatsClient statsClient;

    @Override
    public List<EventShortDto> getPublicEvents(
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
    ) {
        LocalDateTime start = parseDate(rangeStart);
        LocalDateTime end = parseDate(rangeEnd);

        if (start == null && end == null) {
            start = LocalDateTime.now();
        }

        PageRequest pageRequest = PageRequest.of(
                from / size,
                size,
                Sort.by(Sort.Direction.ASC, "eventDate")
        );

        List<Event> events = eventRepository.findPublicEvents(
                EventState.PUBLISHED,
                normalizeText(text),
                emptyToNull(categories),
                paid,
                start,
                end,
                pageRequest
        ).getContent();

        Map<Long, Long> confirmedRequests = getConfirmedRequests(events);

        if (Boolean.TRUE.equals(onlyAvailable)) {
            events = events.stream()
                    .filter(event -> isAvailable(event, confirmedRequests.getOrDefault(event.getId(), 0L)))
                    .toList();
        }

        Map<String, Long> views = getViews(events);
        hit(request);

        List<EventShortDto> result = events.stream()
                .map(event -> {
                    EventShortDto dto = eventMapper.toShortDto(event);
                    dto.setConfirmedRequests(confirmedRequests.getOrDefault(event.getId(), 0L));
                    dto.setViews(views.getOrDefault("/events/" + event.getId(), 0L));
                    return dto;
                })
                .toList();

        if ("VIEWS".equalsIgnoreCase(sort)) {
            return result.stream()
                    .sorted(Comparator.comparing(EventShortDto::getViews).reversed())
                    .toList();
        }

        return result;
    }

    @Override
    public EventFullDto getPublicEvent(Long eventId, HttpServletRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        if (event.getState() != EventState.PUBLISHED) {
            throw new NotFoundException("Event with id=" + eventId + " was not found");
        }

        long confirmedRequests = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);

        hit(request);

        EventFullDto dto = eventMapper.toFullDto(event);
        dto.setConfirmedRequests(confirmedRequests);
        dto.setViews(getViews(List.of(event)).getOrDefault("/events/" + eventId, 0L));

        return dto;
    }

    private void hit(HttpServletRequest request) {
        statsClient.hit(new EndpointHitDto(
                APP_NAME,
                request.getRequestURI(),
                request.getRemoteAddr(),
                LocalDateTime.now()
        ));
    }

    private Map<String, Long> getViews(List<Event> events) {
        if (events == null || events.isEmpty()) {
            return Map.of();
        }

        List<String> uris = events.stream()
                .map(event -> "/events/" + event.getId())
                .toList();

        LocalDateTime start = LocalDateTime.of(2000, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.now().plusSeconds(1);

        return statsClient.getStats(start, end, uris, true)
                .stream()
                .collect(Collectors.toMap(ViewStatsDto::uri, ViewStatsDto::hits));
    }

    private Map<Long, Long> getConfirmedRequests(List<Event> events) {
        if (events == null || events.isEmpty()) {
            return Map.of();
        }

        List<Long> eventIds = events.stream()
                .map(Event::getId)
                .toList();

        return requestRepository.countByEventIdsAndStatus(eventIds, RequestStatus.CONFIRMED)
                .stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (Long) row[1]
                ));
    }

    private boolean isAvailable(Event event, Long confirmedRequests) {
        Integer limit = event.getParticipantLimit();

        return limit == null || limit == 0 || confirmedRequests < limit;
    }

    private LocalDateTime parseDate(String value) {
        return value == null || value.isBlank()
                ? null
                : LocalDateTime.parse(value, FORMATTER);
    }

    private String normalizeText(String text) {
        return text == null || text.isBlank() ? null : text;
    }

    private List<Long> emptyToNull(List<Long> values) {
        return values == null || values.isEmpty() ? null : values;
    }
}