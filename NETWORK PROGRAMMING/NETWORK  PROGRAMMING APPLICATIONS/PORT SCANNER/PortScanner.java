
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class PortScanner {

    public static void main(String[] args) {
        // Create Scanner object to take user input
        try (Scanner scanner = new Scanner(System.in)) {

            // Input: Target IP address
            System.out.print("Enter the target IP address: ");
            String targetIP = scanner.nextLine();

            // Input: Starting and Ending Port Range
            System.out.print("Enter the starting port number: ");
            int startPort = scanner.nextInt();

            System.out.print("Enter the ending port number: ");
            int endPort = scanner.nextInt();

            System.out.println("\nScanning ports on target: " + targetIP);
            System.out.println("Scanning range: " + startPort + " to " + endPort);

            // Start port scanning
            for (int port = startPort; port <= endPort; port++) {
                scanPort(targetIP, port);
            }
        } // Scanner will be closed automatically here
    }

    // Method to check if a port is open or closed
    public static void scanPort(String targetIP, int port) {
        try (Socket socket = new Socket()) {
            // Try to connect to the target IP and port
            socket.connect(new java.net.InetSocketAddress(targetIP, port), 2000);
            System.out.println("Port " + port + " is OPEN");
        } catch (IOException e) {
            // If unable to connect, the port is closed
            System.out.println("Port " + port + " is CLOSED");
        }
    }
}
