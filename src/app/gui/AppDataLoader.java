package app.gui;

import achievements.PlatformAchievement;
import community.CommunityMember;
import core.Customer;
import core.Game;
import core.ObjectPlus;
import core.Purchase;
import core.TechSupport;
import core.User;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.List;
import promotion.Promotion;
import ticket.Ticket;

// Laduje ekstensje z pliku lub tworzy SampleData.
public final class AppDataLoader {
    private AppDataLoader() {}

    // Wejscie glowne: najpierw ekstensja, potem dane przykladowe.
    public static AppData load() throws Exception {
        try {
            ObjectPlus.loadExtent();
        } catch (NoSuchFileException ex) {
            // Dane demo tylko przy pierwszym uruchomieniu. Inne bledy odczytu
            // musza byc zgloszone, aby nie nadpisac istniejacego zapisu.
            AppData data = SampleData.create();
            ObjectPlus.saveExtent();
            return data;
        }
        User.rebuildUserIndex();
        Game.rebuildAchievementIndex();
        return fromExtent();
    }

    // Buduje AppData na podstawie ekstensji ObjectPlus.
    private static AppData fromExtent() {
        List<Game> games = new ArrayList<>(ObjectPlus.getExtentFromClass(Game.class));
        List<Customer> customers = new ArrayList<>(
                ObjectPlus.getExtentFromClassIncludingSubclasses(Customer.class));
        List<Purchase> purchases = new ArrayList<>(ObjectPlus.getExtentFromClass(Purchase.class));
        List<Promotion> promotions = new ArrayList<>(
                ObjectPlus.getExtentFromClassIncludingSubclasses(Promotion.class));
        List<PlatformAchievement> achievements = new ArrayList<>(
                ObjectPlus.getExtentFromClassIncludingSubclasses(PlatformAchievement.class));
        List<Ticket> tickets = new ArrayList<>(
                ObjectPlus.getExtentFromClassIncludingSubclasses(Ticket.class));
        List<CommunityMember> members = new ArrayList<>(
                ObjectPlus.getExtentFromClass(CommunityMember.class));
        List<TechSupport> supportUsers = new ArrayList<>(
                ObjectPlus.getExtentFromClass(TechSupport.class));
        return new AppData(
                games,
                customers,
                purchases,
                promotions,
                achievements,
                tickets,
                members,
                supportUsers
        );
    }
}
