
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class WebsiteDownloader {

    public static void main(String[] args) {
        try ( // Create a Scanner object to take input from the user
                Scanner scanner = new Scanner(System.in)) {
            System.out.print("Enter the URL to download HTML: ");
            String url = scanner.nextLine(); // Read the URL input from the user
            StringBuilder htmlContent = new StringBuilder();
            // Create a thread to download the HTML
            Thread downloadThread = new Thread(() -> {
                try {
                    // Create a URL object
                    URL website = new URL(url);
                    HttpURLConnection connection = (HttpURLConnection) website.openConnection();
                    connection.setRequestMethod("GET");

                    // Check the response code
                    int responseCode = connection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        // Read the response
                        try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                            String inputLine;
                            while ((inputLine = in.readLine()) != null) {
                                htmlContent.append(inputLine).append("\n");
                            }
                        }
                    } else {
                        System.out.println("Error: received response code " + responseCode);
                    }
                } catch (IOException e) {
                    System.out.println("Error fetching URL: " + e.getMessage());
                }
            });
            // Start the download thread
            downloadThread.start();
            // Wait for the download to complete
            try {
                downloadThread.join();
            } catch (InterruptedException e) {
                System.out.println("Download interrupted: " + e.getMessage());
            }
            // Print the downloaded HTML content
            System.out.println("Downloaded HTML content:");
            System.out.println(htmlContent.toString());
            // Close the scanner    }

        }
    }
}
