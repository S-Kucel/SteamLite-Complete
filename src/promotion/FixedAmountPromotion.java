package promotion;

import java.time.LocalDate;

/**
 * Konkretny typ promocji:
 * stała kwota zniżki (np. -20 PLN).
 */
public class FixedAmountPromotion extends Promotion {

    private double amount;   // kwota zniżki, np. 20.0

    public FixedAmountPromotion(String name,
                                LocalDate startDate,
                                LocalDate endDate,
                                double amount) {
        super(name, startDate, endDate);

        if (amount <= 0) {
            throw new IllegalArgumentException("Discount amount must be positive");
        }
        this.amount = amount;
    }

    public double getAmount() {
        return amount;
    }

    @Override
    public double getDiscountAmount(double basePrice) {
        if (basePrice < 0) {
            throw new IllegalArgumentException("Base price cannot be negative");
        }
        // zniżka nie może być większa niż cena bazowa
        return Math.min(amount, basePrice);
    }

    @Override
    public String toString() {
        return "FixedAmountPromotion{" +
                "name='" + getName() + '\'' +
                ", amount=" + amount +
                "}";
    }
}
