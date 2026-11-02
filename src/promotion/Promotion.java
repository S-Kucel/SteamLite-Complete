package promotion;

import core.ObjectPlus4;
import java.time.LocalDate;

/**
 * Klasa abstrakcyjna opisująca ogólną promocję.
 * Nie tworzymy obiektów Promotion bezpośrednio – zawsze przez podklasy.
 * Pokazuje:
 *  - klasę abstrakcyjną,
 *  - metodę abstrakcyjną (getDiscountAmount).
 */
public abstract class Promotion extends ObjectPlus4 {

    private String name;
    private String code;
    private LocalDate startDate;
    private LocalDate endDate;

    public Promotion(String name, LocalDate startDate, LocalDate endDate) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Promotion name cannot be empty");
        }
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Dates cannot be null");
        }
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("End date cannot be before start date");
        }

        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    /**
     * Metoda abstrakcyjna – każda konkretna promocja
     * inaczej liczy kwotę zniżki dla danej ceny bazowej.
     */
    public abstract double getDiscountAmount(double basePrice);

    /**
     * Prosta metoda pomocnicza – zwraca cenę po uwzględnieniu zniżki.
     * Nie jest abstrakcyjna – jest wspólna dla wszystkich podklas.
     */
    public double apply(double basePrice) {
        if (basePrice < 0) {
            throw new IllegalArgumentException("Base price cannot be negative");
        }
        double discount = getDiscountAmount(basePrice);
        double finalPrice = basePrice - discount;
        return Math.max(finalPrice, 0);
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Promotion code cannot be empty");
        }
        this.code = code;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public boolean isActive(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }

    public boolean isActiveNow() {
        return isActive(LocalDate.now());
    }

    @Override
    public String toString() {
        return "Promotion{" +
                "name='" + name + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                '}';
    }
}
