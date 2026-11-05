package ua.lpnu.kzp;

/**
 * Категорія візиту до ветклініки: фіксований набір значень замість довільного тексту.
 *
 * <p>Кожна константа знає свій підпис для людини та чи вважається візит ургентним.
 * Значення порівнюються через {@code ==}, бо JVM створює кожну константу один раз.
 */
public enum VisitKind {
    /** Плановий візит: огляд, щеплення, контроль. */
    ROUTINE("плановий", false),
    /** Ургентний візит: невідкладна допомога з надбавкою до вартості. */
    URGENT("ургентний", true);

    private final String label;
    private final boolean urgent;

    /* Конструктор enum викликає лише JVM під час створення двох констант. */
    VisitKind(String label, boolean urgent) {
        this.label = label;
        this.urgent = urgent;
    }

    /**
     * Повертає українську назву категорії для звіту чи повідомлень.
     *
     * @return підпис категорії, наприклад {@code плановий}
     */
    public String label() {
        return label;
    }

    /**
     * Повідомляє, чи належить категорія до ургентних візитів.
     *
     * @return {@code true} лише для {@link #URGENT}
     */
    public boolean isUrgent() {
        return urgent;
    }
}
