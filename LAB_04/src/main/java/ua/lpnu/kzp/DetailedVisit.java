package ua.lpnu.kzp;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Objects;

/**
 * Візит із додатковими даними для потокових запитів.
 *
 * @param visit візит із базовими полями та поліморфним обчисленням вартості
 * @param owner власник тварини
 * @param date дата візиту
 * @param diagnosis діагноз
 */
public record DetailedVisit(VetVisit visit, String owner, LocalDate date, String diagnosis) {
    private static final int FIELD_COUNT = 8;

    /**
     * Створює розширений візит із відомим видом тварини й заповненими додатковими полями.
     *
     * @throws NullPointerException якщо будь-який аргумент дорівнює {@code null}
     * @throws IllegalArgumentException якщо вид невідомий або текстове поле порожнє
     */
    public DetailedVisit {
        Objects.requireNonNull(visit, "visit не може бути null");
        AnimalSpecies.fromLabel(visit.getSpecies());
        requireText(owner, "owner");
        Objects.requireNonNull(date, "date не може бути null");
        requireText(diagnosis, "diagnosis");
    }

    /** Повертає вид тварини для типізованого групування. */
    public AnimalSpecies species() {
        return AnimalSpecies.fromLabel(visit.getSpecies());
    }

    /**
     * Створює розширений візит із рядка CSV без заголовка.
     *
     * @param line рядок у форматі {@code animal;species;visitType;price;urgent;owner;date;diagnosis}
     * @return візит із датою, розібраною за ISO-8601
     * @throws NullPointerException якщо рядок дорівнює {@code null}
     * @throws IllegalArgumentException якщо структура рядка чи значення полів некоректні
     */
    public static DetailedVisit fromCsv(String line) {
        String[] fields = VetVisit.csvFields(line, FIELD_COUNT);
        VetVisit visit = VetVisit.fromCsvFields(fields);
        LocalDate date;
        try {
            date = LocalDate.parse(fields[6]);
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException("date має бути коректною датою ISO (YYYY-MM-DD)",
                    exception);
        }
        return new DetailedVisit(visit, fields[5], date, fields[7]);
    }

    /** Перевіряє наявність тексту, зберігаючи його початкове написання. */
    private static void requireText(String value, String fieldName) {
        Objects.requireNonNull(value, "%s не може бути null".formatted(fieldName));
        if (value.isBlank()) {
            throw new IllegalArgumentException("порожнє поле %s".formatted(fieldName));
        }
    }
}
