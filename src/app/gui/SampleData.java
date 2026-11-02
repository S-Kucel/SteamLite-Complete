package app.gui;

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
import core.Platform;
import core.Purchase;
import core.Studio;
import core.TechSupport;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import promotion.FixedAmountPromotion;
import promotion.PercentagePromotion;
import promotion.Promotion;
import ticket.ClosedTicket;
import ticket.InProgressTicket;
import ticket.OpenTicket;
import ticket.Ticket;

// Buduje dane startowe do pierwszego uruchomienia.
public final class SampleData {
    private SampleData() {}

    // Tworzy obiekty demo i laczy relacje.
    public static AppData create() throws Exception {
        Studio cdpr = new CorporationStudio("CD Projekt RED", "Poland", "CD PROJEKT S.A.");
        Studio larian = new IndieStudio("Larian Studios", "Belgium", 400);
        Studio supergiant = new IndieStudio("Supergiant Games", "USA", 25);

        // Gry + platformy.
        Game w3 = new Game("The Witcher 3", 2015, 129.99, cdpr,
                List.of(Platform.PC, Platform.PLAYSTATION, Platform.XBOX));
        Game bg3 = new Game("Baldur's Gate 3", 2023, 249.99, larian,
                List.of(Platform.PC, Platform.PLAYSTATION));
        Game hades = new Game("Hades", 2020, 89.99, supergiant,
                List.of(Platform.PC, Platform.NINTENDO_SWITCH));

        // Osiagniecia gry (kompozycja).
        Achievement.createForGame(w3, "Master of Gwent", "Win 50 Gwent matches");
        Achievement.createForGame(w3, "Butcher of Blaviken", "Defeat 100 enemies");
        Achievement.createForGame(bg3, "Hero of Faerun", "Finish the main story");

        // Klienci i tworca.
        Customer c1 = new Customer("guiUser1", "pass12345", 24);
        Customer c2 = new Customer("guiUser2", "pass12345", 20);
        Customer c3 = new CreatorCustomer("guiUser3", "pass12345", 26, "Indie Dev");

        c1.addFunds(700);
        c1.purchaseGame(w3);
        c1.purchaseGame(bg3);
        c1.purchaseGame(w3); // bag: duplicate purchase

        c2.addFunds(300);
        c2.purchaseGame(w3);
        c2.purchaseGame(hades);

        c1.setPlaytimeHours(w3, 120);
        c1.setPlaytimeHours(bg3, 64);
        c2.setPlaytimeHours(w3, 18);
        c2.setPlaytimeHours(hades, 9);

        c1.writeReview(w3, 9, "Great story and atmosphere.");
        c2.writeReview(w3, 8, "Solid RPG with rich world.");
        c1.createMod(w3, "Improved textures pack.");
        c2.createMod(hades, "High contrast UI for better readability.");

        // Zakupy do dalszych operacji.
        List<Purchase> purchases = new ArrayList<>();
        purchases.addAll(c1.getPurchases());
        purchases.addAll(c2.getPurchases());

        // Promocje czasowe.
        LocalDate now = LocalDate.now();
        LocalDate twoMonthsFromNow = now.plusMonths(2);
        PercentagePromotion weekendPromo = new PercentagePromotion(
                "Weekend -10%",
                now.minusDays(2),
                now.plusDays(7),
                10.0
        );
        FixedAmountPromotion couponPromo = new FixedAmountPromotion(
                "Coupon -5",
                now.minusDays(1),
                now.plusDays(3),
                5.0
        );
        PercentagePromotion seasonPromo = new PercentagePromotion(
                "Season -15%",
                now,
                twoMonthsFromNow,
                15.0
        );
        FixedAmountPromotion loyaltyPromo = new FixedAmountPromotion(
                "Loyalty -20",
                now,
                twoMonthsFromNow,
                20.0
        );
        FixedAmountPromotion springPromo = new FixedAmountPromotion(
                "Spring -12",
                now,
                twoMonthsFromNow,
                12.0
        );
        FixedAmountPromotion memberPromo = new FixedAmountPromotion(
                "Member -30",
                now,
                twoMonthsFromNow,
                30.0
        );
        List<Promotion> promotions = new ArrayList<>();
        promotions.add(weekendPromo);
        promotions.add(couponPromo);
        promotions.add(seasonPromo);
        promotions.add(loyaltyPromo);
        promotions.add(springPromo);
        promotions.add(memberPromo);

        if (!purchases.isEmpty()) {
            purchases.get(purchases.size() - 1).applyPercentagePromotion(weekendPromo);
        }

        PlatformAchievement pa1 = new OwnershipAchievement(
                "OWN_10_GAMES",
                "Small library",
                "Own at least 10 games.",
                AchievementTier.BRONZE,
                100,
                10
        );
        PlatformAchievement pa2 = new PlaytimeAchievement(
                "PLAY_500_HOURS",
                "Committed player",
                "Play at least 500 hours.",
                AchievementTier.SILVER,
                200,
                500
        );
        PlatformAchievement pa3 = new CommunityEngagementAchievement(
                "COMMUNITY_HELPER",
                "Helpful reviewer",
                "Write reviews and help others.",
                AchievementTier.GOLD,
                300,
                20,
                50
        );
        List<PlatformAchievement> platformAchievements = new ArrayList<>();
        platformAchievements.add(pa1);
        platformAchievements.add(pa2);
        platformAchievements.add(pa3);

        Ticket t1 = new OpenTicket("T-1001", "Game crash on start", "Crash after launch", "player123");
        Ticket t2 = new InProgressTicket(
                new OpenTicket("T-1002", "Installation stuck", "Installer freezes at 80%.", "player789"),
                "SupportAgent01"
        );
        Ticket t3 = new ClosedTicket(
                new InProgressTicket(
                        new OpenTicket("T-1003", "No sound", "Audio is missing in-game.", "player555"),
                        "SupportAgent02"
                ),
                "Updated audio drivers",
                true
        );
        List<Ticket> tickets = new ArrayList<>();
        tickets.add(t1);
        tickets.add(t2);
        tickets.add(t3);

        CommunityMember m1 = c1.createCommunityProfile("c1@example.com");
        CommunityMember m2 = c2.createCommunityProfile("mod@example.com");
        CommunityMember m3 = c3.createCommunityProfile("creator@example.com");

        List<CommunityMember> communityMembers = new ArrayList<>();
        communityMembers.add(m1);
        communityMembers.add(m2);
        communityMembers.add(m3);

        TechSupport support = new TechSupport("SupportAgent", "support1", "pass12345", 30);
        List<TechSupport> supportUsers = new ArrayList<>();
        supportUsers.add(support);

        IGamePublisher devAccount = new DeveloperAccount("Lone Dev Studio");
        devAccount.publishGame(w3);
        devAccount.registerSale(w3, 100.0);

        List<Game> games = new ArrayList<>();
        games.add(w3);
        games.add(bg3);
        games.add(hades);

        List<Customer> customers = new ArrayList<>();
        customers.add(c1);
        customers.add(c2);
        customers.add(c3);

        return new AppData(
                games,
                customers,
                purchases,
                promotions,
                platformAchievements,
                tickets,
                communityMembers,
                supportUsers
        );
    }
}
