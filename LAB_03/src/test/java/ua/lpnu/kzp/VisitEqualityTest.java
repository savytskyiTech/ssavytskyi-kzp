package ua.lpnu.kzp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

/** Тести узгоджених {@code equals}/{@code hashCode} та поведінки в {@link HashSet} і {@link HashMap}. */
class VisitEqualityTest {
    /* Рефлексивність і порівняння з null та іншим типом. */
    @Test
    void equalsHandlesSelfNullAndOtherType() {
        VetVisit visit = new RoutineVisit("Мурчик", "Кіт", "Огляд", 450.0);

        assertEquals(visit, visit);
        assertNotEquals(null, visit);
        assertFalse(visit.equals("Мурчик"));
    }

    /* Логічно рівні об'єкти рівні в обидва боки й мають однаковий hashCode. */
    @Test
    void equalVisitsHaveSameHashCode() {
        VetVisit first = VetVisit.fromCsv("Рекс;Собака;Невідкладна допомога;750.00;true");
        VetVisit second = new UrgentVisit("Рекс", "Собака", "Невідкладна допомога", 750.0);

        assertEquals(first, second);
        assertEquals(second, first);
        assertEquals(first.hashCode(), second.hashCode());
    }

    /* Різні підтипи з однаковими полями не рівні: відрізняється підсумкова вартість. */
    @Test
    void differentSubtypesAreNotEqual() {
        VetVisit routine = new RoutineVisit("Мурчик", "Кіт", "Огляд", 450.0);
        VetVisit urgent = new UrgentVisit("Мурчик", "Кіт", "Огляд", 450.0);

        assertNotEquals(routine, urgent);
        assertNotEquals(urgent, routine);
    }

    /* Кожне поле бере участь у рівності. */
    @Test
    void differentFieldsAreNotEqual() {
        VetVisit base = new RoutineVisit("Мурчик", "Кіт", "Огляд", 450.0);

        assertNotEquals(base, new RoutineVisit("Барсик", "Кіт", "Огляд", 450.0));
        assertNotEquals(base, new RoutineVisit("Мурчик", "Собака", "Огляд", 450.0));
        assertNotEquals(base, new RoutineVisit("Мурчик", "Кіт", "Щеплення", 450.0));
        assertNotEquals(base, new RoutineVisit("Мурчик", "Кіт", "Огляд", 450.01));
    }

    /* HashSet не дублює логічно рівні візити, але зберігає різні підтипи окремо. */
    @Test
    void hashSetKeepsOneEntryPerEqualVisit() {
        Set<VetVisit> visits = new HashSet<>();
        visits.add(VetVisit.fromCsv("Мурчик;Кіт;Огляд;450.00;false"));
        visits.add(VetVisit.fromCsv("Мурчик;Кіт;Огляд;450;false"));
        visits.add(new RoutineVisit("Мурчик", "Кіт", "Огляд", 450.0));
        visits.add(new UrgentVisit("Мурчик", "Кіт", "Огляд", 450.0));

        assertEquals(2, visits.size());
        assertTrue(visits.contains(new UrgentVisit("Мурчик", "Кіт", "Огляд", 450.0)));
    }

    /* Новий, але рівний об'єкт знаходить значення в HashMap. */
    @Test
    void hashMapFindsValueByEqualKey() {
        Map<VetVisit, Integer> repeats = new HashMap<>();
        for (VetVisit visit : List.of(
                new RoutineVisit("Соня", "Кролик", "Щеплення", 600.0),
                new RoutineVisit("Соня", "Кролик", "Щеплення", 600.0),
                new UrgentVisit("Соня", "Кролик", "Щеплення", 600.0))) {
            repeats.merge(visit, 1, Integer::sum);
        }

        assertEquals(2, repeats.size());
        assertEquals(2, repeats.get(new RoutineVisit("Соня", "Кролик", "Щеплення", 600.0)));
        assertEquals(1, repeats.get(new UrgentVisit("Соня", "Кролик", "Щеплення", 600.0)));
    }
}
