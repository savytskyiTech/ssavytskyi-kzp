package ua.lpnu.kzp;

import java.util.EnumMap;
import java.util.List;
import java.util.stream.Collectors;

/** Потокові запити до розширених записів ветклініки. */
public final class VisitQueries {
    private static final double PRICE_THRESHOLD = 600.00;

    private VisitQueries() {
    }

    /**
     * Відбирає візити з базовою вартістю строго понад 600 гривень.
     *
     * @param visits вхідні візити
     * @return відібрані візити у вхідному порядку
     */
    public static List<DetailedVisit> abovePrice(List<DetailedVisit> visits) {
        return visits.stream()
                .filter(record -> record.visit().getPrice() > PRICE_THRESHOLD)
                .toList();
    }

    /**
     * Перетворює кожен візит на вид тварини.
     *
     * @param visits вхідні візити
     * @return види у вхідному порядку зі збереженням повторів
     */
    public static List<AnimalSpecies> animalSpecies(List<DetailedVisit> visits) {
        return visits.stream()
                .map(DetailedVisit::species)
                .toList();
    }

    /**
     * Підраховує кількість візитів за кожним наявним видом тварини.
     *
     * @param visits вхідні візити
     * @return кількість візитів за видами тварин
     */
    public static EnumMap<AnimalSpecies, Long> countBySpecies(List<DetailedVisit> visits) {
        return visits.stream()
                .collect(Collectors.groupingBy(
                        DetailedVisit::species,
                        () -> new EnumMap<>(AnimalSpecies.class),
                        Collectors.counting()));
    }
}
