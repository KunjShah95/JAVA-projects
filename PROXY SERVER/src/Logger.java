
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {

    private static final String LOG_FILE = "logs/access.log";

    public static void initialize() {
        new File("logs").mkdir();
    }

    public static void logRequest(String clientIP, String request) {
        log("REQUEST", clientIP + " - " + request);
    }

    public static void logInfo(String message) {
        log("INFO", message);
    }

    public static void logError(String message) {
        log("ERROR", message);
    }

    private static void log(String type, String message) {
        try (FileWriter writer = new FileWriter(LOG_FILE, true)) {
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            writer.write("[" + timestamp + "] [" + type + "] " + message + "\n");
        } catch (IOException e) {
            System.err.println("Logging error: " + e.getMessage());
        }
    }
}
