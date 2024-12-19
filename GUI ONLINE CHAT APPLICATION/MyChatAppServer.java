
import java.io.*;
import java.net.*;
import java.util.*;

public class MyChatAppServer {

    private static final int PORT = 9999;
    private static final Map<String, ClientHandler> clients = Collections.synchronizedMap(new HashMap<>());

    public static void main(String[] args) {
        System.out.println("Server starting...");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is running on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }

    static void broadcastMessage(String message, ClientHandler sender) {
        synchronized (clients) {
            for (ClientHandler client : clients.values()) {
                if (client != sender) {
                    client.sendMessage(message);
                }
            }
        }
    }

    static void sendPrivateMessage(String recipient, String message, ClientHandler sender) {
        ClientHandler client = clients.get(recipient);
        if (client != null) {
            client.sendMessage("[Private] " + sender.getUsername() + ": " + message);
        } else {
            sender.sendMessage("User " + recipient + " not found.");
        }
    }

    static void removeClient(ClientHandler clientHandler) {
        clients.remove(clientHandler.getUsername());
        broadcastMessage("** " + clientHandler.getUsername() + " has left the chat **", null);
    }

    static void addClient(String username, ClientHandler clientHandler) {
        clients.put(username, clientHandler);
        broadcastMessage("** " + username + " has joined the chat **", null);
    }
}

class ClientHandler implements Runnable {

    private final Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private String username;

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(clientSocket.getOutputStream(), true);

            username = in.readLine(); // First message is the username
            if (username == null || username.trim().isEmpty()) {
                clientSocket.close();
                return;
            }
            MyChatAppServer.addClient(username, this);

            String message;
            while ((message = in.readLine()) != null) {
                if (message.equalsIgnoreCase("end")) {
                    break;
                } else if (message.startsWith("@")) { // Private message
                    int spaceIndex = message.indexOf(' ');
                    if (spaceIndex > 1) {
                        String recipient = message.substring(1, spaceIndex);
                        String privateMessage = message.substring(spaceIndex + 1);
                        MyChatAppServer.sendPrivateMessage(recipient, privateMessage, this);
                    }
                } else {
                    MyChatAppServer.broadcastMessage(username + ": " + message, this);
                }
            }
        } catch (IOException e) {
            System.err.println("Error with client " + username + ": " + e.getMessage());
        } finally {
            MyChatAppServer.removeClient(this);
            closeResources();
        }
    }

    void sendMessage(String message) {
        out.println(message);
    }

    String getUsername() {
        return username;
    }

    private void closeResources() {
        try {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
            if (clientSocket != null) {
                clientSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing resources: " + e.getMessage());
        }
    }
}
