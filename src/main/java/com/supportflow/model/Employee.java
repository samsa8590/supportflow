package com.supportflow.model;

import java.util.Objects;

public class Employee {

    private final long id;
    private final String name;
    private final String email;

    public Employee(long id, String name, String email) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Имя сотрудника не может быть пустым");
        }
        this.id = id;
        this.name = name.trim();
        this.email = email;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Employee other && id == other.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return name + " (id=" + id + ")";
    }
}
