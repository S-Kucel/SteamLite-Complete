package app.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;
import ticket.ClosedTicket;
import ticket.InProgressTicket;
import ticket.OpenTicket;
import ticket.Ticket;
import ticket.TicketMessage;

// Panel supportu do przegladania i obslugi ticketow.
public final class SupportFrame extends JFrame {
    private final AppState state;
    private final UserSession session;
    private final List<Ticket> currentTickets = new ArrayList<>();

    private DefaultTableModel ticketsModel;
    private JTable ticketsTable;
    private JTextArea detailsArea;

    public SupportFrame(AppState state, UserSession session) {
        this.state = state;
        this.session = session;
        setSystemLookAndFeel();
        setTitle("SteamLite - Support");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(900, 600));
        setSize(1100, 700);
        setLocationRelativeTo(null);
        add(buildContent(), BorderLayout.CENTER);
        add(buildStatusBar(), BorderLayout.SOUTH);
    }

    private void setSystemLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }
    }

    // Tabela ticketow + panel szczegolow.
    private JPanel buildContent() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        panel.add(sectionTitle("Support tickets"), BorderLayout.NORTH);

        ticketsModel = createTableModel(new String[]{"Id", "Status", "Title", "Reporter", "Updated"});
        ticketsTable = new JTable(ticketsModel);
        ticketsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        detailsArea = new JTextArea();
        detailsArea.setEditable(false);
        detailsArea.setLineWrap(true);
        detailsArea.setWrapStyleWord(true);
        detailsArea.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        // Zmiana zaznaczenia odswieza szczegoly.
        ticketsTable.getSelectionModel().addListSelectionListener(e -> {
            int row = ticketsTable.getSelectedRow();
            if (row >= 0 && row < currentTickets.size()) {
                detailsArea.setText(buildTicketDetails(currentTickets.get(row)));
            }
        });

        refreshTickets();

        JPanel center = new JPanel(new BorderLayout(8, 8));
        center.add(new JScrollPane(ticketsTable), BorderLayout.CENTER);
        center.add(new JScrollPane(detailsArea), BorderLayout.EAST);

        panel.add(center, BorderLayout.CENTER);
        panel.add(buildActions(), BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildActions() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton start = new JButton("Start processing");
        start.addActionListener(e -> onStartTicket());

        JButton reply = new JButton("Reply");
        reply.addActionListener(e -> onReplyTicket());

        JButton close = new JButton("Close ticket");
        close.addActionListener(e -> onCloseTicket());

        JButton refresh = new JButton("Refresh");
        refresh.addActionListener(e -> refreshTickets());

        panel.add(refresh);
        panel.add(start);
        panel.add(reply);
        panel.add(close);
        return panel;
    }

    private JPanel buildStatusBar() {
        JPanel status = new JPanel(new BorderLayout());
        status.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        String text = "Logged in: " + session.getSupport().getUsername() + " (TechSupport)";
        status.add(new JLabel(text), BorderLayout.WEST);
        JButton logout = new JButton("Logout");
        logout.addActionListener(e -> onLogout());
        status.add(logout, BorderLayout.EAST);
        return status;
    }

    // Przejscie OPEN -> IN_PROGRESS.
    private void onStartTicket() {
        Ticket ticket = getSelectedTicket();
        if (ticket == null) {
            showError("Select a ticket to start.");
            return;
        }
        if (!(ticket instanceof OpenTicket)) {
            showError("Only open tickets can be started.");
            return;
        }
        try {
            state.startTicket(ticket, session.getSupport());
            refreshTickets();
            persistState();
        } catch (Exception ex) {
            showError("Start failed: " + ex.getMessage());
        }
    }

    // Zamkniecie ticketa lub dopisanie rozwiazania.
    private void onCloseTicket() {
        Ticket ticket = getSelectedTicket();
        if (ticket == null) {
            showError("Select a ticket to close.");
            return;
        }
        if (!(ticket instanceof InProgressTicket)) {
            showError("Only in-progress tickets can be closed.");
            return;
        }

        JTextField resolutionField = new JTextField(24);
        JCheckBox solvedBox = new JCheckBox("Solved", true);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4, 4, 4, 4);
        gc.anchor = GridBagConstraints.WEST;
        gc.gridx = 0;
        gc.gridy = 0;
        panel.add(new JLabel("Message:"), gc);
        gc.gridx = 1;
        panel.add(resolutionField, gc);
        gc.gridx = 1;
        gc.gridy = 1;
        panel.add(solvedBox, gc);

        int result = JOptionPane.showConfirmDialog(
                this,
                panel,
                "Close ticket",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );
        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        String message = resolutionField.getText().trim();
        if (message.isBlank()) {
            showError("Message is required.");
            return;
        }

        try {
            if (solvedBox.isSelected()) {
                state.closeTicket(ticket, message, true);
            } else {
                state.addTicketMessage(ticket, session.getSupport().getUsername(), message);
            }
            refreshTickets();
            persistState();
        } catch (Exception ex) {
            showError("Close failed: " + ex.getMessage());
        }
    }

    // Odpowiedz do ticketu w toku.
    private void onReplyTicket() {
        Ticket ticket = getSelectedTicket();
        if (ticket == null) {
            showError("Select a ticket to reply.");
            return;
        }
        if (!(ticket instanceof InProgressTicket)) {
            showError("Start the ticket before replying.");
            return;
        }

        JTextField messageField = new JTextField(24);
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4, 4, 4, 4);
        gc.anchor = GridBagConstraints.WEST;
        gc.gridx = 0;
        gc.gridy = 0;
        panel.add(new JLabel("Message:"), gc);
        gc.gridx = 1;
        panel.add(messageField, gc);

        int result = JOptionPane.showConfirmDialog(
                this,
                panel,
                "Reply to ticket",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );
        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        String message = messageField.getText().trim();
        if (message.isBlank()) {
            showError("Message is required.");
            return;
        }

        try {
            state.addTicketMessage(ticket, session.getSupport().getUsername(), message);
            refreshTickets();
            persistState();
        } catch (Exception ex) {
            showError("Reply failed: " + ex.getMessage());
        }
    }

    private Ticket getSelectedTicket() {
        int row = ticketsTable.getSelectedRow();
        if (row < 0 || row >= currentTickets.size()) {
            return null;
        }
        return currentTickets.get(row);
    }

    // Odswieza wiersze tabeli z listy ticketow.
    private void refreshTickets() {
        currentTickets.clear();
        currentTickets.addAll(state.getTickets());
        ticketsModel.setRowCount(0);
        for (Ticket t : currentTickets) {
            ticketsModel.addRow(new Object[]{
                    t.getId(),
                    t.getStatus(),
                    t.getTitle(),
                    t.getReporter(),
                    t.getLastUpdated()
            });
        }
        if (ticketsModel.getRowCount() > 0) {
            ticketsTable.setRowSelectionInterval(0, 0);
        }
    }

    // Buduje tekst szczegolow z ticketa i jego wiadomosci.
    private String buildTicketDetails(Ticket ticket) {
        StringBuilder sb = new StringBuilder();
        sb.append("Id: ").append(ticket.getId()).append("\n");
        sb.append("Title: ").append(ticket.getTitle()).append("\n");
        sb.append("Reporter: ").append(ticket.getReporter()).append("\n");
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

    private JLabel sectionTitle(String text) {
        JLabel label = new JLabel(text);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 14f));
        return label;
    }

    // Model tabeli z komorkami tylko do odczytu.
    private DefaultTableModel createTableModel(String[] columns) {
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    // Zapisuje ekstensje na dysk.
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

    private void onLogout() {
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
