package app.gui;

import achievements.PlatformAchievement;
import community.CommunityMember;
import core.Customer;
import core.Game;
import core.Purchase;
import core.TechSupport;
import java.util.List;
import promotion.Promotion;
import ticket.Ticket;

// Prosty kontener danych do inicjalizacji AppState.
public final class AppData {
    // Listy obiektow domenowych zaladowanych przy starcie.
    private final List<Game> games;
    private final List<Customer> customers;
    private final List<Purchase> purchases;
    private final List<Promotion> promotions;
    private final List<PlatformAchievement> platformAchievements;
    private final List<Ticket> tickets;
    private final List<CommunityMember> communityMembers;
    private final List<TechSupport> supportUsers;

    public AppData(List<Game> games,
                   List<Customer> customers,
                   List<Purchase> purchases,
                   List<Promotion> promotions,
                   List<PlatformAchievement> platformAchievements,
                   List<Ticket> tickets,
                   List<CommunityMember> communityMembers,
                   List<TechSupport> supportUsers) {
        this.games = games;
        this.customers = customers;
        this.purchases = purchases;
        this.promotions = promotions;
        this.platformAchievements = platformAchievements;
        this.tickets = tickets;
        this.communityMembers = communityMembers;
        this.supportUsers = supportUsers;
    }

    public List<Game> getGames() {
        return games;
    }

    public List<Customer> getCustomers() {
        return customers;
    }

    public List<Purchase> getPurchases() {
        return purchases;
    }

    public List<Promotion> getPromotions() {
        return promotions;
    }

    public List<PlatformAchievement> getPlatformAchievements() {
        return platformAchievements;
    }

    public List<Ticket> getTickets() {
        return tickets;
    }

    public List<CommunityMember> getCommunityMembers() {
        return communityMembers;
    }

    public List<TechSupport> getSupportUsers() {
        return supportUsers;
    }
}
