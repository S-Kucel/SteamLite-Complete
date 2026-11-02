package core;

import java.util.List;

/**
 * IGamePublisher – interfejs używany do pokazania wielodziedziczenia.
 *
 * Klasa implementująca IGamePublisher potrafi:
 *  - opublikować grę na platformie,
 *  - zarejestrować sprzedaż gry,
 *  - zwrócić listę opublikowanych gier,
 *  - zwrócić łączny przychód z tych gier.
 *
 * W projekcie:
 *  - DeveloperAccount implementuje IGamePublisher (czysty „twórca”),
 *  - CreatorCustomer dziedziczy z Customer oraz implementuje IGamePublisher
 *    (wielodziedziczenie: klient + twórca).
 */
public interface IGamePublisher {

    /**
     * Publikuje grę na platformie.
     */
    void publishGame(Game game);

    /**
     * Rejestruje sprzedaż gry dla tego wydawcy.
     *
     *  game - gra, która została sprzedana
     *  salePrice cena sprzedaży (przychód wydawcy z tej transakcji)
     */
    void registerSale(Game game, double salePrice);

    /**
     * Zwraca niezmienialną listę wszystkich gier opublikowanych
     * przez tego wydawcę.
     */
    List<Game> getPublishedGames();

    /**
     * Zwraca łączny przychód (sumę salePrice) z zarejestrowanych sprzedaży.
     */
    double getTotalRevenue();
}
