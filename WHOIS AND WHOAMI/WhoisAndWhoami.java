
import java.io.*;
import java.net.*;
import java.util.Enumeration;
import java.util.regex.Pattern;

public class WhoisAndWhoami {

    // Perform a WHOIS query for a given domain
    public static void performWhois(String domain) {
        String whoisServer = "whois.iana.org"; // Default WHOIS server

        try (Socket socket = new Socket(whoisServer, 43); OutputStream output = socket.getOutputStream(); InputStream input = socket.getInputStream()) {

            // Send the domain query to the WHOIS server
            output.write((domain + "\r\n").getBytes());
            output.flush();

            // Read and print the WHOIS server response
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            String line;
            System.out.println("WHOIS Information for " + domain + ":\n");
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            System.err.println("Error performing WHOIS query: " + e.getMessage());
        }
    }

    // Retrieve and display the local machine's detailed network information
    public static void performWhoami() {
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            System.out.println("Whoami Information:");
            System.out.println("Hostname: " + localHost.getHostName());
            System.out.println("IP Address: " + localHost.getHostAddress());

            // Fetch all network interfaces and their details
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            System.out.println("\nNetwork Interfaces:");

            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();

                // Skip loopback and non-active interfaces
                if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                    continue;
                }

                System.out.println("Interface Name: " + networkInterface.getName());
                System.out.println("Display Name: " + networkInterface.getDisplayName());

                Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    InetAddress inetAddress = inetAddresses.nextElement();
                    String ipType = inetAddress instanceof Inet4Address ? "IPv4" : inetAddress instanceof Inet6Address ? "IPv6" : "Unknown";
                    if (inetAddress != null) {
                        System.out.println("  " + ipType + " Address: " + inetAddress.getHostAddress());
                    }

                    // Optionally perform reverse DNS lookup
                    try {
                        if (inetAddress != null) {
                            System.out.println("  Hostname (Reverse DNS): " + inetAddress.getCanonicalHostName());
                        } else {
                            System.out.println("  Hostname: N/A");
                        }
                    } catch (Exception e) {
                        System.out.println("  Hostname: N/A");
                    }
                }

                System.out.println("MAC Address: " + getMacAddress(networkInterface));
                System.out.println();
            }
        } catch (UnknownHostException e) {
            System.err.println("Error retrieving local machine information: " + e.getMessage());
        } catch (SocketException e) {
            System.err.println("Error accessing network interface details: " + e.getMessage());
        }
    }

    // Helper method to retrieve the MAC address
    private static String getMacAddress(NetworkInterface networkInterface) {
        try {
            byte[] mac = networkInterface.getHardwareAddress();
            if (mac == null) {
                return "N/A";
            }

            StringBuilder macAddress = new StringBuilder();
            for (byte b : mac) {
                macAddress.append(String.format("%02X:", b));
            }
            // Remove the trailing colon
            if (macAddress.length() > 0) {
                macAddress.setLength(macAddress.length() - 1);
            }
            return macAddress.toString();
        } catch (SocketException e) {
            return "Error retrieving MAC address";
        }
    }

    // Validate domain name format
    public static boolean isValidDomain(String domain) {
        String domainRegex = "^(?!-)[A-Za-z0-9-]{1,63}(?<!-)\\.[A-Za-z]{2,6}$";
        Pattern pattern = Pattern.compile(domainRegex);
        return pattern.matcher(domain).matches();
    }

    // Display help menu
    public static void displayHelp() {
        System.out.println("\nHelp Menu:");
        System.out.println("1. Perform WHOIS query: Enter a valid domain name to get WHOIS information.");
        System.out.println("2. Perform WHOAMI query: Retrieve the local machine's hostname, IP address, and detailed network information.");
        System.out.println("3. Display Help: Show instructions on using this program.");
        System.out.println("4. Exit: Exit the program.");
        System.out.println("Note: For WHOIS queries, use proper domain names (e.g., example.com). WHOAMI displays details for IPv4 and IPv6.");
    }

    public static void main(String[] args) {
        BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));

        while (true) {
            System.out.println("\nChoose an option:");
            System.out.println("1. Perform WHOIS query");
            System.out.println("2. Perform WHOAMI query");
            System.out.println("3. Display Help");
            System.out.println("4. Exit");

            try {
                System.out.print("Enter your choice: ");
                String choice = consoleReader.readLine();

                switch (choice) {
                    case "1" -> {
                        System.out.print("Enter domain name: ");
                        String domain = consoleReader.readLine();
                        if (isValidDomain(domain)) {
                            performWhois(domain);
                        } else {
                            System.out.println("Invalid domain name format. Please try again.");
                        }
                    }
                    case "2" ->
                        performWhoami();
                    case "3" ->
                        displayHelp();
                    case "4" -> {
                        System.out.println("Exiting...");
                        return;
                    }
                    default ->
                        System.out.println("Invalid choice. Please try again.");
                }
            } catch (IOException e) {
                System.err.println("Error reading input: " + e.getMessage());
            }
        }
    }
}
