package com.chatapp.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Base64;

/**
 * Model class representing a message in the chat application
 */
public class Message implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String sender;
    private String content;
    private String receiver; // Can be a username or "ALL" for broadcast
    private LocalDateTime timestamp;
    private MessageType type;
    private String fileName;
    private byte[] fileData;
    private boolean hasFile;
    private long messageId;
    private long fileSize;
    private static long messageCounter = 0;
    
    public enum MessageType {
        CHAT, LOGIN, LOGOUT, REGISTER, STATUS, FILE, EMOJI
    }
    
    public Message() {
        this.timestamp = LocalDateTime.now();
        this.messageId = generateMessageId();
    }
    
    public Message(String sender, String content, String receiver, MessageType type) {
        this.sender = sender;
        this.content = content;
        this.receiver = receiver;
        this.timestamp = LocalDateTime.now();
        this.type = type;
        this.hasFile = false;
        this.messageId = generateMessageId();
    }
    
    // Constructor for file messages
    public Message(String sender, String fileName, byte[] fileData, String receiver) {
        this.sender = sender;
        this.content = "File: " + fileName;
        this.receiver = receiver;
        this.timestamp = LocalDateTime.now();
        this.type = MessageType.FILE;
        this.fileName = fileName;
        this.fileData = fileData;
        this.fileSize = fileData != null ? fileData.length : 0;
        this.hasFile = true;
        this.messageId = generateMessageId();
    }
    
    private synchronized static long generateMessageId() {
        return ++messageCounter;
    }
    
    // Getters and Setters
    public String getSender() {
        return sender;
    }
    
    public void setSender(String sender) {
        this.sender = sender;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public String getReceiver() {
        return receiver;
    }
    
    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public MessageType getType() {
        return type;
    }
    
    public void setType(MessageType type) {
        this.type = type;
    }
    
    public String getFileName() {
        return fileName;
    }
    
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    
    public byte[] getFileData() {
        return fileData;
    }
    
    public void setFileData(byte[] fileData) {
        this.fileData = fileData;
        if (fileData != null) {
            this.fileSize = fileData.length;
            this.hasFile = true;
        }
    }
    
    public boolean hasFile() {
        return hasFile;
    }
    
    public void setHasFile(boolean hasFile) {
        this.hasFile = hasFile;
    }
    
    public long getMessageId() {
        return messageId;
    }
    
    public long getFileSize() {
        return fileSize;
    }
    
    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }
    
    /**
     * Get the file size as a formatted string
     * @return Formatted file size
     */
    public String getFormattedFileSize() {
        if (!hasFile) return "0 B";
        
        if (fileSize < 1024) return fileSize + " B";
        if (fileSize < 1024 * 1024) return (fileSize / 1024) + " KB";
        return (fileSize / (1024 * 1024)) + " MB";
    }
    
    @Override
    public String toString() {
        return "Message{" +
                "sender='" + sender + '\'' +
                ", content='" + content + '\'' +
                ", receiver='" + receiver + '\'' +
                ", timestamp=" + timestamp +
                ", type=" + type +
                ", hasFile=" + hasFile +
                (hasFile ? ", fileName='" + fileName + '\'' : "") +
                (hasFile ? ", fileSize=" + getFormattedFileSize() : "") +
                ", messageId=" + messageId +
                '}';
    }
} 