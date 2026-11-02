package promotion;

import java.time.LocalDate;

/**
 * Konkretny typ promocji:
 * procentowa przecena (np. -30%).
 */
public class PercentagePromotion extends Promotion {

    private double percentage;   // np. 30 oznacza 30%

    public PercentagePromotion(String name,
                               LocalDate startDate,
                               LocalDate endDate,
                               double percentage) {
        super(name, startDate, endDate);

        if (percentage <= 0 || percentage > 100) {
            throw new IllegalArgumentException("Percentage must be in (0, 100]");
        }
        this.percentage = percentage;
    }

    public double getPercentage() {
        return percentage;
    }

    @Override
    public double getDiscountAmount(double basePrice) {
        // konkretny sposób liczenia zniżki dla promocji procentowej
        if (basePrice < 0) {
            throw new IllegalArgumentException("Base price cannot be negative");
        }
        return basePrice * (percentage / 100.0);
    }

    @Override
    public String toString() {
        return "PercentagePromotion{" +
                "name='" + getName() + '\'' +
                ", percentage=" + percentage +
                "%}";
    }
}
