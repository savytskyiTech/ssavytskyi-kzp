package ua.lpnu.kzp;

/**
 * Ургентний візит: невідкладна допомога поза чергою.
 *
 * <p>Власне правило підтипу: ургентний візит не може бути безкоштовним, тобто базова
 * ціна строго більша за нуль. Підсумкова вартість містить надбавку за терміновість.
 */
public final class UrgentVisit extends VetVisit {
    /** Коефіцієнт надбавки за терміновість: +50 % до базової ціни. */
    public static final double URGENT_SURCHARGE = 1.5;

    /**
     * Створює ургентний візит із додатною базовою ціною.
     *
     * <p>Спочатку базовий тип перевіряє спільні поля, потім підтип — власне правило ціни.
     *
     * @param animal ім'я тварини, непорожнє
     * @param species вид тварини, непорожній
     * @param visitType тип візиту, непорожній
     * @param price базова вартість, скінченна й строго додатна
     * @throws NullPointerException якщо текстове поле дорівнює {@code null}
     * @throws IllegalArgumentException якщо порушено спільний інваріант або ціна дорівнює нулю
     */
    public UrgentVisit(String animal, String species, String visitType, double price) {
        super(animal, species, visitType, price, VisitKind.URGENT);
        // Від'ємну ціну вже відхилив базовий тип, тож <= 0 тут ловить лише нуль (і -0.0).
        // Порівняння == для double SpotBugs вважає ризикованим, тому використано <=.
        if (price <= 0) {
            throw new IllegalArgumentException("ургентний візит не може бути безкоштовним");
        }
    }

    /**
     * Обчислює підсумкову вартість ургентного візиту: базова ціна з надбавкою 50 %.
     *
     * @return {@code getPrice() * URGENT_SURCHARGE}
     */
    @Override
    public double finalCost() {
        return getPrice() * URGENT_SURCHARGE;
    }
}
