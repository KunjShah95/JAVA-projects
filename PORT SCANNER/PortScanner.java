
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class PortScanner {

    private static final int DEFAULT_TIMEOUT = 1000; // Default timeout (1 second)

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.print("Enter the target IP address: ");
            final String ipAddress = scanner.nextLine();

            // Validate IP address format (IPv4 and IPv6)
            if (!isValidIPAddress(ipAddress)) {
                System.err.println("Invalid IP address format.");
                return;
            }

            System.out.print("Enter the starting port number: ");
            int portStart = scanner.nextInt();

            System.out.print("Enter the ending port number: ");
            int portEnd = scanner.nextInt();

            System.out.print("Enter timeout in milliseconds (default 1000 ms): ");
            final int inputTimeout = scanner.nextInt();
            final int timeout = inputTimeout > 0 ? inputTimeout : DEFAULT_TIMEOUT; // Use default timeout if invalid

            // Check if the target IP is reachable
            if (!isReachable(ipAddress, timeout)) {
                System.err.println("The IP address is not reachable.");
                return;
            }

            System.out.println("Starting port scan...");
            // Create a thread pool to scan ports concurrently
            ExecutorService executor = Executors.newFixedThreadPool(50); // Adjust number of threads as needed
            List<Future<Void>> futures = new ArrayList<>();

            // Scan ports concurrently using threads
            for (int port = portStart; port <= portEnd; port++) {
                final int currentPort = port;
                futures.add(executor.submit(() -> {
                    scanPort(ipAddress, currentPort, timeout);
                    return null;
                }));
            }

            // Wait for all tasks to finish
            for (Future<Void> future : futures) {
                try {
                    future.get(); // Block until the task completes
                } catch (InterruptedException | ExecutionException e) {
                    System.err.println("Error scanning port: " + e.getMessage());
                }
            }

            executor.shutdown();
            System.out.println("Port scan completed.");
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    // Check if IP address format is valid (both IPv4 and IPv6)
    private static boolean isValidIPAddress(String ipAddress) {
        try {
            InetAddress.getByName(ipAddress); // Will throw an exception if invalid
            return true;
        } catch (UnknownHostException e) {
            return false;
        }
    }

    // Check if the IP address is reachable
    private static boolean isReachable(String ipAddress, int timeout) {
        try {
            return InetAddress.getByName(ipAddress).isReachable(timeout);
        } catch (IOException e) {
            return false;
        }
    }

    // Scan a specific port and print the result
    private static void scanPort(String ipAddress, int port, int timeout) {
        try (Socket socket = new Socket()) {
            socket.setSoTimeout(timeout);
            socket.connect(new InetSocketAddress(ipAddress, port));
            System.out.println("Port " + port + " is open.");
        } catch (SocketTimeoutException e) {
            // Port is closed or filtered
        } catch (IOException e) {
            // Handle other network errors
        }
    }
}
