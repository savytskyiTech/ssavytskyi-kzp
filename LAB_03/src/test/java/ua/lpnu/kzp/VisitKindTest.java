package ua.lpnu.kzp;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** Тести enum {@link VisitKind}: фіксований набір, підписи, ознака ургентності. */
class VisitKindTest {
    /* Набір категорій фіксований: рівно дві константи в заданому порядку. */
    @Test
    void hasExactlyTwoKinds() {
        assertArrayEquals(new VisitKind[] {VisitKind.ROUTINE, VisitKind.URGENT}, VisitKind.values());
    }

    /* Кожна константа має український підпис для звіту. */
    @Test
    void labelsAreUkrainian() {
        assertEquals("плановий", VisitKind.ROUTINE.label());
        assertEquals("ургентний", VisitKind.URGENT.label());
    }

    /* Ознака ургентності зберігається в полі enum, а не обчислюється умовою. */
    @Test
    void onlyUrgentKindIsUrgent() {
        assertFalse(VisitKind.ROUTINE.isUrgent());
        assertTrue(VisitKind.URGENT.isUrgent());
    }

    /* valueOf повертає той самий екземпляр, тому значення enum можна порівнювати через ==. */
    @Test
    void valueOfReturnsSameInstance() {
        assertSame(VisitKind.URGENT, VisitKind.valueOf("URGENT"));
    }

    /* Довільний текст не стає категорією. */
    @Test
    void valueOfRejectsUnknownName() {
        assertThrows(IllegalArgumentException.class, () -> VisitKind.valueOf("ургентний"));
    }

    /* Підтипи передають базовому типу свою категорію. */
    @Test
    void subtypesHaveMatchingKind() {
        assertSame(VisitKind.ROUTINE, new RoutineVisit("Мурчик", "Кіт", "Огляд", 450.0).getKind());
        assertSame(VisitKind.URGENT, new UrgentVisit("Рекс", "Собака", "Операція", 750.0).getKind());
    }
}
