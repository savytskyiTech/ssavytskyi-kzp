package ua.lpnu.kzp;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

/** Консольний звіт про візити до ветклініки. */
public final class Main {
    private Main() {
    }

    /**
     * Запускає програму й повертає ОС код завершення.
     *
     * @param args параметри --help, --version, --input і --output
     */
    public static void main(String[] args) {
        int exitCode;
        try (PrintStream output = new PrintStream(System.out, true, StandardCharsets.UTF_8);
                PrintStream errors = new PrintStream(System.err, true, StandardCharsets.UTF_8)) {
            exitCode = run(args, output, errors);
        }
        System.exit(exitCode);
    }

    /**
     * Обробляє CLI, читає дані та зберігає звіт.
     *
     * @param args аргументи командного рядка
     * @param output потік звіту та довідки
     * @param errors потік діагностичних повідомлень
     * @return 0 у разі успіху, 1 для файлової/арифметичної помилки, 2 для помилки CLI
     */
    static int run(String[] args, PrintStream output, PrintStream errors) {
        Path input = Path.of("data", "input.csv");
        Path destination = Path.of("output", "report.txt");

        try {
            for (int i = 0; i < args.length; i++) {
                String option = args[i];
                if ("--help".equals(option)) {
                    printHelp(output);
                    return 0;
                }
                if ("--version".equals(option)) {
                    output.printf("%s%n", version());
                    return 0;
                }
                if (!"--input".equals(option) && !"--output".equals(option)) {
                    throw new IllegalArgumentException(
                            "Невідомий параметр: %s".formatted(option));
                }
                if (i + 1 >= args.length || args[i + 1].startsWith("--")
                        || args[i + 1].isBlank()) {
                    throw new IllegalArgumentException(
                            "Після %s потрібен шлях до файла".formatted(option));
                }
                Path path = Path.of(args[++i]);
                if ("--input".equals(option)) {
                    input = path;
                } else {
                    destination = path;
                }
            }
        } catch (IllegalArgumentException exception) {
            errors.printf("Помилка CLI: %s%n", exception.getMessage());
            errors.printf("Довідка: --help%n");
            return 2;
        } catch (IOException exception) {
            errors.printf("Помилка: %s%n", exception.getMessage());
            return 1;
        }

        try {
            // Захищаємо вхідні дані від перезапису, зокрема через символічне посилання.
            if (input.toAbsolutePath().normalize().equals(
                    destination.toAbsolutePath().normalize())
                    || (Files.exists(destination) && Files.isSameFile(input, destination))) {
                errors.printf("Помилка CLI: вхідний файл і файл звіту мають бути різними%n");
                return 2;
            }
            List<VetVisit> visits = readRecords(input, errors);
            PriceUrgency summary = calculateStatistics(visits);
            String report = formatReport(summary);
            writeReport(destination, report);
            output.print(report);
            return 0;
        } catch (IOException | ArithmeticException exception) {
            errors.printf("Помилка: %s%n", exception.getMessage());
            return 1;
        }
    }

    /** Виводить параметри та значення за замовчуванням. */
    private static void printHelp(PrintStream output) {
        output.printf("Ветклініка — LAB_02, варіант 21%n");
        output.printf("Запуск: java -jar target/lab02-2.0.0-all.jar [параметри]%n");
        output.printf("  --help           Показати довідку%n");
        output.printf("  --version        Показати версію та номер збірки%n");
        output.printf("  --input <файл>   Вхідний CSV, типово data/input.csv%n");
        output.printf("  --output <файл>  Файл звіту, типово output/report.txt%n");
        output.printf("Формат: animal;species;visitType;price;urgent (UTF-8)%n");
    }

    /**
     * Читає версію POM і номер збірки з ресурсу, заповненого Maven.
     *
     * @return версія у форматі 2.0.0+local або 2.0.0+42
     * @throws IOException якщо ресурс, версія або номер збірки відсутні
     */
    static String version() throws IOException {
        Properties properties = new Properties();
        try (var stream = Main.class.getResourceAsStream("/version.properties")) {
            if (stream == null) {
                throw new IOException("Ресурс version.properties не знайдено");
            }
            properties.load(stream);
        }
        String version = properties.getProperty("version");
        String build = properties.getProperty("build");
        if (version == null || version.isBlank() || build == null || build.isBlank()) {
            throw new IOException("Ресурс version.properties має містити version і build");
        }
        return "%s+%s".formatted(version, build);
    }

    /**
     * Читає UTF-8-файл і перетворює кожен рядок на {@link VetVisit}.
     *
     * <p>Хибний рядок не потрапляє до списку: помилку фабрики перехоплено тут,
     * бо лише цикл читання знає номер рядка. Обробка решти рядків триває.
     *
     * @param input шлях до CSV
     * @param errors потік повідомлень «Рядок N: причина»
     * @return список коректних візитів у порядку файла
     * @throws IOException якщо файл неможливо прочитати
     */
    static List<VetVisit> readRecords(Path input, PrintStream errors) throws IOException {
        List<String> lines = Files.readAllLines(input, StandardCharsets.UTF_8);
        List<VetVisit> visits = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            try {
                visits.add(VetVisit.fromCsv(lines.get(i)));
            } catch (IllegalArgumentException exception) {
                errors.printf("Рядок %d: %s%n", i + 1, exception.getMessage());
            }
        }
        return visits;
    }

    /**
     * Обчислює чотири показники лише за коректними візитами.
     *
     * @param visits візити, створені через {@link VetVisit#fromCsv(String)} або конструктор
     * @return підсумок: кількість, середня вартість, виторг, кількість ургентних візитів
     * @throws ArithmeticException якщо сума цін виходить за межі double
     */
    static PriceUrgency calculateStatistics(List<VetVisit> visits) {
        double revenue = 0;
        int urgentCount = 0;
        for (VetVisit visit : visits) {
            revenue += visit.getPrice();
            if (visit.isUrgent()) {
                urgentCount++;
            }
        }
        if (!Double.isFinite(revenue)) {
            throw new ArithmeticException("сума цін виходить за межі double");
        }
        double average = visits.isEmpty() ? 0.0 : revenue / visits.size();
        return new PriceUrgency(visits.size(), average, revenue, urgentCount);
    }

    /**
     * Формує спільний текст для консолі й файла, незалежний від локальної ОС.
     *
     * @param summary підсумок із calculateStatistics
     * @return звіт із платформонезалежними переносами рядків
     */
    static String formatReport(PriceUrgency summary) {
        return String.format(Locale.ROOT,
                "Коректних записів: %d%nСередня вартість: %.2f грн%nЗагальний виторг: %.2f грн%nУргентних візитів: %d%n",
                summary.count(), summary.averagePrice(), summary.revenue(), summary.urgentCount());
    }

    /**
     * Створює батьківську теку за потреби та записує звіт у UTF-8.
     *
     * @param output шлях до файла звіту
     * @param report готовий текст звіту
     * @throws IOException якщо теку або файл неможливо створити чи записати
     */
    static void writeReport(Path output, String report) throws IOException {
        Path parent = output.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.writeString(output, report, StandardCharsets.UTF_8);
    }
}
