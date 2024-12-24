
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.regex.Pattern;

public class getting_ip_from_domain {

    private static final Pattern DOMAIN_NAME_PATTERN
            = Pattern.compile("^(?!-)[A-Za-z0-9-]{1,63}(?<!-)(\\.[A-Za-z]{2,})+$");

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            boolean continueRunning = true;
            while (continueRunning) {
                System.out.print("Enter the domain name: ");
                String domainName = scanner.nextLine().trim(); // Take input from the user

                if (!isValidDomainName(domainName)) {
                    System.err.println("Error: Invalid domain name format. Please try again.");
                    continue; // Skip to the next iteration
                }

                try {
                    // Get the InetAddress object for the domain
                    InetAddress[] inetAddresses = InetAddress.getAllByName(domainName);

                    // Print the details for each IP address
                    System.out.println("Details for domain: " + domainName);
                    for (InetAddress inetAddress : inetAddresses) {
                        System.out.println("Host Name: " + inetAddress.getHostName());
                        System.out.println("Host Address: " + inetAddress.getHostAddress());
                        System.out.println("Canonical Host Name: " + inetAddress.getCanonicalHostName());
                        System.out.println();
                    }
                } catch (UnknownHostException e) {
                    System.err.println("Error: Could not resolve the domain name. Please check if it exists.");
                }

                // Ask the user if they want to try another domain
                System.out.print("Do you want to try another domain? (yes/no): ");
                String response = scanner.nextLine().trim().toLowerCase();
                continueRunning = response.equals("yes");
            }
            // Close the scanner
        }
    }

    private static boolean isValidDomainName(String domainName) {
        return DOMAIN_NAME_PATTERN.matcher(domainName).matches();
    }
}
