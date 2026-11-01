package ua.lpnu.kzp;

/**
 * Незмінний підсумок візитів: показники вартості та ургентності для звіту.
 *
 * <p>Замінює масив {@code double[]} з лабораторної роботи № 1, де показники
 * розрізнялися лише індексами.
 *
 * @param count кількість коректних візитів
 * @param averagePrice середня вартість візиту, {@code 0.0} для порожнього набору
 * @param revenue загальний виторг — сума вартостей
 * @param urgentCount кількість ургентних візитів
 */
public record PriceUrgency(int count, double averagePrice, double revenue, int urgentCount) {
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
                || !Double.isFinite(revenue) || revenue < 0) {
            throw new IllegalArgumentException(
                    "середня вартість і виторг мають бути скінченними невід'ємними числами");
        }
    }
}
