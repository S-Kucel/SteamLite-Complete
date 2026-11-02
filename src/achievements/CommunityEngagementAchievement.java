package achievements;

// CommunityEngagementAchievement.java
// Podklasa aspektu "typ osiągnięcia":
// osiągnięcie za aktywność w społeczności (recenzje, oceny itp.).

public class CommunityEngagementAchievement extends PlatformAchievement {

    private int requiredReviewsWritten;
    private int requiredHelpfulVotes;

    public CommunityEngagementAchievement(String code,
                                          String name,
                                          String description,
                                          AchievementTier tier,
                                          int basePoints,
                                          int requiredReviewsWritten,
                                          int requiredHelpfulVotes) {
        super(code, name, description, tier, basePoints);

        if (requiredReviewsWritten < 0 || requiredHelpfulVotes < 0) {
            throw new IllegalArgumentException("Requirements cannot be negative");
        }
        this.requiredReviewsWritten = requiredReviewsWritten;
        this.requiredHelpfulVotes = requiredHelpfulVotes;
    }

    public int getRequiredReviewsWritten() {
        return requiredReviewsWritten;
    }

    public int getRequiredHelpfulVotes() {
        return requiredHelpfulVotes;
    }

    @Override
    public String getRequirementDescription() {
        StringBuilder sb = new StringBuilder();
        if (requiredReviewsWritten > 0) {
            sb.append("Napisz co najmniej ")
                    .append(requiredReviewsWritten)
                    .append(" recenzji");
        }
        if (requiredHelpfulVotes > 0) {
            if (sb.length() > 0) {
                sb.append(" oraz ");
            }
            sb.append("otrzymaj co najmniej ")
                    .append(requiredHelpfulVotes)
                    .append(" głosów 'przydatna recenzja'");
        }
        if (sb.length() == 0) {
            sb.append("Brak dodatkowych wymagań (testowe osiągnięcie).");
        } else {
            sb.append(".");
        }
        return sb.toString();
    }
}
