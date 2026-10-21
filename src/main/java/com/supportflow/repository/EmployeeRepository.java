package com.supportflow.repository;

import com.supportflow.model.Employee;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

public class EmployeeRepository {

    private final Map<Long, Employee> storage = new LinkedHashMap<>();
    private final AtomicLong sequence = new AtomicLong();

    public long nextId() {
        return sequence.incrementAndGet();
    }

    public Employee save(Employee employee) {
        storage.put(employee.getId(), employee);
        return employee;
    }

    public Optional<Employee> findById(long id) {
        return Optional.ofNullable(storage.get(id));
    }

    public Optional<Employee> findByEmail(String email) {
        return storage.values().stream()
                .filter(e -> e.getEmail() != null && e.getEmail().equalsIgnoreCase(email))
                .findFirst();
    }

    public List<Employee> findAll() {
        return new ArrayList<>(storage.values());
    }
}
