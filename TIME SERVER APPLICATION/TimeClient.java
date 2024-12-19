
import java.io.*;
import java.net.*;

public class TimeClient {

    public static void main(String[] args) {
        try {
            // Ask the user for server details
            BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
            System.out.print("Enter server address: ");
            String serverAddress = userInput.readLine();

            System.out.print("Enter server port: ");
            int port = Integer.parseInt(userInput.readLine());

            try (Socket socket = new Socket(serverAddress, port); BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream())); PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

                System.out.println("Connected to the Time Server!");

                // Print server's welcome message
                String serverMessage;
                while ((serverMessage = in.readLine()) != null) {
                    System.out.println(serverMessage);

                    // Exit the loop if server says goodbye
                    if (serverMessage.equalsIgnoreCase("Goodbye!")) {
                        break;
                    }

                    // Send a command to the server
                    System.out.print("Enter command: ");
                    String command = userInput.readLine();
                    out.println(command);
                }
            }
        } catch (IOException | NumberFormatException e) {
            System.out.println("Client error: " + e.getMessage());
        }
    }
}
