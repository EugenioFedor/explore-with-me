package ru.practicum.ewm.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.model.Comment;
import ru.practicum.ewm.model.CommentStatus;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    // Найти все комментарии по событию с пагинацией
    Page<Comment> findByEventIdAndStatus(Long eventId, CommentStatus status, Pageable pageable);

    // Найти все комментарии пользователя с пагинацией
    Page<Comment> findByAuthorId(Long authorId, Pageable pageable);

    // Найти комментарий по id и eventId
    Optional<Comment> findByIdAndEventId(Long id, Long eventId);

    // Найти комментарий по id и authorId (для проверки авторства)
    Optional<Comment> findByIdAndAuthorId(Long id, Long authorId);

    // Найти все комментарии по статусу (для админа)
    Page<Comment> findByStatus(CommentStatus status, Pageable pageable);

    // Найти все комментарии с фильтрацией по статусу (если статус не указан - все)
    @Query("SELECT c FROM Comment c WHERE (:status IS NULL OR c.status = :status)")
    Page<Comment> findAllByStatus(@Param("status") CommentStatus status, Pageable pageable);

    // Подсчитать количество комментариев у события
    long countByEventIdAndStatus(Long eventId, CommentStatus status);
}
