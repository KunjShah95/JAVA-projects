
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeServer {

    public static void main(String[] args) {
        try {
            // Ask the user to input the port number
            System.out.print("Enter port number: ");
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            int port = Integer.parseInt(br.readLine());

            // Start the server socket
            try (ServerSocket serverSocket = new ServerSocket(port)) {
                System.out.println("Time Server is running on port " + port);

                while (true) {
                    // Accept new client connections
                    Socket clientSocket = serverSocket.accept();
                    new ClientHandler(clientSocket).start();
                }
            }
        } catch (IOException | NumberFormatException e) {
            System.out.println("Server error: " + e.getMessage());
        }
    }
}

class ClientHandler extends Thread {

    private final Socket clientSocket;

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
    }

    @Override
    public void run() {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream())); PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {
            // Send a welcome message
            out.println("Welcome to the Time Server! Available commands:");
            out.println("  1. TIME - Get the current time (HH:mm:ss)");
            out.println("  2. DATE - Get the current date (yyyy-MM-dd)");
            out.println("  3. DATETIME - Get both date and time");
            out.println("  4. EXIT - Disconnect from the server");

            String clientMessage;
            while ((clientMessage = in.readLine()) != null) {
                if (clientMessage.equalsIgnoreCase("TIME")) {
                    String currentTime = new SimpleDateFormat("HH:mm:ss").format(new Date());
                    out.println("Current Time: " + currentTime);
                } else if (clientMessage.equalsIgnoreCase("DATE")) {
                    String currentDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
                    out.println("Current Date: " + currentDate);
                } else if (clientMessage.equalsIgnoreCase("DATETIME")) {
                    String currentDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                    out.println("Current Date and Time: " + currentDateTime);
                } else if (clientMessage.equalsIgnoreCase("EXIT")) {
                    out.println("Goodbye!");
                    break;
                } else {
                    out.println("Invalid command. Please use TIME, DATE, DATETIME, or EXIT.");
                }
            }
        } catch (IOException e) {
            System.out.println("Error handling client: " + e.getMessage());
        }
    }
}
