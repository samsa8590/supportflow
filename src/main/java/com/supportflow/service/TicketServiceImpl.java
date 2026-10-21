package com.supportflow.service;

import com.supportflow.exception.TicketNotFoundException;
import com.supportflow.model.Comment;
import com.supportflow.model.Employee;
import com.supportflow.model.Priority;
import com.supportflow.model.Ticket;
import com.supportflow.model.TicketStatus;
import com.supportflow.repository.EmployeeRepository;
import com.supportflow.repository.TicketRepository;

import java.util.Comparator;
import java.util.List;

public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;
    private final EmployeeRepository employeeRepository;

    public TicketServiceImpl(TicketRepository ticketRepository, EmployeeRepository employeeRepository) {
        this.ticketRepository = ticketRepository;
        this.employeeRepository = employeeRepository;
    }

    @Override
    public Ticket createTicket(String title, String description, Priority priority, long authorId) {
        Employee author = findEmployee(authorId);
        Ticket ticket = new Ticket(ticketRepository.nextId(), title, description, priority, author);
        return ticketRepository.save(ticket);
    }

    @Override
    public Ticket getTicket(long ticketId) {
        return ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException(ticketId));
    }

    @Override
    public List<Ticket> listTickets(TicketStatus status) {
        List<Ticket> tickets = status == null
                ? ticketRepository.findAll()
                : ticketRepository.findByStatus(status);
        // сначала самые срочные, внутри приоритета самые старые
        return tickets.stream()
                .sorted(Comparator.comparing(Ticket::getPriority).reversed()
                        .thenComparing(Ticket::getCreatedAt))
                .toList();
    }

    @Override
    public Ticket changeStatus(long ticketId, TicketStatus target) {
        Ticket ticket = getTicket(ticketId);
        ticket.changeStatus(target);   // правила переходов проверяет сама сущность
        return ticketRepository.save(ticket);
    }

    @Override
    public Ticket assign(long ticketId, long employeeId) {
        Ticket ticket = getTicket(ticketId);
        ticket.assignTo(findEmployee(employeeId));
        return ticketRepository.save(ticket);
    }

    @Override
    public Comment addComment(long ticketId, long authorId, String text) {
        Ticket ticket = getTicket(ticketId);
        Comment comment = new Comment(findEmployee(authorId), text);
        ticket.addComment(comment);
        ticketRepository.save(ticket);
        return comment;
    }

    @Override
    public Employee registerEmployee(String name, String email) {
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Некорректный email: " + email);
        }
        if (employeeRepository.findByEmail(email.trim()).isPresent()) {
            throw new IllegalArgumentException("Сотрудник с email " + email.trim() + " уже есть");
        }
        Employee employee = new Employee(employeeRepository.nextId(), name, email.trim());
        return employeeRepository.save(employee);
    }

    @Override
    public List<Employee> listEmployees() {
        return employeeRepository.findAll();
    }

    private Employee findEmployee(long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Сотрудник не найден: " + id));
    }
}
