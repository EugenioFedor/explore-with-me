package ru.practicum.ewm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.ewm.model.Request;
import ru.practicum.ewm.model.RequestStatus;

import java.util.Collection;
import java.util.List;

public interface RequestRepository extends JpaRepository<Request, Long> {

    long countByEventIdAndStatus(Long eventId, RequestStatus status);

    @Query("""
            select r.event.id, count(r.id)
            from Request r
            where r.event.id in :eventIds
              and r.status = :status
            group by r.event.id
            """)
    List<Object[]> countByEventIdsAndStatus(Collection<Long> eventIds, RequestStatus status);
}