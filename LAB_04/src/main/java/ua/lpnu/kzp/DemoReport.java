package ua.lpnu.kzp;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/** Форматує результати готових потокових запитів для демонстрації на захисті. */
final class DemoReport {
    private static final int TOP_LIMIT = 5;

    private DemoReport() {
    }

    /**
     * Викликає всі шість запитів над актуальними даними CSV та формує спільний звіт.
     *
     * @param visits коректні розширені візити в порядку вхідного файла
     * @return текст для консолі й файла, з явною обробкою порожніх результатів
     */
    static String format(List<DetailedVisit> visits) {
        StringBuilder report = new StringBuilder();
        report.append(String.format(Locale.ROOT,
                "ДЕМОНСТРАЦІЯ LAB_04: STREAM API%nКоректних записів: %d%n", visits.size()));

        report.append(String.format(Locale.ROOT, "%nВІЗИТИ ДОРОЖЧІ ЗА %.2f ГРН%n",
                VisitQueries.priceThreshold()));
        appendVisits(report, VisitQueries.abovePrice(visits));

        report.append(String.format(Locale.ROOT, "%nВИДИ ТВАРИН (ІЗ ПОВТОРАМИ)%n"));
        String species = VisitQueries.animalSpecies(visits).stream()
                .map(AnimalSpecies::label)
                .collect(Collectors.joining(", "));
        report.append(String.format(Locale.ROOT, "%s%n", species.isEmpty() ? "Немає даних" : species));

        report.append(String.format(Locale.ROOT, "%nКІЛЬКІСТЬ ВІЗИТІВ ЗА ВИДОМ%n"));
        var counts = VisitQueries.countBySpecies(visits);
        if (counts.isEmpty()) {
            report.append(String.format(Locale.ROOT, "Немає даних%n"));
        } else {
            counts.forEach((kind, count) -> report.append(String.format(Locale.ROOT,
                    "%s: %d%n", kind.label(), count)));
        }

        report.append(String.format(Locale.ROOT, "%nСТАТИСТИКА БАЗОВИХ ЦІН%n"));
        VisitQueries.priceStatistics(visits).ifPresentOrElse(
                statistics -> report.append(String.format(Locale.ROOT,
                        """
                        Кількість: %d%n\
                        Мінімум: %.2f грн%n\
                        Максимум: %.2f грн%n\
                        Сума: %.2f грн%n\
                        Середня: %.2f грн%n""",
                        statistics.getCount(), statistics.getMin(), statistics.getMax(),
                        statistics.getSum(), statistics.getAverage())),
                () -> report.append(String.format(Locale.ROOT, "Немає даних%n")));

        report.append(String.format(Locale.ROOT, "%nTOP-%d: ЦІНА ЗА СПАДАННЯМ, ДАТА ЗА ЗРОСТАННЯМ%n",
                TOP_LIMIT));
        appendVisits(report, VisitQueries.topByPrice(visits, TOP_LIMIT));

        for (String owner : List.of("Власник А", "Власник Я")) {
            report.append(String.format(Locale.ROOT, "%nПОШУК ЗА ВЛАСНИКОМ: %s%n", owner));
            VisitQueries.findFirstByOwner(visits, owner).ifPresentOrElse(
                    visit -> appendVisits(report, List.of(visit)),
                    () -> report.append(String.format(Locale.ROOT, "Візит не знайдено%n")));
        }
        return report.toString();
    }

    /** Виводить записи, зберігаючи порядок результату запиту, або явний порожній результат. */
    private static void appendVisits(StringBuilder report, List<DetailedVisit> visits) {
        if (visits.isEmpty()) {
            report.append(String.format(Locale.ROOT, "Візитів немає%n"));
            return;
        }
        for (DetailedVisit record : visits) {
            report.append(String.format(Locale.ROOT,
                    "%s | %s | %.2f грн | %s | %s | %s%n",
                    record.visit().getAnimal(), record.species().label(), record.visit().getPrice(),
                    record.date(), record.owner(), record.diagnosis()));
        }
    }
}
