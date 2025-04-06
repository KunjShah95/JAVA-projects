package com.chatapp.server;

import com.chatapp.model.Message;
import com.chatapp.model.Message.MessageType;
import com.chatapp.model.User;
import com.chatapp.util.MessageHistory;
import com.chatapp.util.UserAuthentication;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Server for the chat application that handles client connections
 */
public class ChatServer {
    private static final int PORT = 5000;
    private static final Map<String, ClientHandler> clientHandlers = new ConcurrentHashMap<>();
    private static final Logger logger = Logger.getLogger(ChatServer.class.getName());
    private static FileHandler logFile;
    
    public static void main(String[] args) {
        setupLogger();
        loadHistoryAndUsers();
        
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            logger.info("Chat Server started on port " + PORT);
            System.out.println("Chat Server started on port " + PORT);
            
            // Pre-register some users for testing if no users exist
            if (UserAuthentication.getUsers().isEmpty()) {
                UserAuthentication.register("admin", "admin123", "admin@example.com");
                UserAuthentication.register("user1", "pass123", "user1@example.com");
                logger.info("Created default users for testing");
            }
            
            while (true) {
                Socket clientSocket = serverSocket.accept();
                logger.info("New client connected: " + clientSocket);
                System.out.println("New client connected: " + clientSocket);
                
                // Create a new handler for this client
                ClientHandler handler = new ClientHandler(clientSocket);
                Thread thread = new Thread(handler);
                thread.start();
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Server exception: " + e.getMessage(), e);
            System.out.println("Server exception: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Set up the logger for server logs
     */
    private static void setupLogger() {
        try {
            logFile = new FileHandler("server_log.txt", true);
            logFile.setFormatter(new SimpleFormatter());
            logger.addHandler(logFile);
            logger.setLevel(Level.ALL);
        } catch (IOException e) {
            System.out.println("Could not set up logger: " + e.getMessage());
        }
    }
    
    /**
     * Load chat history and user data
     */
    private static void loadHistoryAndUsers() {
        // Load message history
        MessageHistory.loadHistory();
        // User data is loaded automatically in UserAuthentication
    }
    
    /**
     * Broadcast a message to all connected clients
     * @param message Message to broadcast
     */
    public static void broadcast(Message message) {
        // Add to message history
        if (message.getType() == MessageType.CHAT || message.getType() == MessageType.FILE) {
            MessageHistory.addMessage(message);
        }
        
        for (ClientHandler handler : clientHandlers.values()) {
            handler.sendMessage(message);
        }
    }
    
    /**
     * Send a message to a specific client
     * @param message Message to send
     * @param recipient Username of the recipient
     */
    public static void sendToUser(Message message, String recipient) {
        // Add to message history
        if (message.getType() == MessageType.CHAT || message.getType() == MessageType.FILE) {
            MessageHistory.addMessage(message);
        }
        
        ClientHandler handler = clientHandlers.get(recipient);
        if (handler != null) {
            handler.sendMessage(message);
        }
    }
    
    /**
     * Get a list of online users
     * @return Comma-separated list of usernames
     */
    public static String getOnlineUsers() {
        StringBuilder sb = new StringBuilder();
        for (String user : clientHandlers.keySet()) {
            sb.append(user).append(",");
        }
        
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1); // Remove last comma
        }
        
        return sb.toString();
    }
    
    /**
     * Handler class for each client connection
     */
    private static class ClientHandler implements Runnable {
        private Socket clientSocket;
        private ObjectInputStream input;
        private ObjectOutputStream output;
        private String username;
        
        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
            try {
                output = new ObjectOutputStream(socket.getOutputStream());
                input = new ObjectInputStream(socket.getInputStream());
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Error creating client handler: " + e.getMessage(), e);
                e.printStackTrace();
            }
        }
        
        @Override
        public void run() {
            try {
                Message message;
                
                while ((message = (Message) input.readObject()) != null) {
                    switch (message.getType()) {
                        case LOGIN:
                            handleLogin(message);
                            break;
                        case REGISTER:
                            handleRegister(message);
                            break;
                        case LOGOUT:
                            handleLogout(message);
                            break;
                        case CHAT:
                            handleChat(message);
                            break;
                        case FILE:
                            handleFile(message);
                            break;
                        case STATUS:
                            handleStatus(message);
                            break;
                    }
                }
            } catch (EOFException e) {
                // Client disconnected
                handleDisconnect();
            } catch (IOException | ClassNotFoundException e) {
                logger.log(Level.WARNING, "Error in client handler: " + e.getMessage(), e);
                e.printStackTrace();
                handleDisconnect();
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    logger.log(Level.WARNING, "Error closing client socket: " + e.getMessage(), e);
                    e.printStackTrace();
                }
            }
        }
        
        /**
         * Handle a login message
         * @param message Login message
         */
        private void handleLogin(Message message) {
            String[] credentials = message.getContent().split(":");
            if (credentials.length != 2) {
                sendErrorMessage("Invalid login format");
                return;
            }
            
            String username = credentials[0];
            String password = credentials[1];
            
            User user = UserAuthentication.authenticate(username, password);
            if (user != null) {
                // Check if user is already logged in
                if (clientHandlers.containsKey(username)) {
                    sendErrorMessage("User already logged in from another location");
                    return;
                }
                
                this.username = username;
                clientHandlers.put(username, this);
                
                // Send success message to client
                Message response = new Message("SERVER", "Login successful", username, MessageType.STATUS);
                sendMessage(response);
                
                // Notify all users about new login
                Message notification = new Message("SERVER", username + " has joined the chat", "ALL", MessageType.CHAT);
                broadcast(notification);
                
                // Send list of online users
                sendOnlineUsers();
                
                logger.info("User logged in: " + username);
            } else {
                sendErrorMessage("Invalid username or password");
                logger.info("Failed login attempt for username: " + username);
            }
        }
        
        /**
         * Handle a registration message
         * @param message Registration message
         */
        private void handleRegister(Message message) {
            String[] userData = message.getContent().split(":");
            if (userData.length != 3) {
                sendErrorMessage("Invalid registration format");
                return;
            }
            
            String username = userData[0];
            String password = userData[1];
            String email = userData[2];
            
            boolean success = UserAuthentication.register(username, password, email);
            if (success) {
                sendMessage(new Message("SERVER", "Registration successful", username, MessageType.STATUS));
                logger.info("New user registered: " + username);
            } else {
                sendErrorMessage("Username or email already exists");
                logger.info("Registration failed for username (already exists): " + username);
            }
        }
        
        /**
         * Handle a logout message
         * @param message Logout message
         */
        private void handleLogout(Message message) {
            if (username != null) {
                UserAuthentication.setUserOnlineStatus(username, false);
                clientHandlers.remove(username);
                
                // Notify all users
                Message notification = new Message("SERVER", username + " has left the chat", "ALL", MessageType.CHAT);
                broadcast(notification);
                
                logger.info("User logged out: " + username);
                username = null;
            }
        }
        
        /**
         * Handle a chat message
         * @param message Chat message
         */
        private void handleChat(Message message) {
            logger.info("Chat message from " + message.getSender() + " to " + message.getReceiver() + ": " + message.getContent());
            
            if (message.getReceiver().equals("ALL")) {
                broadcast(message);
            } else {
                sendToUser(message, message.getReceiver());
                // Also send to the sender if not a broadcast
                if (!message.getSender().equals(message.getReceiver())) {
                    sendMessage(message);
                }
            }
        }
        
        /**
         * Handle a file transfer message
         * @param message File message
         */
        private void handleFile(Message message) {
            logger.info("File transfer from " + message.getSender() + " to " + message.getReceiver() + 
                    ": " + message.getFileName() + " (" + message.getFileSize() + ")");
            
            if (message.getReceiver().equals("ALL")) {
                broadcast(message);
            } else {
                sendToUser(message, message.getReceiver());
                // Also send to the sender if not a broadcast
                if (!message.getSender().equals(message.getReceiver())) {
                    sendMessage(message);
                }
            }
        }
        
        /**
         * Handle a status message
         * @param message Status message
         */
        private void handleStatus(Message message) {
            String content = message.getContent();
            logger.info("Status update from " + message.getSender() + ": " + content);
            
            // Handle password reset request
            if (content.startsWith("RESET_REQUEST:")) {
                handlePasswordResetRequest(message);
                return;
            }
            
            // Handle password reset
            if (content.startsWith("RESET_PASSWORD:")) {
                handlePasswordReset(message);
                return;
            }
            
            // Handle profile update
            if (content.startsWith("UPDATE_PROFILE:")) {
                handleProfileUpdate(message);
                return;
            }
            
            // Handle user search
            if (content.startsWith("SEARCH_USERS:")) {
                handleUserSearch(message);
                return;
            }
            
            // Handle admin functions
            if (content.equals("DELETE_ALL_USERS") && "admin".equals(message.getSender())) {
                handleDeleteAllUsers();
                return;
            }
            
            // Update user status if needed
            if (!message.getSender().equals("SERVER") && username != null) {
                // This would update user status in a real implementation
                logger.info("Status update from " + username + ": " + content);
            }
        }
        
        /**
         * Handle a password reset request
         * @param message Password reset request message
         */
        private void handlePasswordResetRequest(Message message) {
            // Extract email from content
            String[] parts = message.getContent().split(":");
            if (parts.length < 2) {
                sendMessage(new Message("SERVER", "RESET_FAILED", "", MessageType.STATUS));
                return;
            }
            
            String email = parts[1];
            logger.info("Password reset request for email: " + email);
            
            // Request password reset code
            String resetCode = UserAuthentication.requestPasswordReset(email);
            if (resetCode != null) {
                // Send reset code to client
                sendMessage(new Message("SERVER", "RESET_CODE:" + resetCode, "", MessageType.STATUS));
                logger.info("Password reset code sent for email: " + email);
            } else {
                // Email not found
                sendMessage(new Message("SERVER", "RESET_FAILED", "", MessageType.STATUS));
                logger.info("Password reset failed - email not found: " + email);
            }
        }
        
        /**
         * Handle a password reset
         * @param message Password reset message
         */
        private void handlePasswordReset(Message message) {
            // Extract email, code, and new password from content
            String[] parts = message.getContent().split(":");
            if (parts.length < 4) {
                sendMessage(new Message("SERVER", "RESET_FAILED", "", MessageType.STATUS));
                return;
            }
            
            String email = parts[1];
            String resetCode = parts[2];
            String newPassword = parts[3];
            
            logger.info("Password reset attempt for email: " + email);
            
            // Reset password
            boolean success = UserAuthentication.resetPassword(email, resetCode, newPassword);
            if (success) {
                // Send success message
                sendMessage(new Message("SERVER", "RESET_SUCCESS", "", MessageType.STATUS));
                logger.info("Password reset successful for email: " + email);
            } else {
                // Reset failed
                sendMessage(new Message("SERVER", "RESET_FAILED", "", MessageType.STATUS));
                logger.info("Password reset failed for email: " + email);
            }
        }
        
        /**
         * Handle profile update
         * @param message Profile update message
         */
        private void handleProfileUpdate(Message message) {
            if (username == null) {
                return;
            }
            
            // Extract status and bio from content
            String[] parts = message.getContent().split(":");
            if (parts.length < 3) {
                return;
            }
            
            String status = parts[1];
            String bio = parts[2];
            byte[] avatar = message.hasFile() ? message.getFileData() : null;
            
            // Update user profile
            boolean success = UserAuthentication.updateUserProfile(username, status, bio, avatar);
            if (success) {
                sendMessage(new Message("SERVER", "Profile updated successfully", username, MessageType.STATUS));
                logger.info("Profile updated for user: " + username);
            } else {
                sendErrorMessage("Failed to update profile");
                logger.info("Profile update failed for user: " + username);
            }
        }
        
        /**
         * Handle user search
         * @param message User search message
         */
        private void handleUserSearch(Message message) {
            // Extract search term from content
            String[] parts = message.getContent().split(":");
            String searchTerm = parts.length > 1 ? parts[1] : "";
            
            // Search for users
            List<String> results = UserAuthentication.searchUsers(searchTerm);
            
            // Send results to client
            StringBuilder resultContent = new StringBuilder("SEARCH_RESULTS:");
            for (String user : results) {
                resultContent.append(user).append(",");
            }
            
            if (resultContent.charAt(resultContent.length() - 1) == ',') {
                resultContent.deleteCharAt(resultContent.length() - 1);
            }
            
            sendMessage(new Message("SERVER", resultContent.toString(), username, MessageType.STATUS));
            logger.info("User search performed by " + username + ": " + searchTerm);
        }
        
        /**
         * Handle delete all users (admin function)
         */
        private void handleDeleteAllUsers() {
            if (username == null || !username.equals("admin")) {
                sendErrorMessage("Unauthorized operation");
                return;
            }
            
            // Delete all users except admin
            UserAuthentication.deleteAllUsers();
            
            // Re-register admin
            UserAuthentication.register("admin", "admin123", "admin@example.com");
            
            sendMessage(new Message("SERVER", "All users deleted successfully", username, MessageType.STATUS));
            logger.warning("All users deleted by admin");
            
            // Notify all connected clients and force them to disconnect
            Message notification = new Message("SERVER", "Server is being reset. Please reconnect.", "ALL", MessageType.STATUS);
            broadcast(notification);
            
            // Disconnect all clients except admin
            for (Map.Entry<String, ClientHandler> entry : clientHandlers.entrySet()) {
                if (!entry.getKey().equals("admin")) {
                    try {
                        entry.getValue().clientSocket.close();
                    } catch (IOException e) {
                        logger.log(Level.WARNING, "Error disconnecting client: " + e.getMessage(), e);
                    }
                }
            }
            
            // Clear client handlers except admin
            String[] userArray = clientHandlers.keySet().toArray(new String[0]);
            for (String user : userArray) {
                if (!user.equals("admin")) {
                    clientHandlers.remove(user);
                }
            }
        }
        
        /**
         * Handle client disconnection
         */
        private void handleDisconnect() {
            if (username != null) {
                UserAuthentication.setUserOnlineStatus(username, false);
                clientHandlers.remove(username);
                
                // Notify all users
                Message notification = new Message("SERVER", username + " has disconnected", "ALL", MessageType.CHAT);
                broadcast(notification);
                
                // Update online users list for remaining clients
                sendOnlineUsersToAll();
                
                logger.info("User disconnected: " + username);
            }
        }
        
        /**
         * Send a message to the client
         * @param message Message to send
         */
        public void sendMessage(Message message) {
            try {
                output.writeObject(message);
                output.flush();
            } catch (IOException e) {
                logger.log(Level.WARNING, "Error sending message to client: " + e.getMessage(), e);
                e.printStackTrace();
            }
        }
        
        /**
         * Send an error message to the client
         * @param errorMessage Error message content
         */
        private void sendErrorMessage(String errorMessage) {
            Message error = new Message("SERVER", errorMessage, username != null ? username : "", MessageType.STATUS);
            sendMessage(error);
        }
        
        /**
         * Send the list of online users to the client
         */
        private void sendOnlineUsers() {
            String onlineUsersList = getOnlineUsers();
            Message userListMessage = new Message("SERVER", onlineUsersList, username, MessageType.STATUS);
            sendMessage(userListMessage);
        }
        
        /**
         * Send the list of online users to all clients
         */
        private void sendOnlineUsersToAll() {
            String onlineUsersList = getOnlineUsers();
            for (ClientHandler handler : clientHandlers.values()) {
                Message userListMessage = new Message("SERVER", onlineUsersList, handler.username, MessageType.STATUS);
                handler.sendMessage(userListMessage);
            }
        }
    }
} 