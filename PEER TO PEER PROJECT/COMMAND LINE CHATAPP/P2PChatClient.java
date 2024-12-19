
import java.io.*;
import java.net.*;

public class P2PChatClient {

    private static final String SERVER_IP = "localhost"; // Replace with server IP if needed
    private static final int SERVER_PORT = 5000;        // Same port as server

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_IP, SERVER_PORT); BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream())); PrintWriter out = new PrintWriter(socket.getOutputStream(), true); BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in))) {

            // Read the welcome message and nickname prompt
            System.out.println(in.readLine());
            System.out.print("Enter your nickname: ");
            String nickname = userInput.readLine();
            out.println(nickname);

            System.out.println("Connected to the chat server. Type your messages:");

            // Thread to read messages from server
            new Thread(() -> {
                try {
                    String serverMessage;
                    while ((serverMessage = in.readLine()) != null) {
                        System.out.println(serverMessage);
                    }
                } catch (IOException e) {
                    System.err.println("Connection closed by the server.");
                }
            }).start();

            // Send messages to server
            String userMessage;
            while ((userMessage = userInput.readLine()) != null) {
                out.println(userMessage);
                if ("exit".equalsIgnoreCase(userMessage)) {
                    System.out.println("Exiting chat...");
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("Client Error: " + e.getMessage());
        }
    }
}
