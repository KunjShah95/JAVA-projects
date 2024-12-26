
import java.io.*;
import java.net.URL;
import java.nio.file.*;

public class CacheManager {

    private static final String CACHE_DIR = "cache/";

    public static void initialize() {
        new File(CACHE_DIR).mkdir();
    }

    public static String getCachedResponse(URL url) {
        String filePath = CACHE_DIR + url.getHost() + url.getPath().replace("/", "_");
        File cacheFile = new File(filePath);
        if (cacheFile.exists()) {
            try {
                return Files.readString(cacheFile.toPath());
            } catch (IOException e) {
                Logger.logError("Cache read error: " + e.getMessage());
            }
        }
        return null;
    }

    public static void cacheResponse(URL url, String response) {
        String filePath = CACHE_DIR + url.getHost() + url.getPath().replace("/", "_");
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write(response);
        } catch (IOException e) {
            Logger.logError("Cache write error: " + e.getMessage());
        }
    }
}
