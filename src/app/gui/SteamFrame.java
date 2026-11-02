package app.gui;

import achievements.PlatformAchievement;
import community.CommunityMember;
import community.MemberRole;
import core.Achievement;
import core.CorporationStudio;
import core.Customer;
import core.Game;
import core.IGamePublisher;
import core.IndieStudio;
import core.Mod;
import core.Platform;
import core.Purchase;
import core.Review;
import core.Studio;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;
import promotion.FixedAmountPromotion;
import promotion.PercentagePromotion;
import promotion.Promotion;
import ticket.ClosedTicket;
import ticket.InProgressTicket;
import ticket.Ticket;
import ticket.TicketMessage;

// Glowne okno klienta (sklep, biblioteka, tickety, profil, promocje, community).
public final class SteamFrame extends JFrame {
    private final AppState state;
    private final UserSession session;

    private DefaultTableModel storeModel;
    private JTable storeTable;
    private JTextArea storeDetails;
    private JLabel balanceLabel;
    private JComboBox<String> storeFilter;
    private final List<Game> currentStoreGames = new ArrayList<>();

    private DefaultListModel<String> libraryModel;
    private JList<String> libraryList;
    private final List<Game> currentLibraryGames = new ArrayList<>();
    private JLabel libraryInfo;
    private JLabel libraryPlaytimeLabel;
    private DefaultListModel<String> unlockedAchievementsModel;
    private DefaultListModel<String> lockedAchievementsModel;

    private DefaultTableModel purchasesModel;
    private DefaultTableModel ticketsModel;
    private JTable ticketsTable;
    private JTextArea ticketDetails;
    private final List<Ticket> currentTickets = new ArrayList<>();
    private JTextField promoCodeField;
    private DefaultTableModel modsModel;
    private JTable modsTable;
    private DefaultTableModel reviewsModel;
    private JTable reviewsTable;
    private JLabel averageRatingLabel;
    private final List<Mod> currentMods = new ArrayList<>();
    private final List<Review> currentReviews = new ArrayList<>();

    private DefaultListModel<Game> achievementsGameModel;
    private DefaultListModel<String> achievementsListModel;

    private DefaultTableModel promotionsModel;
    private JTable promotionsTable;
    private DefaultTableModel claimedCodesModel;
    private final List<Promotion> currentPromotions = new ArrayList<>();

    private JLabel profileGamesLabel;
    private JLabel profileReviewsLabel;
    private JLabel profileModsLabel;
    private JLabel profilePlaytimeLabel;
    private DefaultTableModel profileAchievementsModel;
    private JLabel statusLabel;

    private JLabel communityNicknameLabel;
    private JLabel communityEmailLabel;
    private JLabel communityRolesLabel;
    private JLabel communityGamesLabel;
    private JLabel communityReviewsLabel;
    private JLabel communityModsLabel;
    private JLabel communityPlaytimeLabel;
    private JLabel communityAverageLabel;
    private JLabel communityScoreLabel;
    private DefaultTableModel communityRankingModel;
    private JComboBox<String> communityFilter;

    private static final int ACHIEVEMENT_HOURS_STEP = 10;
    private static final float DETAILS_FONT_SIZE = 14f;

    private DefaultListModel<String> publishedModel;
    private DefaultComboBoxModel<Game> publishGameModel;
    private JLabel revenueLabel;

    public SteamFrame(AppState state, UserSession session) {
        this.state = state;
        this.session = session;
        setSystemLookAndFeel();
        setTitle("SteamLite");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(980, 620));
        setSize(1200, 760);
        setLocationRelativeTo(null);
        add(buildContent(), BorderLayout.CENTER);
        add(buildStatusBar(), BorderLayout.SOUTH);
    }

    // Ustawia wyglad zgodny z systemem.
    private void setSystemLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }
    }

    // Buduje zakladki glownego okna.
    private JPanel buildContent() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Store", buildStoreTab());
        tabs.addTab("Library", buildLibraryTab());
        tabs.addTab("Purchases", buildPurchasesTab());
        tabs.addTab("Tickets", buildTicketsTab());
        tabs.addTab("Profile", buildProfileTab());
        tabs.addTab("Promotions", buildPromotionsTab());
        tabs.addTab("Community", buildCommunityTab());

        if (session.isDeveloper()) {
            tabs.addTab("Developer", buildDeveloperTab());
        }

        panel.add(tabs, BorderLayout.CENTER);
        return panel;
    }

    // Pasek statusu z informacja o sesji i wylogowaniem.
    private JPanel buildStatusBar() {
        JPanel status = new JPanel(new BorderLayout());
        status.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        statusLabel = new JLabel();
        updateStatusBar();
        status.add(statusLabel, BorderLayout.WEST);
        JButton logout = new JButton("Logout");
        logout.addActionListener(e -> onLogout());
        status.add(logout, BorderLayout.EAST);
        return status;
    }

    // SEKCJA: SKLEP (Store)
    // Zakladka sklepu z lista gier i szczegolami.
    private JPanel buildStoreTab() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        JPanel header = new JPanel(new BorderLayout(4, 4));
        header.add(sectionTitle("Store - Games"), BorderLayout.NORTH);
        header.add(buildStoreFilters(), BorderLayout.SOUTH);
        panel.add(header, BorderLayout.NORTH);

        storeModel = createTableModel(new String[]{"Title", "Studio", "Year", "Price", "Platforms"});
        storeTable = new JTable(storeModel);
        storeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        storeDetails = createDetailsArea();

        refreshStore();

        storeTable.getSelectionModel().addListSelectionListener(e -> {
            refreshStoreSelection();
        });

        JSplitPane rightSplit = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT,
                new JScrollPane(storeDetails),
                buildModsReviewsPanel()
        );
        rightSplit.setResizeWeight(0.35);

        JSplitPane split = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(storeTable),
                rightSplit
        );
        split.setResizeWeight(0.6);
        panel.add(split, BorderLayout.CENTER);
        panel.add(buildStoreActions(), BorderLayout.SOUTH);
        return panel;
    }

    // SKLEP: Studio filter (lista wyboru typu studia).
    private JPanel buildStoreFilters() {
        // Filtry sklepu po typie studia.
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.add(new JLabel("Studio filter:"));
        storeFilter = new JComboBox<>(new String[]{"All", "Independent", "Corporate"});
        storeFilter.addActionListener(e -> refreshStore());
        panel.add(storeFilter);
        return panel;
    }

    // SKLEP: Add funds / Promo code / Buy selected (dolny pasek akcji).
    private JPanel buildStoreActions() {
        // Saldo, zasilenie i zakup.
        JPanel panel = new JPanel(new BorderLayout());
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT));
        balanceLabel = new JLabel();
        refreshBalance();
        left.add(balanceLabel);

        left.add(new JLabel("Add funds:"));
        JTextField fundsField = new JTextField(6);
        left.add(fundsField);
        JButton addFunds = new JButton("Add");
        addFunds.addActionListener(e -> {
            String text = fundsField.getText().trim();
            try {
                double amount = Double.parseDouble(text);
                session.getCustomer().addFunds(amount);
                refreshBalance();
                refreshLibrary();
                fundsField.setText("");
                persistState();
            } catch (Exception ex) {
                showError("Add funds failed: " + ex.getMessage());
            }
        });
        left.add(addFunds);

        left.add(new JLabel("Promo code:"));
        promoCodeField = new JTextField(10);
        left.add(promoCodeField);

        JButton buy = new JButton("Buy selected");
        buy.addActionListener(e -> onBuySelected());

        panel.add(left, BorderLayout.WEST);
        panel.add(buy, BorderLayout.EAST);
        return panel;
    }

    private JSplitPane buildModsReviewsPanel() {
        JSplitPane split = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                buildModsPanel(),
                buildReviewsPanel()
        );
        split.setResizeWeight(0.5);
        return split;
    }

    private JPanel buildModsPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createTitledBorder("Mods"));

        modsModel = createTableModel(new String[]{"Author", "Date", "Description"});
        modsTable = new JTable(modsModel);

        JButton viewMod = new JButton("View");
        viewMod.addActionListener(e -> onViewMod());
        JButton addMod = new JButton("Add mod");
        addMod.addActionListener(e -> onAddMod());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.add(viewMod);
        actions.add(addMod);

        panel.add(new JScrollPane(modsTable), BorderLayout.CENTER);
        panel.add(actions, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildReviewsPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createTitledBorder("Reviews"));

        reviewsModel = createTableModel(new String[]{"Author", "Rating", "Helpful", "Date", "Description"});
        reviewsTable = new JTable(reviewsModel);

        averageRatingLabel = new JLabel("Average rating: N/A");
        JButton markHelpful = new JButton("Helpful");
        markHelpful.addActionListener(e -> onMarkReviewHelpful());
        JButton viewReview = new JButton("View");
        viewReview.addActionListener(e -> onViewReview());
        JButton addReview = new JButton("Add review");
        addReview.addActionListener(e -> onAddReview());

        JPanel footer = new JPanel(new BorderLayout());
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.add(markHelpful);
        actions.add(viewReview);
        actions.add(addReview);
        footer.add(averageRatingLabel, BorderLayout.WEST);
        footer.add(actions, BorderLayout.EAST);

        panel.add(new JScrollPane(reviewsTable), BorderLayout.CENTER);
        panel.add(footer, BorderLayout.SOUTH);
        return panel;
    }

    // SKLEP: Opis gry po prawej (details) + podmiana Mods/Reviews.
    private void refreshStoreSelection() {
        // Zmiana zaznaczenia w tabeli sklepu.
        Game game = getSelectedStoreGame();
        if (game == null) {
            storeDetails.setText("");
            clearModsReviews();
            return;
        }
        storeDetails.setText(buildGameDetails(game));
        refreshModsReviews(game);
    }

    private void refreshModsReviews(Game game) {
        if (modsModel == null || reviewsModel == null || game == null) {
            return;
        }
        modsModel.setRowCount(0);
        currentMods.clear();
        for (Mod mod : game.getMods()) {
            currentMods.add(mod);
            modsModel.addRow(new Object[]{
                    mod.getAuthor().getUsername(),
                    mod.getCreatedAt(),
                    mod.getDescription()
            });
        }

        reviewsModel.setRowCount(0);
        currentReviews.clear();
        for (Review review : game.getReviews()) {
            currentReviews.add(review);
            reviewsModel.addRow(new Object[]{
                    review.getAuthor().getUsername(),
                    review.getRating(),
                    review.getHelpfulCount(),
                    review.getCreatedAt(),
                    review.getDescription()
            });
        }

        double avg = game.getAverageRating();
        if (averageRatingLabel != null) {
            if (Double.isNaN(avg)) {
                averageRatingLabel.setText("Average rating: N/A");
            } else {
                averageRatingLabel.setText("Average rating: " + formatScore(avg));
            }
        }
    }

    private void clearModsReviews() {
        if (modsModel != null) {
            modsModel.setRowCount(0);
        }
        if (reviewsModel != null) {
            reviewsModel.setRowCount(0);
        }
        currentMods.clear();
        currentReviews.clear();
        if (averageRatingLabel != null) {
            averageRatingLabel.setText("Average rating: N/A");
        }
    }

    // MODS: Add mod (nowa modyfikacja do gry).
    private void onAddMod() {
        Game game = getSelectedStoreGame();
        if (game == null) {
            showError("Select a game to add a mod.");
            return;
        }
        JTextArea descArea = new JTextArea(3, 24);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);

        int result = JOptionPane.showConfirmDialog(
                this,
                new JScrollPane(descArea),
                "New mod description",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );
        if (result != JOptionPane.OK_OPTION) {
            return;
        }
        String description = descArea.getText().trim();
        if (description.isBlank()) {
            showError("Description is required.");
            return;
        }
        try {
            state.addMod(session.getCustomer(), game, description);
            refreshModsReviews(game);
            storeDetails.setText(buildGameDetails(game));
            refreshProfile();
            persistState();
        } catch (Exception ex) {
            showError("Add mod failed: " + ex.getMessage());
        }
    }

    private void onViewMod() {
        if (modsTable == null) {
            return;
        }
        int row = modsTable.getSelectedRow();
        if (row < 0 || row >= currentMods.size()) {
            showError("Select a mod to view.");
            return;
        }
        Mod mod = currentMods.get(row);
        showTextDialog(
                "Mod details",
                "Author: " + mod.getAuthor().getUsername()
                        + "\nDate: " + mod.getCreatedAt()
                        + "\n\n" + mod.getDescription()
        );
    }

    // REVIEWS: Add review (nowa recenzja gry).
    private void onAddReview() {
        // Dialog dodawania recenzji dla wybranej gry.
        Game game = getSelectedStoreGame();
        if (game == null) {
            showError("Select a game to add a review.");
            return;
        }
        JTextField ratingField = new JTextField(4);
        JTextArea descArea = new JTextArea(3, 24);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4, 4, 4, 4);
        gc.anchor = GridBagConstraints.WEST;
        gc.gridx = 0;
        gc.gridy = 0;
        panel.add(new JLabel("Rating (1-10):"), gc);
        gc.gridx = 1;
        panel.add(ratingField, gc);
        gc.gridx = 0;
        gc.gridy = 1;
        panel.add(new JLabel("Review:"), gc);
        gc.gridx = 1;
        panel.add(new JScrollPane(descArea), gc);

        int result = JOptionPane.showConfirmDialog(
                this,
                panel,
                "New review",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );
        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        int rating;
        try {
            rating = Integer.parseInt(ratingField.getText().trim());
        } catch (NumberFormatException ex) {
            showError("Rating must be a number between 1 and 10.");
            return;
        }
        String description = descArea.getText().trim();
        if (description.isBlank()) {
            showError("Review description is required.");
            return;
        }
        try {
            state.addReview(session.getCustomer(), game, rating, description);
            refreshModsReviews(game);
            storeDetails.setText(buildGameDetails(game));
            refreshProfile();
            persistState();
        } catch (Exception ex) {
            showError("Add review failed: " + ex.getMessage());
        }
    }

    private void onViewReview() {
        if (reviewsTable == null) {
            return;
        }
        int row = reviewsTable.getSelectedRow();
        if (row < 0 || row >= currentReviews.size()) {
            showError("Select a review to view.");
            return;
        }
        Review review = currentReviews.get(row);
        showTextDialog(
                "Review details",
                "Author: " + review.getAuthor().getUsername()
                        + "\nRating: " + review.getRating()
                        + "\nHelpful: " + review.getHelpfulCount()
                        + "\nDate: " + review.getCreatedAt()
                        + "\n\n" + review.getDescription()
        );
    }

    // REVIEWS: Helpful (glos na przydatnosc recenzji).
    private void onMarkReviewHelpful() {
        if (reviewsTable == null) {
            return;
        }
        int row = reviewsTable.getSelectedRow();
        if (row < 0 || row >= currentReviews.size()) {
            showError("Select a review to mark as helpful.");
            return;
        }
        Review review = currentReviews.get(row);
        try {
            state.markReviewHelpful(session.getCustomer(), review);
            refreshModsReviews(review.getGame());
            persistState();
        } catch (Exception ex) {
            showError("Helpful vote failed: " + ex.getMessage());
        }
    }

    private Game getSelectedStoreGame() {
        int row = storeTable.getSelectedRow();
        if (row < 0 || row >= currentStoreGames.size()) {
            return null;
        }
        return currentStoreGames.get(row);
    }

    private void showTextDialog(String title, String text) {
        JTextArea area = new JTextArea(text, 10, 40);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setEditable(false);
        area.setFont(area.getFont().deriveFont(DETAILS_FONT_SIZE));
        JOptionPane.showMessageDialog(
                this,
                new JScrollPane(area),
                title,
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    // SEKCJA: BIBLIOTEKA (Library)
    // Zakladka biblioteki klienta.
    // BIBLIOTEKA: lista gier klienta.
    private JPanel buildLibraryTab() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        libraryModel = new DefaultListModel<>();
        libraryList = new JList<>(libraryModel);
        libraryList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        libraryList.addListSelectionListener(e -> refreshLibraryDetails());

        libraryInfo = new JLabel();
        refreshLibrary();

        JPanel header = new JPanel(new BorderLayout());
        header.add(sectionTitle("Library"), BorderLayout.NORTH);
        header.add(libraryInfo, BorderLayout.SOUTH);

        panel.add(header, BorderLayout.NORTH);
        JSplitPane split = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(libraryList),
                buildLibraryDetailsPanel()
        );
        split.setResizeWeight(0.35);
        panel.add(split, BorderLayout.CENTER);
        return panel;
    }

    // BIBLIOTEKA: szczegoly gry (playtime + osiagniecia).
    private JPanel buildLibraryDetailsPanel() {
        // Szczegoly wybranej gry w bibliotece.
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createTitledBorder("Selected game"));

        libraryPlaytimeLabel = new JLabel("Playtime: 0 h");
        panel.add(libraryPlaytimeLabel, BorderLayout.NORTH);

        unlockedAchievementsModel = new DefaultListModel<>();
        lockedAchievementsModel = new DefaultListModel<>();
        JList<String> unlockedList = new JList<>(unlockedAchievementsModel);
        JList<String> lockedList = new JList<>(lockedAchievementsModel);

        JPanel unlockedPanel = new JPanel(new BorderLayout(4, 4));
        unlockedPanel.add(new JLabel("Unlocked achievements"), BorderLayout.NORTH);
        unlockedPanel.add(new JScrollPane(unlockedList), BorderLayout.CENTER);

        JPanel lockedPanel = new JPanel(new BorderLayout(4, 4));
        lockedPanel.add(new JLabel("Locked achievements"), BorderLayout.NORTH);
        lockedPanel.add(new JScrollPane(lockedList), BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                unlockedPanel,
                lockedPanel
        );
        split.setResizeWeight(0.5);
        panel.add(split, BorderLayout.CENTER);
        return panel;
    }

    // SEKCJA: ZAKUPY (Purchases)
    // Historia zakupow klienta.
    // ZAKUPY: historia zakupow klienta.
    private JPanel buildPurchasesTab() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        panel.add(sectionTitle("Purchases"), BorderLayout.NORTH);

        purchasesModel = createTableModel(new String[]{"Game", "Date", "Price", "Promotion"});
        JTable table = new JTable(purchasesModel);
        refreshPurchases();

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    // SEKCJA: TICKETY (Tickets)
    // Zakladka ticketow (zgloszenia).
    private JPanel buildTicketsTab() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        panel.add(sectionTitle("Support tickets"), BorderLayout.NORTH);

        ticketsModel = createTableModel(new String[]{"Id", "Status", "Title", "Updated"});
        ticketsTable = new JTable(ticketsModel);
        ticketsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ticketDetails = createDetailsArea();

        ticketsTable.getSelectionModel().addListSelectionListener(e -> updateTicketDetails());
        refreshTickets();

        JSplitPane split = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(ticketsTable),
                new JScrollPane(ticketDetails)
        );
        split.setResizeWeight(0.55);
        panel.add(split, BorderLayout.CENTER);

        JPanel actions = new JPanel(new BorderLayout(8, 8));
        actions.add(buildTicketReplyForm(), BorderLayout.NORTH);
        actions.add(buildTicketForm(), BorderLayout.SOUTH);
        panel.add(actions, BorderLayout.SOUTH);
        return panel;
    }

    // SEKCJA: PROFIL (Profile)
    // Profil klienta i statystyki.
    private JPanel buildProfileTab() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JPanel summary = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4, 4, 4, 12);
        gc.anchor = GridBagConstraints.WEST;

        gc.gridx = 0;
        gc.gridy = 0;
        summary.add(new JLabel("Username:"), gc);
        gc.gridx = 1;
        summary.add(new JLabel(session.getCustomer().getUsername()), gc);

        gc.gridx = 0;
        gc.gridy = 1;
        summary.add(new JLabel("Games owned:"), gc);
        gc.gridx = 1;
        profileGamesLabel = new JLabel();
        summary.add(profileGamesLabel, gc);

        gc.gridx = 0;
        gc.gridy = 2;
        summary.add(new JLabel("Reviews written:"), gc);
        gc.gridx = 1;
        profileReviewsLabel = new JLabel();
        summary.add(profileReviewsLabel, gc);

        gc.gridx = 0;
        gc.gridy = 3;
        summary.add(new JLabel("Mods created:"), gc);
        gc.gridx = 1;
        profileModsLabel = new JLabel();
        summary.add(profileModsLabel, gc);

        gc.gridx = 0;
        gc.gridy = 4;
        summary.add(new JLabel("Total playtime:"), gc);
        gc.gridx = 1;
        profilePlaytimeLabel = new JLabel();
        summary.add(profilePlaytimeLabel, gc);

        gc.gridx = 0;
        gc.gridy = 5;
        JButton changeUsername = new JButton("Change username");
        changeUsername.addActionListener(e -> onChangeUsername());
        summary.add(changeUsername, gc);

        gc.gridx = 1;
        JButton changePassword = new JButton("Change password");
        changePassword.addActionListener(e -> onChangePassword());
        summary.add(changePassword, gc);

        panel.add(summary, BorderLayout.NORTH);

        profileAchievementsModel = createTableModel(
                new String[]{"Code", "Name", "Tier", "Progress", "Status"});
        JTable table = new JTable(profileAchievementsModel);
        refreshProfile();

        JPanel tablePanel = new JPanel(new BorderLayout(8, 8));
        tablePanel.add(sectionTitle("Achievements"), BorderLayout.NORTH);
        tablePanel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(tablePanel, BorderLayout.CENTER);
        return panel;
    }

    // TICKETY: odpowiedz na wybrane zgloszenie.
    private JPanel buildTicketReplyForm() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createTitledBorder("Reply to selected ticket"));

        JTextArea replyArea = new JTextArea(3, 20);
        replyArea.setLineWrap(true);
        replyArea.setWrapStyleWord(true);

        JButton send = new JButton("Send reply");
        send.addActionListener(e -> {
            Ticket ticket = getSelectedTicket();
            if (ticket == null) {
                showError("Select a ticket to reply.");
                return;
            }
            if (ticket instanceof ClosedTicket) {
                showError("Closed tickets cannot receive replies.");
                return;
            }
            String message = replyArea.getText().trim();
            if (message.isBlank()) {
                showError("Message is required.");
                return;
            }
            try {
                state.addTicketMessage(ticket, session.getCustomer().getUsername(), message);
                refreshTickets();
                replyArea.setText("");
                persistState();
            } catch (Exception ex) {
                showError("Reply failed: " + ex.getMessage());
            }
        });

        panel.add(new JScrollPane(replyArea), BorderLayout.CENTER);
        panel.add(send, BorderLayout.EAST);
        return panel;
    }

    // TICKETY: tworzenie nowego zgloszenia.
    private JPanel buildTicketForm() {
        // Formularz nowego zgloszenia.
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createTitledBorder("New ticket"));

        JTextField titleField = new JTextField(20);
        JTextArea descArea = new JTextArea(3, 20);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);

        JPanel fields = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(2, 2, 2, 2);
        gc.anchor = GridBagConstraints.WEST;

        gc.gridx = 0;
        gc.gridy = 0;
        fields.add(new JLabel("Title:"), gc);
        gc.gridx = 1;
        fields.add(titleField, gc);

        gc.gridx = 0;
        gc.gridy = 1;
        fields.add(new JLabel("Description:"), gc);
        gc.gridx = 1;
        fields.add(new JScrollPane(descArea), gc);

        JButton create = new JButton("Create");
        create.addActionListener(e -> {
            String title = titleField.getText().trim();
            String desc = descArea.getText().trim();
            try {
                state.createTicket(session.getCustomer(), title, desc);
                refreshTickets();
                titleField.setText("");
                descArea.setText("");
                persistState();
            } catch (Exception ex) {
                showError("Ticket creation failed: " + ex.getMessage());
            }
        });

        panel.add(fields, BorderLayout.CENTER);
        panel.add(create, BorderLayout.EAST);
        return panel;
    }

    // SEKCJA: OSIAGNIECIA (Achievements)
    private JPanel buildAchievementsTab() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        panel.add(sectionTitle("Achievements"), BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Game achievements", buildGameAchievementsTab());
        tabs.addTab("Platform achievements", buildPlatformAchievementsTab());

        panel.add(tabs, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildGameAchievementsTab() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        achievementsGameModel = new DefaultListModel<>();
        achievementsListModel = new DefaultListModel<>();
        JList<Game> gameList = new JList<>(achievementsGameModel);
        gameList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        gameList.setCellRenderer((list, value, index, isSelected, cellHasFocus) -> {
            JLabel label = new JLabel(value.getTitle());
            label.setOpaque(true);
            if (isSelected) {
                label.setBackground(list.getSelectionBackground());
                label.setForeground(list.getSelectionForeground());
            }
            return label;
        });

        JList<String> achList = new JList<>(achievementsListModel);

        gameList.addListSelectionListener(e -> {
            Game selected = gameList.getSelectedValue();
            achievementsListModel.clear();
            if (selected != null) {
                for (Achievement a : selected.getAchievements()) {
                    achievementsListModel.addElement(a.getName());
                }
            }
        });

        refreshAchievements();
        if (achievementsGameModel.getSize() > 0) {
            gameList.setSelectedIndex(0);
        }

        JSplitPane split = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(gameList),
                new JScrollPane(achList)
        );
        split.setResizeWeight(0.3);
        panel.add(split, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildPlatformAchievementsTab() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        DefaultTableModel model = createTableModel(
                new String[]{"Code", "Name", "Tier", "Reward", "Requirements"});
        JTable table = new JTable(model);

        for (PlatformAchievement pa : state.getPlatformAchievements()) {
            model.addRow(new Object[]{
                    pa.getCode(),
                    pa.getName(),
                    pa.getTier(),
                    pa.getRewardPoints(),
                    pa.getRequirementDescription()
            });
        }

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    // SEKCJA: PROMOCJE (Promotions)
    // Zakladka promocji i kodow.
    // PROMOCJE: lista promocji + kody klienta.
    private JPanel buildPromotionsTab() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        panel.add(sectionTitle("Promotions"), BorderLayout.NORTH);

        promotionsModel = createTableModel(new String[]{"Name", "Type", "Value", "Start", "End", "Code", "Active"});
        promotionsTable = new JTable(promotionsModel);
        promotionsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        refreshPromotions();

        JButton claim = new JButton("Claim code");
        claim.addActionListener(e -> onClaimPromotion());

        JPanel promosPanel = new JPanel(new BorderLayout(8, 8));
        promosPanel.add(new JScrollPane(promotionsTable), BorderLayout.CENTER);
        promosPanel.add(claim, BorderLayout.SOUTH);

        claimedCodesModel = createTableModel(new String[]{"Code", "Promotion", "Status", "Valid until"});
        JTable claimedTable = new JTable(claimedCodesModel);
        refreshClaimedCodes();

        JPanel claimedPanel = new JPanel(new BorderLayout(8, 8));
        claimedPanel.add(sectionTitle("My promo codes"), BorderLayout.NORTH);
        claimedPanel.add(new JScrollPane(claimedTable), BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT,
                promosPanel,
                claimedPanel
        );
        split.setResizeWeight(0.6);
        panel.add(split, BorderLayout.CENTER);
        return panel;
    }

    // SEKCJA: SPOLECZNOSC (Community)
    // Zakladka spolecznosci i rankingu.
    private JPanel buildCommunityTab() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        panel.add(sectionTitle("Community"), BorderLayout.NORTH);

        JPanel profilePanel = buildCommunityProfilePanel();
        JPanel rankingPanel = buildCommunityRankingPanel();

        JSplitPane split = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT,
                profilePanel,
                rankingPanel
        );
        split.setResizeWeight(0.35);
        panel.add(split, BorderLayout.CENTER);
        refreshCommunity();
        return panel;
    }

    private JPanel buildCommunityProfilePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("My community profile"));

        communityNicknameLabel = new JLabel("-");
        communityEmailLabel = new JLabel("-");
        communityRolesLabel = new JLabel("-");
        communityGamesLabel = new JLabel("-");
        communityReviewsLabel = new JLabel("-");
        communityModsLabel = new JLabel("-");
        communityPlaytimeLabel = new JLabel("-");
        communityAverageLabel = new JLabel("-");
        communityScoreLabel = new JLabel("-");

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4, 4, 4, 4);
        gc.anchor = GridBagConstraints.WEST;

        int row = 0;
        addCommunityRow(panel, gc, row++, "Nickname:", communityNicknameLabel);
        addCommunityRow(panel, gc, row++, "Email:", communityEmailLabel);
        addCommunityRow(panel, gc, row++, "Roles:", communityRolesLabel);
        addCommunityRow(panel, gc, row++, "Games owned:", communityGamesLabel);
        addCommunityRow(panel, gc, row++, "Hours played:", communityPlaytimeLabel);
        addCommunityRow(panel, gc, row++, "Reviews written:", communityReviewsLabel);
        addCommunityRow(panel, gc, row++, "Mods published:", communityModsLabel);
        addCommunityRow(panel, gc, row++, "Average review:", communityAverageLabel);
        addCommunityRow(panel, gc, row, "Community score:", communityScoreLabel);

        return panel;
    }

    private void addCommunityRow(JPanel panel, GridBagConstraints gc, int row, String label, JLabel value) {
        gc.gridx = 0;
        gc.gridy = row;
        panel.add(new JLabel(label), gc);
        gc.gridx = 1;
        panel.add(value, gc);
    }

    private JPanel buildCommunityRankingPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createTitledBorder("Community ranking"));

        communityRankingModel = createTableModel(new String[]{
                "Nickname", "Roles", "Games", "Reviews", "Mods", "Hours", "Score"
        });
        JTable table = new JTable(communityRankingModel);

        JPanel header = new JPanel(new BorderLayout());
        header.add(new JLabel("Top 10 by community score"), BorderLayout.WEST);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        filterPanel.add(new JLabel("Filter:"));
        communityFilter = new JComboBox<>(new String[]{"All", "PLAYER", "REVIEWER", "MODDER"});
        communityFilter.addActionListener(e -> refreshCommunityRanking());
        filterPanel.add(communityFilter);
        header.add(filterPanel, BorderLayout.EAST);

        panel.add(header, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private void refreshCommunity() {
        updateCommunityProfile();
        refreshCommunityRanking();
    }

    private void updateCommunityProfile() {
        if (communityNicknameLabel == null) {
            return;
        }
        CommunityMember member = state.getCommunityProfile(session.getCustomer());
        if (member == null) {
            communityNicknameLabel.setText("-");
            communityEmailLabel.setText("-");
            communityRolesLabel.setText("None");
            communityGamesLabel.setText("0");
            communityReviewsLabel.setText("0");
            communityModsLabel.setText("0");
            communityPlaytimeLabel.setText("0");
            communityAverageLabel.setText("N/A");
            communityScoreLabel.setText("0.0");
            return;
        }

        communityNicknameLabel.setText(member.getNickname());
        communityEmailLabel.setText(member.getEmail());
        communityRolesLabel.setText(formatRoles(member.getRoles()));
        communityGamesLabel.setText(String.valueOf(member.getGamesOwned()));
        communityReviewsLabel.setText(String.valueOf(member.getReviewsWritten()));
        communityModsLabel.setText(String.valueOf(member.getModsPublished()));
        communityPlaytimeLabel.setText(String.valueOf(member.getHoursPlayed()));
        communityAverageLabel.setText(member.getReviewsWritten() == 0
                ? "N/A"
                : formatScore(member.getAverageReviewScore()));
        communityScoreLabel.setText(formatScore(member.getCommunityScore()));
    }

    private void refreshCommunityRanking() {
        if (communityRankingModel == null) {
            return;
        }
        communityRankingModel.setRowCount(0);
        List<CommunityMember> members = new ArrayList<>(state.getCommunityMembers());
        MemberRole filterRole = getCommunityFilterRole();
        if (filterRole != null) {
            members.removeIf(member -> !member.hasRole(filterRole));
        }
        members.sort(Comparator.comparingDouble(CommunityMember::getCommunityScore).reversed());
        int limit = Math.min(10, members.size());
        for (int i = 0; i < limit; i++) {
            CommunityMember member = members.get(i);
            communityRankingModel.addRow(new Object[]{
                    member.getNickname(),
                    formatRoles(member.getRoles()),
                    member.getGamesOwned(),
                    member.getReviewsWritten(),
                    member.getModsPublished(),
                    member.getHoursPlayed(),
                    formatScore(member.getCommunityScore())
            });
        }
    }

    private MemberRole getCommunityFilterRole() {
        if (communityFilter == null) {
            return null;
        }
        Object selected = communityFilter.getSelectedItem();
        if (selected == null) {
            return null;
        }
        String value = selected.toString();
        if ("All".equals(value)) {
            return null;
        }
        try {
            return MemberRole.valueOf(value);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private String formatRoles(Set<MemberRole> roles) {
        if (roles == null || roles.isEmpty()) {
            return "None";
        }
        return String.join(", ", roles.stream().map(Enum::name).toList());
    }

    // SEKCJA: TWORCA (Developer)
    // Zakladka tworcy/wydawcy (jesli user jest developerem).
    private JPanel buildDeveloperTab() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        panel.add(sectionTitle("Developer tools"), BorderLayout.NORTH);

        publishedModel = new DefaultListModel<>();
        JList<String> publishedList = new JList<>(publishedModel);
        revenueLabel = new JLabel();
        refreshPublished();

        JPanel left = new JPanel(new BorderLayout(4, 4));
        left.add(new JLabel("Published games:"), BorderLayout.NORTH);
        left.add(new JScrollPane(publishedList), BorderLayout.CENTER);
        left.add(revenueLabel, BorderLayout.SOUTH);

        JPanel right = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4, 4, 4, 4);
        gc.anchor = GridBagConstraints.WEST;
        gc.gridx = 0;
        gc.gridy = 0;
        right.add(new JLabel("Publish game:"), gc);

        publishGameModel = new DefaultComboBoxModel<>();
        JComboBox<Game> publishBox = new JComboBox<>(publishGameModel);
        publishBox.setRenderer((list, value, index, isSelected, cellHasFocus) -> {
            JLabel label = new JLabel(value == null ? "" : value.getTitle());
            label.setOpaque(true);
            if (isSelected) {
                label.setBackground(list.getSelectionBackground());
                label.setForeground(list.getSelectionForeground());
            }
            return label;
        });

        gc.gridx = 1;
        right.add(publishBox, gc);
        refreshPublishGames();

        JButton publish = new JButton("Publish");
        publish.addActionListener(e -> {
            Game game = (Game) publishBox.getSelectedItem();
            if (game == null) {
                showError("Select a game to publish.");
                return;
            }
            try {
                IGamePublisher publisher = session.getPublisher();
                if (state.getPublisherFor(game) == publisher) {
                    showError("Game is already published by you.");
                    return;
                }
                state.publishGame(publisher, game);
                refreshPublished();
                persistState();
            } catch (Exception ex) {
                showError("Publish failed: " + ex.getMessage());
            }
        });

        gc.gridx = 2;
        right.add(publish, gc);

        gc.gridx = 0;
        gc.gridy = 1;
        JButton createGame = new JButton("Create new game");
        createGame.addActionListener(e -> openCreateGameDialog());
        right.add(createGame, gc);

        JSplitPane split = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                left,
                right
        );
        split.setResizeWeight(0.5);
        panel.add(split, BorderLayout.CENTER);
        return panel;
    }

    // TWORCA: formularz tworzenia nowej gry.
    private void openCreateGameDialog() {
        JTextField titleField = new JTextField(16);
        JTextField yearField = new JTextField(6);
        JTextField priceField = new JTextField(6);
        JTextField studioNameField = new JTextField(16);
        JTextField countryField = new JTextField(10);
        JTextField teamSizeField = new JTextField(5);
        JTextField parentCompanyField = new JTextField(16);

        JCheckBox pc = new JCheckBox("PC", true);
        JCheckBox ps = new JCheckBox("PlayStation");
        JCheckBox xbox = new JCheckBox("Xbox");
        JCheckBox sw = new JCheckBox("Switch");
        JCheckBox mobile = new JCheckBox("Mobile");

        JCheckBox corpStudio = new JCheckBox("Corporate studio");
        JCheckBox publishNow = new JCheckBox("Publish now", true);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4, 4, 4, 4);
        gc.anchor = GridBagConstraints.WEST;

        int row = 0;
        gc.gridx = 0;
        gc.gridy = row;
        panel.add(new JLabel("Title:"), gc);
        gc.gridx = 1;
        panel.add(titleField, gc);

        row++;
        gc.gridx = 0;
        gc.gridy = row;
        panel.add(new JLabel("Year:"), gc);
        gc.gridx = 1;
        panel.add(yearField, gc);

        row++;
        gc.gridx = 0;
        gc.gridy = row;
        panel.add(new JLabel("Price:"), gc);
        gc.gridx = 1;
        panel.add(priceField, gc);

        row++;
        gc.gridx = 0;
        gc.gridy = row;
        panel.add(new JLabel("Studio name:"), gc);
        gc.gridx = 1;
        panel.add(studioNameField, gc);

        row++;
        gc.gridx = 0;
        gc.gridy = row;
        panel.add(new JLabel("Country:"), gc);
        gc.gridx = 1;
        panel.add(countryField, gc);

        row++;
        gc.gridx = 0;
        gc.gridy = row;
        panel.add(new JLabel("Team size:"), gc);
        gc.gridx = 1;
        panel.add(teamSizeField, gc);

        row++;
        gc.gridx = 0;
        gc.gridy = row;
        panel.add(new JLabel("Parent company:"), gc);
        gc.gridx = 1;
        panel.add(parentCompanyField, gc);

        row++;
        gc.gridx = 0;
        gc.gridy = row;
        panel.add(new JLabel("Platforms:"), gc);
        gc.gridx = 1;
        JPanel platforms = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        platforms.add(pc);
        platforms.add(ps);
        platforms.add(xbox);
        platforms.add(sw);
        platforms.add(mobile);
        panel.add(platforms, gc);

        row++;
        gc.gridx = 0;
        gc.gridy = row;
        panel.add(corpStudio, gc);
        gc.gridx = 1;
        panel.add(publishNow, gc);

        int result = JOptionPane.showConfirmDialog(
                this,
                panel,
                "Create game",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );
        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            String title = titleField.getText().trim();
            int year = Integer.parseInt(yearField.getText().trim());
            double price = Double.parseDouble(priceField.getText().trim());
            String studioName = studioNameField.getText().trim();
            String country = countryField.getText().trim();
            String teamSizeText = teamSizeField.getText().trim();
            String parentCompany = parentCompanyField.getText().trim();

            List<Platform> platformsList = new ArrayList<>();
            if (pc.isSelected()) {
                platformsList.add(Platform.PC);
            }
            if (ps.isSelected()) {
                platformsList.add(Platform.PLAYSTATION);
            }
            if (xbox.isSelected()) {
                platformsList.add(Platform.XBOX);
            }
            if (sw.isSelected()) {
                platformsList.add(Platform.NINTENDO_SWITCH);
            }
            if (mobile.isSelected()) {
                platformsList.add(Platform.MOBILE);
            }

            Studio studio;
            if (corpStudio.isSelected()) {
                if (parentCompany.isBlank()) {
                    throw new Exception("Parent company is required for corporate studio.");
                }
                studio = new core.CorporationStudio(studioName, country, parentCompany);
            } else {
                int teamSize = Integer.parseInt(teamSizeText);
                studio = new IndieStudio(studioName, country, teamSize);
            }

            Game game = state.createGame(session.getPublisher(), title, year, price, studio, platformsList);
            refreshStore();
            refreshPublishGames();

            if (publishNow.isSelected()) {
                state.publishGame(session.getPublisher(), game);
                refreshPublished();
            }
            persistState();
        } catch (Exception ex) {
            showError("Create game failed: " + ex.getMessage());
        }
    }

    // SKLEP: Buy selected (zakup wybranej gry).
    private void onBuySelected() {
        Game game = getSelectedStoreGame();
        if (game == null) {
            showError("Select a game to buy.");
            return;
        }
        String promoCode = promoCodeField == null ? "" : promoCodeField.getText().trim();
        try {
            state.purchaseGame(session.getCustomer(), game, promoCode);
            refreshPurchases();
            refreshLibrary();
            refreshBalance();
            refreshClaimedCodes();
            refreshProfile();
            if (promoCodeField != null) {
                promoCodeField.setText("");
            }
            persistState();
        } catch (Exception ex) {
            showError("Purchase failed: " + ex.getMessage());
        }
    }

    // PROMOCJE: Claim code (przypisanie kodu klientowi).
    private void onClaimPromotion() {
        if (promotionsTable == null) {
            return;
        }
        int row = promotionsTable.getSelectedRow();
        if (row < 0 || row >= currentPromotions.size()) {
            showError("Select a promotion to claim.");
            return;
        }
        Promotion promotion = currentPromotions.get(row);
        try {
            state.claimPromotion(session.getCustomer(), promotion);
            refreshClaimedCodes();
            persistState();
        } catch (Exception ex) {
            showError("Claim failed: " + ex.getMessage());
        }
    }

    // PROFIL: zmiana nazwy uzytkownika.
    private void onChangeUsername() {
        JPasswordField currentPassword = new JPasswordField(12);
        JTextField newUsername = new JTextField(12);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4, 4, 4, 4);
        gc.anchor = GridBagConstraints.WEST;
        gc.gridx = 0;
        gc.gridy = 0;
        panel.add(new JLabel("Current password:"), gc);
        gc.gridx = 1;
        panel.add(currentPassword, gc);
        gc.gridx = 0;
        gc.gridy = 1;
        panel.add(new JLabel("New username:"), gc);
        gc.gridx = 1;
        panel.add(newUsername, gc);

        int result = JOptionPane.showConfirmDialog(
                this,
                panel,
                "Change username",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );
        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        String current = new String(currentPassword.getPassword());
        String username = newUsername.getText().trim();
        try {
            state.updateUsername(session.getCustomer(), current, username);
            updateStatusBar();
            refreshCommunity();
            persistState();
        } catch (Exception ex) {
            showError("Username change failed: " + ex.getMessage());
        }
    }

    // PROFIL: zmiana hasla.
    private void onChangePassword() {
        JPasswordField currentPassword = new JPasswordField(12);
        JPasswordField newPassword = new JPasswordField(12);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4, 4, 4, 4);
        gc.anchor = GridBagConstraints.WEST;
        gc.gridx = 0;
        gc.gridy = 0;
        panel.add(new JLabel("Current password:"), gc);
        gc.gridx = 1;
        panel.add(currentPassword, gc);
        gc.gridx = 0;
        gc.gridy = 1;
        panel.add(new JLabel("New password:"), gc);
        gc.gridx = 1;
        panel.add(newPassword, gc);

        int result = JOptionPane.showConfirmDialog(
                this,
                panel,
                "Change password",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );
        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        String current = new String(currentPassword.getPassword());
        String next = new String(newPassword.getPassword());
        try {
            state.updatePassword(session.getCustomer(), current, next);
            persistState();
        } catch (Exception ex) {
            showError("Password change failed: " + ex.getMessage());
        }
    }

    private void updateStatusBar() {
        if (statusLabel != null) {
            statusLabel.setText("Logged in: " + session.getCustomer().getUsername()
                    + " (" + session.getRoleLabel() + ")");
        }
    }

    private boolean matchesStoreFilter(Game game) {
        if (storeFilter == null) {
            return true;
        }
        Object selected = storeFilter.getSelectedItem();
        if (selected == null) {
            return true;
        }
        String value = selected.toString();
        if ("Independent".equals(value)) {
            return game.getStudio().isIndependent();
        }
        if ("Corporate".equals(value)) {
            return !game.getStudio().isIndependent();
        }
        return true;
    }

    // SEKCJA odświeżanie

    // Odswieza liste gier w sklepie.
    private void refreshStore() {
        storeModel.setRowCount(0);
        currentStoreGames.clear();
        for (Game game : state.getGames()) {
            if (!matchesStoreFilter(game)) {
                continue;
            }
            currentStoreGames.add(game);
            storeModel.addRow(new Object[]{
                    game.getTitle(),
                    game.getStudio().getName(),
                    game.getReleaseYear(),
                    formatPrice(game.getPrice()),
                    formatPlatforms(game)
            });
        }
        if (storeModel.getRowCount() > 0) {
            storeTable.setRowSelectionInterval(0, 0);
        }
        refreshStoreSelection();
    }

    // Odswieza liste gier w bibliotece.
    private void refreshLibrary() {
        libraryModel.clear();
        currentLibraryGames.clear();
        Customer customer = session.getCustomer();
        for (Game g : customer.getLibraryGames()) {
            currentLibraryGames.add(g);
            libraryModel.addElement(g.getTitle());
        }
        libraryInfo.setText("Purchase history: " + customer.getPurchaseHistory().size()
                + " | Library size: " + customer.getLibraryGames().size());
        if (libraryModel.getSize() > 0) {
            libraryList.setSelectedIndex(0);
        } else {
            refreshLibraryDetails();
        }
    }

    // ZAKUPY: odswiezanie tabeli historii zakupow.
    private void refreshPurchases() {
        purchasesModel.setRowCount(0);
        for (Purchase p : state.getPurchasesFor(session.getCustomer())) {
            purchasesModel.addRow(new Object[]{
                    p.getGame().getTitle(),
                    p.getPurchaseDate(),
                    formatPrice(p.getPricePaid()),
                    formatPromotion(p)
            });
        }
    }

    // BIBLIOTEKA: odswiezanie panelu szczegolow wybranej gry.
    private void refreshLibraryDetails() {
        Game game = getSelectedLibraryGame();
        if (game == null) {
            if (libraryPlaytimeLabel != null) {
                libraryPlaytimeLabel.setText("Playtime: 0 h");
            }
            if (unlockedAchievementsModel != null) {
                unlockedAchievementsModel.clear();
            }
            if (lockedAchievementsModel != null) {
                lockedAchievementsModel.clear();
            }
            return;
        }

        int hours = session.getCustomer().getPlaytimeHours(game);
        if (libraryPlaytimeLabel != null) {
            libraryPlaytimeLabel.setText("Playtime: " + hours + " h");
        }

        if (unlockedAchievementsModel == null || lockedAchievementsModel == null) {
            return;
        }
        unlockedAchievementsModel.clear();
        lockedAchievementsModel.clear();

        List<Achievement> achievements = game.getAchievements();
        int unlockCount = Math.min(achievements.size(), hours / ACHIEVEMENT_HOURS_STEP);
        for (int i = 0; i < achievements.size(); i++) {
            Achievement achievement = achievements.get(i);
            if (i < unlockCount) {
                unlockedAchievementsModel.addElement(achievement.getName());
            } else {
                lockedAchievementsModel.addElement(achievement.getName());
            }
        }
    }

    private Game getSelectedLibraryGame() {
        if (libraryList == null) {
            return null;
        }
        int index = libraryList.getSelectedIndex();
        if (index < 0 || index >= currentLibraryGames.size()) {
            return null;
        }
        return currentLibraryGames.get(index);
    }

    private void refreshPromotions() {
        // Odswieza tabele promocji.
        if (promotionsModel == null) {
            return;
        }
        promotionsModel.setRowCount(0);
        currentPromotions.clear();
        for (Promotion p : state.getPromotions()) {
            currentPromotions.add(p);
            String type = p instanceof PercentagePromotion ? "Percent" : "Fixed";
            String value = p instanceof PercentagePromotion
                    ? ((PercentagePromotion) p).getPercentage() + "%"
                    : formatPrice(((FixedAmountPromotion) p).getAmount());
            promotionsModel.addRow(new Object[]{
                    p.getName(),
                    type,
                    value,
                    p.getStartDate(),
                    p.getEndDate(),
                    state.getPromotionCode(p),
                    state.isPromotionActive(p) ? "Yes" : "No"
            });
        }
        if (promotionsModel.getRowCount() > 0) {
            promotionsTable.setRowSelectionInterval(0, 0);
        }
    }

    private void refreshClaimedCodes() {
        // Odswieza tabele kodow promocyjnych klienta.
        if (claimedCodesModel == null) {
            return;
        }
        claimedCodesModel.setRowCount(0);
        List<String> codes = state.getClaimedCodesFor(session.getCustomer());
        for (String code : codes) {
            Promotion promotion = state.getPromotionForCode(code);
            if (promotion == null) {
                continue;
            }
            boolean used = state.isPromotionCodeUsed(session.getCustomer(), code);
            String status = used ? "Used" : (promotion.isActiveNow() ? "Active" : "Expired");
            claimedCodesModel.addRow(new Object[]{
                    code,
                    promotion.getName(),
                    status,
                    promotion.getEndDate()
            });
        }
    }

    private void refreshProfile() {
        if (profileGamesLabel != null) {
            profileGamesLabel.setText(String.valueOf(session.getCustomer().getLibraryGames().size()));
        }
        if (profileReviewsLabel != null) {
            profileReviewsLabel.setText(String.valueOf(session.getCustomer().getReviews().size()));
        }
        if (profileModsLabel != null) {
            profileModsLabel.setText(String.valueOf(session.getCustomer().getMods().size()));
        }
        if (profilePlaytimeLabel != null) {
            profilePlaytimeLabel.setText(session.getCustomer().getTotalPlaytimeHours() + " h");
        }
        refreshProfileAchievements();
        refreshCommunity();
    }

    private void refreshProfileAchievements() {
        if (profileAchievementsModel == null) {
            return;
        }
        profileAchievementsModel.setRowCount(0);
        for (PlatformAchievement achievement : state.getPlatformAchievements()) {
            profileAchievementsModel.addRow(new Object[]{
                    achievement.getCode(),
                    achievement.getName(),
                    achievement.getTier(),
                    state.getAchievementProgress(session.getCustomer(), achievement),
                    state.isAchievementUnlocked(session.getCustomer(), achievement) ? "Unlocked" : "Locked"
            });
        }
    }

    private void refreshTickets() {
        // Odswieza liste ticketow klienta.
        currentTickets.clear();
        currentTickets.addAll(state.getTicketsFor(session.getCustomer()));
        ticketsModel.setRowCount(0);
        for (Ticket t : currentTickets) {
            ticketsModel.addRow(new Object[]{
                    t.getId(),
                    t.getStatus(),
                    t.getTitle(),
                    t.getLastUpdated()
            });
        }
        if (ticketsModel.getRowCount() > 0) {
            ticketsTable.setRowSelectionInterval(0, 0);
        } else if (ticketDetails != null) {
            ticketDetails.setText("");
        }
    }

    // TICKETY: odswiezanie panelu szczegolow zgloszenia.
    private void updateTicketDetails() {
        if (ticketDetails == null) {
            return;
        }
        Ticket ticket = getSelectedTicket();
        if (ticket == null) {
            ticketDetails.setText("");
            return;
        }
        ticketDetails.setText(buildTicketDetails(ticket));
    }

    private Ticket getSelectedTicket() {
        if (ticketsTable == null) {
            return null;
        }
        int row = ticketsTable.getSelectedRow();
        if (row < 0 || row >= currentTickets.size()) {
            return null;
        }
        return currentTickets.get(row);
    }

    private String buildTicketDetails(Ticket ticket) {
        StringBuilder sb = new StringBuilder();
        sb.append("Id: ").append(ticket.getId()).append("\n");
        sb.append("Title: ").append(ticket.getTitle()).append("\n");
        sb.append("Status: ").append(ticket.getStatus()).append("\n");
        sb.append("Created: ").append(ticket.getCreatedAt()).append("\n");
        sb.append("Updated: ").append(ticket.getLastUpdated()).append("\n");
        sb.append("Description: ").append(ticket.getDescription()).append("\n");
        sb.append("\nMessages:\n");
        if (ticket.getMessages().isEmpty()) {
            sb.append("  (no replies yet)\n");
        } else {
            for (TicketMessage message : ticket.getMessages()) {
                sb.append("  [")
                        .append(message.getCreatedAt())
                        .append("] ")
                        .append(message.getAuthor())
                        .append(": ")
                        .append(message.getMessage())
                        .append("\n");
            }
        }
        if (ticket instanceof InProgressTicket ip) {
            sb.append("Assignee: ").append(ip.getAssignee()).append("\n");
        }
        if (ticket instanceof ClosedTicket closed) {
            sb.append("Resolved: ").append(closed.isSolved()).append("\n");
            sb.append("Resolution: ").append(closed.getResolutionSummary()).append("\n");
        }
        return sb.toString();
    }

    private void refreshAchievements() {
        achievementsGameModel.clear();
        for (Game g : state.getGames()) {
            achievementsGameModel.addElement(g);
        }
    }

    // TWORCA: lista opublikowanych gier i przychod.
    private void refreshPublished() {
        publishedModel.clear();
        IGamePublisher publisher = session.getPublisher();
        if (publisher != null) {
            for (Game g : publisher.getPublishedGames()) {
                publishedModel.addElement(g.getTitle());
            }
            revenueLabel.setText("Total revenue: " + formatPrice(publisher.getTotalRevenue()));
        }
    }

    // TWORCA: lista gier gotowych do publikacji.
    private void refreshPublishGames() {
        publishGameModel.removeAllElements();
        for (Game g : state.getPublishableGamesFor(session.getPublisher())) {
            publishGameModel.addElement(g);
        }
    }

    private void refreshBalance() {
        balanceLabel.setText("Balance: " + formatPrice(session.getCustomer().getBalance()));
    }

    private JLabel sectionTitle(String text) {
        JLabel label = new JLabel(text);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 14f));
        return label;
    }

    private DefaultTableModel createTableModel(String[] columns) {
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private JTextArea createDetailsArea() {
        JTextArea details = new JTextArea();
        details.setEditable(false);
        details.setLineWrap(true);
        details.setWrapStyleWord(true);
        details.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        details.setFont(details.getFont().deriveFont(DETAILS_FONT_SIZE));
        return details;
    }

    private String buildGameDetails(Game game) {
        // Buduje opis gry do panelu szczegolow.
        StringBuilder sb = new StringBuilder();
        sb.append("Title: ").append(game.getTitle()).append("\n");
        Studio studio = game.getStudio();
        sb.append("Studio: ").append(studio.getName()).append("\n");
        sb.append("Studio type: ").append(studio.isIndependent() ? "Independent" : "Corporate").append("\n");
        if (studio instanceof IndieStudio indie) {
            sb.append("Team size: ").append(indie.getTeamSize()).append("\n");
        }
        if (studio instanceof CorporationStudio corporation) {
            sb.append("Parent company: ").append(corporation.getParentCompany()).append("\n");
        }
        sb.append("Year: ").append(game.getReleaseYear()).append("\n");
        sb.append("Price: ").append(formatPrice(game.getPrice())).append("\n");
        sb.append("Platforms: ").append(formatPlatforms(game)).append("\n");
        sb.append("Playtime: ").append(session.getCustomer().getPlaytimeHours(game)).append(" h\n");
        double avg = game.getAverageRating();
        sb.append("Average rating: ").append(Double.isNaN(avg) ? "N/A" : formatScore(avg)).append("\n");
        sb.append("Achievements: ").append(game.getAchievements().size()).append("\n");
        for (Achievement a : game.getAchievements()) {
            sb.append("  - ").append(a.getName()).append("\n");
        }
        return sb.toString();
    }

    private String formatPlatforms(Game game) {
        return String.join(", ", game.getPlatforms().stream().map(Enum::name).toList());
    }

    private String formatPrice(double value) {
        return String.format(Locale.US, "%.2f", value);
    }

    private String formatScore(double value) {
        return String.format(Locale.US, "%.1f", value);
    }

    private String formatPromotion(Purchase purchase) {
        // Opis promocji wykorzystanej przy zakupie.
        if (purchase.getPercentagePromotion() != null) {
            return "Percent " + purchase.getPercentagePromotion().getPercentage() + "%";
        }
        if (purchase.getFixedAmountPromotion() != null) {
            return "Fixed " + formatPrice(purchase.getFixedAmountPromotion().getAmount());
        }
        return "None";
    }

    // Zapisuje zmiany do pliku ekstensji.
    private void persistState() {
        try {
            state.save();
        } catch (Exception ex) {
            showError("Save failed: " + ex.getMessage());
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(
                this,
                message,
                "Error",
                JOptionPane.ERROR_MESSAGE
        );
    }

    // SESJA: wylogowanie i powrot do okna logowania.
    private void onLogout() {
        // Wylogowanie i powrot do okna logowania.
        dispose();
        LoginDialog dialog = new LoginDialog(state);
        UserSession newSession = dialog.showDialog();
        if (newSession == null) {
            return;
        }
        if (newSession.isSupport()) {
            SupportFrame frame = new SupportFrame(state, newSession);
            frame.setVisible(true);
        } else {
            SteamFrame frame = new SteamFrame(state, newSession);
            frame.setVisible(true);
        }
    }
}
