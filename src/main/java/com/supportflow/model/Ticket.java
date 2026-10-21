package com.supportflow.model;

import com.supportflow.exception.InvalidStatusTransitionException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Обращение в поддержку. Состояние меняется только через методы,
 * поэтому правила (например, допустимые переходы статусов) нельзя обойти.
 */
public class Ticket {

    private final long id;
    private final String title;
    private final String description;
    private final Employee author;
    private final Instant createdAt;
    private final List<Comment> comments = new ArrayList<>();

    private Priority priority;
    private TicketStatus status = TicketStatus.NEW;
    private Employee assignee;
    private Instant updatedAt;

    public Ticket(long id, String title, String description, Priority priority, Employee author) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Тема обращения не может быть пустой");
        }
        if (priority == null) {
            throw new IllegalArgumentException("Приоритет обязателен");
        }
        if (author == null) {
            throw new IllegalArgumentException("У обращения должен быть автор");
        }
        this.id = id;
        this.title = title.trim();
        this.description = description == null ? "" : description.trim();
        this.priority = priority;
        this.author = author;
        this.createdAt = Instant.now();
        this.updatedAt = createdAt;
    }

    public void changeStatus(TicketStatus target) {
        if (!status.canTransitionTo(target)) {
            throw new InvalidStatusTransitionException(status, target);
        }
        this.status = target;
        touch();
    }

    public void assignTo(Employee employee) {
        if (status == TicketStatus.CLOSED) {
            throw new IllegalStateException("Нельзя назначить исполнителя на закрытое обращение");
        }
        this.assignee = employee;
        touch();
    }

    public void changePriority(Priority newPriority) {
        if (newPriority == null) {
            throw new IllegalArgumentException("Приоритет обязателен");
        }
        if (status == TicketStatus.CLOSED) {
            throw new IllegalStateException("Нельзя менять приоритет закрытого обращения");
        }
        this.priority = newPriority;
        touch();
    }

    public void addComment(Comment comment) {
        if (status == TicketStatus.CLOSED) {
            throw new IllegalStateException("Нельзя комментировать закрытое обращение");
        }
        comments.add(comment);
        touch();
    }

    private void touch() {
        this.updatedAt = Instant.now();
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Priority getPriority() {
        return priority;
    }

    public TicketStatus getStatus() {
        return status;
    }

    public Employee getAuthor() {
        return author;
    }

    // Optional вместо null: вызывающий код обязан обработать случай «исполнитель не назначен»
    public Optional<Employee> getAssignee() {
        return Optional.ofNullable(assignee);
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    // наружу отдаём копию только для чтения, чтобы список нельзя было изменить в обход addComment
    public List<Comment> getComments() {
        return Collections.unmodifiableList(comments);
    }
}
