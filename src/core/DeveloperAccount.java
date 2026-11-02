package core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * DeveloperAccount – konto dewelopera / wydawcy gier.
 *
 * Pokazuje „czystą” implementację interfejsu IGamePublisher,
 * niezależną od klasy Customer.
 *
 * Biznesowo: ma swoją nazwę, listę opublikowanych gier oraz
 * zlicza przychód z ich sprzedaży.
 */
public class DeveloperAccount extends ObjectPlus implements IGamePublisher {

    private String developerName;
    private List<Game> publishedGames = new ArrayList<>();
    private double totalRevenue;

    public DeveloperAccount(String developerName) {
        if (developerName == null || developerName.trim().isEmpty()) {
            throw new IllegalArgumentException("Developer name cannot be null or empty.");
        }
        this.developerName = developerName;
    }

    public String getDeveloperName() {
        return developerName;
    }

    @Override
    public void publishGame(Game game) {
        if (game == null) {
            throw new IllegalArgumentException("Game cannot be null.");
        }
        if (!publishedGames.contains(game)) {
            publishedGames.add(game);
        }
    }

    @Override
    public void registerSale(Game game, double salePrice) {
        if (game == null) {
            throw new IllegalArgumentException("Game cannot be null.");
        }
        if (salePrice < 0) {
            throw new IllegalArgumentException("Sale price cannot be negative.");
        }
        if (!publishedGames.contains(game)) {
            throw new IllegalArgumentException("Game " + game.getTitle() +
                    " is not published by this developer.");
        }
        totalRevenue += salePrice;
    }

    @Override
    public List<Game> getPublishedGames() {
        return Collections.unmodifiableList(publishedGames);
    }

    @Override
    public double getTotalRevenue() {
        return totalRevenue;
    }

    @Override
    public String toString() {
        return "DeveloperAccount{" +
                "developerName='" + developerName + '\'' +
                ", publishedGames=" + publishedGames.stream().map(Game::getTitle).toList() +
                ", totalRevenue=" + totalRevenue +
                '}';
    }
}
