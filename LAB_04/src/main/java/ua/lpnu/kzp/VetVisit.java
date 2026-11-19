package ua.lpnu.kzp;

import java.util.Locale;
import java.util.Objects;

/**
 * Спільний абстрактний тип візиту до ветклініки (варіант 21).
 *
 * <p>Тут зібрано все, що однакове для всіх візитів: поля {@code animal}, {@code species},
 * {@code visitType}, {@code price}, категорію {@link VisitKind}, їхню перевірку, розбір
 * CSV, рівність і текстове подання. Підтипи {@link RoutineVisit} і {@link UrgentVisit}
 * відрізняються лише тим, як обчислюють {@link #finalCost()}, і власними правилами.
 *
 * <p>Інваріанти: текстові поля непорожні; {@code price} — скінченне невід'ємне число;
 * категорія задана. Перевірка виконується до присвоєння полів, тому некоректний
 * об'єкт не може потрапити до списку чи статистики.
 */
public abstract class VetVisit {
    /** Кількість полів у CSV-рядку: animal;species;visitType;price;urgent. */
    private static final int FIELD_COUNT = 5;

    private final String animal;
    private final String species;
    private final String visitType;
    private final double price;
    private final VisitKind kind;

    /**
     * Створює спільну частину візиту після перевірки всіх інваріантів.
     *
     * <p>Конструктор {@code protected}: базовий об'єкт без підтипу створити не можна,
     * його викликають лише конструктори підтипів через {@code super(...)}.
     *
     * @param animal ім'я тварини, непорожнє
     * @param species вид тварини, непорожній
     * @param visitType тип візиту, непорожній
     * @param price базова вартість візиту в гривнях, скінченна й невід'ємна
     * @param kind категорія візиту, яку передає підтип
     * @throws NullPointerException якщо текстове поле або категорія дорівнює {@code null}
     * @throws IllegalArgumentException якщо текстове поле порожнє або ціна недопустима
     */
    protected VetVisit(String animal, String species, String visitType, double price,
            VisitKind kind) {
        // Аргументи перевіряються зліва направо ще до виклику приватного конструктора,
        // тож об'єкт із хибним станом навіть не починає створюватися.
        this(requireText(animal, "animal"), requireText(species, "species"),
                requireText(visitType, "visitType"), requirePrice(price), requireKind(kind), true);
    }

    /* Присвоює вже перевірений стан; boolean лише відрізняє сигнатуру від protected. */
    private VetVisit(String animal, String species, String visitType, double price,
            VisitKind kind, boolean validated) {
        this.animal = animal;
        this.species = species;
        this.visitType = visitType;
        this.price = price;
        this.kind = kind;
    }

    /**
     * Створює візит потрібного підтипу за ознакою ургентності.
     *
     * <p>Це єдине місце програми, де обирається конкретний клас. Решта коду працює з
     * типом {@code VetVisit} і не перевіряє, який підтип отримала.
     *
     * @param animal ім'я тварини
     * @param species вид тварини
     * @param visitType тип візиту
     * @param price базова вартість візиту
     * @param urgent {@code true} — {@link UrgentVisit}, {@code false} — {@link RoutineVisit}
     * @return новий візит відповідного підтипу
     * @throws NullPointerException якщо текстове поле дорівнює {@code null}
     * @throws IllegalArgumentException якщо порушено інваріант базового типу чи підтипу
     */
    public static VetVisit of(String animal, String species, String visitType, double price,
            boolean urgent) {
        return urgent
                ? new UrgentVisit(animal, species, visitType, price)
                : new RoutineVisit(animal, species, visitType, price);
    }

    /**
     * Створює візит з одного CSV-рядка без заголовка.
     *
     * <p>Фабрика відповідає лише за структуру рядка й типи полів, а інваріанти перевіряють
     * конструктори. Текстові поля не обрізаються, як і в лабораторній роботі № 1.
     *
     * @param line рядок у форматі {@code animal;species;visitType;price;urgent}
     * @return створений візит: {@link RoutineVisit} або {@link UrgentVisit}
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
        return of(fields[0], fields[1], fields[2], price, parseUrgent(fields[4]));
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
     * Перевіряє спільне для всіх підтипів правило ціни.
     *
     * @param price базова вартість
     * @return та сама вартість, якщо вона скінченна й невід'ємна
     */
    private static double requirePrice(double price) {
        if (!Double.isFinite(price) || price < 0) {
            throw new IllegalArgumentException("price має бути скінченним невід'ємним числом");
        }
        return price;
    }

    /**
     * Перевіряє, що підтип передав категорію.
     *
     * @param kind категорія візиту
     * @return та сама категорія
     */
    private static VisitKind requireKind(VisitKind kind) {
        return Objects.requireNonNull(kind, "kind не може бути null");
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
     * Обчислює підсумкову вартість візиту — поліморфна операція варіанта 21.
     *
     * <p>Кожен підтип дає власну реалізацію. Код звіту викликає цей метод через тип
     * {@code VetVisit}, а Java під час виконання обирає реалізацію фактичного об'єкта.
     *
     * @return підсумкова вартість у гривнях, скінченна й невід'ємна
     */
    public abstract double finalCost();

    /**
     * Повертає ім'я тварини.
     *
     * @return непорожнє ім'я
     */
    public final String getAnimal() {
        return animal;
    }

    /**
     * Повертає вид тварини.
     *
     * @return непорожній вид
     */
    public final String getSpecies() {
        return species;
    }

    /**
     * Повертає тип візиту.
     *
     * @return непорожній тип візиту
     */
    public final String getVisitType() {
        return visitType;
    }

    /**
     * Повертає базову вартість візиту (поле {@code price} із CSV).
     *
     * @return скінченна невід'ємна вартість у гривнях
     */
    public final double getPrice() {
        return price;
    }

    /**
     * Повертає категорію візиту.
     *
     * @return {@link VisitKind#ROUTINE} або {@link VisitKind#URGENT}
     */
    public final VisitKind getKind() {
        return kind;
    }

    /**
     * Повідомляє, чи є візит ургентним; відповідь бере з категорії.
     *
     * @return {@code true} для ургентного візиту
     */
    public final boolean isUrgent() {
        return kind.isUrgent();
    }

    /**
     * Порівнює візити за підтипом і всіма полями запису.
     *
     * <p>Два однакові рядки CSV дають рівні об'єкти. Перевірка {@code getClass()} не
     * допускає рівності планового й ургентного візиту з однаковими полями.
     *
     * @param other об'єкт для порівняння
     * @return {@code true}, якщо підтип і всі поля збігаються
     */
    @Override
    public final boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        VetVisit visit = (VetVisit) other;
        // Double.compare коректно порівнює double, зокрема 0.0 і -0.0 узгоджено з hashCode.
        return Double.compare(price, visit.price) == 0
                && animal.equals(visit.animal)
                && species.equals(visit.species)
                && visitType.equals(visit.visitType);
    }

    /**
     * Повертає хеш-код, узгоджений із {@link #equals(Object)}: ті самі поля й підтип.
     *
     * @return хеш-код візиту
     */
    @Override
    public final int hashCode() {
        return Objects.hash(getClass(), animal, species, visitType, price);
    }

    /**
     * Повертає текстове подання візиту для налагодження та списків.
     *
     * <p>Ціна форматується з {@code Locale.ROOT}, тому десятковий роздільник — крапка
     * на будь-якій ОС. Формат збережено з лабораторної роботи № 2.
     *
     * @return рядок на кшталт {@code Мурчик (Кіт), Огляд: 450.00 грн, ургентний: ні}
     */
    @Override
    public String toString() {
        return String.format(Locale.ROOT, "%s (%s), %s: %.2f грн, ургентний: %s",
                animal, species, visitType, price, isUrgent() ? "так" : "ні");
    }
}
