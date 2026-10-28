package ua.lpnu.kzp;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/** Стартова програма для перевірки збірки та запуску на різних ОС. */
public final class Main {
    private Main() {
    }

    /**
     * Виводить стартове повідомлення, довідку або версію збірки.
     *
     * @param args без аргументів, {@code --help} або {@code --version}
     * @throws IOException якщо ресурс версії відсутній або не читається
     * @throws IllegalArgumentException якщо передано непідтримувані аргументи
     */
    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            System.out.println("Лабораторна № 1 — Ветклініка. Інфраструктура готова; обробка CSV ще не реалізована.");
            return;
        }
        if (args.length != 1) {
            throw new IllegalArgumentException("Очікується один параметр: --help або --version.");
        }
        switch (args[0]) {
            case "--help" -> System.out.println("""
                    Лабораторна № 1 — Ветклініка (варіант 21)
                    --help     Показати довідку
                    --version  Показати версію та номер збірки
                    Обробка CSV буде додана на наступному етапі.""");
            case "--version" -> System.out.println(version());
            default -> throw new IllegalArgumentException(
                    "Невідомий параметр: %s. Скористайтеся --help.".formatted(args[0]));
        }
    }

    /**
     * Читає версію та номер збірки з ресурсу, відфільтрованого Maven.
     *
     * @return версія у форматі {@code 1.0.0+local} або {@code 1.0.0+42}
     * @throws IOException якщо ресурс або необхідні властивості відсутні
     */
    private static String version() throws IOException {
        Properties properties = new Properties();
        try (InputStream stream = Main.class.getResourceAsStream("/version.properties")) {
            if (stream == null) {
                throw new IOException("Ресурс version.properties не знайдено.");
            }
            properties.load(stream);
        }
        String version = properties.getProperty("version");
        String build = properties.getProperty("build");
        if (version == null || build == null) {
            throw new IOException("Ресурс версії має містити version і build.");
        }
        return "%s+%s".formatted(version, build);
    }
}
