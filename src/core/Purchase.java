package core;

import java.time.LocalDate;
import java.util.Locale;
import promotion.FixedAmountPromotion;
import promotion.PercentagePromotion;

public class Purchase extends ObjectPlus4 {
    private static final String ROLE_PERCENT_PROMO = "percentPromotion";
    private static final String ROLE_PERCENT_PROMO_FOR = "percentPromotionFor";
    private static final String ROLE_FIXED_PROMO = "fixedPromotion";
    private static final String ROLE_FIXED_PROMO_FOR = "fixedPromotionFor";

    // "zewnętrzne" obiekty
    private Customer customer;
    private Game game;

    // atrybuty asocjacji
    private LocalDate purchaseDate;
    private double pricePaid;
    private PercentagePromotion percentagePromotion;
    private FixedAmountPromotion fixedAmountPromotion;

    /**
     * Tworzy nowe wystąpienie asocjacji między Customer i Game.
     * Konstruktor od razu zakłada wszystkie potrzebne powiązania
     * (referencje na zewnątrz + dodanie do kolekcji po obu stronach).
     */
    public Purchase(Customer customer, Game game, double pricePaid) {
        if (customer == null || game == null) {
            throw new IllegalArgumentException("Customer and Game must not be null.");
        }
        if (pricePaid < 0) {
            throw new IllegalArgumentException("Price paid cannot be negative.");
        }

        this.customer = customer;
        this.game = game;
        this.pricePaid = pricePaid;
        this.purchaseDate = LocalDate.now();
        addXorRole(ROLE_PERCENT_PROMO);
        addXorRole(ROLE_FIXED_PROMO);

        // połączenia zwrotne (1-* i *-1)
        customer._addPurchase(this);
        game._addPurchase(this);
    }

    public Customer getCustomer() {
        return customer;
    }

    public Game getGame() {
        return game;
    }

    public LocalDate getPurchaseDate() {
        return purchaseDate;
    }

    public double getPricePaid() {
        return pricePaid;
    }

    public void applyPercentagePromotion(PercentagePromotion promotion) throws Exception {
        if (promotion == null) {
            throw new IllegalArgumentException("Promotion cannot be null.");
        }
        addLinkXor(ROLE_PERCENT_PROMO, ROLE_PERCENT_PROMO_FOR, promotion);
        this.percentagePromotion = promotion;
    }

    public void applyFixedAmountPromotion(FixedAmountPromotion promotion) throws Exception {
        if (promotion == null) {
            throw new IllegalArgumentException("Promotion cannot be null.");
        }
        addLinkXor(ROLE_FIXED_PROMO, ROLE_FIXED_PROMO_FOR, promotion);
        this.fixedAmountPromotion = promotion;
    }

    public PercentagePromotion getPercentagePromotion() {
        return percentagePromotion;
    }

    public FixedAmountPromotion getFixedAmountPromotion() {
        return fixedAmountPromotion;
    }

    @Override
    public String toString() {
        return "Purchase{" +
                "customer=" + customer.getUsername() +
                ", game=" + game.getTitle() +
                ", purchaseDate=" + purchaseDate +
                ", pricePaid=" + String.format(Locale.US, "%.2f", pricePaid) +
                '}';
    }
}
