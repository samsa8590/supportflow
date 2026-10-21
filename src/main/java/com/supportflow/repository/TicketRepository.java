package com.supportflow.repository;

import com.supportflow.model.Ticket;
import com.supportflow.model.TicketStatus;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Хранилище обращений в памяти. Позже его можно заменить на JDBC или JPA,
 * не трогая сервис: достаточно сохранить те же публичные методы.
 */
public class TicketRepository {

    private final Map<Long, Ticket> storage = new LinkedHashMap<>();
    private final AtomicLong sequence = new AtomicLong();

    public long nextId() {
        return sequence.incrementAndGet();
    }

    public Ticket save(Ticket ticket) {
        storage.put(ticket.getId(), ticket);
        return ticket;
    }

    public Optional<Ticket> findById(long id) {
        return Optional.ofNullable(storage.get(id));
    }

    public List<Ticket> findAll() {
        return new ArrayList<>(storage.values());
    }

    public List<Ticket> findByStatus(TicketStatus status) {
        return storage.values().stream()
                .filter(t -> t.getStatus() == status)
                .toList();
    }
}
