package ua.lpnu.kzp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/** Перевіряє запуск демонстрації, актуальність CSV та збереження файлів користувача. */
class DemoModeTest {
    @TempDir
    Path tempDir;

    private final ByteArrayOutputStream stdout = new ByteArrayOutputStream();
    private final ByteArrayOutputStream stderr = new ByteArrayOutputStream();

    @Test
    void runsAllQueriesOnDefaultDetailedCsv() throws IOException {
        Path report = tempDir.resolve("demo.txt");

        assertEquals(0, run("--demo", "--output", report.toString()));

        String output = stdout.toString(StandardCharsets.UTF_8);
        assertEquals(output, Files.readString(report, StandardCharsets.UTF_8));
        assertTrue(output.contains("Коректних записів: 6"));
        assertTrue(output.contains("ВІЗИТИ ДОРОЖЧІ ЗА 600.00 ГРН"));
        assertTrue(output.contains("ВИДИ ТВАРИН (ІЗ ПОВТОРАМИ)"));
        assertTrue(output.contains("Кіт, Собака, Кролик, Кіт, Собака, Кролик"));
        assertTrue(output.contains("Кіт: 2"));
        assertTrue(output.contains("Собака: 2"));
        assertTrue(output.contains("Кролик: 2"));
        assertTrue(output.contains("Сума: 3550.00 грн"));
        assertTrue(output.contains("Середня: 591.67 грн"));
        String top = output.substring(output.indexOf("TOP-5"), output.indexOf("ПОШУК"));
        assertTrue(top.indexOf("Пушок") < top.indexOf("Луна"));
        assertTrue(top.indexOf("Луна") < top.indexOf("Рекс"));
        assertTrue(top.indexOf("Рекс") < top.indexOf("Соня"));
        assertTrue(top.indexOf("Соня") < top.indexOf("Мурчик"));
        assertFalse(top.contains("Барсик"));
        assertTrue(output.contains("ПОШУК ЗА ВЛАСНИКОМ: Власник А"));
        assertTrue(output.contains("Візит не знайдено"));
        assertEquals("", stderr.toString(StandardCharsets.UTF_8));
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void supportsCustomInputRegardlessOfDemoArgumentOrder(boolean demoFirst) throws IOException {
        Path input = tempDir.resolve("візити.csv");
        Path output = tempDir.resolve("demo.txt");
        Files.writeString(input, "Новий;Кіт;Огляд;800;false;Власник А;2026-11-19;Контроль",
                StandardCharsets.UTF_8);
        String[] args = demoFirst
                ? new String[] {"--demo", "--input", input.toString(), "--output", output.toString()}
                : new String[] {"--input", input.toString(), "--output", output.toString(), "--demo"};

        assertEquals(0, run(args));

        String report = Files.readString(output, StandardCharsets.UTF_8);
        assertTrue(report.contains("Коректних записів: 1"));
        assertTrue(report.contains("Новий | Кіт | 800.00 грн"));
        assertTrue(report.contains("Сума: 800.00 грн"));
        assertFalse(report.contains("Мурчик"));
    }

    @Test
    void rereadsCsvAfterStudentChangesPriceAndOwner() throws IOException {
        Path input = tempDir.resolve("visits.csv");
        Path output = tempDir.resolve("demo.txt");
        Files.writeString(input, "Тест;Кіт;Огляд;600;false;Інший;2026-11-19;Огляд",
                StandardCharsets.UTF_8);
        assertEquals(0, run("--demo", "--input", input.toString(), "--output", output.toString()));
        assertTrue(Files.readString(output, StandardCharsets.UTF_8).contains("Сума: 600.00 грн"));
        Files.writeString(input, "Тест;Кіт;Огляд;801;false;Власник А;2026-11-19;Огляд",
                StandardCharsets.UTF_8);

        assertEquals(0, run("--demo", "--input", input.toString(), "--output", output.toString()));

        String report = Files.readString(output, StandardCharsets.UTF_8);
        assertTrue(report.contains("Сума: 801.00 грн"));
        assertTrue(report.contains("ПОШУК ЗА ВЛАСНИКОМ: Власник А"));
        assertFalse(report.contains("600.00 грн"));
    }

    @Test
    void handlesEmptyCsvWithoutInfinityOrMissingSections() throws IOException {
        Path input = tempDir.resolve("empty.csv");
        Files.writeString(input, "", StandardCharsets.UTF_8);

        assertEquals(0, run("--demo", "--input", input.toString(),
                "--output", tempDir.resolve("demo.txt").toString()));

        String report = stdout.toString(StandardCharsets.UTF_8);
        assertTrue(report.contains("Коректних записів: 0"));
        assertTrue(report.contains("Немає даних"));
        assertTrue(report.contains("Візитів немає"));
        assertTrue(report.contains("Візит не знайдено"));
        assertFalse(report.contains("Infinity"));
        assertFalse(report.contains("NaN"));
    }

    @Test
    void skipsInvalidLineAndContinuesDemo() throws IOException {
        Path input = tempDir.resolve("mixed.csv");
        Files.write(input, List.of("хибний рядок",
                "Тест;Кіт;Огляд;800;false;Власник А;2026-11-19;Огляд"), StandardCharsets.UTF_8);

        assertEquals(0, run("--demo", "--input", input.toString(),
                "--output", tempDir.resolve("demo.txt").toString()));

        assertTrue(stderr.toString(StandardCharsets.UTF_8).contains("Рядок 1: очікується 8 полів"));
        assertTrue(stdout.toString(StandardCharsets.UTF_8).contains("Коректних записів: 1"));
    }

    @Test
    void reportsMissingDemoInputWithoutCreatingOutput() {
        Path output = tempDir.resolve("demo.txt");

        assertEquals(1, run("--demo", "--input", tempDir.resolve("missing.csv").toString(),
                "--output", output.toString()));

        assertEquals("", stdout.toString(StandardCharsets.UTF_8));
        assertTrue(stderr.toString(StandardCharsets.UTF_8).contains("Помилка:"));
        assertFalse(Files.exists(output));
    }

    @Test
    void refusesToOverwriteDemoInput() throws IOException {
        Path input = tempDir.resolve("visits.csv");
        String content = "Тест;Кіт;Огляд;800;false;Власник А;2026-11-19;Огляд";
        Files.writeString(input, content, StandardCharsets.UTF_8);

        assertEquals(2, run("--demo", "--input", input.toString(), "--output", input.toString()));

        assertEquals(content, Files.readString(input, StandardCharsets.UTF_8));
        assertEquals("", stdout.toString(StandardCharsets.UTF_8));
    }

    @Test
    void formatsDemoDecimalsIndependentlyOfDefaultLocale() {
        Locale original = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("uk-UA"));
            String report = DemoReport.format(List.of(DetailedVisit.fromCsv(
                    "Тест;Кіт;Огляд;800.5;false;Власник А;2026-11-19;Огляд")));
            assertTrue(report.contains("800.50 грн"));
            assertFalse(report.contains("800,50"));
        } finally {
            Locale.setDefault(original);
        }
    }

    /** Запускає CLI без завершення тестового процесу. */
    private int run(String... args) {
        try (PrintStream output = new PrintStream(stdout, true, StandardCharsets.UTF_8);
                PrintStream errors = new PrintStream(stderr, true, StandardCharsets.UTF_8)) {
            return Main.run(args, output, errors);
        }
    }
}
