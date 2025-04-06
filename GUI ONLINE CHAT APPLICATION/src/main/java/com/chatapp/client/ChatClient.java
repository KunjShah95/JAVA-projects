package com.chatapp.client;

import com.chatapp.model.Message;
import com.chatapp.model.Message.MessageType;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Client for the chat application that handles communication with the server
 */
public class ChatClient {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 5000;
    private static final Logger logger = Logger.getLogger(ChatClient.class.getName());
    
    private Socket socket;
    private ObjectOutputStream output;
    private ObjectInputStream input;
    private boolean connected = false;
    private ExecutorService executor;
    
    private List<Consumer<Message>> messageHandlers = new ArrayList<>();
    private List<Consumer<String>> connectionHandlers = new ArrayList<>();
    private List<String> onlineUsers = new ArrayList<>();
    private LoginFrame loginFrame;
    private ChatFrame chatFrame;
    private String currentUsername;
    
    /**
     * Create a new chat client and connect to the server
     */
    public ChatClient() {
        executor = Executors.newSingleThreadExecutor();
    }
    
    /**
     * Connect to the chat server
     * @return true if connection was successful
     */
    public boolean connect() {
        try {
            socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            output = new ObjectOutputStream(socket.getOutputStream());
            input = new ObjectInputStream(socket.getInputStream());
            connected = true;
            
            // Start listener thread
            executor.submit(this::listenForMessages);
            
            logger.info("Connected to server at " + SERVER_ADDRESS + ":" + SERVER_PORT);
            
            // Notify connection handlers
            for (Consumer<String> handler : connectionHandlers) {
                handler.accept("CONNECTED");
            }
            
            return true;
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to connect to server: " + e.getMessage(), e);
            connected = false;
            
            // Notify connection handlers
            for (Consumer<String> handler : connectionHandlers) {
                handler.accept("FAILED");
            }
            
            return false;
        }
    }
    
    /**
     * Disconnect from the server
     */
    public void disconnect() {
        if (!connected) {
            return;
        }
        
        try {
            if (currentUsername != null) {
                // Send logout message first
                Message logoutMessage = new Message(currentUsername, "Logging out", "SERVER", MessageType.LOGOUT);
                output.writeObject(logoutMessage);
                output.flush();
            }
            
            connected = false;
            
            // Close resources
            if (input != null) input.close();
            if (output != null) output.close();
            if (socket != null) socket.close();
            
            // Notify connection handlers
            for (Consumer<String> handler : connectionHandlers) {
                handler.accept("DISCONNECTED");
            }
            
            logger.info("Disconnected from server");
        } catch (IOException e) {
            logger.log(Level.WARNING, "Error disconnecting: " + e.getMessage(), e);
        }
    }
    
    /**
     * Check if connected to the server
     * @return true if connected
     */
    public boolean isConnected() {
        return connected;
    }
    
    /**
     * Show the login frame
     */
    private void showLoginFrame() {
        loginFrame = new LoginFrame(this);
        loginFrame.setVisible(true);
    }
    
    /**
     * Listen for messages from the server
     */
    private void listenForMessages() {
        try {
            Message message;
            while (connected && (message = (Message) input.readObject()) != null) {
                Message finalMessage = message;
                
                // Process the message
                switch (message.getType()) {
                    case CHAT:
                    case FILE:
                        // Notify message handlers
                        for (Consumer<Message> handler : messageHandlers) {
                            handler.accept(finalMessage);
                        }
                        break;
                    case STATUS:
                        processStatusMessage(message);
                        break;
                    default:
                        processStatusMessage(message);
                        break;
                }
            }
        } catch (EOFException e) {
            logger.warning("Connection closed by server");
            handleDisconnect();
        } catch (SocketException e) {
            logger.warning("Socket error: " + e.getMessage());
            handleDisconnect();
        } catch (IOException | ClassNotFoundException e) {
            logger.log(Level.SEVERE, "Error reading from server: " + e.getMessage(), e);
            handleDisconnect();
        }
    }
    
    /**
     * Process a status message from the server
     * @param message Status message
     */
    private void processStatusMessage(Message message) {
        String content = message.getContent();
        
        if (content.startsWith("Login successful")) {
            if (loginFrame != null) {
                loginFrame.handleLoginResponse(true, "Login successful");
            }
            
            currentUsername = message.getReceiver();
            chatFrame = new ChatFrame(this, currentUsername);
            chatFrame.setVisible(true);
        } else if (content.equals("Registration successful")) {
            if (loginFrame != null) {
                loginFrame.handleRegisterResponse(true, "Registration successful");
            }
        } else if (content.startsWith("Invalid")) {
            if (loginFrame != null) {
                loginFrame.handleLoginResponse(false, content);
            }
        } else if (content.startsWith("Username") && content.contains("exists")) {
            if (loginFrame != null) {
                loginFrame.handleRegisterResponse(false, content);
            }
        } else if (content.startsWith("RESET_CODE:")) {
            // Handle reset code response
            String resetCode = content.substring("RESET_CODE:".length());
            if (loginFrame != null) {
                loginFrame.handleResetCodeResponse(resetCode);
            }
        } else if (content.equals("RESET_SUCCESS")) {
            // Handle successful password reset
            if (loginFrame != null) {
                loginFrame.handlePasswordResetResponse(true, "Password reset successful");
            }
        } else if (content.equals("RESET_FAILED")) {
            // Handle failed password reset
            if (loginFrame != null) {
                loginFrame.handlePasswordResetResponse(false, "Password reset failed");
            }
        } else if (content.startsWith("SEARCH_RESULTS:")) {
            // Handle user search results
            String results = content.substring("SEARCH_RESULTS:".length());
            if (chatFrame != null) {
                chatFrame.handleUserSearchResults(results);
            }
        } else if (content.equals("Profile updated successfully")) {
            // Handle profile update
            if (chatFrame != null) {
                chatFrame.handleProfileUpdateResponse(true);
            }
        } else if (content.equals("Server is being reset. Please reconnect.")) {
            // Server reset notification
            if (chatFrame != null) {
                chatFrame.dispose();
            }
            handleDisconnect();
            connect();
            showLoginFrame();
        } else {
            // Must be online users list
            if (chatFrame != null) {
                chatFrame.updateOnlineUsers(content);
            }
        }
    }
    
    /**
     * Handle disconnect from server
     */
    private void handleDisconnect() {
        connected = false;
        
        // Notify connection handlers
        for (Consumer<String> handler : connectionHandlers) {
            handler.accept("DISCONNECTED");
        }
        
        // Close resources
        try {
            if (input != null) input.close();
            if (output != null) output.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            logger.log(Level.WARNING, "Error closing resources: " + e.getMessage(), e);
        }
    }
    
    /**
     * Send a login request to the server
     * @param username Username
     * @param password Password
     */
    public void login(String username, String password) {
        if (!connected) {
            logger.warning("Cannot login: Not connected to server");
            return;
        }
        
        try {
            Message loginMessage = new Message("CLIENT", username + ":" + password, "SERVER", MessageType.LOGIN);
            output.writeObject(loginMessage);
            output.flush();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error sending login request: " + e.getMessage(), e);
        }
    }
    
    /**
     * Send a registration request to the server
     * @param username Username
     * @param password Password
     * @param email Email address
     */
    public void register(String username, String password, String email) {
        if (!connected) {
            logger.warning("Cannot register: Not connected to server");
            return;
        }
        
        try {
            Message registerMessage = new Message("CLIENT", username + ":" + password + ":" + email, "SERVER", MessageType.REGISTER);
            output.writeObject(registerMessage);
            output.flush();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error sending registration request: " + e.getMessage(), e);
        }
    }
    
    /**
     * Send a chat message
     * @param content Message content
     * @param receiver Recipient username
     */
    public void sendMessage(String content, String receiver) {
        if (!connected) {
            logger.warning("Cannot send message: Not connected to server");
            return;
        }
        
        try {
            Message chatMessage = new Message(currentUsername, content, receiver, MessageType.CHAT);
            output.writeObject(chatMessage);
            output.flush();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error sending chat message: " + e.getMessage(), e);
        }
    }
    
    /**
     * Send a chat message (alias for sendMessage)
     * @param content Message content
     * @param receiver Recipient username
     */
    public void sendChatMessage(String content, String receiver) {
        sendMessage(content, receiver);
    }
    
    /**
     * Send a file to a recipient
     * @param file File to send
     * @param receiver Recipient username
     */
    public void sendFile(File file, String receiver) {
        if (!connected) {
            logger.warning("Cannot send file: Not connected to server");
            return;
        }
        
        try {
            // Read file into byte array
            byte[] fileData = new byte[(int) file.length()];
            try (FileInputStream fis = new FileInputStream(file)) {
                fis.read(fileData);
            }
            
            // Create file message
            Message fileMessage = new Message(currentUsername, "File transfer: " + file.getName(), receiver, MessageType.FILE);
            fileMessage.setFileName(file.getName());
            fileMessage.setFileSize(file.length());
            fileMessage.setFileData(fileData);
            
            // Send message
            output.writeObject(fileMessage);
            output.flush();
            
            logger.info("File sent: " + file.getName() + " to " + receiver);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error sending file: " + e.getMessage(), e);
        }
    }
    
    /**
     * Send a file message with file name and data
     * @param fileName Name of the file
     * @param fileData File data as byte array
     * @param receiver Recipient username
     */
    public void sendFileMessage(String fileName, byte[] fileData, String receiver) {
        if (!connected) {
            logger.warning("Cannot send file: Not connected to server");
            return;
        }
        
        try {
            // Create file message
            Message fileMessage = new Message(currentUsername, "File transfer: " + fileName, receiver, MessageType.FILE);
            fileMessage.setFileName(fileName);
            fileMessage.setFileSize(fileData.length);
            fileMessage.setFileData(fileData);
            
            // Send message
            output.writeObject(fileMessage);
            output.flush();
            
            logger.info("File sent: " + fileName + " to " + receiver);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error sending file message: " + e.getMessage(), e);
        }
    }
    
    /**
     * Send a status update
     * @param status Status message
     */
    public void sendStatus(String status) {
        if (!connected) {
            logger.warning("Cannot send status: Not connected to server");
            return;
        }
        
        try {
            Message statusMessage = new Message(currentUsername, status, "SERVER", MessageType.STATUS);
            output.writeObject(statusMessage);
            output.flush();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error sending status update: " + e.getMessage(), e);
        }
    }
    
    /**
     * Send a logout request
     */
    public void logout() {
        if (!connected) {
            logger.warning("Cannot logout: Not connected to server");
            return;
        }
        
        try {
            Message logoutMessage = new Message(currentUsername, "Logging out", "SERVER", MessageType.LOGOUT);
            output.writeObject(logoutMessage);
            output.flush();
            
            if (chatFrame != null) {
                chatFrame.dispose();
                chatFrame = null;
            }
            
            currentUsername = null;
            
            // Show login frame again
            showLoginFrame();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error sending logout request: " + e.getMessage(), e);
        }
    }
    
    /**
     * Update user profile
     * @param status Status message
     * @param bio Bio text
     * @param avatar Avatar image data (can be null)
     */
    public void updateProfile(String status, String bio, byte[] avatar) {
        if (!connected) {
            logger.warning("Cannot update profile: Not connected to server");
            return;
        }
        
        try {
            Message profileMessage = new Message(currentUsername, "UPDATE_PROFILE:" + status + ":" + bio, "SERVER", MessageType.STATUS);
            if (avatar != null) {
                profileMessage.setFileData(avatar);
            }
            
            output.writeObject(profileMessage);
            output.flush();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error updating profile: " + e.getMessage(), e);
        }
    }
    
    /**
     * Send a password reset request to the server
     * @param email The email address for the account to reset
     * @throws IOException if there is an error sending the request
     */
    public void sendResetRequest(String email) throws IOException {
        if (!isConnected()) {
            throw new IOException("Not connected to server");
        }
        
        // Create a status message for the reset request
        String content = "RESET_REQUEST:" + email;
        Message message = new Message("", content, "SERVER", MessageType.STATUS);
        
        try {
            output.writeObject(message);
            output.flush();
            logger.fine("Sent password reset request for email: " + email);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Error sending reset request: " + ex.getMessage(), ex);
            throw new IOException("Failed to send reset request: " + ex.getMessage());
        }
    }
    
    /**
     * Send a password reset to the server
     * @param email The email address for the account to reset
     * @param resetCode The reset code received from the server
     * @param newPassword The new password to set
     * @throws IOException if there is an error sending the request
     */
    public void sendPasswordReset(String email, String resetCode, String newPassword) throws IOException {
        if (!isConnected()) {
            throw new IOException("Not connected to server");
        }
        
        // Create a status message for the password reset
        String content = "RESET_PASSWORD:" + email + ":" + resetCode + ":" + newPassword;
        Message message = new Message("", content, "SERVER", MessageType.STATUS);
        
        try {
            output.writeObject(message);
            output.flush();
            logger.fine("Sent password reset for email: " + email);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Error sending password reset: " + ex.getMessage(), ex);
            throw new IOException("Failed to send password reset: " + ex.getMessage());
        }
    }
    
    /**
     * Delete all registered users (admin function)
     */
    public void deleteAllUsers() {
        if (!connected) {
            logger.warning("Cannot delete users: Not connected to server");
            return;
        }
        
        try {
            Message deleteMessage = new Message(currentUsername, "DELETE_ALL_USERS", "SERVER", MessageType.STATUS);
            output.writeObject(deleteMessage);
            output.flush();
            
            logger.warning("Delete all users request sent");
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error sending delete all users request: " + e.getMessage(), e);
        }
    }
    
    /**
     * Search for users
     * @param searchTerm Search term
     */
    public void searchUsers(String searchTerm) {
        if (!connected) {
            logger.warning("Cannot search users: Not connected to server");
            return;
        }
        
        try {
            Message searchMessage = new Message(currentUsername, "SEARCH_USERS:" + searchTerm, "SERVER", MessageType.STATUS);
            output.writeObject(searchMessage);
            output.flush();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error searching users: " + e.getMessage(), e);
        }
    }
    
    /**
     * Add a message handler
     * @param handler Message handler
     */
    public void addMessageHandler(Consumer<Message> handler) {
        messageHandlers.add(handler);
    }
    
    /**
     * Add a connection handler
     * @param handler Connection handler
     */
    public void addConnectionHandler(Consumer<String> handler) {
        connectionHandlers.add(handler);
    }
    
    /**
     * Get the list of online users
     * @return List of online usernames
     */
    public List<String> getOnlineUsers() {
        return new ArrayList<>(onlineUsers);
    }
    
    /**
     * Get the current username
     * @return Current username
     */
    public String getUsername() {
        return currentUsername;
    }
} 