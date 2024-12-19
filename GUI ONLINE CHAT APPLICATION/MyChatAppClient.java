
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import javax.swing.*;

public class MyChatAppClient extends JFrame implements ActionListener {

    private static String serverName;

    public static String getServerName() {
        return serverName;
    }

    private final PrintWriter pw;
    private final BufferedReader br;
    private final String username;
    private JTextArea taMessages;
    private JTextField tfInput;
    private JButton btnSend, btnExit;
    private final Socket client;

    public MyChatAppClient(String username, String serverName) throws IOException {
        super("Chat - " + username); // Set the title of the JFrame
        this.username = username;
        this.client = new Socket(serverName, 12345); // Initialize the client socket
        br = new BufferedReader(new InputStreamReader(client.getInputStream()));
        pw = new PrintWriter(new OutputStreamWriter(client.getOutputStream()), true);

        // Send username to the server
        pw.println(username);

        // Build the graphical interface
        buildInterface();

        // Start the message thread
        startMessageThread();
    }

    private void buildInterface() {
        btnSend = new JButton("Send");
        btnExit = new JButton("Exit");
        taMessages = new JTextArea();
        taMessages.setRows(10);
        taMessages.setColumns(50);
        taMessages.setEditable(false);
        tfInput = new JTextField(50);
        JScrollPane sp = new JScrollPane(taMessages, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        add(sp, "Center");
        JPanel bp = new JPanel(new FlowLayout());
        bp.add(tfInput);
        bp.add(btnSend);
        bp.add(btnExit);
        add(bp, "South");

        // Add action listeners for buttons
        btnSend.addActionListener(this);
        btnExit.addActionListener(this);

        // Set JFrame properties
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
        pack();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnExit) {
            // Notify server of client termination and exit
            pw.println("end");
            closeResources();
            System.exit(0);
        } else if (e.getSource() == btnSend) {
            String message = tfInput.getText().trim();
            if (!message.isEmpty()) {
                if (message.startsWith("@private")) {
                    String[] parts = message.split(" ", 3);
                    if (parts.length >= 3) {
                        sendPrivateMessage(parts[1], parts[2]);
                    }
                } else if (message.startsWith("@file")) {
                    String[] parts = message.split(" ", 2);
                    if (parts.length >= 2) {
                        sendFile(parts[1]);
                    }
                } else if (message.equals("@online")) {
                    requestOnlineUsers();
                } else {
                    pw.println(addTimestamp(message));
                }
                tfInput.setText(""); // Clear input field after sending
            }
        }
    }

    // Method to send private messages
    private void sendPrivateMessage(String recipient, String message) {
        pw.println("@private " + recipient + " " + message);
    }

    // Method to request the list of online users
    private void requestOnlineUsers() {
        pw.println("@online");
    }

    // Method to send a file
    private void sendFile(String filePath) {
        // Implementation for file sharing
        // This is a placeholder and needs to be implemented
        pw.println("@file " + filePath);
    }

    // Method to add timestamps to messages
    private String addTimestamp(String message) {
        return "[" + java.time.LocalTime.now().toString() + "] " + message;
    }

    // Method to close resources
    private void closeResources() {
        try {
            if (pw != null) {
                pw.close();
            }
            if (br != null) {
                br.close();
            }
            if (client != null) {
                client.close();
            }
        } catch (IOException ex) {
            System.err.println("Error closing resources: " + ex.getMessage());
        }
    }

    private void connect() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    // MessageThread class to handle incoming messages
    class MessageThread extends Thread {

        @Override
        public void run() {
            try {
                String line;
                while ((line = br.readLine()) != null) {
                    final String finalLine = line;
                    if (finalLine.startsWith("@online")) {
                        // Handle online users list
                        String[] users = finalLine.substring(8).split(",");
                        SwingUtilities.invokeLater(() -> taMessages.append("Online users: " + String.join(", ", users) + "\n"));
                    } else if (finalLine.startsWith("@file")) {
                        // Handle file reception
                        // This is a placeholder and needs to be implemented
                        SwingUtilities.invokeLater(() -> taMessages.append("File received: " + finalLine.substring(6) + "\n"));
                        SwingUtilities.invokeLater(() -> taMessages.append(finalLine + "\n"));
                        SwingUtilities.invokeLater(() -> taMessages.append(finalLine + "\n"));
                    }
                }
            } catch (IOException e) {
                System.err.println("Error reading from server: " + e.getMessage());
            } finally {
                closeResources();
            }
        }
    }

    private void startMessageThread() {
        new MessageThread().start();
    }

    public static void main(String[] args) {
        String name = JOptionPane.showInputDialog(null, "Enter your name: ", "Username", JOptionPane.PLAIN_MESSAGE);
        if (name == null || name.trim().isEmpty()) {
            System.out.println("Username is required to join the chat.");
            return;
        }

        String localServerName = "localhost"; // Default server address
        try {
            MyChatAppClient client = new MyChatAppClient(name, localServerName);
            client.connect(); // Assuming there's a connect method to start the client
        } catch (IOException exception) {
            System.err.println("Error: " + exception.getMessage());
        }
    }

    public String getUsername() {
        return username;
    }
}
