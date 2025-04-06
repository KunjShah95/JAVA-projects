import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class P2PChatServer {

    private static final int SERVER_PORT = 5000; // Fixed port
    private static final Set<ClientHandler> clientHandlers = ConcurrentHashMap.newKeySet(); // Thread-safe set of clients

    public static void main(String[] args) {
        System.out.println("Starting P2P Chat Server on port " + SERVER_PORT);
        try (ServerSocket serverSocket = new ServerSocket(SERVER_PORT)) {
            System.out.println("Server is listening on port " + SERVER_PORT);

            // Accept multiple clients
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress().getHostAddress());

                ClientHandler clientHandler = new ClientHandler(clientSocket);
                clientHandlers.add(clientHandler);
                new Thread(clientHandler).start(); // Handle client in a new thread
            }
        } catch (IOException e) {
            System.err.println("Server Error: " + e.getMessage());
        }
    }

    // Broadcast message to all connected clients
    private static void broadcastMessage(String message, ClientHandler sender) {
        for (ClientHandler client : clientHandlers) {
            if (client != sender) { // Avoid echoing back to the sender
                client.sendMessage(message);
            }
        }
    }

    // ClientHandler class to manage individual client connections
    private static class ClientHandler implements Runnable {

        private final Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private String nickname;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                // Set up streams for communication
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                // Get nickname from client
                out.println("Enter your nickname: ");
                nickname = in.readLine();
                System.out.println(nickname + " has joined the chat!");
                broadcastMessage(nickname + " has joined the chat!", this);

                // Handle client messages
                String clientMessage;
                while ((clientMessage = in.readLine()) != null) {
                    if ("exit".equalsIgnoreCase(clientMessage)) {
                        System.out.println(nickname + " has left the chat.");
                        broadcastMessage(nickname + " has left the chat.", this);
                        break;
                    }
                    String encryptedMessage = encryptMessage(clientMessage);
                    System.out.println("Encrypted message from " + nickname + ": " + encryptedMessage);
                    broadcastMessage(nickname + ": " + decryptMessage(encryptedMessage), this);
                }
            } catch (IOException e) {
                System.err.println("Error handling client: " + e.getMessage());
            } finally {
                closeConnection();
            }
        }

        // Send a message to this client
        public void sendMessage(String message) {
            out.println(message);
        }

        // Close client connection
        private void closeConnection() {
            try {
                clientHandlers.remove(this);
                if (socket != null) {
                    socket.close();
                }
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }

        // Basic encryption (Caesar cipher)
        private String encryptMessage(String message) {
            StringBuilder encrypted = new StringBuilder();
            for (char c : message.toCharArray()) {
                encrypted.append((char) (c + 3)); // Shift each character by 3
            }
            return encrypted.toString();
        }

        // Basic decryption (Caesar cipher)
        private String decryptMessage(String message) {
            StringBuilder decrypted = new StringBuilder();
            for (char c : message.toCharArray()) {
                decrypted.append((char) (c - 3)); // Shift each character back by 3
            }
            return decrypted.toString();
        }
    }
}