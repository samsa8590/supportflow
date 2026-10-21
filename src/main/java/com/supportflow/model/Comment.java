package com.supportflow.model;

import java.time.Instant;

/**
 * Комментарий к обращению. Record, то есть неизменяемый по определению:
 * создали и больше не правим.
 */
public record Comment(Employee author, String text, Instant createdAt) {

    public Comment {
        if (author == null) {
            throw new IllegalArgumentException("У комментария должен быть автор");
        }
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Текст комментария не может быть пустым");
        }
        text = text.trim();
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public Comment(Employee author, String text) {
        this(author, text, Instant.now());
    }
}
