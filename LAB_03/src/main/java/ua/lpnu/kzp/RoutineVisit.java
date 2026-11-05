package ua.lpnu.kzp;

/**
 * Плановий візит: огляд, щеплення, контроль після лікування.
 *
 * <p>Підсумкова вартість дорівнює базовій ціні. Нульова ціна допустима — наприклад,
 * безкоштовний повторний огляд.
 */
public final class RoutineVisit extends VetVisit {
    /**
     * Створює плановий візит; усі перевірки виконує базовий тип.
     *
     * @param animal ім'я тварини, непорожнє
     * @param species вид тварини, непорожній
     * @param visitType тип візиту, непорожній
     * @param price базова вартість, скінченна й невід'ємна (нуль допустимий)
     * @throws NullPointerException якщо текстове поле дорівнює {@code null}
     * @throws IllegalArgumentException якщо текстове поле порожнє або ціна недопустима
     */
    public RoutineVisit(String animal, String species, String visitType, double price) {
        super(animal, species, visitType, price, VisitKind.ROUTINE);
    }

    /**
     * Обчислює підсумкову вартість планового візиту: без надбавок.
     *
     * @return базова вартість {@link #getPrice()}
     */
    @Override
    public double finalCost() {
        return getPrice();
    }
}
