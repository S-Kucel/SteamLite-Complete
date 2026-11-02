package core;

import java.time.LocalDate;

public class Mod extends ObjectPlus {
    private final Customer author;
    private final Game game;
    private final String description;
    private final LocalDate createdAt;

    public Mod(Customer author, Game game, String description) {
        if (author == null || game == null) {
            throw new IllegalArgumentException("Author and game must not be null.");
        }
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Mod description cannot be empty.");
        }
        this.author = author;
        this.game = game;
        this.description = description;
        this.createdAt = LocalDate.now();

        author._addMod(this);
        game._addMod(this);
    }

    public Customer getAuthor() {
        return author;
    }

    public Game getGame() {
        return game;
    }

    public String getDescription() {
        return description;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    @Override
    public String toString() {
        return "Mod{" +
                "author=" + author.getUsername() +
                ", game=" + game.getTitle() +
                ", createdAt=" + createdAt +
                '}';
    }
}
