// Klasa zbyteczna, używana jedynie do pokazania zastsowowań konstrukcji dla MiniProjektów. 

package app;

import achievements.AchievementTier;
import achievements.CommunityEngagementAchievement;
import achievements.OwnershipAchievement;
import achievements.PlatformAchievement;
import achievements.PlaytimeAchievement;
import community.CommunityMember;
import core.Achievement;
import core.CorporationStudio;
import core.CreatorCustomer;
import core.Customer;
import core.DeveloperAccount;
import core.Game;
import core.IGamePublisher;
import core.IndieStudio;
import core.ObjectPlus;
import core.Platform;
import core.Purchase;
import core.Studio;
import core.User;
import promotion.FixedAmountPromotion;
import promotion.PercentagePromotion;
import promotion.Promotion;
import ticket.ClosedTicket;
import ticket.InProgressTicket;
import ticket.OpenTicket;
import ticket.Ticket;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

public class main {
    public static void main(String[] args) {
        try {
            //  EKSTENSJA (przed tworzeniem obiektów)
            System.out.println("Gier w ekstensji (start): "
                    + ObjectPlus.getExtentFromClass(Game.class).size());

            // DZIEDZICZENIE ROZŁĄCZNE (disjoint) – tworzymy konkretne podklasy Studio
            Studio cdpr = new CorporationStudio(
                    "CD Projekt RED",
                    "Poland",
                    "CD PROJEKT S.A."
            );
            Studio larian = new IndieStudio(
                    "Larian Studios",
                    "Belgium",
                    400   // przykładowy rozmiar zespołu
            );

            System.out.println("\nPrzykład dziedziczenia rozłącznego (disjoint) - Studio:");
            System.out.println("  IndieStudio: " + larian);
            System.out.println("  CorporationStudio: " + cdpr);

            // ATR. ZŁOŻONY + ATR. POWTARZALNY (w Game: Studio i Platformy)
            Game w3 = new Game(
                    "The Witcher 3", 2015, 129.99, cdpr,
                    List.of(Platform.PC, Platform.PLAYSTATION, Platform.XBOX)
            );

            Game bg3 = new Game(
                    "Baldur's Gate 3", 2023, 249.99, larian,
                    List.of(Platform.PC, Platform.PLAYSTATION)
            );

            // PRZECIĄŻENIE + ATR. OPCJONALNY (User/Customer z/bez nicku)
            Customer c1 = new Customer("Skai", "skaiUser", "StrongPass1", 21);
            Customer c2 = new Customer("onlyUser", "SecretPass1", 19);
            Customer c3 = new Customer("ReviewerPro", "reviewUser", "SecretPass2", 23);

            // ATR. KLASOWY (User.minimalAge)
            try {
                new Customer("tooYoung", "pass12345", 15); // powinno rzucić wyjątek
            } catch (IllegalArgumentException ex) {
                System.out.println("Ograniczenie wieku działa (atrybut klasowy minimalAge): "
                        + ex.getMessage());
            }

            // PRZESŁONIĘCIE (toString)
            System.out.println("\nGame.toString() -> " + w3);

            // ATR. POCHODNY + METODA KLASOWA
            System.out.println("Wiek gry (atrybut pochodny): " + w3.getAge() + " lat");
            Game.adjustPrice(-20); // [METODA KLASOWA]
            System.out.println("Nowa cena W3 po adjustPrice(-20): " + formatPrice(w3.getPrice()));

            // ATR. POWTARZALNY u klienta (lista zakupionych gier)
            c1.addFunds(500);
            c1.purchaseGame(w3);
            c1.purchaseGame(bg3);

            c2.addFunds(200);
            c2.purchaseGame(w3);

            c3.addFunds(200);
            c3.purchaseGame(w3);

            System.out.println("\nZakupione gry c1: "
                    + c1.getPurchasedGames().stream().map(Game::getTitle).toList());

            // --- ASOCJACJA BINARNA 1-*: Studio – Game ---
            System.out.println("\nGry studia CDPR (asocjacja binarna 1-* Studio-Game):");
            for (Game g : cdpr.getGames()) {
                System.out.println("  - " + g.getTitle());
            }

            System.out.println("Gry studia Larian:");
            for (Game g : larian.getGames()) {
                System.out.println("  - " + g.getTitle());
            }

            // --- ASOCJACJA Z ATRUBYTEM: Customer – Game przez Purchase ---
            System.out.println("\nZakupy c1 (asocjacja z atrybutem - klasa Purchase):");
            for (Purchase p : c1.getPurchases()) {
                System.out.println("  " + p);
            }

            System.out.println("\nZakupy gry W3 (z perspektywy Game):");
            for (Purchase p : w3.getPurchases()) {
                System.out.println("  " + p);
            }

            // --- KOMPOZYCJA: Game – Achievement ---
            Achievement a1 = Achievement.createForGame(
                    w3,
                    "Butcher of Blaviken",
                    "Defeat 100 enemies in a single playthrough"
            );
            Achievement a2 = Achievement.createForGame(
                    w3,
                    "Master of Gwent",
                    "Win 50 Gwent matches"
            );

            System.out.println("\nOsiągnięcia gry W3 (kompozycja Game-Achievement):");
            for (Achievement a : w3.getAchievements()) {
                System.out.println("  - " + a);
            }

            // --- ASOCJACJA KWALIFIKOWANA: biblioteka gier klienta ---
            System.out.println("\nBiblioteka gier c1 (asocjacja kwalifikowana po tytule):");
            c1.getLibraryGames().forEach(g ->
                    System.out.println("  - " + g.getTitle()));

            System.out.println("Biblioteka gier c2:");
            c2.getLibraryGames().forEach(g ->
                    System.out.println("  - " + g.getTitle()));

            System.out.println("\nWłaściciele gry \"The Witcher 3\" (z perspektywy Game):");
            for (Customer owner : w3.getLibraryOwners()) {
                System.out.println("  - " + owner.getUsername());
            }

            System.out.println("\nWyszukiwanie w bibliotece c1 po tytule \"The Witcher 3\":");
            Game gameFromLibrary = c1.findGameInLibrary("The Witcher 3");
            System.out.println("  znaleziono: " +
                    (gameFromLibrary != null ? gameFromLibrary.getTitle() : "brak"));

            //  EKSTENSJA (po utworzeniu obiektów)
            System.out.println("\nGier w ekstensji (po utworzeniu): "
                    + ObjectPlus.getExtentFromClass(Game.class).size());
            System.out.println("Studiów w ekstensji: "
                    + ObjectPlus.getExtentFromClassIncludingSubclasses(Studio.class).size());
            System.out.println("Użytkowników w ekstensji: "
                    + ObjectPlus.getExtentFromClassIncludingSubclasses(User.class).size());
            System.out.println("Zakupów (Purchase) w ekstensji: "
                    + ObjectPlus.getExtentFromClass(Purchase.class).size());
            System.out.println("Osiągnięć (Achievement) w ekstensji: "
                    + ObjectPlus.getExtentFromClass(Achievement.class).size());

            // EKSTENSJA – TRWAŁOŚĆ (zapis i odczyt)
            ObjectPlus.saveExtent();
            ObjectPlus.loadExtent();
            System.out.println("\nPo loadExtent() liczba gier: "
                    + ObjectPlus.getExtentFromClass(Game.class).size());

            //   KLASA ABSTRAKCYJNA (Promotion) + METODA ABSTRAKCYJNA

            System.out.println("\n--- Klasa abstrakcyjna: Promotion ---");

            double basePrice = 100.0;  // przykładowa cena bazowa produktu

            Promotion weekendPromo = new PercentagePromotion(
                    "Weekend -30%",
                    LocalDate.of(2025, 11, 28),
                    LocalDate.of(2025, 11, 30),
                    30.0
            );

            Promotion couponPromo = new FixedAmountPromotion(
                    "Kupon -20 PLN",
                    LocalDate.of(2025, 12, 1),
                    LocalDate.of(2025, 12, 31),
                    20.0
            );

            printPromotionResult(basePrice, weekendPromo);
            printPromotionResult(basePrice, couponPromo);

            // DZIEDZICZENIE ŁĄCZNE (OVERLAPPING) – CommunityMember

            System.out.println("\n=== Dziedziczenie łączne (overlapping) - CommunityMember ===");

            c1.setPlaytimeHours(w3, 120);
            c1.writeReview(w3, 9, "Great story and atmosphere.");
            c1.createMod(w3, "Balance tweaks and quality of life.");

            c3.setPlaytimeHours(w3, 40);
            c3.writeReview(w3, 8, "Solid RPG with a rich world.");

            CommunityMember m1 = c1.createCommunityProfile("c1@example.com");
            CommunityMember m2 = c2.createCommunityProfile("c2@example.com");
            CommunityMember m3 = c3.createCommunityProfile("rev@example.com");

            System.out.println(m1);
            System.out.println(m2);
            System.out.println(m3);

            System.out.println(
                    "\nLiczba członków społeczności w ekstensji CommunityMember: "
                            + ObjectPlus.getExtentFromClass(CommunityMember.class).size()
            );

            // WIELODZIEDZICZENIE (multi-inheritance) – IGamePublisher

            System.out.println("\n=== Wielodziedziczenie - interfejs IGamePublisher ===");

            IGamePublisher devAccount = new DeveloperAccount("Lone Dev Studio");

            CreatorCustomer creatorDev = new CreatorCustomer(
                    "DevGamer",
                    "devgamerUser",
                    "VeryStrongPass1",
                    26,
                    "DevGamer Studio"
            );

            devAccount.publishGame(w3);
            creatorDev.publishGame(bg3);

            devAccount.registerSale(w3, 100.0);
            devAccount.registerSale(w3, 120.0);

            creatorDev.registerSale(bg3, 200.0);
            creatorDev.registerSale(bg3, 180.0);

            System.out.println("\nWIELODZIEDZICZENIE: Customer + IGamePublisher (CreatorCustomer)");
            System.out.println("DeveloperAccount (czysty wydawca): " + devAccount);
            System.out.println("CreatorCustomer (klient + wydawca): " + creatorDev);

            System.out.println("\nPolimorficzna lista wydawców (typ IGamePublisher):");
            java.util.List<IGamePublisher> publishers = java.util.List.of(devAccount, creatorDev);

            for (IGamePublisher pub : publishers) {
                System.out.println(
                        "  -> opublikowane gry: "
                                + pub.getPublishedGames().stream().map(Game::getTitle).toList()
                                + ", łączny przychód: " + pub.getTotalRevenue()
                );
            }

            // --- DZIEDZICZENIE WIELOASPEKTOWE (multi-aspect) – PlatformAchievement ---

            System.out.println("\n=== Dziedziczenie wieloaspektowe (multi-aspect) - PlatformAchievement ===");

            PlatformAchievement pa1 = new OwnershipAchievement(
                    "OWN_10_GAMES",
                    "Mała biblioteka",
                    "Posiadasz pierwszą większą kolekcję gier.",
                    AchievementTier.BRONZE,
                    100,
                    10
            );

            PlatformAchievement pa2 = new PlaytimeAchievement(
                    "PLAY_500_HOURS",
                    "Połknął bakcyla",
                    "Spędziłeś setki godzin w grach.",
                    AchievementTier.SILVER,
                    200,
                    500
            );

            PlatformAchievement pa3 = new CommunityEngagementAchievement(
                    "COMMUNITY_HELPER",
                    "Pomocny recenzent",
                    "Aktywnie pomagasz innym graczom swoimi recenzjami.",
                    AchievementTier.GOLD,
                    300,
                    20,
                    50
            );

            java.util.List<PlatformAchievement> platformAchievements =
                    java.util.List.of(pa1, pa2, pa3);

            for (PlatformAchievement pa : platformAchievements) {
                System.out.println("  - " + pa);
            }

            System.out.println(
                    "\nLiczba platformowych osiągnięć w ekstensji PlatformAchievement: "
                            + ObjectPlus.getExtentFromClassIncludingSubclasses(PlatformAchievement.class).size()
            );

        } catch (Exception e) {
            e.printStackTrace();
        }

        // --- DZIEDZICZENIE DYNAMICZNE (dynamic) – Ticket ---
        System.out.println("\n=== Dziedziczenie dynamiczne (dynamic) - Ticket ===");

        // 1. Tworzymy zgłoszenie jako otwarte
        Ticket ticket = new OpenTicket(
                "T-1001",
                "Problem z uruchomieniem gry",
                "Gra crashuje zaraz po starcie.",
                "player123"
        );
        System.out.println("Aktualny stan: " + ticket);

        // 2. Zmieniamy klasę na InProgressTicket
        ticket = new InProgressTicket(
                ticket,              // poprzedni obiekt (OpenTicket)
                "SupportAgent01"     // agent obsługujący zgłoszenie
        );
        System.out.println("Aktualny stan: " + ticket);

        // 3. Zmieniamy klasę na ClosedTicket
        ticket = new ClosedTicket(
                ticket,   // poprzedni obiekt (InProgressTicket)
                "Zaktualizowano sterowniki GPU - problem ustąpił.",
                true
        );
        System.out.println("Aktualny stan: " + ticket);

        // --- NEW CONSTRAINTS DEMO ---
        System.out.println("\n=== New constraints demo ===");

        // UNIQUE
        System.out.println("\n--- UNIQUE ---");
        new Customer("uniqueDemo", "pass12345", 20);
        try {
            new Customer("uniqueDemo", "pass12345", 21);
            System.out.println("UNIQUE: no exception (unexpected)");
        } catch (RuntimeException ex) {
            System.out.println("UNIQUE: ok -> " + ex.getCause().getMessage());
        }

        // SUBSET + BAG
        System.out.println("\n--- SUBSET + BAG ---");
        try {
            Studio subsetStudio = new IndieStudio("Subset Studio", "PL", 3);
            Game subsetGame = new Game("Subset Game", 2022, 30.0, subsetStudio, List.of(Platform.PC));
            Customer subsetCustomer = new Customer("subsetUser", "pass12345", 25);

            try {
                subsetCustomer.addGameToLibrary(subsetGame);
                System.out.println("SUBSET: no exception (unexpected)");
            } catch (Exception ex) {
                System.out.println("SUBSET: ok -> " + ex.getMessage());
            }

            subsetCustomer.addFunds(100.0);
            subsetCustomer.purchaseGame(subsetGame);
            subsetCustomer.purchaseGame(subsetGame);

            System.out.println("Purchase history size (bag): " + subsetCustomer.getPurchaseHistory().size());
            System.out.println("Library size: " + subsetCustomer.getLibraryGames().size());
        } catch (Exception ex) {
            System.out.println("SUBSET/BAG demo error: " + ex.getMessage());
        }

        // ORDERED
        System.out.println("\n--- ORDERED ---");
        Studio orderedStudio = new IndieStudio("Ordered Studio", "PL", 4);
        Game orderedGame = new Game("Ordered Game", 2023, 40.0, orderedStudio, List.of(Platform.PC));
        Achievement.createForGame(orderedGame, "Zeta", "z");
        Achievement.createForGame(orderedGame, "Alpha", "a");
        Achievement.createForGame(orderedGame, "Beta", "b");
        System.out.println("Achievements order: " +
                orderedGame.getAchievements().stream().map(Achievement::getName).toList());

        // XOR
        System.out.println("\n--- XOR ---");
        try {
            Studio promoStudio = new IndieStudio("Promo Studio", "PL", 2);
            Game promoGame = new Game("Promo Game", 2021, 50.0, promoStudio, List.of(Platform.PC));
            Customer promoCustomer = new Customer("promoUser", "pass12345", 28);
            Purchase promoPurchase = new Purchase(promoCustomer, promoGame, promoGame.getPrice());

            PercentagePromotion percentPromo = new PercentagePromotion(
                    "Percent10",
                    LocalDate.of(2025, 1, 1),
                    LocalDate.of(2025, 12, 31),
                    10.0
            );
            FixedAmountPromotion fixedPromo = new FixedAmountPromotion(
                    "Fixed5",
                    LocalDate.of(2025, 1, 1),
                    LocalDate.of(2025, 12, 31),
                    5.0
            );

            promoPurchase.applyPercentagePromotion(percentPromo);
            try {
                promoPurchase.applyFixedAmountPromotion(fixedPromo);
                System.out.println("XOR: no exception (unexpected)");
            } catch (Exception ex) {
                System.out.println("XOR: ok -> " + ex.getMessage());
            }
        } catch (Exception ex) {
            System.out.println("XOR demo error: " + ex.getMessage());
        }
    }

    private static String formatPrice(double value) {
        return String.format(Locale.US, "%.2f", value);
    }

    /**
     * Pomocnicza metoda tylko na potrzeby przykładu z klasą abstrakcyjną.
     */
    private static void printPromotionResult(double basePrice, Promotion promotion) {
        double discount = promotion.getDiscountAmount(basePrice);
        double finalPrice = promotion.apply(basePrice);

        System.out.println("\nPromocja: " + promotion);
        System.out.println("  Cena bazowa: " + basePrice);
        System.out.println("  Kwota zniżki (getDiscountAmount): " + discount);
        System.out.println("  Cena po zniżce (apply): " + finalPrice);
    }
}
