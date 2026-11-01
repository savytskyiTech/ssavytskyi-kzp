package ua.lpnu.kzp;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class MainTest {
    private static final List<String> SAMPLE_LINES = List.of(
            "Мурчик;Кіт;Огляд;450.00;false",
            "Рекс;Собака;Невідкладна допомога;750.00;true",
            "Соня;Кролик;Щеплення;600.00;false",
            ";Кіт;Огляд;300;false",
            "Барсик;Кіт;Огляд;-100;false");
    private static final List<String> SAMPLE_ERRORS = List.of(
            "Рядок 4: порожнє поле animal",
            "Рядок 5: price має бути скінченним невід'ємним числом");
    /** Показники для трьох коректних рядків SAMPLE_LINES. */
    private static final PriceUrgency SAMPLE_SUMMARY = new PriceUrgency(3, 600.0, 1800.0, 1);

    @TempDir
    Path tempDir;

    private final ByteArrayOutputStream stdout = new ByteArrayOutputStream();
    private final ByteArrayOutputStream stderr = new ByteArrayOutputStream();
    private final Locale originalLocale = Locale.getDefault();

    @AfterEach
    void restoreLocale() {
        Locale.setDefault(originalLocale);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("statisticsCases")
    void calculatesStatistics(String name, List<VetVisit> records, double[] expected) {
        assertArrayEquals(expected, toArray(Main.calculateStatistics(records)), 0.0001);
    }

    static Stream<Arguments> statisticsCases() {
        return Stream.of(
                Arguments.of("Три записи прикладу",
                        records(SAMPLE_LINES.get(0), SAMPLE_LINES.get(1), SAMPLE_LINES.get(2)),
                        new double[] {3, 600.0, 1800.0, 1}),
                Arguments.of("Порожній список", records(), new double[] {0, 0.0, 0.0, 0}),
                Arguments.of("Один візит", records("x;y;z;450;false"),
                        new double[] {1, 450.0, 450.0, 0}),
                Arguments.of("Усі візити ургентні", records("x;y;z;100;true", "a;b;c;200;true"),
                        new double[] {2, 150.0, 300.0, 2}),
                Arguments.of("Жодного ургентного", records("x;y;z;100;false", "a;b;c;200;false"),
                        new double[] {2, 150.0, 300.0, 0}),
                Arguments.of("Дробове середнє", records("x;y;z;100;false", "a;b;c;201;true"),
                        new double[] {2, 150.5, 301.0, 1}));
    }

    @Test
    void rejectsRevenueOverflow() {
        String line = "x;y;z;%s;false".formatted(Double.MAX_VALUE);
        List<VetVisit> records = records(line, line);
        assertThrows(ArithmeticException.class, () -> Main.calculateStatistics(records));
    }

    @Test
    void formatsAllStatistics() {
        String expected = String.join(System.lineSeparator(),
                "Коректних записів: 3",
                "Середня вартість: 600.00 грн",
                "Загальний виторг: 1800.00 грн",
                "Ургентних візитів: 1", "");
        assertEquals(expected, Main.formatReport(SAMPLE_SUMMARY));
    }

    @Test
    void formatsDecimalPointInUkrainianLocale() {
        Locale.setDefault(Locale.forLanguageTag("uk-UA"));
        String report = Main.formatReport(SAMPLE_SUMMARY);
        assertTrue(report.contains("600.00 грн"));
        assertTrue(report.contains("1800.00 грн"));
        assertFalse(report.contains("600,00"));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("lineEndings")
    void readsUtf8RecordsAndReportsInvalidLineNumbers(String name, String separator) throws IOException {
        Path input = tempDir.resolve("my data/input.csv");
        Files.createDirectories(input.getParent());
        Files.writeString(input, String.join(separator, SAMPLE_LINES), StandardCharsets.UTF_8);

        List<VetVisit> records;
        try (PrintStream errors = new PrintStream(stderr, true, StandardCharsets.UTF_8)) {
            records = Main.readRecords(input, errors);
        }

        assertEquals(3, records.size());
        assertArrayEquals(new Object[] {"Мурчик", "Кіт", "Огляд", 450.0, false}, toArray(records.get(0)));
        assertArrayEquals(new Object[] {"Рекс", "Собака", "Невідкладна допомога", 750.0, true}, toArray(records.get(1)));
        assertArrayEquals(new Object[] {"Соня", "Кролик", "Щеплення", 600.0, false}, toArray(records.get(2)));
        assertEquals(SAMPLE_ERRORS, stderr.toString(StandardCharsets.UTF_8).lines().toList());
    }

    static Stream<Arguments> lineEndings() {
        return Stream.of(Arguments.of("LF", "\n"), Arguments.of("CRLF", "\r\n"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "невалідний рядок"})
    void readsNoValidRecords(String content) throws IOException {
        Path input = tempDir.resolve("input.csv");
        Files.writeString(input, content, StandardCharsets.UTF_8);
        try (PrintStream errors = new PrintStream(stderr, true, StandardCharsets.UTF_8)) {
            assertTrue(Main.readRecords(input, errors).isEmpty());
        }
        if (content.isEmpty()) {
            assertEquals("", stderr.toString(StandardCharsets.UTF_8));
        } else {
            assertEquals(List.of("Рядок 1: очікується 5 полів, отримано 1"),
                    stderr.toString(StandardCharsets.UTF_8).lines().toList());
        }
    }

    @Test
    void continuesReadingAfterInvalidLine() throws IOException {
        Path input = tempDir.resolve("input.csv");
        Files.writeString(input, String.join(System.lineSeparator(),
                "хибний рядок", SAMPLE_LINES.get(0), SAMPLE_LINES.get(1)), StandardCharsets.UTF_8);
        try (PrintStream errors = new PrintStream(stderr, true, StandardCharsets.UTF_8)) {
            List<VetVisit> records = Main.readRecords(input, errors);
            assertEquals(2, records.size());
            assertEquals("Мурчик", records.get(0).getAnimal());
            assertEquals("Рекс", records.get(1).getAnimal());
            assertArrayEquals(new double[] {2, 600.0, 1200.0, 1},
                    toArray(Main.calculateStatistics(records)), 0.0001);
        }
        assertEquals(1, stderr.toString(StandardCharsets.UTF_8).lines().count());
    }

    @Test
    void rejectsMissingInputFile() {
        Path input = tempDir.resolve("відсутній.csv");
        try (PrintStream errors = new PrintStream(stderr, true, StandardCharsets.UTF_8)) {
            assertThrows(NoSuchFileException.class, () -> Main.readRecords(input, errors));
        }
    }

    @Test
    void writesUtf8ReportAndCreatesDirectories() throws IOException {
        Path output = tempDir.resolve("out/sub/report.txt");
        String report = Main.formatReport(SAMPLE_SUMMARY);
        Main.writeReport(output, report);
        assertEquals(report, Files.readString(output, StandardCharsets.UTF_8));
    }

    @Test
    void showsHelpInStdout() {
        assertEquals(0, run("--help"));
        String output = stdout.toString(StandardCharsets.UTF_8);
        assertTrue(output.contains("Ветклініка"));
        assertTrue(output.contains("--help"));
        assertTrue(output.contains("--version"));
        assertTrue(output.contains("--input"));
        assertTrue(output.contains("--output"));
        assertTrue(output.contains("java -jar target/lab02-2.0.0-all.jar"));
        assertEquals("", stderr.toString(StandardCharsets.UTF_8));
    }

    @Test
    void showsVersionWithFilteredBuildNumber() throws IOException {
        Properties properties = new Properties();
        try (var stream = Main.class.getResourceAsStream("/version.properties")) {
            properties.load(stream);
        }
        assertEquals(0, run("--version"));
        String output = stdout.toString(StandardCharsets.UTF_8);
        assertEquals("2.0.0+%s%n".formatted(properties.getProperty("build")), output);
        assertTrue(output.strip().matches("2\\.0\\.0\\+(local|[0-9]+)"));
        assertEquals("", stderr.toString(StandardCharsets.UTF_8));
    }

    @Test
    void writesUtf8ToConsoleWithAsciiDefaults() throws IOException, InterruptedException {
        Path input = tempDir.resolve("input.csv");
        Path report = tempDir.resolve("report.txt");
        Path output = tempDir.resolve("stdout.txt");
        Path errors = tempDir.resolve("stderr.txt");
        Files.writeString(input, String.join(System.lineSeparator(), SAMPLE_LINES), StandardCharsets.UTF_8);
        String executable = System.getProperty("os.name").startsWith("Windows") ? "java.exe" : "java";
        Process process = new ProcessBuilder(
                Path.of(System.getProperty("java.home"), "bin", executable).toString(),
                "-Dfile.encoding=US-ASCII", "-Dstdout.encoding=US-ASCII", "-Dstderr.encoding=US-ASCII",
                "-cp", System.getProperty("java.class.path"), Main.class.getName(),
                "--input", input.toString(), "--output", report.toString())
                .redirectOutput(output.toFile()).redirectError(errors.toFile()).start();
        try {
            assertTrue(process.waitFor(20, TimeUnit.SECONDS), "CLI не завершив роботу за 20 секунд");
            assertEquals(0, process.exitValue());
        } finally {
            if (process.isAlive()) {
                process.destroyForcibly();
            }
        }
        String console = Files.readString(output, StandardCharsets.UTF_8);
        assertEquals(Main.formatReport(SAMPLE_SUMMARY), console);
        assertEquals(console, Files.readString(report, StandardCharsets.UTF_8));
        assertEquals(SAMPLE_ERRORS, Files.readAllLines(errors, StandardCharsets.UTF_8));
    }

    @Test
    void runsWithExplicitPathsAndWritesSameReportToConsoleAndFile() throws IOException {
        Path input = tempDir.resolve("my data/input.csv");
        Path output = tempDir.resolve("out/sub/report.txt");
        Files.createDirectories(input.getParent());
        Files.writeString(input, String.join(System.lineSeparator(), SAMPLE_LINES), StandardCharsets.UTF_8);

        assertEquals(0, run("--input", input.toString(), "--output", output.toString()));

        String report = stdout.toString(StandardCharsets.UTF_8);
        assertEquals(Main.formatReport(SAMPLE_SUMMARY), report);
        assertEquals(report, Files.readString(output, StandardCharsets.UTF_8));
        assertEquals(SAMPLE_ERRORS, stderr.toString(StandardCharsets.UTF_8).lines().toList());
    }

    @Test
    void readsDefaultInputWithTemporaryOutput() throws IOException {
        Path output = tempDir.resolve("report.txt");
        assertEquals(0, run("--output", output.toString()));
        String report = stdout.toString(StandardCharsets.UTF_8);
        assertEquals(Main.formatReport(SAMPLE_SUMMARY), report);
        assertEquals(report, Files.readString(output, StandardCharsets.UTF_8));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidArguments")
    void rejectsInvalidArguments(String name, String[] args, String message) {
        assertEquals(2, run(args));
        assertEquals("", stdout.toString(StandardCharsets.UTF_8));
        assertTrue(stderr.toString(StandardCharsets.UTF_8).contains(message));
    }

    static Stream<Arguments> invalidArguments() {
        return Stream.of(
                Arguments.of("Невідомий параметр", new String[] {"--unknown"}, "Невідомий параметр"),
                Arguments.of("Відсутній вхідний шлях", new String[] {"--input"}, "потрібен шлях"),
                Arguments.of("Параметр замість шляху", new String[] {"--input", "--output", "x"}, "потрібен шлях"),
                Arguments.of("Відсутній вихідний шлях", new String[] {"--output"}, "потрібен шлях"),
                Arguments.of("Порожній шлях", new String[] {"--input", " "}, "потрібен шлях"));
    }

    @Test
    void reportsMissingFileInStderr() {
        Path input = tempDir.resolve("відсутній.csv");
        Path output = tempDir.resolve("report.txt");
        assertEquals(1, run("--input", input.toString(), "--output", output.toString()));
        assertEquals("", stdout.toString(StandardCharsets.UTF_8));
        String errors = stderr.toString(StandardCharsets.UTF_8);
        assertTrue(errors.contains("Помилка:"));
        assertTrue(errors.contains(input.toString()));
        assertFalse(Files.exists(output));
    }

    @Test
    void rejectsSameInputAndOutputWithoutOverwritingInput() throws IOException {
        Path input = tempDir.resolve("input.csv");
        String content = SAMPLE_LINES.get(0);
        Files.writeString(input, content, StandardCharsets.UTF_8);
        assertEquals(2, run("--input", input.toString(), "--output", input.toString()));
        assertEquals("", stdout.toString(StandardCharsets.UTF_8));
        assertTrue(stderr.toString(StandardCharsets.UTF_8).contains("мають бути різними"));
        assertEquals(content, Files.readString(input, StandardCharsets.UTF_8));
    }

    private static List<VetVisit> records(String... lines) {
        return Arrays.stream(lines).map(VetVisit::fromCsv).toList();
    }

    /** Подає підсумок у вигляді масиву з LAB_01, щоб очікування тестів лишилися тими самими. */
    private static double[] toArray(PriceUrgency summary) {
        return new double[] {summary.count(), summary.averagePrice(), summary.revenue(), summary.urgentCount()};
    }

    /** Подає поля візиту масивом для порівняння одним assertArrayEquals. */
    private static Object[] toArray(VetVisit visit) {
        return new Object[] {visit.getAnimal(), visit.getSpecies(), visit.getVisitType(),
            visit.getPrice(), visit.isUrgent()};
    }

    private int run(String... args) {
        try (PrintStream output = new PrintStream(stdout, true, StandardCharsets.UTF_8);
                PrintStream errors = new PrintStream(stderr, true, StandardCharsets.UTF_8)) {
            return Main.run(args, output, errors);
        }
    }
}
