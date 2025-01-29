
import java.awt.BorderLayout;
import java.io.IOException;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
/**
 *
 * @author birkanegee
 */
public class GUI extends JFrame{
    private JTextArea messageArea;
    private JTextField textField;
    private JButton sendRequestButton, exitButton;
    private Client client;

    public GUI() {
        super("ChatApp");
        setSize(400, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        messageArea = new JTextArea();
        messageArea.setEditable(false);
        add(new JScrollPane(messageArea), BorderLayout.CENTER);

        String name = JOptionPane.showInputDialog(this, "Enter your name:", "Name Entry", JOptionPane.PLAIN_MESSAGE);
        this.setTitle("ChatApp - " + name);

        textField = new JTextField();
        textField.addActionListener(e -> {
            String message = textField.getText().trim();
            if (!message.isEmpty()) {
                client.sendMessage(message); 
                messageArea.append("Me: " + message + "\n");
                textField.setText("");
            } else {
                messageArea.append("No active chat session. Start a new chat or wait for acceptance.\n");
            }
        });

        sendRequestButton = new JButton("Send Chat Request");
        sendRequestButton.addActionListener(e -> {
            String targetNickname = JOptionPane.showInputDialog(this, "Enter the nickname of the person you want to chat with:", 
                "Chat Request", JOptionPane.PLAIN_MESSAGE);
            if (targetNickname != null && !targetNickname.trim().isEmpty()) {
                if (targetNickname.trim().equalsIgnoreCase(name)) { 
                    JOptionPane.showMessageDialog(this, "You cannot send a chat request to yourself!", 
                            "Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    client.sendChatRequest(targetNickname.trim());
                }
            }
        });

        exitButton = new JButton("Exit");
        exitButton.addActionListener(e -> {
            try {
                client.closeConnection();
                System.exit(0);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(textField, BorderLayout.CENTER);
        bottomPanel.add(sendRequestButton, BorderLayout.WEST);
        bottomPanel.add(exitButton, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);

        try {
            this.client = new Client("127.0.0.1", 8000, this::onMessageReceived, name);
            client.startClient();
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error connecting to the server", "Connection Error",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    private void onMessageReceived(String message) {
        SwingUtilities.invokeLater(() -> {
            if (message.startsWith("/request:")) {
                String requester = message.substring(9);
                int response = JOptionPane.showConfirmDialog(this, requester + " wants to chat with you. Accept?",
                        "Chat Request", JOptionPane.YES_NO_OPTION);
                if (response == JOptionPane.YES_OPTION) {
                    client.acceptChat(requester);
                } else {
                    client.rejectChat(requester);
                }
            } else if (message.startsWith("/accepted:")) {
                String acceptedBy = message.substring(10);
                messageArea.append("Chat request accepted by " + acceptedBy + ". You can now chat privately.\n");
            } else if (message.startsWith("/rejected:")) {
                String rejectedBy = message.substring(10);
                messageArea.append("Chat request rejected by " + rejectedBy + ".\n");
            } else {
                messageArea.append(message + "\n");
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GUI().setVisible(true));
    }
}
