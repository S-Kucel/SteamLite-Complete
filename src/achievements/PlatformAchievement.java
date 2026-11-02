package achievements;

import core.ObjectPlus;

// PlatformAchievement.java
// Dziedziczenie wieloaspektowe – aspekt 1: typ osiągnięcia (hierarchia klas).
// Aspekt 2 (poziom trudności) jest zrealizowany jako enum AchievementTier
// w tej nadklasie (spłaszczenie drugiego aspektu).

public abstract class PlatformAchievement extends ObjectPlus {

    private String code;         // unikalny kod, np. "OWN_10_GAMES"
    private String name;         // nazwa wyświetlana
    private String description;  // opis ogólny

    // Drugi aspekt (tier trudności / rzadkości) – NIE jako dziedziczenie,
    // tylko jako dyskryminator w nadklasie:
    private AchievementTier tier;

    // bazowa liczba punktów, mnożona przez multiplier z AchievementTier
    private int basePoints;

    protected PlatformAchievement(String code,
                                  String name,
                                  String description,
                                  AchievementTier tier,
                                  int basePoints) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Code cannot be null or blank");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be null or blank");
        }
        if (description == null) {
            description = "";
        }
        if (tier == null) {
            throw new IllegalArgumentException("Tier cannot be null");
        }
        if (basePoints <= 0) {
            throw new IllegalArgumentException("Base points must be positive");
        }

        this.code = code;
        this.name = name;
        this.description = description;
        this.tier = tier;
        this.basePoints = basePoints;
    }

    // --- Drugi aspekt: operacje na tierze ---

    public AchievementTier getTier() {
        return tier;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getBasePoints() {
        return basePoints;
    }

    /**
     * Łączna liczba punktów przyznawanych za to osiągnięcie,
     * liczona na podstawie basePoints i mnożnika z AchievementTier.
     */
    public int getRewardPoints() {
        return (int) Math.round(basePoints * tier.getMultiplier());
    }

    // --- Abstrakcyjna metoda opisująca wymagania (polimorfizm po typie) ---

    /**
     * Abstrakcyjna metoda – każda podklasa opisuje własny
     * sposób spełnienia wymagań (aspekt "typ osiągnięcia").
     */
    public abstract String getRequirementDescription();

    @Override
    public String toString() {
        return "PlatformAchievement{" +
                "code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", tier=" + tier +
                ", rewardPoints=" + getRewardPoints() +
                ", requirements='" + getRequirementDescription() + '\'' +
                '}';
    }
}
