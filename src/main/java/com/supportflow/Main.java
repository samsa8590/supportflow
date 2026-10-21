package com.supportflow;

import com.supportflow.repository.EmployeeRepository;
import com.supportflow.repository.TicketRepository;
import com.supportflow.service.TicketService;
import com.supportflow.service.TicketServiceImpl;
import com.supportflow.ui.ConsoleApp;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

public class Main {

    public static void main(String[] args) {
        // Собираем зависимости вручную. Позже эту работу возьмёт на себя Spring.
        TicketService service = new TicketServiceImpl(new TicketRepository(), new EmployeeRepository());
        seedEmployees(service);

        // UTF-8 явно, чтобы русский текст не превращался в «????» в консоли
        PrintStream out = new PrintStream(System.out, true, StandardCharsets.UTF_8);
        new ConsoleApp(service, System.in, out).run();
    }

    private static void seedEmployees(TicketService service) {
        service.registerEmployee("Анна Смирнова", "anna@supportflow.local");
        service.registerEmployee("Иван Петров", "ivan@supportflow.local");
        service.registerEmployee("Мария Орлова", "maria@supportflow.local");
    }
}
