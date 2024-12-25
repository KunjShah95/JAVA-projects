
import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

public class HttpServer {

    private static final int PORT = 8080;  // Use HTTP port
    private static final String ROOT_DIR = "webroot";  // Directory for static files
    private static final Map<String, Integer> requestCounts = new ConcurrentHashMap<>();
    private static final int RATE_LIMIT = 100;  // Max requests per minute per IP
    private static final long RATE_LIMIT_WINDOW = 60000;  // 1 minute window for rate limiting

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            System.out.println("HTTP Server is running on port " + PORT);

            // Ensure the root directory exists
            File root = new File(ROOT_DIR);
            if (!root.exists()) {
                root.mkdir();
            }

            // Listen for incoming client requests
            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            System.err.println("Error handling request: " + e.getMessage());
        }
    }

    // ClientHandler class to handle individual HTTP requests
    private static class ClientHandler implements Runnable {

        private static final String SESSION_COOKIE_NAME = "SESSION_ID";
        private static final Map<String, String> userSessions = new ConcurrentHashMap<>();

        public static Map<String, String> getUserSessions() {
            return userSessions;
        }
        private final Socket clientSocket;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            try (
                    InputStream inputStream = clientSocket.getInputStream(); OutputStream outputStream = clientSocket.getOutputStream(); BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream)); PrintWriter writer = new PrintWriter(outputStream, true)) {

                // Read the HTTP request line
                String requestLine = reader.readLine();
                if (requestLine == null) {
                    return;
                }

                System.out.println("Request: " + requestLine);
                StringTokenizer tokenizer = new StringTokenizer(requestLine);
                String method = tokenizer.nextToken();  // GET, POST, etc.
                String url = tokenizer.nextToken();    // /index.html, /, etc.

                // Rate Limiting: Check if the IP is exceeding the request limit
                String clientIp = clientSocket.getInetAddress().getHostAddress();
                if (isRateLimited(clientIp)) {
                    sendErrorResponse(writer, 429, "Too Many Requests");
                    return;
                }

                // Log request
                logRequest(clientIp, url, method);

                // Handle different HTTP methods
                switch (method) {
                    case "GET" ->
                        handleGetRequest(url, writer, clientIp);
                    case "POST" ->
                        handlePostRequest(reader, writer);
                    case "PUT" ->
                        handlePutRequest(reader, writer);
                    case "DELETE" ->
                        handleDeleteRequest(url, writer);
                    default ->
                        sendErrorResponse(writer, 405, "Method Not Allowed");
                }

            } catch (IOException e) {
                System.err.println("Error: " + e.getMessage());
                sendErrorResponse(null, 500, "Internal Server Error");
            } finally {
                try {
                    clientSocket.close();  // Close the connection after handling the request
                } catch (IOException e) {
                    System.err.println("Error closing client socket: " + e.getMessage());
                }
            }
        }

        private void handleGetRequest(String url, PrintWriter writer, String clientIp) {
            // Session handling: Check for session cookies
            String sessionId = getSessionIdFromCookie();
            if (sessionId == null) {
                sessionId = UUID.randomUUID().toString();  // New session ID
                setSessionCookie(writer, sessionId);  // Send session cookie back to client
                userSessions.put(sessionId, "User_" + clientIp);  // Store session in memory
                saveSessionToFile(sessionId, "User_" + clientIp);  // Persist session data
            }

            // Handle basic routing or serve static files
            switch (url) {
                case "/" ->
                    sendHomePage(writer);
                case "/about" ->
                    sendAboutPage(writer);
                case "/directory" ->
                    listDirectoryContents(writer);
                default ->
                    serveStaticFile(url, writer);
            }
        }

        private void handlePostRequest(BufferedReader reader, PrintWriter writer) {
            try {
                StringBuilder postData = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null && !line.isEmpty()) {
                    postData.append(line).append("\n");
                }
                sendResponse(writer, 200, "OK", "POST Data received: " + postData.toString());
            } catch (IOException e) {
                sendErrorResponse(writer, 500, "Internal Server Error");
            }
        }

        private void handlePutRequest(BufferedReader reader, PrintWriter writer) {
            try {
                StringBuilder putData = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    putData.append(line).append("\n");
                }
                sendResponse(writer, 200, "OK", "PUT Data received and resource updated");
            } catch (IOException e) {
                sendErrorResponse(writer, 500, "Internal Server Error");
            }
        }

        private void handleDeleteRequest(String url, PrintWriter writer) {
            try {
                File file = new File(ROOT_DIR + url);
                if (file.exists()) {
                    file.delete();  // Delete the file
                    sendResponse(writer, 200, "OK", "Resource deleted");
                } else {
                    sendErrorResponse(writer, 404, "Not Found");
                }
            } catch (Exception e) {
                sendErrorResponse(writer, 500, "Internal Server Error");
            }
        }

        private void sendHomePage(PrintWriter writer) {
            String response = "<html><body><h1>Home Page</h1><p>Welcome to the Advanced HTTP Server!</p></body></html>";
            sendResponse(writer, 200, "OK", response);
        }

        private void sendAboutPage(PrintWriter writer) {
            String response = "<html><body><h1>About Us</h1><p>This is a more feature-rich HTTP server.</p></body></html>";
            sendResponse(writer, 200, "OK", response);
        }

        private void listDirectoryContents(PrintWriter writer) {
            File dir = new File(ROOT_DIR);
            StringBuilder response = new StringBuilder("<html><body><h1>Directory Listing</h1><ul>");
            for (File file : dir.listFiles()) {
                response.append("<li><a href='/").append(file.getName()).append("'>")
                        .append(file.getName()).append("</a></li>");
            }
            response.append("</ul></body></html>");
            sendResponse(writer, 200, "OK", response.toString());
        }

        private void serveStaticFile(String url, PrintWriter writer) {
            String filePath = ROOT_DIR + url;
            if (url.equals("/")) {
                filePath = ROOT_DIR + "/index.html";  // Default file
            }
            File file = new File(filePath);
            if (file.exists() && !file.isDirectory()) {
                try {
                    String fileExtension = getFileExtension(file);
                    String contentType = getContentType(fileExtension);
                    byte[] fileBytes = Files.readAllBytes(file.toPath());

                    String headers = """
                                     HTTP/1.1 200 OK\r
                                     Content-Type: """ + contentType + "\r\n"
                            + "Content-Length: " + fileBytes.length + "\r\n"
                            + "Connection: close\r\n"
                            + "\r\n";
                    writer.print(headers);
                    writer.flush();
                    writer.write(new String(fileBytes));
                } catch (IOException e) {
                    sendErrorResponse(writer, 500, "Internal Server Error");
                }
            } else {
                sendErrorResponse(writer, 404, "Not Found");
            }
        }

        private void sendResponse(PrintWriter writer, int statusCode, String statusMessage, String responseBody) {
            writer.println("HTTP/1.1 " + statusCode + " " + statusMessage);
            writer.println("Content-Type: text/html; charset=UTF-8");
            writer.println("Connection: close");
            writer.println();
            writer.println(responseBody);
            writer.flush();
        }

        private void sendErrorResponse(PrintWriter writer, int statusCode, String statusMessage) {
            String response = "<html><body><h1>Error " + statusCode + ": " + statusMessage + "</h1></body></html>";
            sendResponse(writer, statusCode, statusMessage, response);
        }

        private boolean isRateLimited(String clientIp) {
            long currentTime = System.currentTimeMillis();
            requestCounts.putIfAbsent(clientIp, 0);
            long lastRequestTime = currentTime - requestCounts.get(clientIp);

            if (lastRequestTime < RATE_LIMIT_WINDOW) {
                if (requestCounts.get(clientIp) > RATE_LIMIT) {
                    return true;
                }
            } else {
                requestCounts.put(clientIp, 0);  // Reset counter after 1 minute window
            }

            requestCounts.put(clientIp, requestCounts.get(clientIp) + 1);
            return false;
        }

        private String getFileExtension(File file) {
            String fileName = file.getName();
            int dotIndex = fileName.lastIndexOf('.');
            return (dotIndex == -1) ? "" : fileName.substring(dotIndex + 1);
        }

        private String getContentType(String extension) {
            return switch (extension.toLowerCase()) {
                case "html" ->
                    "text/html";
                case "css" ->
                    "text/css";
                case "js" ->
                    "application/javascript";
                case "jpg", "jpeg" ->
                    "image/jpeg";
                case "png" ->
                    "image/png";
                case "gif" ->
                    "image/gif";
                default ->
                    "application/octet-stream";
            };
        }

        private String getSessionIdFromCookie() {
            // Placeholder: Extract session ID from cookies (request headers)
            String cookieHeader = ""; // Read cookies from request (you can implement this part)
            if (cookieHeader.contains(SESSION_COOKIE_NAME)) {
                return cookieHeader.split("=")[1].split(";")[0];  // Extract session ID
            }
            return null;
        }

        private void setSessionCookie(PrintWriter writer, String sessionId) {
            writer.println("Set-Cookie: " + SESSION_COOKIE_NAME + "=" + sessionId + "; HttpOnly; Path=/");
        }

        private void saveSessionToFile(String sessionId, String userData) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(ROOT_DIR + "/sessions.txt", true))) {
                writer.write(sessionId + "=" + userData + "\n");
            } catch (IOException e) {
                System.err.println("Error saving session: " + e.getMessage());
            }
        }

        private void logRequest(String clientIp, String url, String method) {
            try (BufferedWriter logWriter = new BufferedWriter(new FileWriter("server_logs.txt", true))) {
                String logEntry = String.format("IP: %s | Method: %s | URL: %s | Timestamp: %s",
                        clientIp, method, url, new Date().toString());
                logWriter.write(logEntry);
                logWriter.newLine();
            } catch (IOException e) {
                System.err.println("Error writing log: " + e.getMessage());
            }
        }
    }
}
