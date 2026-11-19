package ua.lpnu.kzp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.DoubleSummaryStatistics;
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
    void summarizesBasePrices() {
        List<DetailedVisit> visits = new ArrayList<>(INPUT);

        DoubleSummaryStatistics statistics = VisitQueries.priceStatistics(visits).orElseThrow();

        assertEquals(4, statistics.getCount());
        assertEquals(450.0, statistics.getMin(), 0.0001);
        assertEquals(900.0, statistics.getMax(), 0.0001);
        assertEquals(2700.0, statistics.getSum(), 0.0001);
        assertEquals(675.0, statistics.getAverage(), 0.0001);
        assertEquals(INPUT, visits);
    }

    @Test
    void sortsByDescendingPriceThenAscendingDateBeforeTakingTopFive() throws IOException {
        List<DetailedVisit> visits = Main.readDetailedRecords(Path.of("data", "visits.csv"), System.err);
        List<DetailedVisit> original = List.copyOf(visits);

        List<String> topNames = VisitQueries.topByPrice(visits, 5).stream()
                .map(record -> record.visit().getAnimal())
                .toList();

        assertEquals(List.of("Пушок", "Луна", "Рекс", "Соня", "Мурчик"), topNames);
        assertEquals(original, visits);
    }

    @Test
    void allowsZeroAndOversizedLimits() {
        List<DetailedVisit> visits = new ArrayList<>(INPUT);

        assertTrue(VisitQueries.topByPrice(visits, 0).isEmpty());
        assertEquals(List.of(EXPENSIVE_ROUTINE, EXPENSIVE_URGENT, AT_THRESHOLD, BELOW_THRESHOLD),
                VisitQueries.topByPrice(visits, INPUT.size() + 1));
        assertEquals(INPUT, visits);
    }

    @Test
    void rejectsNegativeLimit() {
        assertThrows(IllegalArgumentException.class, () -> VisitQueries.topByPrice(INPUT, -1));
    }

    @Test
    void returnsEmptyResultsForEmptyInput() {
        List<DetailedVisit> visits = List.of();

        assertTrue(VisitQueries.abovePrice(visits).isEmpty());
        assertTrue(VisitQueries.animalSpecies(visits).isEmpty());
        assertTrue(VisitQueries.countBySpecies(visits).isEmpty());
        assertTrue(VisitQueries.priceStatistics(visits).isEmpty());
        assertTrue(VisitQueries.topByPrice(visits, 5).isEmpty());
    }
}
