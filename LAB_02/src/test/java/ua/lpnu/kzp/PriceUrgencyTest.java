package ua.lpnu.kzp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class PriceUrgencyTest {
    /* Методи доступу record повертають передані компоненти. */
    @Test
    void accessorsReturnComponents() {
        PriceUrgency summary = new PriceUrgency(3, 600.0, 1800.0, 1);

        assertEquals(3, summary.count());
        assertEquals(600.0, summary.averagePrice(), 0.0001);
        assertEquals(1800.0, summary.revenue(), 0.0001);
        assertEquals(1, summary.urgentCount());
    }

    /* record порівнюється за значеннями, а не за посиланнями. */
    @Test
    void recordUsesValueEquality() {
        PriceUrgency first = new PriceUrgency(3, 600.0, 1800.0, 1);
        PriceUrgency second = new PriceUrgency(3, 600.0, 1800.0, 1);

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
        assertNotEquals(first, new PriceUrgency(3, 600.0, 1800.0, 2));
    }

    /* Автоматичний toString містить назви компонентів. */
    @Test
    void toStringNamesComponents() {
        String text = new PriceUrgency(1, 450.0, 450.0, 0).toString();

        assertTrue(text.startsWith("PriceUrgency["), text);
        assertTrue(text.contains("urgentCount=0"), text);
    }

    /* Граничні коректні підсумки: порожній набір і всі візити ургентні. */
    @Test
    void acceptsBoundarySummaries() {
        assertEquals(0, new PriceUrgency(0, 0.0, 0.0, 0).count());
        assertEquals(2, new PriceUrgency(2, 150.0, 300.0, 2).urgentCount());
    }

    /* Неузгоджені або недопустимі показники не можуть утворити підсумок. */
    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidSummaries")
    void rejectsInvalidSummary(String name, int count, double average, double revenue, int urgent) {
        assertThrows(IllegalArgumentException.class,
                () -> new PriceUrgency(count, average, revenue, urgent));
    }

    static Stream<Arguments> invalidSummaries() {
        return Stream.of(
                Arguments.of("Від'ємна кількість", -1, 0.0, 0.0, 0),
                Arguments.of("Від'ємна кількість ургентних", 1, 1.0, 1.0, -1),
                Arguments.of("Ургентних більше, ніж візитів", 1, 1.0, 1.0, 2),
                Arguments.of("Від'ємна середня", 1, -1.0, 1.0, 0),
                Arguments.of("NaN середня", 1, Double.NaN, 1.0, 0),
                Arguments.of("Від'ємний виторг", 1, 1.0, -1.0, 0),
                Arguments.of("Нескінченний виторг", 1, 1.0, Double.POSITIVE_INFINITY, 0));
    }
}
