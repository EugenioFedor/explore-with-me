package ru.practicum.ewm.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.model.Event;
import ru.practicum.ewm.model.EventState;

import java.time.LocalDateTime;
import java.util.Collection;

import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {

    @Query("SELECT COUNT(e) > 0 FROM Event e WHERE e.category.id = :catId")
    boolean existsByCategoryId(@Param("catId") Long catId);

    @Query("""
            select e
            from Event e
            where e.state = :state
              and (:text is null or lower(e.annotation) like lower(concat('%', :text, '%'))
                   or lower(e.description) like lower(concat('%', :text, '%')))
              and (:categories is null or e.category.id in :categories)
              and (:paid is null or e.paid = :paid)
              and (:rangeStart is null or e.eventDate >= :rangeStart)
              and (:rangeEnd is null or e.eventDate <= :rangeEnd)
            """)
    Page<Event> findPublicEvents(
            @Param("state") EventState state,
            @Param("text") String text,
            @Param("categories") Collection<Long> categories,
            @Param("paid") Boolean paid,
            @Param("rangeStart") LocalDateTime rangeStart,
            @Param("rangeEnd") LocalDateTime rangeEnd,
            Pageable pageable
    );

    Page<Event> findByInitiatorId(Long initiatorId, Pageable pageable);

    Optional<Event> findByIdAndInitiatorId(Long id, Long initiatorId);
}