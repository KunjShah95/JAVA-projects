import java.io.IOException;
import java.net.InetAddress;
import java.util.Scanner;

public class PingUtility {
    public static void main(String[] args) {
        // Create a Scanner object to take input from the user
        Scanner scanner = new Scanner(System.in);

        // Ask the user to enter the domain name or IP address
        System.out.print("Enter the domain name or IP address to ping: ");
        String host = scanner.nextLine();

        try {
            // Get the InetAddress object for the specified host
            InetAddress inetAddress = InetAddress.getByName(host);

            // Try to ping the host
            long startTime = System.currentTimeMillis();  // Start time of ping
            boolean reachable = inetAddress.isReachable(5000);  // Timeout of 5 seconds

            // Calculate the round-trip time (RTT)
            long endTime = System.currentTimeMillis();  // End time of ping
            long roundTripTime = endTime - startTime;

            if (reachable) {
                System.out.println("Ping successful!");
                System.out.println("Round-trip time: " + roundTripTime + "ms");
            } else {
                System.out.println("Ping failed! Host is unreachable.");
            }
        } catch (IOException e) {
            System.err.println("Error: Unable to ping the host.");
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }
}
