package ua.lpnu.kzp;

import java.util.Locale;
import java.util.Objects;

/**
 * Незмінний запис про візит тварини до ветклініки (варіант 21).
 *
 * <p>Інваріанти: {@code animal}, {@code species} і {@code visitType} непорожні;
 * {@code price} — скінченне невід'ємне число. Перевірка виконується в конструкторі,
 * тому некоректний об'єкт не може потрапити до списку чи статистики.
 */
public final class VetVisit {
    /** Кількість полів у CSV-рядку: animal;species;visitType;price;urgent. */
    private static final int FIELD_COUNT = 5;

    private final String animal;
    private final String species;
    private final String visitType;
    private final double price;
    private final boolean urgent;

    /**
     * Створює коректний візит після перевірки всіх інваріантів.
     *
     * @param animal ім'я тварини, непорожнє
     * @param species вид тварини, непорожній
     * @param visitType тип візиту, непорожній
     * @param price вартість візиту в гривнях, скінченна й невід'ємна (нуль допустимий)
     * @param urgent чи є візит ургентним
     * @throws NullPointerException якщо текстове поле дорівнює {@code null}
     * @throws IllegalArgumentException якщо текстове поле порожнє або ціна недопустима
     */
    public VetVisit(String animal, String species, String visitType, double price, boolean urgent) {
        // Перевірки йдуть до присвоєння, тому некоректний об'єкт не створюється.
        this.animal = requireText(animal, "animal");
        this.species = requireText(species, "species");
        this.visitType = requireText(visitType, "visitType");
        if (!Double.isFinite(price) || price < 0) {
            throw new IllegalArgumentException("price має бути скінченним невід'ємним числом");
        }
        this.price = price;
        this.urgent = urgent;
    }

    /**
     * Створює візит з одного CSV-рядка без заголовка.
     *
     * <p>Фабрика відповідає лише за структуру рядка й типи полів, а інваріанти перевіряє
     * конструктор. Текстові поля не обрізаються, як і в лабораторній роботі № 1.
     *
     * @param line рядок у форматі {@code animal;species;visitType;price;urgent}
     * @return створений візит
     * @throws NullPointerException якщо {@code line} дорівнює {@code null}
     * @throws IllegalArgumentException якщо структура, число, літерал або інваріант некоректні
     */
    public static VetVisit fromCsv(String line) {
        Objects.requireNonNull(line, "рядок не може бути null");
        if (line.isBlank()) {
            throw new IllegalArgumentException("порожній рядок");
        }
        // Ліміт -1 зберігає порожні кінцеві поля, тож "a;b;c;1;" дає 5 полів, а не 4.
        String[] fields = line.split(";", -1);
        if (fields.length != FIELD_COUNT) {
            throw new IllegalArgumentException(
                    "очікується %d полів, отримано %d".formatted(FIELD_COUNT, fields.length));
        }
        double price;
        try {
            price = Double.parseDouble(fields[3]);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("price має бути числом із крапкою", exception);
        }
        return new VetVisit(fields[0], fields[1], fields[2], price, parseUrgent(fields[4]));
    }

    /**
     * Перевіряє, що текстове поле задане й не складається лише з пробілів.
     *
     * @param value значення поля
     * @param name назва поля для повідомлення про помилку
     * @return те саме значення без змін
     */
    private static String requireText(String value, String name) {
        Objects.requireNonNull(value, "%s не може бути null".formatted(name));
        if (value.isBlank()) {
            throw new IllegalArgumentException("порожнє поле %s".formatted(name));
        }
        return value;
    }

    /**
     * Розбирає літерал {@code true} або {@code false}.
     *
     * <p>{@code Boolean.parseBoolean} не підходить: для "abc" він повертає {@code false}
     * без помилки, і хибний запис пройшов би перевірку.
     *
     * @param value текст поля urgent
     * @return значення ознаки ургентності
     */
    private static boolean parseUrgent(String value) {
        if ("true".equals(value)) {
            return true;
        }
        if ("false".equals(value)) {
            return false;
        }
        throw new IllegalArgumentException("urgent має дорівнювати точно true або false");
    }

    /**
     * Повертає ім'я тварини.
     *
     * @return непорожнє ім'я
     */
    public String getAnimal() {
        return animal;
    }

    /**
     * Повертає вид тварини.
     *
     * @return непорожній вид
     */
    public String getSpecies() {
        return species;
    }

    /**
     * Повертає тип візиту.
     *
     * @return непорожній тип візиту
     */
    public String getVisitType() {
        return visitType;
    }

    /**
     * Повертає вартість візиту.
     *
     * @return скінченна невід'ємна вартість у гривнях
     */
    public double getPrice() {
        return price;
    }

    /**
     * Повідомляє, чи є візит ургентним.
     *
     * @return {@code true} для ургентного візиту
     */
    public boolean isUrgent() {
        return urgent;
    }

    /**
     * Повертає текстове подання візиту для налагодження та списків.
     *
     * <p>Ціна форматується з {@code Locale.ROOT}, тому десятковий роздільник — крапка
     * на будь-якій ОС. Формат CSV цей метод не замінює.
     *
     * @return рядок на кшталт {@code Мурчик (Кіт), Огляд: 450.00 грн, ургентний: ні}
     */
    @Override
    public String toString() {
        return String.format(Locale.ROOT, "%s (%s), %s: %.2f грн, ургентний: %s",
                animal, species, visitType, price, urgent ? "так" : "ні");
    }
}
