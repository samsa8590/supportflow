package com.supportflow.ui;

import com.supportflow.repository.EmployeeRepository;
import com.supportflow.repository.TicketRepository;
import com.supportflow.service.TicketService;
import com.supportflow.service.TicketServiceImpl;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ConsoleAppTest {

    private String runSession(String input) {
        TicketService service = new TicketServiceImpl(new TicketRepository(), new EmployeeRepository());
        service.registerEmployee("Анна", "anna@example.com");
        service.registerEmployee("Иван", "ivan@example.com");

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(buffer, true, StandardCharsets.UTF_8);
        new ConsoleApp(service, new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)), out).run();
        return buffer.toString(StandardCharsets.UTF_8);
    }

    @Test
    void createAndShowTicket() {
        String output = runSession(String.join("\n",
                "1", "1", "Не работает почта", "Письма не приходят", "high",
                "3", "1",
                "0")
                + "\n");
        assertTrue(output.contains("Создано обращение #1"));
        assertTrue(output.contains("Не работает почта"));
        assertTrue(output.contains("Высокий"));
    }

    @Test
    void invalidTransitionShowsFriendlyError() {
        String output = runSession(String.join("\n",
                "1", "1", "Тема", "Описание", "LOW",
                "4", "1", "RESOLVED",
                "0")
                + "\n");
        assertTrue(output.contains("Ошибка: Переход из «Новое» в «Решено» недопустим"));
    }

    @Test
    void badNumberDoesNotCrash() {
        String output = runSession("3\nabc\n0\n");
        assertTrue(output.contains("Ожидалось число"));
        assertTrue(output.contains("До свидания!"));
    }

    @Test
    void endOfInputExitsGracefully() {
        String output = runSession("2\n");
        assertTrue(output.contains("Ввод завершён"));
    }
}
