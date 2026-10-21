package com.supportflow.model;

import java.util.EnumSet;
import java.util.Set;

/**
 * Статусы обращения и конечный автомат допустимых переходов.
 * Правила описаны в одном месте, поэтому их легко проверить одним тестом.
 */
public enum TicketStatus {
    NEW("Новое"),
    IN_PROGRESS("В работе"),
    WAITING_FOR_CUSTOMER("Ждём клиента"),
    RESOLVED("Решено"),
    CLOSED("Закрыто"),
    REOPENED("Открыто повторно");

    private final String title;

    TicketStatus(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public Set<TicketStatus> allowedTransitions() {
        return switch (this) {
            case NEW                  -> EnumSet.of(IN_PROGRESS, CLOSED);
            case IN_PROGRESS          -> EnumSet.of(WAITING_FOR_CUSTOMER, RESOLVED);
            case WAITING_FOR_CUSTOMER -> EnumSet.of(IN_PROGRESS, RESOLVED);
            case RESOLVED             -> EnumSet.of(CLOSED, REOPENED);
            case REOPENED             -> EnumSet.of(IN_PROGRESS);
            case CLOSED               -> EnumSet.noneOf(TicketStatus.class);
        };
    }

    public boolean canTransitionTo(TicketStatus target) {
        return allowedTransitions().contains(target);
    }
}
