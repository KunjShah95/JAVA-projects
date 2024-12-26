
import java.util.*;

public class AccessControl {

    private static final Set<String> BLOCKED_URLS = new HashSet<>(Arrays.asList(
            "http://example.com/blocked"
    ));
    private static final Set<String> BLOCKED_IPS = new HashSet<>(Arrays.asList(
            "192.168.1.100"
    ));

    public static boolean isAllowed(String clientIP, String url) {
        if (BLOCKED_IPS.contains(clientIP) || BLOCKED_URLS.contains(url)) {
            Logger.logInfo("Access denied for IP: " + clientIP + " or URL: " + url);
            return false;
        }
        return true;
    }
}
