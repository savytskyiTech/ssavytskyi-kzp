package ua.lpnu.kzp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class DetailedVisitTest {
    private static final String VALID_ROW =
            "Рекс;Собака;Огляд;750.00;true;Власник Б;2026-11-12;Травма лапи";

    @Test
    void parsesDetailsAndPreservesVisitSubtype() {
        DetailedVisit details = DetailedVisit.fromCsv(VALID_ROW);

        assertEquals(AnimalSpecies.DOG, details.species());
        assertEquals("Власник Б", details.owner());
        assertEquals(LocalDate.of(2026, 11, 12), details.date());
        assertEquals("Травма лапи", details.diagnosis());
        assertInstanceOf(UrgentVisit.class, details.visit());
        assertEquals("Рекс", details.visit().getAnimal());
        assertEquals(750.0, details.visit().getPrice(), 0.0001);
        assertEquals(1125.0, details.visit().finalCost(), 0.0001);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "Рекс;Собака;Огляд;750.00;true",
        "Рекс;Собака;Огляд;750.00;true;;2026-11-12;Травма лапи",
        "Рекс;Собака;Огляд;750.00;true;Власник Б;2026-02-30;Травма лапи",
        "Рекс;Собака;Огляд;750.00;true;Власник Б;2026-11-12;",
        "Рекс;Невідомий вид;Огляд;750.00;true;Власник Б;2026-11-12;Травма лапи",
        "Рекс;Собака;Огляд;0.00;true;Власник Б;2026-11-12;Травма лапи"
    })
    void rejectsInvalidDetailedRows(String line) {
        assertThrows(IllegalArgumentException.class, () -> DetailedVisit.fromCsv(line));
    }

    @Test
    void retainsFileOrderAndReportsInvalidLine(@TempDir Path directory) throws IOException {
        Path input = directory.resolve("візити.csv");
        String lastRow = "Луна;Кіт;Огляд;600.00;false;Власник А;2026-11-02;Запалення вуха";
        Files.writeString(input, String.join("\r\n", VALID_ROW, "", lastRow),
                StandardCharsets.UTF_8);
        ByteArrayOutputStream diagnostics = new ByteArrayOutputStream();
        try (PrintStream errors = new PrintStream(diagnostics, true, StandardCharsets.UTF_8)) {
            assertEquals(List.of(DetailedVisit.fromCsv(VALID_ROW), DetailedVisit.fromCsv(lastRow)),
                    Main.readDetailedRecords(input, errors));
        }
        assertEquals("Рядок 2: порожній рядок%n".formatted(),
                diagnostics.toString(StandardCharsets.UTF_8));
    }

    @Test
    void loadsAllSixDemonstrationRecords() throws IOException {
        ByteArrayOutputStream diagnostics = new ByteArrayOutputStream();
        try (PrintStream errors = new PrintStream(diagnostics, true, StandardCharsets.UTF_8)) {
            List<DetailedVisit> visits = Main.readDetailedRecords(Path.of("data", "visits.csv"), errors);
            assertEquals(6, visits.size());
            assertEquals(600.0, visits.get(2).visit().getPrice(), 0.0001);
            assertEquals(visits.get(0).owner(), visits.get(4).owner());
        }
        assertTrue(diagnostics.toString(StandardCharsets.UTF_8).isEmpty());
    }
}
