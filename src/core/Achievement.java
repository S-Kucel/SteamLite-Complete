package core;

import java.time.LocalDate;

public class Achievement extends ObjectPlus {

    // Całość, do której należy osiągnięcie
    private Game game;

    private String name;
    private String description;
    private LocalDate createdAt;

    /**
     * Konstruktor prywatny – część nie może być tworzona samodzielnie
     * spoza klasy (tylko przez metodę createForGame).
     */
    private Achievement(Game game, String name, String description) {
        if (game == null) {
            throw new IllegalArgumentException("Game in composition cannot be null.");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Achievement name cannot be null or empty.");
        }
        this.game = game;
        this.name = name;
        this.description = (description != null ? description : "");
        this.createdAt = LocalDate.now();
    }

    /**
     * Metoda klasowa tworząca część w kontekście danej całości.
     * Tworzy Achievement i od razu łączy go z Game (połączenie zwrotne).
     */
    public static Achievement createForGame(Game game, String name, String description) {
        if (game == null) {
            throw new IllegalArgumentException("Game in composition cannot be null.");
        }

        Achievement achievement = new Achievement(game, name, description);

        // dodajemy do całości; Game pilnuje braku współdzielenia
        game.addAchievement(achievement);

        return achievement;
    }

    Game getGame() {
        return game;
    }

    void clearGame() {
        this.game = null;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    @Override
    public String toString() {
        return "Achievement{" +
                "name='" + name + '\'' +
                ", gameTitle=" + (game != null ? game.getTitle() : "none") +
                ", createdAt=" + createdAt +
                '}';
    }


}
