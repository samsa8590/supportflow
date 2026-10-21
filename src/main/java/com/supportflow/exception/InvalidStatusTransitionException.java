package com.supportflow.exception;

import com.supportflow.model.TicketStatus;

public class InvalidStatusTransitionException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public InvalidStatusTransitionException(TicketStatus from, TicketStatus to) {
        super("Переход из «" + from.getTitle() + "» в «" + to.getTitle() + "» недопустим");
    }
}
