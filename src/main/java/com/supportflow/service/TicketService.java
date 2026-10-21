package com.supportflow.service;

import com.supportflow.model.Comment;
import com.supportflow.model.Employee;
import com.supportflow.model.Priority;
import com.supportflow.model.Ticket;
import com.supportflow.model.TicketStatus;

import java.util.List;

/**
 * Прикладные сценарии работы с обращениями. UI зависит только от этого интерфейса.
 */
public interface TicketService {

    Ticket createTicket(String title, String description, Priority priority, long authorId);

    Ticket getTicket(long ticketId);

    /** @param status фильтр по статусу или null, чтобы получить все обращения */
    List<Ticket> listTickets(TicketStatus status);

    Ticket changeStatus(long ticketId, TicketStatus target);

    Ticket assign(long ticketId, long employeeId);

    Comment addComment(long ticketId, long authorId, String text);

    /** Регистрирует сотрудника. Email должен быть уникальным без учёта регистра. */
    Employee registerEmployee(String name, String email);

    List<Employee> listEmployees();
}
