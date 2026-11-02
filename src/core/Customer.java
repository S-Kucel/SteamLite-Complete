package core;

import achievements.PlatformAchievement;
import community.CommunityMember;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import promotion.FixedAmountPromotion;
import promotion.PercentagePromotion;
import promotion.Promotion;

public class Customer extends User {
    private static final String ROLE_PURCHASED = "purchased";
    private static final String ROLE_PURCHASED_BY = "purchasedBy";
    private static final String ROLE_LIBRARY = "library";
    private static final String ROLE_LIBRARY_OWNER = "libraryOwner";

    // atrybut powtarzalny – lista kupionych gier (jak wcześniej)
    private List<Game> purchasedGames = new ArrayList<>();

    // asocjacja z atrybutem: Customer 1..* – Purchase
    private List<Purchase> purchases = new ArrayList<>();

    // {bag} / {history}: historia zakupów z duplikatami
    private List<Game> purchaseHistory = new ArrayList<>();

    // recenzje i modyfikację stworzone przez użytkownika
    private List<Review> reviews = new ArrayList<>();
    private List<Mod> mods = new ArrayList<>();

    // playtime w grach(w godzinach)
    private Map<Game, Integer> playtimeHours = new HashMap<>();

    private CommunityMember communityProfile;
    private Set<String> claimedPromotionCodes = new HashSet<>();
    private Set<String> usedPromotionCodes = new HashSet<>();
    private Set<PlatformAchievement> unlockedPlatformAchievements = new HashSet<>();

    // ASOCJACJA KWALIFIKOWANA: biblioteka gier klienta
    // kluczem jest kwalifikator – tytuł gry
    private Map<String, Game> libraryByTitle = new TreeMap<>();

    private double balance;

    public Customer(String nick, String username, String password, int age) {
        super(nick, username, password, age);
    }

    public Customer(String username, String password, int age) {
        super(username, password, age);
    }

    /**
     * Biznesowa operacja zakupu gry przez klienta.
     * - kontrola salda
     * - utworzenie obiektu Purchase (klasa asocjacji z atrybutem)
     * - aktualizacja listy purchasedGames
     * - dodanie gry do biblioteki kwalifikowanej (po tytule)
     */
    public void purchaseGame(Game game) throws Exception {
        purchaseGameInternal(game, game.getPrice(), null);
    }

    public void purchaseGame(Game game, Promotion promotion) throws Exception {
        if (promotion == null) {
            purchaseGame(game);
            return;
        }
        if (!promotion.isActive(LocalDate.now())) {
            throw new IllegalArgumentException("Promotion is not active.");
        }
        double finalPrice = promotion.apply(game.getPrice());
        purchaseGameInternal(game, finalPrice, promotion);
    }

    private void purchaseGameInternal(Game game, double pricePaid, Promotion promotion) throws Exception {
        if (game == null) {
            throw new IllegalArgumentException("Game must not be null.");
        }
        if (pricePaid < 0) {
            throw new IllegalArgumentException("Price paid cannot be negative.");
        }
        if (balance >= pricePaid) {
            balance -= pricePaid;

            // klasa asocjacji z atrybutem (purchaseDate, pricePaid)
            Purchase purchase = new Purchase(this, game, pricePaid);
            if (promotion instanceof PercentagePromotion percent) {
                purchase.applyPercentagePromotion(percent);
            } else if (promotion instanceof FixedAmountPromotion fixed) {
                purchase.applyFixedAmountPromotion(fixed);
            } else if (promotion != null) {
                throw new IllegalArgumentException("Unsupported promotion type.");
            }

            // {bag}: zapisujemy kazdy zakup, nawet jesli gra sie powtarza
            purchaseHistory.add(game);

            // utrzymujemy prosta liste gier jako atrybut powtarzalny
            if (!purchasedGames.contains(game)) {
                purchasedGames.add(game);
            }

            // asocjacja nadrzedna: zakupione gry (Customer-Game)
            addLink(ROLE_PURCHASED, ROLE_PURCHASED_BY, game);

            // dodajemy gre do kwalifikowanej biblioteki
            addGameToLibrary(game);
        } else {
            throw new IllegalArgumentException("Insufficient balance to purchase the game.");
        }
    }

    public Review writeReview(Game game, int rating, String description) throws Exception {
        if (game == null) {
            throw new IllegalArgumentException("Game must not be null.");
        }
        if (!purchasedGames.contains(game)) {
            throw new IllegalArgumentException("You can review only purchased games.");
        }
        return new Review(this, game, rating, description);
    }

    public Mod createMod(Game game, String description) throws Exception {
        if (game == null) {
            throw new IllegalArgumentException("Game must not be null.");
        }
        if (!purchasedGames.contains(game)) {
            throw new IllegalArgumentException("You can create mods only for purchased games.");
        }
        return new Mod(this, game, description);
    }

    public CommunityMember getCommunityProfile() {
        return communityProfile;
    }

    public CommunityMember createCommunityProfile(String email) {
        if (communityProfile != null) {
            return communityProfile;
        }
        return new CommunityMember(this, email);
    }

    public void setCommunityProfile(CommunityMember profile) {
        if (profile == null) {
            throw new IllegalArgumentException("Community profile is required.");
        }
        if (profile.getCustomer() != this) {
            throw new IllegalArgumentException("Profile belongs to another customer.");
        }
        if (communityProfile != null && communityProfile != profile) {
            throw new IllegalStateException("Community profile already set.");
        }
        communityProfile = profile;
    }

    void _addReview(Review review) {
        if (review == null) {
            return;
        }
        if (!reviews.contains(review)) {
            reviews.add(review);
        }
    }

    void _addMod(Mod mod) {
        if (mod == null) {
            return;
        }
        if (!mods.contains(mod)) {
            mods.add(mod);
        }
    }

    public List<Review> getReviews() {
        return Collections.unmodifiableList(reviews);
    }

    public List<Mod> getMods() {
        return Collections.unmodifiableList(mods);
    }

    public int getHelpfulVotesReceived() {
        int total = 0;
        for (Review review : reviews) {
            total += review.getHelpfulCount();
        }
        return total;
    }

    public void setPlaytimeHours(Game game, int hours) {
        if (game == null) {
            throw new IllegalArgumentException("Game must not be null.");
        }
        if (hours < 0) {
            throw new IllegalArgumentException("Hours played cannot be negative.");
        }
        playtimeHours.put(game, hours);
    }

    public int getPlaytimeHours(Game game) {
        if (game == null) {
            return 0;
        }
        return playtimeHours.getOrDefault(game, 0);
    }

    public int getTotalPlaytimeHours() {
        int total = 0;
        for (int hours : playtimeHours.values()) {
            total += hours;
        }
        return total;
    }

    public double getBalance() {
        return balance;
    }

    public void addFunds(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount to add must be positive.");
        }
        this.balance += amount;
    }

    public boolean hasClaimedPromotionCode(String code) {
        if (code == null) {
            return false;
        }
        return claimedPromotionCodes.contains(code);
    }

    public boolean isPromotionCodeUsed(String code) {
        if (code == null) {
            return false;
        }
        return usedPromotionCodes.contains(code);
    }

    public void claimPromotionCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Promo code cannot be empty.");
        }
        claimedPromotionCodes.add(code);
    }

    public void markPromotionCodeUsed(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Promo code cannot be empty.");
        }
        usedPromotionCodes.add(code);
    }

    public Set<String> getClaimedPromotionCodes() {
        return Collections.unmodifiableSet(claimedPromotionCodes);
    }

    public void unlockPlatformAchievement(PlatformAchievement achievement) {
        if (achievement == null) {
            return;
        }
        unlockedPlatformAchievements.add(achievement);
    }

    public boolean hasUnlockedPlatformAchievement(PlatformAchievement achievement) {
        if (achievement == null) {
            return false;
        }
        return unlockedPlatformAchievements.contains(achievement);
    }

    public Set<PlatformAchievement> getUnlockedPlatformAchievements() {
        return Collections.unmodifiableSet(unlockedPlatformAchievements);
    }

    public List<Game> getPurchasedGames() {
        return Collections.unmodifiableList(purchasedGames);
    }

    public List<Game> getPurchaseHistory() {
        return Collections.unmodifiableList(purchaseHistory);
    }

    // --- ASOCJACJA Z ATRYBUTEM: Customer 1..* — Purchase ---

    // pakietowa – wywoływana tylko z klasy Purchase
    void _addPurchase(Purchase purchase) {
        if (purchase == null) return;
        if (!purchases.contains(purchase)) {
            purchases.add(purchase);
        }
    }

    public List<Purchase> getPurchases() {
        return Collections.unmodifiableList(purchases);
    }

    // --- ASOCJACJA KWALIFIKOWANA: biblioteka gier klienta ---

    /**
     * Dodanie gry do biblioteki kwalifikowanej.
     * Kluczem jest tytuł gry (kwalifikator).
     * Tworzy też połączenie zwrotne po stronie Game.
     */
    public void addGameToLibrary(Game game) throws Exception {
        if (game == null) {
            throw new IllegalArgumentException("Game cannot be null.");
        }
        String key = game.getTitle();

        // jeżeli nie mamy jeszcze gry o takim tytule w bibliotece
        if (!libraryByTitle.containsKey(key)) {
            // ograniczenie subset: biblioteka jest podzbiorem zakupionych gier
            addLink_subset(ROLE_LIBRARY, ROLE_LIBRARY_OWNER, ROLE_PURCHASED, game);
            libraryByTitle.put(key, game);

            // połączenie zwrotne
            game._addLibraryOwner(this);
        }
    }

    /**
     * Znalezienie gry w bibliotece po kwalifikatorze (tytuł).
     * 0..1 – może nie być gry o takim tytule.
     */
    public Game findGameInLibrary(String title) {
        if (title == null) {
            throw new IllegalArgumentException("Title must not be null.");
        }
        if (!libraryByTitle.containsKey(title)) {
            return null; // albo można rzucić wyjątek, jak w przykładzie z wykładu
        }
        return libraryByTitle.get(title);
    }

    /**
     * Zwraca wszystkie gry z biblioteki klienta.
     */
    public Collection<Game> getLibraryGames() {
        return Collections.unmodifiableCollection(libraryByTitle.values());
    }

    @Override
    public String toString() {

        return "Customer{" +
                "nick='" + getNick() + '\'' +
                ", username='" + getUsername() + '\'' +
                ", age=" + getAge() +
                ", balance=" + getBalance() +
                ", purchasedGames=" + getPurchasedGames().stream().map(Game::getTitle).toList() +
                '}';
    }
}



