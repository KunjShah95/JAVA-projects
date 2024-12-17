
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class WebsiteReader {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Prompt the user for a URL
        System.out.print("Enter the URL to access: ");
        String urlString = scanner.nextLine(); // Read the URL input from the user

        try {
            // Create a URL object
            URL url = new URL(urlString);
            // Open a connection to the URL
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET"); // Set the request method to GET

            // Check the response code
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                StringBuilder content;
                try ( // 200 OK
                        // Create a BufferedReader to read the response
                        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    String inputLine;
                    content = new StringBuilder();
                    // Read the response line by line
                    while ((inputLine = in.readLine()) != null) {
                        content.append(inputLine).append("\n");
                    }
                    // Close the BufferedReader
                }

                // Print the content
                System.out.println(content.toString());
            } else {
                System.out.println("GET request failed. Response Code: " + responseCode);
            }

            // Disconnect the connection
            connection.disconnect();
        } catch (java.net.MalformedURLException e) {
            System.out.println("The URL is malformed: " + e.getMessage());
        } catch (java.io.IOException e) {
            System.out.println("An I/O error occurred: " + e.getMessage());
        } finally {
            // Close the scanner
            scanner.close();
        }
    }
}
