package com.chatapp.util;

import com.chatapp.model.Message;

import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Utility class for storing and retrieving message history
 */
public class MessageHistory {
    private static Map<String, List<Message>> messageHistory = new HashMap<>();
    private static final String HISTORY_FILE = "chat_history.dat";
    private static final int MAX_MESSAGES_PER_CONVERSATION = 100;
    
    /**
     * Add a message to the history
     * 
     * @param message Message to add
     */
    public static synchronized void addMessage(Message message) {
        String conversationKey = getConversationKey(message.getSender(), message.getReceiver());
        
        if (!messageHistory.containsKey(conversationKey)) {
            messageHistory.put(conversationKey, new ArrayList<>());
        }
        
        List<Message> conversation = messageHistory.get(conversationKey);
        conversation.add(message);
        
        // Trim conversation if it gets too large
        if (conversation.size() > MAX_MESSAGES_PER_CONVERSATION) {
            conversation.remove(0);
        }
        
        // Save history periodically (could be optimized to save less frequently)
        saveHistory();
    }
    
    /**
     * Get conversation history between two users
     * 
     * @param user1 First user
     * @param user2 Second user
     * @return List of messages in the conversation
     */
    public static List<Message> getConversation(String user1, String user2) {
        String conversationKey = getConversationKey(user1, user2);
        return messageHistory.getOrDefault(conversationKey, new ArrayList<>());
    }
    
    /**
     * Get all messages for a user (private and broadcast)
     * 
     * @param username Username
     * @return List of messages
     */
    public static List<Message> getAllMessagesForUser(String username) {
        List<Message> allMessages = new ArrayList<>();
        
        // Add broadcast messages
        String broadcastKey = getConversationKey("SERVER", "ALL");
        if (messageHistory.containsKey(broadcastKey)) {
            allMessages.addAll(messageHistory.get(broadcastKey));
        }
        
        // Add private messages
        for (Map.Entry<String, List<Message>> entry : messageHistory.entrySet()) {
            String key = entry.getKey();
            if (key.contains(username) && !key.equals(broadcastKey)) {
                allMessages.addAll(entry.getValue().stream()
                        .filter(m -> m.getSender().equals(username) || m.getReceiver().equals(username))
                        .collect(Collectors.toList()));
            }
        }
        
        // Sort by timestamp
        allMessages.sort((m1, m2) -> m1.getTimestamp().compareTo(m2.getTimestamp()));
        
        return allMessages;
    }
    
    /**
     * Get messages since a specific time
     * 
     * @param timestamp Time to get messages since
     * @return List of messages
     */
    public static List<Message> getMessagesSince(LocalDateTime timestamp) {
        List<Message> recentMessages = new ArrayList<>();
        
        for (List<Message> messages : messageHistory.values()) {
            for (Message message : messages) {
                if (message.getTimestamp().isAfter(timestamp)) {
                    recentMessages.add(message);
                }
            }
        }
        
        // Sort by timestamp
        recentMessages.sort((m1, m2) -> m1.getTimestamp().compareTo(m2.getTimestamp()));
        
        return recentMessages;
    }
    
    /**
     * Get a conversation key for storing messages between two users
     * 
     * @param user1 First user
     * @param user2 Second user
     * @return Conversation key
     */
    private static String getConversationKey(String user1, String user2) {
        // For broadcast messages
        if (user2.equals("ALL") || user1.equals("ALL")) {
            return "BROADCAST";
        }
        
        // Normalize server mentions
        if (user1.equals("SERVER") || user2.equals("SERVER")) {
            return "SERVER:" + (user1.equals("SERVER") ? user2 : user1);
        }
        
        // For private messages, sort usernames to ensure consistent key
        if (user1.compareTo(user2) < 0) {
            return user1 + ":" + user2;
        } else {
            return user2 + ":" + user1;
        }
    }
    
    /**
     * Save message history to file
     */
    public static synchronized void saveHistory() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(HISTORY_FILE))) {
            out.writeObject(messageHistory);
        } catch (IOException e) {
            System.err.println("Error saving message history: " + e.getMessage());
        }
    }
    
    /**
     * Load message history from file
     */
    @SuppressWarnings("unchecked")
    public static synchronized void loadHistory() {
        File historyFile = new File(HISTORY_FILE);
        if (!historyFile.exists()) {
            return;
        }
        
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(historyFile))) {
            messageHistory = (Map<String, List<Message>>) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading message history: " + e.getMessage());
        }
    }
    
    /**
     * Clear message history
     */
    public static synchronized void clearHistory() {
        messageHistory.clear();
        File historyFile = new File(HISTORY_FILE);
        if (historyFile.exists()) {
            historyFile.delete();
        }
    }
} 