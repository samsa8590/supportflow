package com.supportflow.ui;

import com.supportflow.exception.InvalidStatusTransitionException;
import com.supportflow.exception.TicketNotFoundException;
import com.supportflow.model.Comment;
import com.supportflow.model.Employee;
import com.supportflow.model.Priority;
import com.supportflow.model.Ticket;
import com.supportflow.model.TicketStatus;
import com.supportflow.service.TicketService;

import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;
import java.util.function.Function;

/**
 * Консольный интерфейс. Знает только про TicketService: никакой бизнес-логики здесь нет,
 * только ввод, вывод и перехват ошибок.
 */
public class ConsoleApp {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm").withZone(ZoneId.systemDefault());

    private final TicketService service;
    private final Scanner in;
    private final PrintStream out;

    public ConsoleApp(TicketService service, InputStream input, PrintStream out) {
        this.service = service;
        this.in = new Scanner(input, StandardCharsets.UTF_8);
        this.out = out;
    }

    public void run() {
        out.println("=== SupportFlow: система обработки обращений ===");
        try {
            boolean running = true;
            while (running) {
                printMenu();
                String choice = prompt("Выбор").trim();
                try {
                    running = handle(choice);
                } catch (TicketNotFoundException | InvalidStatusTransitionException
                         | IllegalArgumentException | IllegalStateException e) {
                    // сюда попадают и ошибки ввода чисел: они бросаются как IllegalArgumentException
                    out.println("Ошибка: " + e.getMessage());
                }
            }
        } catch (InputEnded e) {
            // поток ввода закрыт (Ctrl+D или конец файла): выходим спокойно, без стектрейса
            out.println();
            out.println("Ввод завершён.");
        }
        out.println("До свидания!");
    }

    private boolean handle(String choice) {
        switch (choice) {
            case "1" -> createTicket();
            case "2" -> listTickets();
            case "3" -> showTicket();
            case "4" -> changeStatus();
            case "5" -> assignTicket();
            case "6" -> addComment();
            case "0" -> {
                return false;
            }
            default -> out.println("Неизвестный пункт меню: " + choice);
        }
        return true;
    }

    // ---------- сценарии ----------

    private void createTicket() {
        long authorId = chooseEmployee("Кто создаёт обращение");
        String title = prompt("Тема");
        String description = prompt("Описание");
        Priority priority = chooseEnum(Priority.class, "Приоритет", Priority::getTitle);
        Ticket ticket = service.createTicket(title, description, priority, authorId);
        out.println("Создано обращение #" + ticket.getId());
    }

    private void listTickets() {
        out.println("Фильтр по статусу (Enter, чтобы показать все):");
        TicketStatus filter = chooseOptionalEnum(TicketStatus.class, TicketStatus::getTitle);
        List<Ticket> tickets = service.listTickets(filter);
        if (tickets.isEmpty()) {
            out.println("Обращений нет.");
            return;
        }
        out.printf("%-5s %-12s %-18s %-18s %s%n", "ID", "Приоритет", "Статус", "Исполнитель", "Тема");
        for (Ticket t : tickets) {
            String assignee = t.getAssignee().map(Employee::getName).orElse("—");
            out.printf("%-5d %-12s %-18s %-18s %s%n", t.getId(), t.getPriority().getTitle(),
                    t.getStatus().getTitle(), assignee, t.getTitle());
        }
    }

    private void showTicket() {
        Ticket t = service.getTicket(readLong("ID обращения"));
        out.println("--------------------------------------------");
        out.println("Обращение #" + t.getId() + ": " + t.getTitle());
        out.println("Статус:      " + t.getStatus().getTitle());
        out.println("Приоритет:   " + t.getPriority().getTitle());
        out.println("Автор:       " + t.getAuthor().getName());
        out.println("Исполнитель: " + t.getAssignee().map(Employee::getName).orElse("не назначен"));
        out.println("Создано:     " + DATE_FORMAT.format(t.getCreatedAt()));
        out.println("Обновлено:   " + DATE_FORMAT.format(t.getUpdatedAt()));
        out.println("Описание:    " + (t.getDescription().isEmpty() ? "—" : t.getDescription()));
        out.println("Допустимые переходы: " + (t.getStatus().allowedTransitions().isEmpty()
                ? "нет"
                : t.getStatus().allowedTransitions().stream().map(TicketStatus::getTitle).toList()));
        out.println("Комментарии (" + t.getComments().size() + "):");
        for (Comment c : t.getComments()) {
            out.printf("  [%s] %s: %s%n", DATE_FORMAT.format(c.createdAt()),
                    c.author().getName(), c.text());
        }
        out.println("--------------------------------------------");
    }

    private void changeStatus() {
        long id = readLong("ID обращения");
        TicketStatus target = chooseEnum(TicketStatus.class, "Новый статус", TicketStatus::getTitle);
        Ticket t = service.changeStatus(id, target);
        out.println("Статус обращения #" + t.getId() + ": " + t.getStatus().getTitle());
    }

    private void assignTicket() {
        long id = readLong("ID обращения");
        long employeeId = chooseEmployee("Кого назначить");
        Ticket t = service.assign(id, employeeId);
        out.println("Исполнитель обращения #" + t.getId() + ": "
                + t.getAssignee().map(Employee::getName).orElse("не назначен"));
    }

    private void addComment() {
        long id = readLong("ID обращения");
        long authorId = chooseEmployee("Кто комментирует");
        String text = prompt("Текст комментария");
        service.addComment(id, authorId, text);
        out.println("Комментарий добавлен.");
    }

    // ---------- ввод и вывод ----------

    private void printMenu() {
        out.println();
        out.println("1. Создать обращение");
        out.println("2. Список обращений");
        out.println("3. Показать обращение");
        out.println("4. Сменить статус");
        out.println("5. Назначить исполнителя");
        out.println("6. Добавить комментарий");
        out.println("0. Выход");
    }

    private long chooseEmployee(String label) {
        out.println(label + ":");
        for (Employee e : service.listEmployees()) {
            out.println("  " + e.getId() + " — " + e.getName());
        }
        return readLong("ID сотрудника");
    }

    private <E extends Enum<E>> E chooseEnum(Class<E> type, String label, Function<E, String> title) {
        out.println(label + " (номер, имя или название):");
        printOptions(type, title);
        return parseEnum(type, title, prompt("Значение").trim());
    }

    /** Пустой ввод означает «без фильтра» и возвращает null. */
    private <E extends Enum<E>> E chooseOptionalEnum(Class<E> type, Function<E, String> title) {
        printOptions(type, title);
        String line = prompt("Значение").trim();
        return line.isEmpty() ? null : parseEnum(type, title, line);
    }

    private <E extends Enum<E>> void printOptions(Class<E> type, Function<E, String> title) {
        E[] values = type.getEnumConstants();
        for (int i = 0; i < values.length; i++) {
            out.println("  " + (i + 1) + " — " + title.apply(values[i]) + " (" + values[i].name() + ")");
        }
    }

    /** Принимает номер из списка, имя константы (high, RESOLVED) или русское название. */
    private <E extends Enum<E>> E parseEnum(Class<E> type, Function<E, String> title, String input) {
        E[] values = type.getEnumConstants();
        if (input.chars().allMatch(Character::isDigit) && !input.isEmpty()) {
            long number = parseNumber(input);
            if (number < 1 || number > values.length) {
                throw new IllegalArgumentException("Нет варианта с номером " + number);
            }
            return values[(int) number - 1];
        }
        for (E value : values) {
            if (value.name().equalsIgnoreCase(input) || title.apply(value).equalsIgnoreCase(input)) {
                return value;
            }
        }
        throw new IllegalArgumentException("Неизвестное значение: «" + input + "»");
    }

    private long readLong(String label) {
        return parseNumber(prompt(label).trim());
    }

    private long parseNumber(String line) {
        try {
            return Long.parseLong(line);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Ожидалось число, а введено: «" + line + "»");
        }
    }

    private String prompt(String label) {
        out.print(label + ": ");
        out.flush();
        if (!in.hasNextLine()) {
            throw new InputEnded();
        }
        return in.nextLine();
    }

    /** Внутренний сигнал: поток ввода закрыт, продолжать нечем. */
    private static class InputEnded extends RuntimeException {
        private static final long serialVersionUID = 1L;

        InputEnded() {
            super(null, null, false, false);   // без стектрейса: это не ошибка
        }
    }
}
