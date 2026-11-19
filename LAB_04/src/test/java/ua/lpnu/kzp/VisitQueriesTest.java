package ua.lpnu.kzp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class VisitQueriesTest {
    private static final DetailedVisit EXPENSIVE_URGENT = DetailedVisit.fromCsv(
            "Рекс;Собака;Огляд;750.00;true;Власник А;2026-11-12;Травма лапи");
    private static final DetailedVisit AT_THRESHOLD = DetailedVisit.fromCsv(
            "Соня;Кролик;Щеплення;600.00;false;Власник Б;2026-11-05;Без ознак захворювання");
    private static final DetailedVisit BELOW_THRESHOLD = DetailedVisit.fromCsv(
            "Мурчик;Кіт;Огляд;450.00;true;Власник В;2026-11-01;Подразнення шкіри");
    private static final DetailedVisit EXPENSIVE_ROUTINE = DetailedVisit.fromCsv(
            "Луна;Собака;Огляд;900.00;false;Власник А;2026-11-02;Запалення вуха");
    private static final List<DetailedVisit> INPUT = List.of(
            EXPENSIVE_URGENT, AT_THRESHOLD, BELOW_THRESHOLD, EXPENSIVE_ROUTINE);

    @Test
    void filtersStrictlyByBasePriceInInputOrder() {
        List<DetailedVisit> visits = new ArrayList<>(INPUT);

        assertEquals(List.of(EXPENSIVE_URGENT, EXPENSIVE_ROUTINE), VisitQueries.abovePrice(visits));
        assertEquals(INPUT, visits);
    }

    @Test
    void mapsSpeciesInInputOrderIncludingRepeats() {
        List<DetailedVisit> visits = new ArrayList<>(INPUT);

        assertEquals(List.of(AnimalSpecies.DOG, AnimalSpecies.RABBIT, AnimalSpecies.CAT,
                AnimalSpecies.DOG), VisitQueries.animalSpecies(visits));
        assertEquals(INPUT, visits);
    }

    @Test
    void countsVisitsBySpecies() {
        List<DetailedVisit> visits = new ArrayList<>(INPUT);

        EnumMap<AnimalSpecies, Long> counts = VisitQueries.countBySpecies(visits);

        assertEquals(Map.of(AnimalSpecies.CAT, 1L, AnimalSpecies.DOG, 2L, AnimalSpecies.RABBIT, 1L),
                counts);
        assertEquals(INPUT, visits);
    }

    @Test
    void returnsEmptyResultsForEmptyInput() {
        List<DetailedVisit> visits = List.of();

        assertTrue(VisitQueries.abovePrice(visits).isEmpty());
        assertTrue(VisitQueries.animalSpecies(visits).isEmpty());
        assertTrue(VisitQueries.countBySpecies(visits).isEmpty());
    }
}
