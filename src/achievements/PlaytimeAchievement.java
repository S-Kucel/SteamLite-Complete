package achievements;

// PlaytimeAchievement.java
// Podklasa aspektu "typ osiągnięcia":
// osiągnięcie za łączny czas spędzony w grach.

public class PlaytimeAchievement extends PlatformAchievement {

    private int requiredHoursPlayed;

    public PlaytimeAchievement(String code,
                               String name,
                               String description,
                               AchievementTier tier,
                               int basePoints,
                               int requiredHoursPlayed) {
        super(code, name, description, tier, basePoints);

        if (requiredHoursPlayed <= 0) {
            throw new IllegalArgumentException("Required hours played must be positive");
        }
        this.requiredHoursPlayed = requiredHoursPlayed;
    }

    public int getRequiredHoursPlayed() {
        return requiredHoursPlayed;
    }

    @Override
    public String getRequirementDescription() {
        return "Rozegraj łącznie co najmniej " + requiredHoursPlayed + " godzin w grach.";
    }
}
