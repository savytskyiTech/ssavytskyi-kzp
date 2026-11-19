package ua.lpnu.kzp;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class StreamEquivalenceTest {
    @ParameterizedTest(name = "{0}")
    @MethodSource("visitSamples")
    void preservesLoopStatisticsAndReport(String sampleName, List<VetVisit> visits) {
        PriceUrgency loopSummary = calculateWithOriginalLoop(visits);
        PriceUrgency streamSummary = Main.calculateStatistics(visits);

        assertEquals(loopSummary, streamSummary);
        assertEquals(Main.formatReport(loopSummary), Main.formatReport(streamSummary));
    }

    static Stream<Arguments> visitSamples() throws IOException {
        try (PrintStream diagnostics =
                new PrintStream(OutputStream.nullOutputStream(), false, StandardCharsets.UTF_8)) {
            List<VetVisit> original = Main.readRecords(Path.of("data", "input.csv"), diagnostics);
            List<VetVisit> detailed = Main.readDetailedRecords(Path.of("data", "visits.csv"), diagnostics)
                    .stream()
                    .map(DetailedVisit::visit)
                    .toList();
            return Stream.of(
                    Arguments.of("Контрольний CSV", original),
                    Arguments.of("Розширений CSV", detailed),
                    Arguments.of("Порожній набір", List.<VetVisit>of()));
        }
    }

    /** Відтворює обчислення LAB_03 для порівняння на тих самих візитах. */
    private static PriceUrgency calculateWithOriginalLoop(List<VetVisit> visits) {
        double revenue = 0;
        double finalRevenue = 0;
        int urgentCount = 0;
        for (VetVisit visit : visits) {
            revenue += visit.getPrice();
            finalRevenue += visit.finalCost();
            if (visit.isUrgent()) {
                urgentCount++;
            }
        }
        if (!Double.isFinite(revenue) || !Double.isFinite(finalRevenue)) {
            throw new ArithmeticException("сума цін виходить за межі double");
        }
        double average = visits.isEmpty() ? 0.0 : revenue / visits.size();
        return new PriceUrgency(visits.size(), average, revenue, urgentCount, finalRevenue);
    }
}
