package core;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

public class Review extends ObjectPlus {
    private final Customer author;
    private final Game game;
    private final int rating;
    private final String description;
    private final LocalDate createdAt;
    private final Set<Customer> helpfulBy = new HashSet<>();

    public Review(Customer author, Game game, int rating, String description) {
        if (author == null || game == null) {
            throw new IllegalArgumentException("Author and game must not be null.");
        }
        if (rating < 1 || rating > 10) {
            throw new IllegalArgumentException("Rating must be between 1 and 10.");
        }
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Review description cannot be empty.");
        }
        this.author = author;
        this.game = game;
        this.rating = rating;
        this.description = description;
        this.createdAt = LocalDate.now();

        author._addReview(this);
        game._addReview(this);
    }

    public Customer getAuthor() {
        return author;
    }

    public Game getGame() {
        return game;
    }

    public int getRating() {
        return rating;
    }

    public String getDescription() {
        return description;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public void addHelpfulVote(Customer voter) {
        if (voter == null) {
            throw new IllegalArgumentException("Voter is required.");
        }
        if (voter == author) {
            throw new IllegalArgumentException("Author cannot mark own review as helpful.");
        }
        if (!helpfulBy.add(voter)) {
            throw new IllegalArgumentException("User already marked this review as helpful.");
        }
    }

    public int getHelpfulCount() {
        return helpfulBy.size();
    }

    public boolean isHelpfulBy(Customer voter) {
        if (voter == null) {
            return false;
        }
        return helpfulBy.contains(voter);
    }

    @Override
    public String toString() {
        return "Review{" +
                "author=" + author.getUsername() +
                ", game=" + game.getTitle() +
                ", rating=" + rating +
                ", createdAt=" + createdAt +
                ", helpfulCount=" + helpfulBy.size() +
                '}';
    }
}
