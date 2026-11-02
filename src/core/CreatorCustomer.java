package core;

import java.util.List;

/**
 * CreatorCustomer – przykład wielodziedziczenia:
 *
 *  - dziedziczy Z KLASY Customer (klient platformy: może kupować gry,
 *    ma saldo, bibliotekę itp.),
 *  - JEDNOCZEŚNIE implementuje interfejs IGamePublisher (jest twórcą,
 *    może publikować swoje gry i mieć z nich przychód).
 *
 * Realna sytuacja biznesowa: niezależny twórca, który na tej samej
 * platformie kupuje gry innych oraz sprzedaje swoje.
 *
 * Implementacja funkcji wydawcy jest delegowana do wewnętrznego
 * obiektu DeveloperAccount – dokładnie jak na wykładzie z WorkingStudent
 * delegującym do Student.
 */
public class CreatorCustomer extends Customer implements IGamePublisher {

    // Część „wydawcy” – delegujemy do niej metody interfejsu
    private DeveloperAccount developerAccount;

    /**
     * Konstruktor z nickiem (dziedziczenie po Customer) + nazwa dewelopera.
     */
    public CreatorCustomer(String nick,
                           String username,
                           String password,
                           int age,
                           String developerName) {
        super(nick, username, password, age);
        this.developerAccount = new DeveloperAccount(developerName);
    }

    /**
     * Konstruktor bez nicka (przeciążony), również tworzy część deweloperską.
     */
    public CreatorCustomer(String username,
                           String password,
                           int age,
                           String developerName) {
        super(username, password, age);
        this.developerAccount = new DeveloperAccount(developerName);
    }

    /**
     * Dostęp pomocniczy – gdybyśmy chcieli np. wyświetlić nazwę dewelopera.
     */
    public DeveloperAccount getDeveloperAccount() {
        return developerAccount;
    }

    // --- Implementacja IGamePublisher przez delegację ---

    @Override
    public void publishGame(Game game) {
        developerAccount.publishGame(game);
    }

    @Override
    public void registerSale(Game game, double salePrice) {
        developerAccount.registerSale(game, salePrice);
    }

    @Override
    public List<Game> getPublishedGames() {
        return developerAccount.getPublishedGames();
    }

    @Override
    public double getTotalRevenue() {
        return developerAccount.getTotalRevenue();
    }

    @Override
    public String toString() {
        return "CreatorCustomer{" +
                "nick='" + getNick() + '\'' +
                ", username='" + getUsername() + '\'' +
                ", age=" + getAge() +
                ", balance=" + getBalance() +
                ", purchasedGames=" + getPurchasedGames().stream().map(Game::getTitle).toList() +
                ", developerName='" + developerAccount.getDeveloperName() + '\'' +
                ", publishedGames=" + getPublishedGames().stream().map(Game::getTitle).toList() +
                ", totalRevenue=" + getTotalRevenue() +
                '}';
    }
}
