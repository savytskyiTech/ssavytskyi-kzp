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
    @MethodSource("validLines")
    void acceptsValidLine(String name, String line, String[] expected) {
        assertArrayEquals(expected, Main.validateLine(line));
    }

    static Stream<Arguments> validLines() {
        return Stream.of(
                Arguments.of("Звичайний візит", SAMPLE_LINES.get(0),
                        new String[] {"Мурчик", "Кіт", "Огляд", "450.00", "false"}),
                Arguments.of("Український текст із пробілом", SAMPLE_LINES.get(1),
                        new String[] {"Рекс", "Собака", "Невідкладна допомога", "750.00", "true"}),
                Arguments.of("Нульова ціна", "Соня;Кролик;Щеплення;0;false",
                        new String[] {"Соня", "Кролик", "Щеплення", "0", "false"}));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidLines")
    void rejectsInvalidLine(String name, String line, String message) {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class, () -> Main.validateLine(line));
        assertTrue(exception.getMessage().contains(message), exception.getMessage());
    }

    static Stream<Arguments> invalidLines() {
        return Stream.of(
                Arguments.of("Порожній рядок", "", "порожній рядок"),
                Arguments.of("Лише пробіли", " ", "порожній рядок"),
                Arguments.of("Чотири поля", "a;b;c;1", "очікується 5 полів, отримано 4"),
                Arguments.of("Шість полів", "a;b;c;1;true;extra", "очікується 5 полів, отримано 6"),
                Arguments.of("Зайве порожнє поле", "a;b;c;1;true;", "очікується 5 полів, отримано 6"),
                Arguments.of("Порожнє ім'я", ";Кіт;Огляд;300;false", "порожнє поле animal"),
                Arguments.of("Порожній вид", "x;;y;1;true", "порожнє поле species"),
                Arguments.of("Порожній тип візиту", "x;y;;1;true", "порожнє поле visitType"),
                Arguments.of("Текст замість ціни", "x;y;z;abc;false", "price має бути числом"),
                Arguments.of("Порожня ціна", "x;y;z;;false", "price має бути числом"),
                Arguments.of("Десяткова кома", "x;y;z;12,5;false", "price має бути числом"),
                Arguments.of("Від'ємна ціна", "x;y;z;-100;false", "скінченним невід'ємним"),
                Arguments.of("NaN", "x;y;z;NaN;false", "скінченним невід'ємним"),
                Arguments.of("Нескінченність", "x;y;z;Infinity;false", "скінченним невід'ємним"),
                Arguments.of("Переповнення ціни", "x;y;z;1e400;false", "скінченним невід'ємним"),
                Arguments.of("True з великої літери", "x;y;z;1;True", "urgent має дорівнювати точно true або false"),
                Arguments.of("TRUE великими літерами", "x;y;z;1;TRUE", "urgent має дорівнювати точно true або false"),
                Arguments.of("Українське так", "x;y;z;1;так", "urgent має дорівнювати точно true або false"),
                Arguments.of("Число замість boolean", "x;y;z;1;1", "urgent має дорівнювати точно true або false"),
                Arguments.of("Порожній urgent", "x;y;z;1;", "urgent має дорівнювати точно true або false"));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("statisticsCases")
    void calculatesStatistics(String name, List<String[]> records, double[] expected) {
        assertArrayEquals(expected, Main.calculateStatistics(records), 0.0001);
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
        List<String[]> records = records(line, line);
        assertThrows(ArithmeticException.class, () -> Main.calculateStatistics(records));
    }

    @Test
    void formatsAllStatistics() {
        String expected = String.join(System.lineSeparator(),
                "Коректних записів: 3",
                "Середня вартість: 600.00 грн",
                "Загальний виторг: 1800.00 грн",
                "Ургентних візитів: 1", "");
        assertEquals(expected, Main.formatReport(new double[] {3, 600.0, 1800.0, 1}));
    }

    @Test
    void formatsDecimalPointInUkrainianLocale() {
        Locale.setDefault(Locale.forLanguageTag("uk-UA"));
        String report = Main.formatReport(new double[] {3, 600.0, 1800.0, 1});
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

        List<String[]> records;
        try (PrintStream errors = new PrintStream(stderr, true, StandardCharsets.UTF_8)) {
            records = Main.readRecords(input, errors);
        }

        assertEquals(3, records.size());
        assertArrayEquals(new String[] {"Мурчик", "Кіт", "Огляд", "450.00", "false"}, records.get(0));
        assertArrayEquals(new String[] {"Рекс", "Собака", "Невідкладна допомога", "750.00", "true"}, records.get(1));
        assertArrayEquals(new String[] {"Соня", "Кролик", "Щеплення", "600.00", "false"}, records.get(2));
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
            List<String[]> records = Main.readRecords(input, errors);
            assertEquals(2, records.size());
            assertEquals("Мурчик", records.get(0)[0]);
            assertEquals("Рекс", records.get(1)[0]);
            assertArrayEquals(new double[] {2, 600.0, 1200.0, 1},
                    Main.calculateStatistics(records), 0.0001);
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
        String report = Main.formatReport(new double[] {3, 600.0, 1800.0, 1});
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
        assertTrue(output.contains("java -jar target/lab01-1.0.0-all.jar"));
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
        assertEquals("1.0.0+%s%n".formatted(properties.getProperty("build")), output);
        assertTrue(output.strip().matches("1\\.0\\.0\\+(local|[0-9]+)"));
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
        assertEquals(Main.formatReport(new double[] {3, 600.0, 1800.0, 1}), console);
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
        assertEquals(Main.formatReport(new double[] {3, 600.0, 1800.0, 1}), report);
        assertEquals(report, Files.readString(output, StandardCharsets.UTF_8));
        assertEquals(SAMPLE_ERRORS, stderr.toString(StandardCharsets.UTF_8).lines().toList());
    }

    @Test
    void readsDefaultInputWithTemporaryOutput() throws IOException {
        Path output = tempDir.resolve("report.txt");
        assertEquals(0, run("--output", output.toString()));
        String report = stdout.toString(StandardCharsets.UTF_8);
        assertEquals(Main.formatReport(new double[] {3, 600.0, 1800.0, 1}), report);
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

    private static List<String[]> records(String... lines) {
        return Arrays.stream(lines).map(Main::validateLine).toList();
    }

    private int run(String... args) {
        try (PrintStream output = new PrintStream(stdout, true, StandardCharsets.UTF_8);
                PrintStream errors = new PrintStream(stderr, true, StandardCharsets.UTF_8)) {
            return Main.run(args, output, errors);
        }
    }
}
