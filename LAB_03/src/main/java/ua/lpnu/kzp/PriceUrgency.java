package ua.lpnu.kzp;

/**
 * Незмінний підсумок візитів: показники вартості та ургентності для звіту.
 *
 * <p>Замінює масив {@code double[]} з лабораторної роботи № 1, де показники
 * розрізнялися лише індексами. У лабораторній роботі № 3 додано компонент
 * {@code finalRevenue} — суму поліморфних {@link VetVisit#finalCost()}.
 *
 * @param count кількість коректних візитів
 * @param averagePrice середня базова вартість візиту, {@code 0.0} для порожнього набору
 * @param revenue загальний виторг — сума базових вартостей {@code price}
 * @param urgentCount кількість ургентних візитів
 * @param finalRevenue сума підсумкових вартостей з урахуванням надбавок підтипів
 */
public record PriceUrgency(int count, double averagePrice, double revenue, int urgentCount,
        double finalRevenue) {
    /**
     * Перевіряє узгодженість показників.
     *
     * @throws IllegalArgumentException якщо лічильник від'ємний, ургентних більше, ніж
     *     усіх візитів, або сума чи середнє від'ємні чи нескінченні
     */
    public PriceUrgency {
        if (count < 0 || urgentCount < 0 || urgentCount > count) {
            throw new IllegalArgumentException(
                    "лічильники мають бути невід'ємними, а ургентних — не більше за всі візити");
        }
        if (!Double.isFinite(averagePrice) || averagePrice < 0
                || !Double.isFinite(revenue) || revenue < 0
                || !Double.isFinite(finalRevenue) || finalRevenue < 0) {
            throw new IllegalArgumentException(
                    "середня вартість і суми мають бути скінченними невід'ємними числами");
        }
    }
}
