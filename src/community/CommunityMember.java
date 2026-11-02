package community;

import core.Customer;
import core.ObjectPlus;
import core.Review;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;

public final class CommunityMember extends ObjectPlus {
    private static final double WEIGHT_GAMES = 0.5;
    private static final double WEIGHT_HOURS = 0.1;
    private static final double WEIGHT_MODS = 10.0;
    private static final double WEIGHT_REVIEWS = 1.5;
    private static final double WEIGHT_AVG_SCORE = 2.0;

    private final Customer customer;
    private final String email;

    public CommunityMember(Customer customer, String email) {
        if (customer == null) {
            throw new IllegalArgumentException("Customer is required.");
        }
        this.customer = customer;
        this.email = normalizeEmail(email, customer);
        customer.setCommunityProfile(this);
    }

    private static String normalizeEmail(String email, Customer customer) {
        if (email != null && !email.isBlank()) {
            return email.trim();
        }
        String username = customer.getUsername();
        if (username == null || username.isBlank()) {
            return "HowDidYouDidIt@community.local";
        }
        return username + "@community.local";
    }

    public Customer getCustomer() {
        return customer;
    }

    public String getNickname() {
        String nick = customer.getNick();
        if (nick == null || nick.isBlank()) {
            return customer.getUsername();
        }
        return nick;
    }

    public String getEmail() {
        return email;
    }

    public Set<MemberRole> getRoles() {
        EnumSet<MemberRole> roles = EnumSet.noneOf(MemberRole.class);
        if (customer.getPurchasedGames().size() >= 1) {
            roles.add(MemberRole.PLAYER);
        }
        if (customer.getReviews().size() >= 1) {
            roles.add(MemberRole.REVIEWER);
        }
        if (customer.getMods().size() >= 1) {
            roles.add(MemberRole.MODDER);
        }
        return roles;
    }

    public boolean hasRole(MemberRole role) {
        return getRoles().contains(role);
    }

    public int getGamesOwned() {
        return customer.getLibraryGames().size();
    }

    public int getHoursPlayed() {
        return customer.getTotalPlaytimeHours();
    }

    public int getReviewsWritten() {
        return customer.getReviews().size();
    }

    public int getModsPublished() {
        return customer.getMods().size();
    }

    public double getAverageReviewScore() {
        if (customer.getReviews().isEmpty()) {
            return 0.0;
        }
        int sum = 0;
        for (Review review : customer.getReviews()) {
            sum += review.getRating();
        }
        return (double) sum / customer.getReviews().size();
    }

    //Ostatnie jest aktem przeciwko osobom wstawiającym specjalnie wszędzie niskie recenzję.
    public double getCommunityScore() {
        return getGamesOwned() * WEIGHT_GAMES
                + getHoursPlayed() * WEIGHT_HOURS
                + getModsPublished() * WEIGHT_MODS
                + getReviewsWritten() * WEIGHT_REVIEWS
                + getAverageReviewScore() * WEIGHT_AVG_SCORE;
    }

    @Override
    public String toString() {
        return "CommunityMember{" +
                "nickname='" + getNickname() + '\'' +
                ", email='" + email + '\'' +
                ", roles=" + getRoles() +
                ", gamesOwned=" + getGamesOwned() +
                ", hoursPlayed=" + getHoursPlayed() +
                ", modsPublished=" + getModsPublished() +
                ", reviewsWritten=" + getReviewsWritten() +
                ", averageReviewScore=" + formatScore(getAverageReviewScore()) +
                ", communityScore=" + formatScore(getCommunityScore()) +
                '}';
    }

    private String formatScore(double value) {
        return String.format(Locale.US, "%.1f", value);
    }
}
