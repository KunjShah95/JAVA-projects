package com.chatapp.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * User class representing a chat application user
 */
public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String username;
    private String password;
    private String email;
    private boolean online;
    private String status;
    private String bio;
    private byte[] avatar;
    private LocalDateTime lastSeen;
    
    /**
     * Default constructor
     */
    public User() {
        this.status = "Available";
        this.bio = "";
        this.lastSeen = LocalDateTime.now();
    }
    
    /**
     * Constructor for creating a new user
     * @param username Username
     * @param password Password
     * @param email Email address
     */
    public User(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.online = false;
        this.status = "Available";
        this.bio = "";
        this.avatar = null;
        this.lastSeen = LocalDateTime.now();
    }
    
    /**
     * Get the username
     * @return Username
     */
    public String getUsername() {
        return username;
    }
    
    /**
     * Set the username
     * @param username New username
     */
    public void setUsername(String username) {
        this.username = username;
    }
    
    /**
     * Get the password
     * @return Password
     */
    public String getPassword() {
        return password;
    }
    
    /**
     * Set the password
     * @param password New password
     */
    public void setPassword(String password) {
        this.password = password;
    }
    
    /**
     * Get the email address
     * @return Email address
     */
    public String getEmail() {
        return email;
    }
    
    /**
     * Set the email address
     * @param email New email address
     */
    public void setEmail(String email) {
        this.email = email;
    }
    
    /**
     * Check if the user is online
     * @return true if online, false otherwise
     */
    public boolean isOnline() {
        return online;
    }
    
    /**
     * Set the user's online status
     * @param online Online status
     */
    public void setOnline(boolean online) {
        this.online = online;
        if (online) {
            this.lastSeen = LocalDateTime.now();
        }
    }
    
    /**
     * Get the user's status message
     * @return Status message
     */
    public String getStatus() {
        return status;
    }
    
    /**
     * Set the user's status message
     * @param status New status message
     */
    public void setStatus(String status) {
        this.status = status;
    }
    
    /**
     * Get the user's bio
     * @return Bio
     */
    public String getBio() {
        return bio;
    }
    
    /**
     * Set the user's bio
     * @param bio New bio
     */
    public void setBio(String bio) {
        this.bio = bio;
    }
    
    /**
     * Get the user's avatar image data
     * @return Avatar image data
     */
    public byte[] getAvatar() {
        return avatar;
    }
    
    /**
     * Set the user's avatar image data
     * @param avatar New avatar image data
     */
    public void setAvatar(byte[] avatar) {
        this.avatar = avatar;
    }
    
    /**
     * Get the user's last seen timestamp
     * @return Last seen timestamp
     */
    public LocalDateTime getLastSeen() {
        return lastSeen;
    }
    
    /**
     * Set the user's last seen timestamp
     * @param lastSeen New last seen timestamp
     */
    public void setLastSeen(LocalDateTime lastSeen) {
        this.lastSeen = lastSeen;
    }
    
    /**
     * Check if the user has an avatar
     * @return true if the user has an avatar, false otherwise
     */
    public boolean hasAvatar() {
        return avatar != null && avatar.length > 0;
    }
    
    @Override
    public String toString() {
        return username + " (" + (online ? "online" : "offline") + ")";
    }
} 