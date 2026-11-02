package core;

import java.time.Year;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Game extends ObjectPlus4 {

    // atrybut powtarzalny
    private List<Platform> platforms = new ArrayList<>();

    // atrybut złożony + strona "1" asocjacji binarnej 1-*
    private Studio studio;
    private IGamePublisher creator;
    private IGamePublisher publisher;

    // asocjacja z atrybutem: Game 1..* — Purchase
    private List<Purchase> purchases = new ArrayList<>();

    // recenzje i mody stworzone dla tej gry
    private List<Review> reviews = new ArrayList<>();
    private List<Mod> mods = new ArrayList<>();

    // KOMPOZYCJA: Game (całość) — Achievement (część)
    private List<Achievement> achievements = new ArrayList<>();
    private static Set<Achievement> allAchievements = new HashSet<>();
    // ordered association: achievements are stored by name, then date
    private static final Comparator<Achievement> ACHIEVEMENT_ORDER =
            Comparator.comparing(Achievement::getName)
                    .thenComparing(Achievement::getCreatedAt)
                    .thenComparingInt(System::identityHashCode);

    // ASOCJACJA KWALIFIKOWANA: biblioteka gier klienta
    // odwrotna strona – lista właścicieli
    private List<Customer> libraryOwners = new ArrayList<>();

    //atrybuty proste
    private String title;
    private int releaseYear;
    private double price;

    public Game(String title, int releaseYear, double price, Studio studio, List<Platform> platforms) {
        try {
            setTitle(title);
            setReleaseYear(releaseYear);
            setStudio(studio);      // asocjacja 1-*
            setPrice(price);
            setPlatforms(platforms);
        } catch (Exception e) {
            removeFromExtent();
            throw new RuntimeException(e);
        }
    }

    //atrybut pochodny
    public int getAge() {
        return Year.now().getValue() - releaseYear;
    }

    //metoda klasowa
    public static void adjustPrice(double percentage) {
        for (Game game : getExtentFromClass(Game.class)) {
            game.setPrice(game.getPrice() * (1 + percentage / 100));
        }
    }

    public static void rebuildAchievementIndex() {
        allAchievements.clear();
        for (Achievement achievement : getExtentFromClass(Achievement.class)) {
            allAchievements.add(achievement);
        }
    }

    public void setPlatforms(List<Platform> platforms) {
        if (platforms == null || platforms.isEmpty()) {
            throw new IllegalArgumentException("A game must be available on at least one platform.");
        }
        this.platforms = platforms;
    }

    public List<Platform> getPlatforms() {
        return Collections.unmodifiableList(platforms);
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        if( price < 0) {
            throw new IllegalArgumentException("Price cannot be negative.");
        }
        this.price = price;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Title cannot be null or empty.");
        }
        this.title = title;
    }

    public int getReleaseYear() {
        return releaseYear;
    }

    public void setReleaseYear(int releaseYear) {
        if (releaseYear < 1950 || releaseYear > Year.now().getValue()) {
            throw new IllegalArgumentException("Release year must be between 1950 and the current year.");
        }
        this.releaseYear = releaseYear;
    }

    // --- ASOCJACJA BINARNA 1-* Z Studio ---

    public Studio getStudio() {
        return studio;
    }

    public void setStudio(Studio studio) {
        if (studio == null) {
            throw new IllegalArgumentException("Studio must not be null.");
        }

        if (this.studio == studio) {
            return;
        }

        Studio oldStudio = this.studio;
        this.studio = studio;

        if (oldStudio != null && oldStudio != studio) {
            oldStudio.removeGame(this);
        }

        if (!studio.getGames().contains(this)) {
            studio.addGame(this);
        }
    }

    public IGamePublisher getCreator() {
        return creator;
    }

    public void setCreator(IGamePublisher creator) {
        this.creator = creator;
    }

    public IGamePublisher getPublisher() {
        return publisher;
    }

    public void setPublisher(IGamePublisher publisher) {
        this.publisher = publisher;
    }

    // --- ASOCJACJA Z ATRYBUTEM: Game 1..* — Purchase ---

    void _addPurchase(Purchase purchase) {
        if (purchase == null) return;
        if (!purchases.contains(purchase)) {
            purchases.add(purchase);
        }
    }

    public List<Purchase> getPurchases() {
        return Collections.unmodifiableList(purchases);
    }

    // --- REVIEWS ---

    void _addReview(Review review) {
        if (review == null) {
            return;
        }
        if (!reviews.contains(review)) {
            reviews.add(review);
        }
    }

    public List<Review> getReviews() {
        return Collections.unmodifiableList(reviews);
    }

    public double getAverageRating() {
        if (reviews.isEmpty()) {
            return Double.NaN;
        }
        int sum = 0;
        for (Review review : reviews) {
            sum += review.getRating();
        }
        return (double) sum / reviews.size();
    }

    // --- MODS ---

    void _addMod(Mod mod) {
        if (mod == null) {
            return;
        }
        if (!mods.contains(mod)) {
            mods.add(mod);
        }
    }

    public List<Mod> getMods() {
        return Collections.unmodifiableList(mods);
    }

    // --- KOMPOZYCJA: Game — Achievement ---

    void addAchievement(Achievement achievement) {
        if (achievement == null) {
            throw new IllegalArgumentException("Achievement cannot be null.");
        }

        if (achievements.contains(achievement)) {
            return; // już przypisane
        }

        if (allAchievements.contains(achievement) && achievement.getGame() != this) {
            throw new IllegalArgumentException("This achievement is already connected with another game.");
        }

        insertAchievementOrdered(achievement);
        allAchievements.add(achievement);
    }

    private void insertAchievementOrdered(Achievement achievement) {
        int index = Collections.binarySearch(achievements, achievement, ACHIEVEMENT_ORDER);
        if (index < 0) {
            index = -index - 1;
        }
        achievements.add(index, achievement);
    }

    public List<Achievement> getAchievements() {
        return Collections.unmodifiableList(achievements);
    }

    public void deleteGameWithAchievements() {
        for (Achievement a : new ArrayList<>(achievements)) {
            allAchievements.remove(a);
            a.clearGame();
            a.removeFromExtent();
        }
        achievements.clear();
        removeFromExtent();
    }

    // --- ASOCJACJA KWALIFIKOWANA: biblioteka gier klienta ---

    /**
     * Połączenie zwrotne z Customer.addGameToLibrary(...)
     */
    void _addLibraryOwner(Customer customer) {
        if (customer == null) return;
        if (!libraryOwners.contains(customer)) {
            libraryOwners.add(customer);
        }
    }

    public List<Customer> getLibraryOwners() {
        return Collections.unmodifiableList(libraryOwners);
    }

    @Override
    public String toString() {
        return "Game{" +
                "title='" + title + '\'' +
                ", releaseYear=" + releaseYear +
                ", studio=" + studio +
                ", price=" + price +
                ", platforms=" + platforms +
                '}';
    }
}

