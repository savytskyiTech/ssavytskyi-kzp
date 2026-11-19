package ua.lpnu.kzp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

/** Тести підтипів {@link RoutineVisit} і {@link UrgentVisit} та поліморфного {@code finalCost}. */
class VisitSubtypesTest {
    /* Плановий візит приймає коректні поля й не змінює базову ціну. */
    @Test
    void routineVisitAcceptsValidFields() {
        RoutineVisit visit = new RoutineVisit("Мурчик", "Кіт", "Огляд", 450.0);

        assertEquals("Мурчик", visit.getAnimal());
        assertEquals("Кіт", visit.getSpecies());
        assertEquals("Огляд", visit.getVisitType());
        assertEquals(450.0, visit.getPrice(), 0.0001);
        assertFalse(visit.isUrgent());
        assertEquals(450.0, visit.finalCost(), 0.0001);
    }

    /* Нульова ціна — граничний коректний випадок для планового візиту. */
    @Test
    void routineVisitAcceptsZeroPrice() {
        assertEquals(0.0, new RoutineVisit("Соня", "Кролик", "Повторний огляд", 0.0).finalCost(), 0.0001);
    }

    /* Ургентний візит приймає коректні поля й додає надбавку 50 %. */
    @Test
    void urgentVisitAcceptsValidFields() {
        UrgentVisit visit = new UrgentVisit("Рекс", "Собака", "Невідкладна допомога", 750.0);

        assertEquals("Рекс", visit.getAnimal());
        assertTrue(visit.isUrgent());
        assertEquals(750.0, visit.getPrice(), 0.0001);
        assertEquals(1125.0, visit.finalCost(), 0.0001);
    }

    /* Мінімальна додатна ціна — граничний коректний випадок для ургентного візиту. */
    @Test
    void urgentVisitAcceptsSmallestPositivePrice() {
        assertEquals(0.015, new UrgentVisit("x", "y", "z", 0.01).finalCost(), 0.0001);
    }

    /* Власне правило ургентного візиту: нульова ціна недопустима. */
    @ParameterizedTest
    @ValueSource(doubles = {0.0, -0.0})
    void urgentVisitRejectsZeroPrice(double price) {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new UrgentVisit("Рекс", "Собака", "Огляд", price));
        assertEquals("ургентний візит не може бути безкоштовним", exception.getMessage());
    }

    /* Спільні правила перевіряє базовий тип для обох підтипів однаково. */
    @Test
    void bothSubtypesRejectSharedInvalidState() {
        assertEquals("порожнє поле animal", assertThrows(IllegalArgumentException.class,
                () -> new RoutineVisit(" ", "Кіт", "Огляд", 1.0)).getMessage());
        assertEquals("порожнє поле animal", assertThrows(IllegalArgumentException.class,
                () -> new UrgentVisit(" ", "Кіт", "Огляд", 1.0)).getMessage());
        assertEquals("price має бути скінченним невід'ємним числом",
                assertThrows(IllegalArgumentException.class,
                        () -> new RoutineVisit("x", "y", "z", -1.0)).getMessage());
        assertEquals("price має бути скінченним невід'ємним числом",
                assertThrows(IllegalArgumentException.class,
                        () -> new UrgentVisit("x", "y", "z", Double.NaN)).getMessage());
        assertThrows(NullPointerException.class, () -> new UrgentVisit("x", null, "z", 1.0));
    }

    /* Спершу перевіряється спільний стан, потім правило підтипу. */
    @Test
    void sharedRulesAreCheckedBeforeSubtypeRule() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> VetVisit.fromCsv(";Кіт;Огляд;0;true"));
        assertEquals("порожнє поле animal", exception.getMessage());
    }

    /* Фабрика fromCsv обирає підтип за полем urgent, формат CSV не змінився. */
    @ParameterizedTest(name = "{0}")
    @CsvSource(delimiter = '|', value = {
        "Мурчик;Кіт;Огляд;450.00;false | false",
        "Рекс;Собака;Невідкладна допомога;750.00;true | true"})
    void fromCsvCreatesMatchingSubtype(String line, boolean urgent) {
        VetVisit visit = VetVisit.fromCsv(line);

        assertEquals(urgent ? UrgentVisit.class : RoutineVisit.class, visit.getClass());
    }

    /* Правило підтипу діє і для рядка CSV: повідомлення піде у «Рядок N: …». */
    @Test
    void fromCsvRejectsFreeUrgentVisit() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> VetVisit.fromCsv("Рекс;Собака;Огляд;0;true"));
        assertEquals("ургентний візит не може бути безкоштовним", exception.getMessage());
    }

    /* Безкоштовний плановий візит із CSV лишається коректним, як у LAB_02. */
    @Test
    void fromCsvAcceptsFreeRoutineVisit() {
        assertInstanceOf(RoutineVisit.class, VetVisit.fromCsv("Соня;Кролик;Щеплення;0;false"));
    }

    /* Один і той самий виклик через базовий тип дає різний результат для різних підтипів. */
    @Test
    void subtypesCalculateFinalCostDifferently() {
        VetVisit routine = new RoutineVisit("Мурчик", "Кіт", "Огляд", 400.0);
        VetVisit urgent = new UrgentVisit("Мурчик", "Кіт", "Огляд", 400.0);

        assertEquals(400.0, routine.finalCost(), 0.0001);
        assertEquals(600.0, urgent.finalCost(), 0.0001);
        assertNotEquals(routine.finalCost(), urgent.finalCost());
    }

    /* Поліморфна колекція: цикл по List<VetVisit> без instanceof рахує суму правильно. */
    @Test
    void polymorphicCollectionSumsFinalCost() {
        List<VetVisit> visits = List.of(
                new RoutineVisit("Мурчик", "Кіт", "Огляд", 450.0),
                new UrgentVisit("Рекс", "Собака", "Невідкладна допомога", 750.0),
                new RoutineVisit("Соня", "Кролик", "Щеплення", 600.0));

        double total = 0;
        for (VetVisit visit : visits) {
            total += visit.finalCost();
        }

        assertEquals(2175.0, total, 0.0001);
    }

    /* Фабрика of і конструктор підтипу дають рівні об'єкти. */
    @Test
    void factoryOfMatchesSubtypeConstructors() {
        assertEquals(new UrgentVisit("x", "y", "z", 1.0), VetVisit.of("x", "y", "z", 1.0, true));
        assertEquals(new RoutineVisit("x", "y", "z", 1.0), VetVisit.of("x", "y", "z", 1.0, false));
    }
}
