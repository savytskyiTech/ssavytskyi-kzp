package ua.lpnu.kzp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Locale;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Тести спільного типу {@link VetVisit}: перенесені з LAB_02 без зміни очікувань.
 *
 * <p>У LAB_03 клас абстрактний, тому {@code new VetVisit(...)} замінено фабрикою
 * {@link VetVisit#of}: вона створює потрібний підтип з тими самими аргументами.
 */
class VetVisitTest {
    private final Locale originalLocale = Locale.getDefault();

    @AfterEach
    void restoreLocale() {
        Locale.setDefault(originalLocale);
    }

    /* Конструктор зберігає всі поля без змін. */
    @Test
    void constructorStoresAllFields() {
        // Arrange і Act
        VetVisit visit = VetVisit.of("Рекс", "Собака", "Невідкладна допомога", 750.0, true);

        // Assert
        assertEquals("Рекс", visit.getAnimal());
        assertEquals("Собака", visit.getSpecies());
        assertEquals("Невідкладна допомога", visit.getVisitType());
        assertEquals(750.0, visit.getPrice(), 0.0001);
        assertTrue(visit.isUrgent());
    }

    /* Нульова ціна — коректний граничний випадок (безкоштовний візит). */
    @Test
    void constructorAcceptsZeroPrice() {
        VetVisit visit = VetVisit.of("Соня", "Кролик", "Щеплення", 0.0, false);

        assertEquals(0.0, visit.getPrice(), 0.0001);
        assertFalse(visit.isUrgent());
    }

    /* null у текстовому полі відрізняється від порожнього тексту: це NullPointerException. */
    @ParameterizedTest(name = "{0}")
    @MethodSource("nullTextFields")
    void constructorRejectsNullText(String field, String animal, String species, String visitType) {
        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> VetVisit.of(animal, species, visitType, 100.0, false));
        assertEquals("%s не може бути null".formatted(field), exception.getMessage());
    }

    static Stream<Arguments> nullTextFields() {
        return Stream.of(
                Arguments.of("animal", null, "Кіт", "Огляд"),
                Arguments.of("species", "Мурчик", null, "Огляд"),
                Arguments.of("visitType", "Мурчик", "Кіт", null));
    }

    /* Порожній текст або лише пробіли відхиляються з назвою поля. */
    @ParameterizedTest(name = "{0}: \"{1}\"")
    @MethodSource("blankTextFields")
    void constructorRejectsBlankText(String field, String animal, String species, String visitType) {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> VetVisit.of(animal, species, visitType, 100.0, false));
        assertEquals("порожнє поле %s".formatted(field), exception.getMessage());
    }

    static Stream<Arguments> blankTextFields() {
        return Stream.of(
                Arguments.of("animal", "", "Кіт", "Огляд"),
                Arguments.of("animal", "   ", "Кіт", "Огляд"),
                Arguments.of("species", "Мурчик", "", "Огляд"),
                Arguments.of("species", "Мурчик", "\t", "Огляд"),
                Arguments.of("visitType", "Мурчик", "Кіт", ""),
                Arguments.of("visitType", "Мурчик", "Кіт", " "));
    }

    /* Від'ємна, NaN і нескінченна ціна зіпсували б статистику, тому відхиляються. */
    @ParameterizedTest
    @ValueSource(doubles = {-0.01, -100.0, Double.NaN, Double.POSITIVE_INFINITY,
        Double.NEGATIVE_INFINITY})
    void constructorRejectsInvalidPrice(double price) {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> VetVisit.of("Мурчик", "Кіт", "Огляд", price, false));
        assertEquals("price має бути скінченним невід'ємним числом", exception.getMessage());
    }

    /* Коректні рядки з LAB_01 дають той самий набір полів. */
    @ParameterizedTest(name = "{0}")
    @MethodSource("validLines")
    void fromCsvAcceptsValidLine(String name, String line, String animal, String species,
            String visitType, double price, boolean urgent) {
        VetVisit visit = VetVisit.fromCsv(line);

        assertEquals(animal, visit.getAnimal());
        assertEquals(species, visit.getSpecies());
        assertEquals(visitType, visit.getVisitType());
        assertEquals(price, visit.getPrice(), 0.0001);
        assertEquals(urgent, visit.isUrgent());
    }

    static Stream<Arguments> validLines() {
        return Stream.of(
                Arguments.of("Звичайний візит", "Мурчик;Кіт;Огляд;450.00;false",
                        "Мурчик", "Кіт", "Огляд", 450.0, false),
                Arguments.of("Український текст із пробілом",
                        "Рекс;Собака;Невідкладна допомога;750.00;true",
                        "Рекс", "Собака", "Невідкладна допомога", 750.0, true),
                Arguments.of("Нульова ціна", "Соня;Кролик;Щеплення;0;false",
                        "Соня", "Кролик", "Щеплення", 0.0, false),
                Arguments.of("Крайові пробіли в тексті зберігаються", " Мурчик ;Кіт;Огляд;1;true",
                        " Мурчик ", "Кіт", "Огляд", 1.0, true));
    }

    /* Усі хибні рядки з LAB_01 відхиляються з тими самими повідомленнями. */
    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidLines")
    void fromCsvRejectsInvalidLine(String name, String line, String message) {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class, () -> VetVisit.fromCsv(line));
        assertTrue(exception.getMessage().contains(message), exception.getMessage());
    }

    static Stream<Arguments> invalidLines() {
        return Stream.of(
                Arguments.of("Порожній рядок", "", "порожній рядок"),
                Arguments.of("Лише пробіли", " ", "порожній рядок"),
                Arguments.of("Чотири поля", "a;b;c;1", "очікується 5 полів, отримано 4"),
                Arguments.of("Шість полів", "a;b;c;1;true;extra", "очікується 5 полів, отримано 6"),
                Arguments.of("Зайве порожнє поле", "a;b;c;1;true;", "очікується 5 полів, отримано 6"),
                Arguments.of("Порожнє ім'я", ";Кіт;Огляд;300;false", "порожнє поле animal"),
                Arguments.of("Порожній вид", "x;;y;1;true", "порожнє поле species"),
                Arguments.of("Порожній тип візиту", "x;y;;1;true", "порожнє поле visitType"),
                Arguments.of("Текст замість ціни", "x;y;z;abc;false", "price має бути числом"),
                Arguments.of("Порожня ціна", "x;y;z;;false", "price має бути числом"),
                Arguments.of("Десяткова кома", "x;y;z;12,5;false", "price має бути числом"),
                Arguments.of("Від'ємна ціна", "x;y;z;-100;false", "скінченним невід'ємним"),
                Arguments.of("NaN", "x;y;z;NaN;false", "скінченним невід'ємним"),
                Arguments.of("Нескінченність", "x;y;z;Infinity;false", "скінченним невід'ємним"),
                Arguments.of("Переповнення ціни", "x;y;z;1e400;false", "скінченним невід'ємним"),
                Arguments.of("True з великої літери", "x;y;z;1;True", "urgent має дорівнювати точно true або false"),
                Arguments.of("TRUE великими літерами", "x;y;z;1;TRUE", "urgent має дорівнювати точно true або false"),
                Arguments.of("Українське так", "x;y;z;1;так", "urgent має дорівнювати точно true або false"),
                Arguments.of("Число замість boolean", "x;y;z;1;1", "urgent має дорівнювати точно true або false"),
                Arguments.of("Порожній urgent", "x;y;z;1;", "urgent має дорівнювати точно true або false"));
    }

    /* Помилка формату числа зберігає початкову причину для налагодження. */
    @Test
    void fromCsvKeepsNumberFormatCause() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> VetVisit.fromCsv("Мурчик;Кіт;Огляд;помилка;false"));
        assertInstanceOf(NumberFormatException.class, exception.getCause());
    }

    /* Відсутній рядок — помилка програміста, а не хибні дані. */
    @Test
    void fromCsvRejectsNull() {
        assertThrows(NullPointerException.class, () -> VetVisit.fromCsv(null));
    }

    /* toString використовує Locale.ROOT: крапка навіть в українській локалі. */
    @Test
    void toStringUsesDecimalPointInUkrainianLocale() {
        Locale.setDefault(Locale.forLanguageTag("uk-UA"));
        VetVisit visit = VetVisit.of("Рекс", "Собака", "Огляд", 1234.5, true);

        assertEquals("Рекс (Собака), Огляд: 1234.50 грн, ургентний: так", visit.toString());
    }

    /* Неургентний візит позначається словом «ні». */
    @Test
    void toStringShowsNonUrgentVisit() {
        VetVisit visit = VetVisit.fromCsv("Мурчик;Кіт;Огляд;450.00;false");

        assertEquals("Мурчик (Кіт), Огляд: 450.00 грн, ургентний: ні", visit.toString());
    }
}
