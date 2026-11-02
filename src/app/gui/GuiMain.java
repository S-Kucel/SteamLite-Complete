package app.gui;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public final class GuiMain {
    private GuiMain() {}

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                AppData data = AppDataLoader.load();
                AppState state = new AppState(data);
                LoginDialog dialog = new LoginDialog(state);
                UserSession session = dialog.showDialog();
                if (session == null) {
                    return;
                }
                if (session.isSupport()) {
                    SupportFrame frame = new SupportFrame(state, session);
                    frame.setVisible(true);
                } else {
                    SteamFrame frame = new SteamFrame(state, session);
                    frame.setVisible(true);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(
                        null,
                        "Failed to start GUI: " + ex.getMessage(),
                        "GUI error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });
    }
}
