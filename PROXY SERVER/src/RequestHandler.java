
import java.io.*;
import java.net.*;

public class RequestHandler implements Runnable {

    private final Socket clientSocket;

    public RequestHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try (
                InputStream clientInput = clientSocket.getInputStream(); OutputStream clientOutput = clientSocket.getOutputStream(); BufferedReader reader = new BufferedReader(new InputStreamReader(clientInput))) {
            String requestLine = reader.readLine();
            if (requestLine == null || requestLine.isEmpty()) {
                return;
            }

            Logger.logRequest(clientSocket.getInetAddress().toString(), requestLine);

            String[] parts = requestLine.split(" ");
            if (parts.length != 3 || !parts[0].equals("GET")) {
                clientOutput.write("HTTP/1.1 400 Bad Request\r\n\r\n".getBytes());
                return;
            }

            String url = parts[1];
            URL targetUrl = new URL(url);

            // Check access control
            if (!AccessControl.isAllowed(clientSocket.getInetAddress().toString(), url)) {
                clientOutput.write("HTTP/1.1 403 Forbidden\r\n\r\n".getBytes());
                return;
            }

            // Check cache
            String cachedResponse = CacheManager.getCachedResponse(targetUrl);
            if (cachedResponse != null) {
                clientOutput.write(cachedResponse.getBytes());
                Logger.logInfo("Served from cache: " + url);
                return;
            }

            // Forward request to target server
            try (Socket serverSocket = new Socket(targetUrl.getHost(), 80); OutputStream serverOutput = serverSocket.getOutputStream(); InputStream serverInput = serverSocket.getInputStream()) {

                String forwardRequest = "GET " + targetUrl.getPath() + " HTTP/1.1\r\n"
                        + "Host: " + targetUrl.getHost() + "\r\n"
                        + "Connection: close\r\n\r\n";

                serverOutput.write(forwardRequest.getBytes());

                ByteArrayOutputStream responseBuffer = new ByteArrayOutputStream();
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = serverInput.read(buffer)) != -1) {
                    responseBuffer.write(buffer, 0, bytesRead);
                    clientOutput.write(buffer, 0, bytesRead);
                }

                CacheManager.cacheResponse(targetUrl, responseBuffer.toString());
                Logger.logInfo("Response cached and served: " + url);
            }
        } catch (Exception e) {
            Logger.logError("Error handling client request: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                Logger.logError("Error closing client socket: " + e.getMessage());
            }
        }
    }
}
