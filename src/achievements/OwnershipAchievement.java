package achievements;

// OwnershipAchievement.java
// Podklasa aspektu "typ osiągnięcia":
// osiągnięcie za posiadanie odpowiedniej liczby gier na platformie.

public class OwnershipAchievement extends PlatformAchievement {

    private int requiredGamesOwned;

    public OwnershipAchievement(String code,
                                String name,
                                String description,
                                AchievementTier tier,
                                int basePoints,
                                int requiredGamesOwned) {
        super(code, name, description, tier, basePoints);

        if (requiredGamesOwned <= 0) {
            throw new IllegalArgumentException("Required games owned must be positive");
        }
        this.requiredGamesOwned = requiredGamesOwned;
    }

    public int getRequiredGamesOwned() {
        return requiredGamesOwned;
    }

    @Override
    public String getRequirementDescription() {
        return "Posiadaj co najmniej " + requiredGamesOwned + " gier na platformie.";
    }
}
