package core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Studio – klasa bazowa (nadklasa) dla dziedziczenia rozłącznego (disjoint).
 *
 * W projekcie występuje jako:
 *  - strona "*" asocjacji binarnej 1-* ze stroną Game
 *  - nadklasa dla IndieStudio oraz CorporationStudio (dziedziczenie disjoint)
 *
 * Obiekt zawsze należy do dokładnie jednej z podklas:
 *  - IndieStudio
 *  - CorporationStudio
 */
public abstract class Studio extends ObjectPlus {

    private String name;
    // flaga wykorzystywana w podklasach:
    // IndieStudio -> true, CorporationStudio -> false
    private boolean isIndependent;
    private String country;

    // strona "*" asocjacji binarnej 1-* z Game
    private List<Game> games = new ArrayList<>();

    /**
     * Główny konstruktor używany przez podklasy.
     * Pozwala ustawić flagę isIndependent.
     */
    protected Studio(String name, String country, boolean isIndependent) {
        try {
            setName(name);
            setCountry(country);
            this.isIndependent = isIndependent;
        } catch (Exception e) {
            removeFromExtent();
            throw new RuntimeException(e);
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if(name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Studio name cannot be null or empty.");
        }
        this.name = name;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        if(country == null || country.trim().isEmpty()) {
            throw new IllegalArgumentException("Country cannot be null or empty.");
        }
        this.country = country;
    }

    /**
     * Informacja wspólna dla całej hierarchii dziedziczenia –
     * wykorzystywana przy rozróżnianiu studiów niezależnych / korporacyjnych.
     */
    public boolean isIndependent() {
        return isIndependent;
    }

    // --- METODY ASOCJACJI BINARNEJ 1-* ---

    public void addGame(Game game) {
        if (game == null) {
            throw new IllegalArgumentException("Game cannot be null.");
        }
        if (games.contains(game)) {
            return; // już powiązana
        }
        games.add(game);

        // połączenie zwrotne – jeśli gra nie wskazuje na to studio
        if (game.getStudio() != this) {
            game.setStudio(this);
        }
    }

    public void removeGame(Game game) {
        if (game == null) {
            return;
        }
        if (!games.contains(game)) {
            return;
        }
        games.remove(game);
        // dalsze zarządzanie przełączaniem studia należy do Game.setStudio(...)
    }

    public List<Game> getGames() {
        return Collections.unmodifiableList(games);
    }

    @Override
    public String toString() {
        return "Studio{" +
                "name='" + name + '\'' +
                ", country='" + country + '\'' +
                ", independent=" + isIndependent +
                '}';
    }
}
