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
            List<String[]> records = readRecords(input, errors);
            double[] statistics = calculateStatistics(records);
            String report = formatReport(statistics);
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
        output.printf("Ветклініка — LAB_01, варіант 21%n");
        output.printf("Запуск: java -jar target/lab01-1.0.0-all.jar [параметри]%n");
        output.printf("  --help           Показати довідку%n");
        output.printf("  --version        Показати версію та номер збірки%n");
        output.printf("  --input <файл>   Вхідний CSV, типово data/input.csv%n");
        output.printf("  --output <файл>  Файл звіту, типово output/report.txt%n");
        output.printf("Формат: animal;species;visitType;price;urgent (UTF-8)%n");
    }

    /**
     * Читає версію POM і номер збірки з ресурсу, заповненого Maven.
     *
     * @return версія у форматі 1.0.0+local або 1.0.0+42
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
     * Читання та перевірка UTF-8
     *
     * @param input шлях до CSV
     * @param errors потік повідомлень із номерами рядків
     * @return список перевірених записів
     * @throws IOException якщо файл неможливо прочитати
     */
    static List<String[]> readRecords(Path input, PrintStream errors) throws IOException {
        List<String> lines = Files.readAllLines(input, StandardCharsets.UTF_8);
        List<String[]> records = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            try {
                records.add(validateLine(lines.get(i)));
            } catch (IllegalArgumentException exception) {
                errors.printf("Рядок %d: %s%n", i + 1, exception.getMessage());
            }
        }
        return records;
    }

    /**
     * Перевіряє п'ять полів запису. Порожнє останнє поле зберігається під час розбору.
     *
     * @param line один рядок CSV без заголовка
     * @return поля animal, species, visitType, price та urgent
     * @throws IllegalArgumentException якщо запис не відповідає формату
     */
    static String[] validateLine(String line) {
        if (line.isBlank()) {
            throw new IllegalArgumentException("порожній рядок");
        }
        String[] fields = line.split(";", -1);
        if (fields.length != 5) {
            throw new IllegalArgumentException(
                    "очікується 5 полів, отримано %d".formatted(fields.length));
        }
        String[] names = {"animal", "species", "visitType"};
        for (int i = 0; i < names.length; i++) {
            if (fields[i].isBlank()) {
                throw new IllegalArgumentException("порожнє поле %s".formatted(names[i]));
            }
        }
        double price;
        try {
            price = Double.parseDouble(fields[3]);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("price має бути числом із крапкою", exception);
        }
        if (!Double.isFinite(price) || price < 0) {
            throw new IllegalArgumentException("price має бути скінченним невід'ємним числом");
        }
        if (!"true".equals(fields[4]) && !"false".equals(fields[4])) {
            throw new IllegalArgumentException("urgent має дорівнювати точно true або false");
        }
        return fields;
    }

    /**
     * Обчислює чотири показники лише за перевіреними записами.
     *
     * @param records записи, отримані з validateLine
     * @return масив: кількість, середня вартість, виторг, кількість ургентних візитів
     * @throws ArithmeticException якщо сума цін виходить за межі double
     */
    static double[] calculateStatistics(List<String[]> records) {
        double revenue = 0;
        int urgentCount = 0;
        for (String[] record : records) {
            revenue += Double.parseDouble(record[3]);
            if ("true".equals(record[4])) {
                urgentCount++;
            }
        }
        if (!Double.isFinite(revenue)) {
            throw new ArithmeticException("сума цін виходить за межі double");
        }
        double average = records.isEmpty() ? 0.0 : revenue / records.size();
        return new double[] {records.size(), average, revenue, urgentCount};
    }

    /**
     * Формує спільний текст для консолі й файла, незалежний від локальної ОС.
     *
     * @param statistics чотири показники з calculateStatistics
     * @return звіт із платформонезалежними переносами рядків
     */
    static String formatReport(double[] statistics) {
        return String.format(Locale.ROOT,
                "Коректних записів: %d%nСередня вартість: %.2f грн%nЗагальний виторг: %.2f грн%nУргентних візитів: %d%n",
                (int) statistics[0], statistics[1], statistics[2], (int) statistics[3]);
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
