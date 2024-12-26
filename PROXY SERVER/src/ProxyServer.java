
import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class ProxyServer {

    private static final int PORT = 8080; // Proxy server port
    private static final int THREAD_POOL_SIZE = 50; // Number of concurrent threads

    public static void main(String[] args) {
        System.out.println("Starting Proxy Server on port " + PORT);

        // Thread pool for handling requests
        ExecutorService threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

        // Initialize cache and logging systems
        CacheManager.initialize();
        Logger.initialize();

        // Start the server
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    threadPool.execute(new RequestHandler(clientSocket));
                } catch (IOException e) {
                    Logger.logError("Error accepting client connection: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            Logger.logError("Server encountered an error: " + e.getMessage());
        } finally {
            threadPool.shutdown();
            try {
                if (!threadPool.awaitTermination(60, TimeUnit.SECONDS)) {
                    threadPool.shutdownNow();
                }
            } catch (InterruptedException ex) {
                threadPool.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}
