import database.DBConnection;
import ui.AuthFrame;

import javax.swing.SwingUtilities;

public class App {
    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeLater(() -> {
            try {
                DBConnection.testConnection();
                AuthFrame frame = new AuthFrame();
                frame.setVisible(true);
            } catch (Exception e) {
                System.err.println("Error starting application: " + e.getMessage());
                e.printStackTrace();
                DBConnection.closeConnection();
            }
        });
    }
}
