package ru.practicum.ewm.model;

public enum CommentStatus {
    PENDING,    // ожидает модерации
    PUBLISHED,  // опубликован (виден всем)
    REJECTED,   // отклонён администратором
    DELETED     // удалён автором (мягкое удаление)
}
