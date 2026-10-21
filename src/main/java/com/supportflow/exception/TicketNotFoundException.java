package com.supportflow.exception;

public class TicketNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public TicketNotFoundException(long id) {
        super("Обращение не найдено: " + id);
    }
}
