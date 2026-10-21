package com.supportflow.service;

import com.supportflow.exception.InvalidStatusTransitionException;
import com.supportflow.exception.TicketNotFoundException;
import com.supportflow.model.Employee;
import com.supportflow.model.Priority;
import com.supportflow.model.Ticket;
import com.supportflow.model.TicketStatus;
import com.supportflow.repository.EmployeeRepository;
import com.supportflow.repository.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

class TicketServiceImplTest {

    private TicketService service;
    private Employee author;
    private Employee agent;

    @BeforeEach
    void setUp() {
        service = new TicketServiceImpl(new TicketRepository(), new EmployeeRepository());
        author = service.registerEmployee("Анна", "anna@example.com");
        agent = service.registerEmployee("Иван", "ivan@example.com");
    }

    private Ticket newTicket() {
        return service.createTicket("Не работает вход", "Ошибка 500 при логине", Priority.HIGH, author.getId());
    }

    @Test
    void newTicketStartsInNewStatus() {
        Ticket t = newTicket();
        assertEquals(TicketStatus.NEW, t.getStatus());
        assertTrue(t.getAssignee().isEmpty());
        assertEquals(1, t.getId());
    }

    @Test
    void fullLifecycle() {
        Ticket t = newTicket();
        service.assign(t.getId(), agent.getId());
        service.changeStatus(t.getId(), TicketStatus.IN_PROGRESS);
        service.changeStatus(t.getId(), TicketStatus.RESOLVED);
        service.changeStatus(t.getId(), TicketStatus.CLOSED);
        assertEquals(TicketStatus.CLOSED, service.getTicket(t.getId()).getStatus());
    }

    @Test
    void invalidTransitionThrows() {
        Ticket t = newTicket();
        assertThrows(InvalidStatusTransitionException.class,
                () -> service.changeStatus(t.getId(), TicketStatus.RESOLVED));
        assertEquals(TicketStatus.NEW, service.getTicket(t.getId()).getStatus());
    }

    @Test
    void missingTicketThrows() {
        assertThrows(TicketNotFoundException.class, () -> service.getTicket(999));
    }

    @Test
    void unknownAuthorIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> service.createTicket("Тема", "Описание", Priority.LOW, 999));
    }

    @Test
    void closedTicketCannotBeCommentedOrReassigned() {
        Ticket t = newTicket();
        service.changeStatus(t.getId(), TicketStatus.CLOSED);
        assertThrows(IllegalStateException.class,
                () -> service.addComment(t.getId(), agent.getId(), "Поздно"));
        assertThrows(IllegalStateException.class,
                () -> service.assign(t.getId(), agent.getId()));
    }

    @Test
    void commentsAreStoredInOrder() {
        Ticket t = newTicket();
        service.addComment(t.getId(), agent.getId(), "Смотрю");
        service.addComment(t.getId(), author.getId(), "Спасибо");
        var comments = service.getTicket(t.getId()).getComments();
        assertEquals(2, comments.size());
        assertEquals("Смотрю", comments.get(0).text());
    }

    @Test
    void emptyCommentIsRejected() {
        Ticket t = newTicket();
        assertThrows(IllegalArgumentException.class,
                () -> service.addComment(t.getId(), agent.getId(), "   "));
    }

    @Test
    void filterByStatus() {
        Ticket first = newTicket();
        newTicket();
        service.changeStatus(first.getId(), TicketStatus.IN_PROGRESS);
        assertEquals(1, service.listTickets(TicketStatus.IN_PROGRESS).size());
        assertEquals(1, service.listTickets(TicketStatus.NEW).size());
        assertEquals(2, service.listTickets(null).size());
    }

    @Test
    void duplicateEmployeeEmailIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> service.registerEmployee("Другая Анна", "ANNA@example.com"));
    }

    @Test
    void blankTitleIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> service.createTicket("  ", "Описание", Priority.LOW, author.getId()));
    }

    @Test
    void listIsSortedByPriorityDescending() {
        Ticket low = service.createTicket("Низкий", "x", Priority.LOW, author.getId());
        Ticket critical = service.createTicket("Критичный", "x", Priority.CRITICAL, author.getId());
        Ticket high = service.createTicket("Высокий", "x", Priority.HIGH, author.getId());

        assertEquals(List.of(critical.getId(), high.getId(), low.getId()),
                service.listTickets(null).stream().map(Ticket::getId).toList());
    }
}
