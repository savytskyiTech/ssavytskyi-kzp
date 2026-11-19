package ua.lpnu.kzp;

import java.util.Comparator;
import java.util.DoubleSummaryStatistics;
import java.util.EnumMap;
import java.util.List;
import java.util.Optional;
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

    /**
     * Обчислює кількість, суму, середню, мінімальну й максимальну базові вартості.
     *
     * @param visits вхідні візити
     * @return статистика базових вартостей або порожній результат, якщо візитів немає
     */
    public static Optional<DoubleSummaryStatistics> priceStatistics(List<DetailedVisit> visits) {
        DoubleSummaryStatistics statistics = visits.stream()
                .collect(Collectors.summarizingDouble(record -> record.visit().getPrice()));
        return statistics.getCount() == 0 ? Optional.empty() : Optional.of(statistics);
    }

    /**
     * Вибирає перші візити за спаданням базової вартості та зростанням дати.
     *
     * @param visits вхідні візити
     * @param limit найбільша кількість результатів
     * @return відсортовані візити, кількість яких не перевищує задану межу
     * @throws IllegalArgumentException якщо межа від'ємна
     */
    public static List<DetailedVisit> topByPrice(List<DetailedVisit> visits, int limit) {
        if (limit < 0) {
            throw new IllegalArgumentException("кількість результатів має бути невід'ємною");
        }
        Comparator<DetailedVisit> order = Comparator
                .comparingDouble((DetailedVisit record) -> record.visit().getPrice())
                .reversed()
                .thenComparing(DetailedVisit::date);
        return visits.stream()
                .sorted(order)
                .limit(limit)
                .toList();
    }
}
