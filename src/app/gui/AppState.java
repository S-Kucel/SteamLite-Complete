package app.gui;

import achievements.CommunityEngagementAchievement;
import achievements.OwnershipAchievement;
import achievements.PlatformAchievement;
import achievements.PlaytimeAchievement;
import community.CommunityMember;
import core.CreatorCustomer;
import core.Customer;
import core.Game;
import core.IGamePublisher;
import core.Mod;
import core.ObjectPlus;
import core.Purchase;
import core.Review;
import core.Studio;
import core.TechSupport;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import promotion.Promotion;
import ticket.ClosedTicket;
import ticket.InProgressTicket;
import ticket.OpenTicket;
import ticket.Ticket;

// Logika aplikacji widoczna dla GUI.
public final class AppState {
    private final List<Game> games;
    private final List<Customer> customers;
    private final List<Purchase> purchases;
    private final List<Promotion> promotions;
    private final List<PlatformAchievement> platformAchievements;
    private final List<Ticket> tickets;
    private final List<CommunityMember> communityMembers;
    private final List<TechSupport> supportUsers;
    // Szybkie wyszukiwanie promocji po kodzie.
    private final Map<String, Promotion> promotionsByCode = new HashMap<>();
    private int ticketCounter;

    public AppState(AppData data) {
        this.games = data.getGames();
        this.customers = data.getCustomers();
        this.purchases = data.getPurchases();
        this.promotions = data.getPromotions();
        this.platformAchievements = data.getPlatformAchievements();
        this.tickets = data.getTickets();
        this.communityMembers = data.getCommunityMembers();
        this.supportUsers = data.getSupportUsers();
        syncCommunityProfiles();
        initPromotionCodes();
        this.ticketCounter = findMaxTicketId(tickets);
    }

    // Zapis ekstensji ObjectPlus.
    public void save() throws Exception {
        ObjectPlus.saveExtent();
    }

    // Logowanie klienta.
    public UserSession login(String username, String password) throws Exception {
        Customer customer = findCustomer(username);
        if (customer == null) {
            throw new Exception("User not found.");
        }
        if (!customer.getPassword().equals(password)) {
            throw new Exception("Invalid password.");
        }
        return new UserSession(customer);
    }

    // Logowanie supportu.
    public UserSession loginSupport(String username, String password) throws Exception {
        TechSupport support = findSupport(username);
        if (support == null) {
            throw new Exception("Support user not found.");
        }
        if (!support.getPassword().equals(password)) {
            throw new Exception("Invalid password.");
        }
        return new UserSession(support);
    }

    // Rejestracja klienta lub tworcy.
    public Customer registerCustomer(String username,
                                     String password,
                                     int age,
                                     boolean isCreator,
                                     String developerName) throws Exception {
        if (isCreator) {
            if (developerName == null || developerName.isBlank()) {
                throw new Exception("Developer name is required.");
            }
            Customer creator = new CreatorCustomer(username, password, age, developerName);
            customers.add(creator);
            ensureCommunityProfile(creator, null);
            return creator;
        }

        Customer customer = new Customer(username, password, age);
        customers.add(customer);
        ensureCommunityProfile(customer, null);
        return customer;
    }

    // Rejestracja uzytkownika supportu.
    public TechSupport registerSupport(String username,
                                       String password,
                                       int age) throws Exception {
        TechSupport support = new TechSupport(username, password, age);
        supportUsers.add(support);
        return support;
    }

    public Purchase purchaseGame(Customer customer, Game game) throws Exception {
        return purchaseGame(customer, game, null);
    }

    // Zakup z opcjonalnym kodem promo.
    public Purchase purchaseGame(Customer customer, Game game, String promoCode) throws Exception {
        Promotion promotion = null;
        String trimmedCode = promoCode == null ? "" : promoCode.trim();
        if (!trimmedCode.isEmpty()) {
            // Walidacja kodu promo i jego stanu.
            promotion = promotionsByCode.get(trimmedCode);
            if (promotion == null) {
                throw new Exception("Invalid promo code.");
            }
            if (!customer.hasClaimedPromotionCode(trimmedCode)) {
                throw new Exception("Promo code was not claimed.");
            }
            if (customer.isPromotionCodeUsed(trimmedCode)) {
                throw new Exception("Promo code was already used.");
            }
            if (!promotion.isActiveNow()) {
                throw new Exception("Promotion is not active.");
            }
        }

        int before = customer.getPurchases().size();
        if (promotion != null) {
            customer.purchaseGame(game, promotion);
        } else {
            customer.purchaseGame(game);
        }
        Purchase purchase = customer.getPurchases().get(before);
        purchases.add(purchase);

        if (promotion != null) {
            customer.markPromotionCodeUsed(trimmedCode);
        }

        // Przekazanie przychodu do wydawcy, jesli istnieje.
        IGamePublisher publisher = game.getPublisher();
        if (publisher != null) {
            publisher.registerSale(game, purchase.getPricePaid());
        }
        return purchase;
    }

    public Review addReview(Customer customer, Game game, int rating, String description) throws Exception {
        if (customer == null || game == null) {
            throw new Exception("Customer and game are required.");
        }
        Review review = customer.writeReview(game, rating, description);
        ensureCommunityProfile(customer, null);
        return review;
    }

    public Mod addMod(Customer customer, Game game, String description) throws Exception {
        if (customer == null || game == null) {
            throw new Exception("Customer and game are required.");
        }
        Mod mod = customer.createMod(game, description);
        ensureCommunityProfile(customer, null);
        return mod;
    }

    public void markReviewHelpful(Customer voter, Review review) throws Exception {
        if (voter == null || review == null) {
            throw new Exception("Voter and review are required.");
        }
        review.addHelpfulVote(voter);
    }

    public void publishGame(IGamePublisher publisher, Game game) throws Exception {
        if (publisher == null || game == null) {
            throw new Exception("Publisher and game are required.");
        }
        IGamePublisher creator = game.getCreator();
        if (creator == null || creator != publisher) {
            throw new Exception("You can publish only games you created.");
        }
        IGamePublisher existing = game.getPublisher();
        if (existing != null && existing != publisher) {
            throw new Exception("Game already published by another developer.");
        }
        publisher.publishGame(game);
        game.setPublisher(publisher);
    }

    public Game createGame(IGamePublisher creator,
                           String title,
                           int releaseYear,
                           double price,
                           Studio studio,
                           List<core.Platform> platforms) {
        Game game = new Game(title, releaseYear, price, studio, platforms);
        games.add(game);
        if (creator != null) {
            game.setCreator(creator);
        }
        return game;
    }

    public Game createGame(String title,
                           int releaseYear,
                           double price,
                           Studio studio,
                           List<core.Platform> platforms) {
        return createGame(null, title, releaseYear, price, studio, platforms);
    }

    // Tworzy nowy ticket open dla klienta.
    public Ticket createTicket(Customer customer, String title, String description) {
        String id = "T-" + (++ticketCounter);
        Ticket ticket = new OpenTicket(id, title, description, customer);
        tickets.add(ticket);
        return ticket;
    }

    public void updateUsername(Customer customer, String currentPassword, String newUsername) throws Exception {
        if (customer == null) {
            throw new Exception("Customer is required.");
        }
        if (currentPassword == null || currentPassword.isBlank()) {
            throw new Exception("Current password is required.");
        }
        if (!customer.getPassword().equals(currentPassword)) {
            throw new Exception("Current password is incorrect.");
        }
        customer.setUsername(newUsername);
    }

    public void updatePassword(Customer customer, String currentPassword, String newPassword) throws Exception {
        if (customer == null) {
            throw new Exception("Customer is required.");
        }
        if (currentPassword == null || currentPassword.isBlank()) {
            throw new Exception("Current password is required.");
        }
        if (!customer.getPassword().equals(currentPassword)) {
            throw new Exception("Current password is incorrect.");
        }
        customer.setPassword(newPassword);
    }

    public void claimPromotion(Customer customer, Promotion promotion) throws Exception {
        if (customer == null || promotion == null) {
            throw new Exception("Customer and promotion are required.");
        }
        if (!promotion.isActiveNow()) {
            throw new Exception("Promotion is not active.");
        }
        String code = promotion.getCode();
        if (code == null || code.isBlank()) {
            throw new Exception("Promotion code not found.");
        }
        if (customer.hasClaimedPromotionCode(code)) {
            throw new Exception("Promo code already claimed.");
        }
        customer.claimPromotionCode(code);
    }

    public List<String> getClaimedCodesFor(Customer customer) {
        if (customer == null) {
            return List.of();
        }
        return new ArrayList<>(customer.getClaimedPromotionCodes());
    }

    public boolean isPromotionCodeUsed(Customer customer, String code) {
        if (customer == null || code == null) {
            return false;
        }
        return customer.isPromotionCodeUsed(code);
    }

    public boolean isPromotionClaimed(Customer customer, String code) {
        if (customer == null || code == null) {
            return false;
        }
        return customer.hasClaimedPromotionCode(code);
    }

    public String getPromotionCode(Promotion promotion) {
        return promotion == null ? null : promotion.getCode();
    }

    public Promotion getPromotionForCode(String code) {
        if (code == null) {
            return null;
        }
        return promotionsByCode.get(code);
    }

    public boolean isPromotionActive(Promotion promotion) {
        return promotion != null && promotion.isActiveNow();
    }

    public String getAchievementProgress(Customer customer, PlatformAchievement achievement) {
        if (customer == null || achievement == null) {
            return "N/A";
        }
        if (achievement instanceof OwnershipAchievement ownership) {
            int current = customer.getLibraryGames().size();
            int required = ownership.getRequiredGamesOwned();
            return current + " / " + required;
        }
        if (achievement instanceof PlaytimeAchievement playtime) {
            int current = customer.getTotalPlaytimeHours();
            int required = playtime.getRequiredHoursPlayed();
            return current + "h / " + required + "h";
        }
        if (achievement instanceof CommunityEngagementAchievement engagement) {
            int reviews = customer.getReviews().size();
            int helpful = customer.getHelpfulVotesReceived();
            return "reviews " + reviews + "/" + engagement.getRequiredReviewsWritten()
                    + ", helpful " + helpful + "/" + engagement.getRequiredHelpfulVotes();
        }
        return "N/A";
    }

    public boolean isAchievementUnlocked(Customer customer, PlatformAchievement achievement) {
        if (customer == null || achievement == null) {
            return false;
        }
        boolean unlocked = false;
        if (achievement instanceof OwnershipAchievement ownership) {
            unlocked = customer.getLibraryGames().size() >= ownership.getRequiredGamesOwned();
        } else if (achievement instanceof PlaytimeAchievement playtime) {
            unlocked = customer.getTotalPlaytimeHours() >= playtime.getRequiredHoursPlayed();
        } else if (achievement instanceof CommunityEngagementAchievement engagement) {
            int reviews = customer.getReviews().size();
            int helpful = customer.getHelpfulVotesReceived();
            unlocked = reviews >= engagement.getRequiredReviewsWritten()
                    && helpful >= engagement.getRequiredHelpfulVotes();
        }
        if (unlocked) {
            customer.unlockPlatformAchievement(achievement);
        }
        return unlocked;
    }

    public Ticket startTicket(Ticket ticket, TechSupport support) throws Exception {
        if (ticket == null || support == null) {
            throw new Exception("Ticket and support user are required.");
        }
        if (ticket instanceof InProgressTicket) {
            throw new Exception("Ticket is already in progress.");
        }
        if (ticket instanceof ClosedTicket) {
            throw new Exception("Ticket is already closed.");
        }
        Ticket updated = new InProgressTicket(ticket, support);
        replaceTicket(ticket, updated);
        return updated;
    }

    public void addTicketMessage(Ticket ticket, String author, String message) throws Exception {
        if (ticket == null) {
            throw new Exception("Ticket is required.");
        }
        if (ticket instanceof ClosedTicket) {
            throw new Exception("Closed tickets cannot receive new messages.");
        }
        if (author == null || author.isBlank()) {
            throw new Exception("Author is required.");
        }
        if (message == null || message.isBlank()) {
            throw new Exception("Message is required.");
        }
        int index = tickets.indexOf(ticket);
        if (index < 0) {
            throw new Exception("Ticket not found.");
        }
        tickets.get(index).addMessage(author, message);
    }

    public Ticket closeTicket(Ticket ticket, String resolutionSummary, boolean solved) throws Exception {
        if (ticket == null) {
            throw new Exception("Ticket is required.");
        }
        if (ticket instanceof ClosedTicket) {
            throw new Exception("Ticket is already closed.");
        }
        if (!(ticket instanceof InProgressTicket)) {
            throw new Exception("Ticket must be in progress to close.");
        }
        Ticket updated = new ClosedTicket(ticket, resolutionSummary, solved);
        replaceTicket(ticket, updated);
        return updated;
    }

    public List<Game> getGames() {
        return games;
    }

    public List<Customer> getCustomers() {
        return customers;
    }

    public List<Purchase> getPurchasesFor(Customer customer) {
        List<Purchase> result = new ArrayList<>();
        for (Purchase p : purchases) {
            if (p.getCustomer() == customer) {
                result.add(p);
            }
        }
        return result;
    }

    public List<Ticket> getTicketsFor(Customer customer) {
        List<Ticket> result = new ArrayList<>();
        for (Ticket t : tickets) {
            if (t.getReporter().equals(customer.getUsername())) {
                result.add(t);
            }
        }
        return result;
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

    public List<TechSupport> getSupportUsers() {
        return supportUsers;
    }

    public List<CommunityMember> getCommunityMembers() {
        return communityMembers;
    }

    public CommunityMember getCommunityProfile(Customer customer) {
        return ensureCommunityProfile(customer, null);
    }

    public IGamePublisher getPublisherFor(Game game) {
        return game == null ? null : game.getPublisher();
    }

    // Nadaje unikalne kody promo i je indeksuje.
    private void initPromotionCodes() {
        promotionsByCode.clear();
        // Nadaj kod jesli brak, oraz zapewnij unikalnosc.
        for (Promotion promotion : promotions) {
            String base = promotion.getCode();
            if (base == null || base.isBlank()) {
                base = generatePromotionCode(promotion.getName());
            }
            String code = base;
            int suffix = 1;
            while (promotionsByCode.containsKey(code)) {
                code = base + "-" + suffix++;
            }
            promotionsByCode.put(code, promotion);
            promotion.setCode(code);
        }
    }

    private String generatePromotionCode(String name) {
        if (name == null) {
            return "PROMO";
        }
        String base = name.trim().toUpperCase(Locale.ROOT);
        base = base.replaceAll("[^A-Z0-9]+", "-");
        base = base.replaceAll("(^-+)|(-+$)", "");
        if (base.isEmpty()) {
            base = "PROMO";
        }
        return base;
    }

    public List<Game> getPublishableGamesFor(IGamePublisher publisher) {
        List<Game> result = new ArrayList<>();
        if (publisher == null) {
            return result;
        }
        for (Game game : games) {
            if (game.getCreator() == publisher && game.getPublisher() == null) {
                result.add(game);
            }
        }
        return result;
    }

    private Customer findCustomer(String username) {
        for (Customer c : customers) {
            if (c.getUsername().equals(username)) {
                return c;
            }
        }
        return null;
    }

    private TechSupport findSupport(String username) {
        for (TechSupport support : supportUsers) {
            if (support.getUsername().equals(username)) {
                return support;
            }
        }
        return null;
    }

    private void replaceTicket(Ticket oldTicket, Ticket newTicket) {
        int index = tickets.indexOf(oldTicket);
        if (index >= 0) {
            tickets.set(index, newTicket);
        } else {
            tickets.add(newTicket);
        }
    }

    // Szuka najwyzszego ID ticketu do kontynuacji numeracji.
    private int findMaxTicketId(List<Ticket> existing) {
        int max = 1000;
        for (Ticket ticket : existing) {
            String id = ticket.getId();
            if (id != null && id.startsWith("T-")) {
                try {
                    int value = Integer.parseInt(id.substring(2));
                    if (value > max) {
                        max = value;
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return max;
    }

    // Zapewnia profil CommunityMember dla kazdego klienta.
    private void syncCommunityProfiles() {
        for (Customer customer : customers) {
            ensureCommunityProfile(customer, null);
        }
    }

    private CommunityMember ensureCommunityProfile(Customer customer, String email) {
        if (customer == null) {
            return null;
        }
        CommunityMember profile = customer.getCommunityProfile();
        if (profile == null) {
            String resolvedEmail = email;
            if (resolvedEmail == null || resolvedEmail.isBlank()) {
                resolvedEmail = defaultCommunityEmail(customer);
            }
            profile = customer.createCommunityProfile(resolvedEmail);
        }
        if (!communityMembers.contains(profile)) {
            communityMembers.add(profile);
        }
        return profile;
    }

    private String defaultCommunityEmail(Customer customer) {
        String username = customer.getUsername();
        if (username == null || username.isBlank()) {
            return "unknown@community.local";
        }
        return username + "@community.local";
    }
}
