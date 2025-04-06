package com.chatapp.util;

import com.chatapp.model.User;
import java.util.HashMap;
import java.util.Map;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Random;
import java.io.*;
import java.util.stream.Collectors;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Handles user authentication, registration and storage
 */
public class UserAuthentication {
    private static final String USER_FILE = "users.dat";
    private static final Logger logger = Logger.getLogger(UserAuthentication.class.getName());
    private static Map<String, User> users = new ConcurrentHashMap<>();
    private static Map<String, String> emailToUsername = new ConcurrentHashMap<>();
    private static Map<String, String> resetCodes = new ConcurrentHashMap<>(); // email -> reset code
    
    static {
        loadUsers();
    }
    
    /**
     * Load users from file
     */
    private static void loadUsers() {
        File file = new File(USER_FILE);
        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                users = (Map<String, User>) ois.readObject();
                
                // Rebuild email to username map
                for (Map.Entry<String, User> entry : users.entrySet()) {
                    emailToUsername.put(entry.getValue().getEmail(), entry.getKey());
                }
                
                logger.info("Loaded " + users.size() + " users from file");
            } catch (IOException | ClassNotFoundException e) {
                logger.warning("Error loading users: " + e.getMessage());
                users = new ConcurrentHashMap<>();
            }
        }
    }
    
    /**
     * Save users to file
     */
    private static void saveUsers() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(USER_FILE))) {
            oos.writeObject(users);
            logger.info("Saved " + users.size() + " users to file");
        } catch (IOException e) {
            logger.warning("Error saving users: " + e.getMessage());
        }
    }
    
    /**
     * Authenticate a user
     * @param username Username
     * @param password Password
     * @return User object if authentication successful, null otherwise
     */
    public static User authenticate(String username, String password) {
        User user = users.get(username);
        if (user != null && user.getPassword().equals(password)) {
            user.setOnline(true);
            saveUsers();
            return user;
        }
        return null;
    }
    
    /**
     * Register a new user
     * @param username Username
     * @param password Password
     * @param email Email address
     * @return true if registration successful, false otherwise
     */
    public static boolean register(String username, String password, String email) {
        // Check if username exists
        if (users.containsKey(username)) {
            return false;
        }
        
        // Check if email exists
        if (emailToUsername.containsKey(email)) {
            return false;
        }
        
        User user = new User(username, password, email);
        users.put(username, user);
        emailToUsername.put(email, username);
        saveUsers();
        
        return true;
    }
    
    /**
     * Get a user by username
     * @param username Username
     * @return User object
     */
    public static User getUser(String username) {
        return users.get(username);
    }
    
    /**
     * Get a user by email
     * @param email Email address
     * @return User object or null if not found
     */
    public static User getUserByEmail(String email) {
        String username = emailToUsername.get(email);
        if (username != null) {
            return users.get(username);
        }
        return null;
    }
    
    /**
     * Update a user's online status
     * @param username Username
     * @param online Online status
     */
    public static void setUserOnlineStatus(String username, boolean online) {
        User user = users.get(username);
        if (user != null) {
            user.setOnline(online);
            saveUsers();
        }
    }
    
    /**
     * Update a user's profile information
     * @param username Username
     * @param status Status message
     * @param bio Bio
     * @param avatar Avatar image data
     * @return true if update successful
     */
    public static boolean updateUserProfile(String username, String status, String bio, byte[] avatar) {
        User user = users.get(username);
        if (user != null) {
            user.setStatus(status);
            user.setBio(bio);
            if (avatar != null) {
                user.setAvatar(avatar);
            }
            saveUsers();
            return true;
        }
        return false;
    }
    
    /**
     * Get all registered users
     * @return Map of usernames to User objects
     */
    public static Map<String, User> getUsers() {
        return users;
    }
    
    /**
     * Search for users by username or email
     * @param searchTerm Term to search for
     * @return List of matching usernames
     */
    public static List<String> searchUsers(String searchTerm) {
        if (searchTerm == null || searchTerm.isEmpty()) {
            return new ArrayList<>(users.keySet());
        }
        
        String term = searchTerm.toLowerCase();
        return users.entrySet().stream()
                .filter(entry -> 
                        entry.getKey().toLowerCase().contains(term) || 
                        entry.getValue().getEmail().toLowerCase().contains(term))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
    
    /**
     * Request a password reset code for the given email
     * @param email Email address
     * @return Reset code or null if email not found
     */
    public static String requestPasswordReset(String email) {
        if (!emailToUsername.containsKey(email)) {
            return null;
        }
        
        // Generate a random 6-digit code
        String resetCode = String.format("%06d", new Random().nextInt(1000000));
        resetCodes.put(email, resetCode);
        
        // In a real application, this would send an email with the reset code
        logger.info("Password reset code for " + email + ": " + resetCode);
        
        return resetCode;
    }
    
    /**
     * Reset a user's password using a reset code
     * @param email Email address
     * @param resetCode Reset code
     * @param newPassword New password
     * @return true if reset successful, false otherwise
     */
    public static boolean resetPassword(String email, String resetCode, String newPassword) {
        // Check if email exists
        if (!emailToUsername.containsKey(email)) {
            return false;
        }
        
        // Check if reset code is valid
        String storedCode = resetCodes.get(email);
        if (storedCode == null || !storedCode.equals(resetCode)) {
            return false;
        }
        
        // Reset password
        String username = emailToUsername.get(email);
        User user = users.get(username);
        user.setPassword(newPassword);
        
        // Remove reset code
        resetCodes.remove(email);
        
        saveUsers();
        return true;
    }
    
    /**
     * Delete all users (admin function)
     */
    public static void deleteAllUsers() {
        users.clear();
        emailToUsername.clear();
        resetCodes.clear();
        saveUsers();
    }
    
    /**
     * Check if username exists
     * @param username Username to check
     * @return true if username exists
     */
    public static boolean usernameExists(String username) {
        return users.containsKey(username);
    }
    
    /**
     * Check if email exists
     * @param email Email to check
     * @return true if email exists
     */
    public static boolean emailExists(String email) {
        return emailToUsername.containsKey(email);
    }
} 