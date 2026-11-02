package achievements;

// AchievementTier.java
// Drugi aspekt dziedziczenia wieloaspektowego – poziom trudności / tier.
// Zastępuje osobną gałąź dziedziczenia za pomocą enum + mnożnika punktów.

public enum AchievementTier {
    BRONZE(1.0),
    SILVER(1.5),
    GOLD(2.0),
    PLATINUM(3.0);

    private final double multiplier;

    AchievementTier(double multiplier) {
        this.multiplier = multiplier;
    }

    /**
     * Mnożnik używany przy liczeniu punktów nagrody.
     * Np. GOLD daje więcej punktów niż BRONZE.
     */
    public double getMultiplier() {
        return multiplier;
    }
}
