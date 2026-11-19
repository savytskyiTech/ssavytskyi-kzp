package ua.lpnu.kzp;

/** Вид тварини в розширеному наборі візитів. */
public enum AnimalSpecies {
    /** Кіт. */
    CAT("Кіт"),
    /** Собака. */
    DOG("Собака"),
    /** Кролик. */
    RABBIT("Кролик");

    private final String label;

    AnimalSpecies(String label) {
        this.label = label;
    }

    /** Повертає точний український підпис виду в CSV. */
    public String label() {
        return label;
    }

    /** Знаходить вид за точним підписом або повідомляє про невідоме значення. */
    static AnimalSpecies fromLabel(String label) {
        for (AnimalSpecies species : values()) {
            if (species.label.equals(label)) {
                return species;
            }
        }
        throw new IllegalArgumentException("невідомий вид тварини: %s".formatted(label));
    }
}
