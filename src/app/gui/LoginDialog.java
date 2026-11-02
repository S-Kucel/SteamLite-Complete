package app.gui;

import core.Customer;
import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

// Modalne okno logowania/rejestracji zwracajace UserSession.
public final class LoginDialog extends JDialog {
    private final AppState state;
    private UserSession session;

    private JTextField loginUsername;
    private JPasswordField loginPassword;
    private JButton loginButton;
    private JRadioButton loginCustomer;
    private JRadioButton loginSupport;

    private JTextField regUsername;
    private JPasswordField regPassword;
    private JTextField regAge;
    private JTextField regDevName;
    private JRadioButton regCustomer;
    private JRadioButton regCreator;
    private JRadioButton regSupport;

    public LoginDialog(AppState state) {
        super((Dialog) null, "Login / Register", true);
        this.state = state;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        add(buildTabs(), BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);
        pack();
        if (getRootPane() != null && loginButton != null) {
            getRootPane().setDefaultButton(loginButton);
        }
        setLocationRelativeTo(null);
    }

    public UserSession showDialog() {
        setVisible(true);
        return session;
    }

    // Buduje zakladki logowania i rejestracji.
    private JTabbedPane buildTabs() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Login", buildLoginTab());
        tabs.addTab("Register", buildRegisterTab());
        return tabs;
    }

    // Formularz logowania uzywa GridBagLayout.
    private JPanel buildLoginTab() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4, 4, 4, 4);
        gc.anchor = GridBagConstraints.WEST;

        gc.gridx = 0;
        gc.gridy = 0;
        panel.add(new JLabel("Username:"), gc);

        gc.gridx = 1;
        loginUsername = new JTextField(16);
        panel.add(loginUsername, gc);

        gc.gridx = 0;
        gc.gridy = 1;
        panel.add(new JLabel("Password:"), gc);

        gc.gridx = 1;
        loginPassword = new JPasswordField(16);
        panel.add(loginPassword, gc);

        gc.gridx = 0;
        gc.gridy = 2;
        panel.add(new JLabel("Account type:"), gc);

        gc.gridx = 1;
        loginCustomer = new JRadioButton("Customer", true);
        loginSupport = new JRadioButton("Tech Support");
        ButtonGroup loginGroup = new ButtonGroup();
        loginGroup.add(loginCustomer);
        loginGroup.add(loginSupport);

        JPanel types = new JPanel(new BorderLayout());
        types.add(loginCustomer, BorderLayout.NORTH);
        types.add(loginSupport, BorderLayout.SOUTH);
        panel.add(types, gc);

        gc.gridx = 1;
        gc.gridy = 3;
        gc.anchor = GridBagConstraints.EAST;
        loginButton = new JButton(new AbstractAction("Login") {
            @Override
            public void actionPerformed(ActionEvent e) {
                onLogin();
            }
        });
        panel.add(loginButton, gc);
        return panel;
    }

    // Formularz rejestracji z wyborem roli.
    private JPanel buildRegisterTab() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4, 4, 4, 4);
        gc.anchor = GridBagConstraints.WEST;

        gc.gridx = 0;
        gc.gridy = 0;
        panel.add(new JLabel("Username:"), gc);

        gc.gridx = 1;
        regUsername = new JTextField(16);
        panel.add(regUsername, gc);

        gc.gridx = 0;
        gc.gridy = 1;
        panel.add(new JLabel("Password:"), gc);

        gc.gridx = 1;
        regPassword = new JPasswordField(16);
        panel.add(regPassword, gc);

        gc.gridx = 0;
        gc.gridy = 2;
        panel.add(new JLabel("Age:"), gc);

        gc.gridx = 1;
        regAge = new JTextField(6);
        panel.add(regAge, gc);

        gc.gridx = 0;
        gc.gridy = 3;
        panel.add(new JLabel("Account type:"), gc);

        gc.gridx = 1;
        regCustomer = new JRadioButton("Customer", true);
        regCreator = new JRadioButton("Creator (Developer)");
        regSupport = new JRadioButton("Tech Support");
        ButtonGroup group = new ButtonGroup();
        group.add(regCustomer);
        group.add(regCreator);
        group.add(regSupport);

        JPanel types = new JPanel(new BorderLayout());
        types.add(regCustomer, BorderLayout.NORTH);
        JPanel secondRow = new JPanel(new BorderLayout());
        secondRow.add(regCreator, BorderLayout.NORTH);
        secondRow.add(regSupport, BorderLayout.SOUTH);
        types.add(secondRow, BorderLayout.SOUTH);
        panel.add(types, gc);

        gc.gridx = 0;
        gc.gridy = 4;
        panel.add(new JLabel("Developer name:"), gc);

        gc.gridx = 1;
        regDevName = new JTextField(16);
        panel.add(regDevName, gc);

        gc.gridx = 1;
        gc.gridy = 5;
        gc.anchor = GridBagConstraints.EAST;
        JButton registerButton = new JButton(new AbstractAction("Register") {
            @Override
            public void actionPerformed(ActionEvent e) {
                onRegister();
            }
        });
        panel.add(registerButton, gc);

        regCustomer.addActionListener(e -> updateRegisterFields());
        regCreator.addActionListener(e -> updateRegisterFields());
        regSupport.addActionListener(e -> updateRegisterFields());
        updateRegisterFields();
        return panel;
    }

    private JPanel buildFooter() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(4, 12, 8, 12));
        JButton cancel = new JButton(new AbstractAction("Cancel") {
            @Override
            public void actionPerformed(ActionEvent e) {
                session = null;
                dispose();
            }
        });
        panel.add(cancel, BorderLayout.EAST);
        return panel;
    }

    // Obsluguje logowanie klienta lub supportu.
    private void onLogin() {
        String username = loginUsername.getText().trim();
        String password = new String(loginPassword.getPassword());
        try {
            if (loginSupport != null && loginSupport.isSelected()) {
                session = state.loginSupport(username, password);
            } else {
                session = state.login(username, password);
            }
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this,
                    extractMessage(ex),
                    "Login failed",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // Obsluguje rejestracje (klient/tworca/support).
    private void onRegister() {
        String username = regUsername.getText().trim();
        String password = new String(regPassword.getPassword());
        String ageText = regAge.getText().trim();
        boolean creator = regCreator.isSelected();
        boolean support = regSupport.isSelected();
        String devName = regDevName.getText().trim();

        int age;
        try {
            age = Integer.parseInt(ageText);
        } catch (NumberFormatException ex) {
            // Wiek musi byc liczba.
            JOptionPane.showMessageDialog(
                    this,
                    "Age must be a number.",
                    "Register failed",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        try {
            if (support) {
                session = new UserSession(state.registerSupport(username, password, age));
            } else {
                Customer customer = state.registerCustomer(username, password, age, creator, devName);
                session = new UserSession(customer);
            }
            persistState();
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this,
                    extractMessage(ex),
                    "Register failed",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // Pole nazwy dewelopera aktywne tylko dla tworcow.
    private void updateRegisterFields() {
        if (regDevName != null) {
            regDevName.setEnabled(regCreator != null && regCreator.isSelected());
        }
    }

    // Zapis danych po udanej rejestracji.
    private void persistState() {
        try {
            state.save();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Save failed: " + extractMessage(ex),
                    "Save failed",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private String extractMessage(Exception ex) {
        if (ex.getCause() != null && ex.getCause().getMessage() != null) {
            return ex.getCause().getMessage();
        }
        return ex.getMessage();
    }
}
