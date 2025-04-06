package com.chatapp.client;

import com.chatapp.model.Message;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

/**
 * Main chat window for the application
 */
public class ChatFrame extends JFrame {
    private ChatClient client;
    private String username;
    
    private JTabbedPane tabbedPane;
    private JPanel globalChatPanel;
    private JTextArea globalChatArea;
    private JTextField messageField;
    private JButton sendButton;
    private JButton fileButton;
    private DefaultListModel<String> userListModel;
    private JList<String> userList;
    private JPopupMenu userMenu;
    private JMenuItem profileMenuItem;
    private JMenuItem privateMenuItem;
    private JButton emojiButton;
    private JPopupMenu emojiMenu;
    
    private Map<String, JTextArea> privateChatAreas = new HashMap<>();
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss");
    
    // Common emoji set
    private static final String[][] EMOJIS = {
            {"😀", "Grinning Face"},
            {"😂", "Face with Tears of Joy"},
            {"😍", "Smiling Face with Heart-Eyes"},
            {"🤔", "Thinking Face"},
            {"😊", "Smiling Face with Smiling Eyes"},
            {"👍", "Thumbs Up"},
            {"❤️", "Red Heart"},
            {"🎉", "Party Popper"},
            {"👋", "Waving Hand"},
            {"🔥", "Fire"}
    };
    
    /**
     * Create a new chat frame
     * @param client Chat client
     * @param username Current username
     */
    public ChatFrame(ChatClient client, String username) {
        this.client = client;
        this.username = username;
        
        setTitle("Chat Application - " + username);
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Add window listener to handle close event
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                client.logout();
            }
        });
        
        createUI();
        
        // Register message handler
        client.addMessageHandler(this::processMessage);
    }
    
    /**
     * Create the user interface
     */
    private void createUI() {
        // Create main panel with border layout
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // Create tabbed pane for chat areas
        tabbedPane = new JTabbedPane();
        
        // Create global chat panel
        globalChatPanel = new JPanel(new BorderLayout());
        globalChatArea = new JTextArea();
        globalChatArea.setEditable(false);
        globalChatArea.setLineWrap(true);
        globalChatArea.setWrapStyleWord(true);
        JScrollPane globalScrollPane = new JScrollPane(globalChatArea);
        globalChatPanel.add(globalScrollPane, BorderLayout.CENTER);
        
        // Add global chat to tabbed pane
        tabbedPane.addTab("Global Chat", globalChatPanel);
        
        // Create message input panel
        JPanel inputPanel = new JPanel(new BorderLayout());
        messageField = new JTextField();
        messageField.addActionListener(e -> sendMessage());
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        // Emoji button
        emojiButton = new JButton("😀");
        emojiButton.setToolTipText("Insert Emoji");
        emojiButton.addActionListener(e -> showEmojiMenu());
        buttonPanel.add(emojiButton);
        
        // Create emoji menu
        createEmojiMenu();
        
        // File button
        fileButton = new JButton("File");
        fileButton.addActionListener(e -> sendFile());
        buttonPanel.add(fileButton);
        
        // Send button
        sendButton = new JButton("Send");
        sendButton.addActionListener(e -> sendMessage());
        buttonPanel.add(sendButton);
        
        inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(buttonPanel, BorderLayout.EAST);
        
        // Add tabbed pane and input panel to main panel
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        mainPanel.add(inputPanel, BorderLayout.SOUTH);
        
        // Create user list panel
        JPanel userPanel = new JPanel(new BorderLayout());
        userPanel.setBorder(BorderFactory.createTitledBorder("Online Users"));
        
        userListModel = new DefaultListModel<>();
        userList = new JList<>(userListModel);
        userList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    openPrivateChat(userList.getSelectedValue());
                } else if (SwingUtilities.isRightMouseButton(e)) {
                    userList.setSelectedIndex(userList.locationToIndex(e.getPoint()));
                    userMenu.show(userList, e.getX(), e.getY());
                }
            }
        });
        
        // Create user context menu
        createUserContextMenu();
        
        JScrollPane userScrollPane = new JScrollPane(userList);
        userPanel.add(userScrollPane, BorderLayout.CENTER);
        
        // Create admin panel
        JPanel adminPanel = new JPanel();
        adminPanel.setLayout(new BoxLayout(adminPanel, BoxLayout.Y_AXIS));
        adminPanel.setBorder(BorderFactory.createTitledBorder("Admin Functions"));
        
        if (username.equals("admin")) {
            JButton deleteAllUsersButton = new JButton("Delete All Users");
            deleteAllUsersButton.addActionListener(e -> {
                int confirm = JOptionPane.showConfirmDialog(
                        this,
                        "Are you sure you want to delete ALL registered users?\nThis cannot be undone!",
                        "Confirm Delete All Users",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                );
                
                if (confirm == JOptionPane.YES_OPTION) {
                    client.deleteAllUsers();
                }
            });
            adminPanel.add(deleteAllUsersButton);
            userPanel.add(adminPanel, BorderLayout.SOUTH);
        }
        
        // Add search user field
        JTextField searchField = new JTextField("Search users...");
        searchField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (searchField.getText().equals("Search users...")) {
                    searchField.setText("");
                }
            }
            
            @Override
            public void focusLost(FocusEvent e) {
                if (searchField.getText().isEmpty()) {
                    searchField.setText("Search users...");
                }
            }
        });
        
        searchField.addActionListener(e -> {
            String searchTerm = searchField.getText();
            if (!searchTerm.equals("Search users...") && !searchTerm.isEmpty()) {
                client.searchUsers(searchTerm);
            }
        });
        
        userPanel.add(searchField, BorderLayout.NORTH);
        
        // Add main and user panels to content pane
        mainPanel.add(userPanel, BorderLayout.EAST);
        
        // Create menu bar
        JMenuBar menuBar = new JMenuBar();
        
        // File menu
        JMenu fileMenu = new JMenu("File");
        JMenuItem logoutMenuItem = new JMenuItem("Logout");
        logoutMenuItem.addActionListener(e -> client.logout());
        fileMenu.add(logoutMenuItem);
        
        JMenuItem exitMenuItem = new JMenuItem("Exit");
        exitMenuItem.addActionListener(e -> {
            client.logout();
            System.exit(0);
        });
        fileMenu.add(exitMenuItem);
        
        // Profile menu
        JMenu profileMenu = new JMenu("Profile");
        JMenuItem editProfileMenuItem = new JMenuItem("Edit Profile");
        editProfileMenuItem.addActionListener(e -> showProfileDialog());
        profileMenu.add(editProfileMenuItem);
        
        // Help menu
        JMenu helpMenu = new JMenu("Help");
        JMenuItem aboutMenuItem = new JMenuItem("About");
        aboutMenuItem.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "Chat Application\nVersion 1.0\n\n" +
                        "A simple chat application with multiple features:\n" +
                        "- Global and private messaging\n" +
                        "- File sharing\n" +
                        "- Emoji support\n" +
                        "- User profiles\n",
                "About Chat Application",
                JOptionPane.INFORMATION_MESSAGE));
        helpMenu.add(aboutMenuItem);
        
        menuBar.add(fileMenu);
        menuBar.add(profileMenu);
        menuBar.add(helpMenu);
        
        setJMenuBar(menuBar);
        
        // Set main panel as content pane
        setContentPane(mainPanel);
    }
    
    /**
     * Create the emoji menu
     */
    private void createEmojiMenu() {
        emojiMenu = new JPopupMenu();
        
        for (String[] emoji : EMOJIS) {
            JMenuItem emojiItem = new JMenuItem(emoji[0] + " " + emoji[1]);
            emojiItem.addActionListener(e -> {
                messageField.setText(messageField.getText() + emoji[0]);
                messageField.requestFocus();
            });
            emojiMenu.add(emojiItem);
        }
    }
    
    /**
     * Show the emoji menu
     */
    private void showEmojiMenu() {
        emojiMenu.show(emojiButton, 0, -emojiMenu.getPreferredSize().height);
    }
    
    /**
     * Create user context menu
     */
    private void createUserContextMenu() {
        userMenu = new JPopupMenu();
        
        privateMenuItem = new JMenuItem("Private Message");
        privateMenuItem.addActionListener(e -> {
            String selectedUser = userList.getSelectedValue();
            if (selectedUser != null && !selectedUser.equals(username)) {
                openPrivateChat(selectedUser);
            }
        });
        userMenu.add(privateMenuItem);
        
        profileMenuItem = new JMenuItem("View Profile");
        profileMenuItem.addActionListener(e -> {
            String selectedUser = userList.getSelectedValue();
            if (selectedUser != null) {
                // This would fetch and display the user's profile
                JOptionPane.showMessageDialog(this, 
                        "Profile for " + selectedUser + "\n" +
                        "Status: Online\n" +
                        "Bio: Not available",
                        selectedUser + "'s Profile",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });
        userMenu.add(profileMenuItem);
    }
    
    /**
     * Process a message received from the server
     * @param message Message received
     */
    private void processMessage(Message message) {
        SwingUtilities.invokeLater(() -> {
            String sender = message.getSender();
            String content = message.getContent();
            String receiver = message.getReceiver();
            
            // Format timestamp
            String timestamp = "[" + TIME_FORMAT.format(new Date()) + "] ";
            
            switch (message.getType()) {
                case CHAT:
                    // Check if private message
                    if (receiver.equals(username) && !sender.equals("SERVER")) {
                        // Private message received
                        openPrivateChat(sender);
                        JTextArea chatArea = privateChatAreas.get(sender);
                        chatArea.append(timestamp + sender + ": " + content + "\n");
                    } else if (sender.equals(username) && !receiver.equals("ALL")) {
                        // Private message sent
                        openPrivateChat(receiver);
                        JTextArea chatArea = privateChatAreas.get(receiver);
                        chatArea.append(timestamp + "You: " + content + "\n");
                    } else {
                        // Global message
                        if (sender.equals("SERVER")) {
                            globalChatArea.append(timestamp + content + "\n");
                        } else {
                            globalChatArea.append(timestamp + sender + ": " + content + "\n");
                        }
                    }
                    break;
                    
                case FILE:
                    // Handle file message
                    String fileInfo = "FILE: " + message.getFileName() + " (" + message.getFormattedFileSize() + ")";
                    
                    if (receiver.equals(username)) {
                        // Private file received
                        openPrivateChat(sender);
                        JTextArea chatArea = privateChatAreas.get(sender);
                        chatArea.append(timestamp + sender + " sent " + fileInfo + "\n");
                        
                        // Ask to save file
                        askToSaveFile(message);
                    } else if (sender.equals(username)) {
                        // Private file sent
                        openPrivateChat(receiver);
                        JTextArea chatArea = privateChatAreas.get(receiver);
                        chatArea.append(timestamp + "You sent " + fileInfo + "\n");
                    } else if (receiver.equals("ALL")) {
                        // Global file
                        globalChatArea.append(timestamp + sender + " sent " + fileInfo + "\n");
                        
                        // Ask to save file
                        askToSaveFile(message);
                    }
                    break;
            }
        });
    }
    
    /**
     * Open a private chat tab with a user
     * @param otherUser Username to chat with
     */
    private void openPrivateChat(String otherUser) {
        // Don't open chat with self or server
        if (otherUser.equals(username) || otherUser.equals("SERVER")) {
            return;
        }
        
        // Check if private chat tab already exists
        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
            if (tabbedPane.getTitleAt(i).equals(otherUser)) {
                tabbedPane.setSelectedIndex(i);
                return;
            }
        }
        
        // Create new private chat tab
        JPanel privateChatPanel = new JPanel(new BorderLayout());
        JTextArea privateChatArea = new JTextArea();
        privateChatArea.setEditable(false);
        privateChatArea.setLineWrap(true);
        privateChatArea.setWrapStyleWord(true);
        JScrollPane privateScrollPane = new JScrollPane(privateChatArea);
        privateChatPanel.add(privateScrollPane, BorderLayout.CENTER);
        
        // Add to map and tabbed pane
        privateChatAreas.put(otherUser, privateChatArea);
        tabbedPane.addTab(otherUser, privateChatPanel);
        tabbedPane.setSelectedComponent(privateChatPanel);
        
        // Add close button to tab
        int index = tabbedPane.indexOfComponent(privateChatPanel);
        JPanel tabPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        tabPanel.setOpaque(false);
        JLabel titleLabel = new JLabel(otherUser + " ");
        JButton closeButton = new JButton("x");
        closeButton.setPreferredSize(new Dimension(16, 16));
        closeButton.setMargin(new Insets(0, 0, 0, 0));
        closeButton.addActionListener(e -> {
            tabbedPane.remove(privateChatPanel);
            privateChatAreas.remove(otherUser);
        });
        
        tabPanel.add(titleLabel);
        tabPanel.add(closeButton);
        tabbedPane.setTabComponentAt(index, tabPanel);
    }
    
    /**
     * Send a message to the current chat
     */
    private void sendMessage() {
        String message = messageField.getText().trim();
        if (message.isEmpty()) {
            return;
        }
        
        // Determine recipient based on selected tab
        String recipient = "ALL"; // Default to global
        int selectedIndex = tabbedPane.getSelectedIndex();
        if (selectedIndex > 0) { // Not global chat
            recipient = tabbedPane.getTitleAt(selectedIndex);
        }
        
        // Send message
        client.sendMessage(message, recipient);
        
        // Clear message field
        messageField.setText("");
    }
    
    /**
     * Send a file to the current chat
     */
    private void sendFile() {
        // Determine recipient based on selected tab
        String recipient = "ALL"; // Default to global
        int selectedIndex = tabbedPane.getSelectedIndex();
        if (selectedIndex > 0) { // Not global chat
            recipient = tabbedPane.getTitleAt(selectedIndex);
        }
        
        // Open file chooser
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(this);
        
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            
            // Check file size (limit to 10MB for example)
            if (selectedFile.length() > 10 * 1024 * 1024) {
                JOptionPane.showMessageDialog(this,
                        "File is too large. Maximum size is 10MB.",
                        "File Too Large",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Send file
            client.sendFile(selectedFile, recipient);
        }
    }
    
    /**
     * Ask user if they want to save a received file
     * @param message File message
     */
    private void askToSaveFile(Message message) {
        int option = JOptionPane.showConfirmDialog(this,
                "You received a file: " + message.getFileName() + " (" + message.getFormattedFileSize() + ")\n" +
                "Do you want to save it?",
                "File Received",
                JOptionPane.YES_NO_OPTION);
        
        if (option == JOptionPane.YES_OPTION) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setSelectedFile(new File(message.getFileName()));
            int result = fileChooser.showSaveDialog(this);
            
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                
                try {
                    java.nio.file.Files.write(selectedFile.toPath(), message.getFileData());
                    JOptionPane.showMessageDialog(this,
                            "File saved successfully to " + selectedFile.getAbsolutePath(),
                            "File Saved",
                            JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this,
                            "Error saving file: " + e.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
    
    /**
     * Show profile editing dialog
     */
    private void showProfileDialog() {
        JDialog profileDialog = new JDialog(this, "Edit Profile", true);
        profileDialog.setLayout(new BorderLayout());
        
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Status field
        JLabel statusLabel = new JLabel("Status:");
        JTextField statusField = new JTextField(20);
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(statusLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 0;
        formPanel.add(statusField, gbc);
        
        // Bio field
        JLabel bioLabel = new JLabel("Bio:");
        JTextArea bioArea = new JTextArea(5, 20);
        bioArea.setLineWrap(true);
        JScrollPane bioScrollPane = new JScrollPane(bioArea);
        
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(bioLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 1;
        formPanel.add(bioScrollPane, gbc);
        
        // Avatar upload
        JLabel avatarLabel = new JLabel("Avatar:");
        JButton avatarButton = new JButton("Choose Image");
        final byte[][] avatarData = new byte[1][];
        
        avatarButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
                @Override
                public boolean accept(File f) {
                    if (f.isDirectory()) return true;
                    String name = f.getName().toLowerCase();
                    return name.endsWith(".jpg") || name.endsWith(".jpeg") || 
                           name.endsWith(".png") || name.endsWith(".gif");
                }
                
                @Override
                public String getDescription() {
                    return "Image files (*.jpg, *.png, *.gif)";
                }
            });
            
            int result = fileChooser.showOpenDialog(profileDialog);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                try {
                    avatarData[0] = java.nio.file.Files.readAllBytes(selectedFile.toPath());
                    avatarButton.setText(selectedFile.getName());
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(profileDialog,
                            "Error reading image: " + ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        
        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(avatarLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 2;
        formPanel.add(avatarButton, gbc);
        
        // Save button
        JButton saveButton = new JButton("Save Profile");
        saveButton.addActionListener(e -> {
            String status = statusField.getText().trim();
            String bio = bioArea.getText().trim();
            
            // Update profile
            client.updateProfile(status, bio, avatarData[0]);
            profileDialog.dispose();
        });
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(saveButton);
        
        profileDialog.add(formPanel, BorderLayout.CENTER);
        profileDialog.add(buttonPanel, BorderLayout.SOUTH);
        
        profileDialog.pack();
        profileDialog.setLocationRelativeTo(this);
        profileDialog.setVisible(true);
    }
    
    /**
     * Update the list of online users
     * @param userListString Comma-separated list of usernames
     */
    public void updateOnlineUsers(String userListString) {
        String[] users = userListString.split(",");
        
        SwingUtilities.invokeLater(() -> {
            userListModel.clear();
            for (String user : users) {
                if (!user.isEmpty()) {
                    userListModel.addElement(user);
                }
            }
        });
    }
    
    /**
     * Handle user search results
     * @param results Comma-separated list of usernames
     */
    public void handleUserSearchResults(String results) {
        String[] users = results.split(",");
        
        SwingUtilities.invokeLater(() -> {
            userListModel.clear();
            for (String user : users) {
                if (!user.isEmpty()) {
                    userListModel.addElement(user);
                }
            }
        });
    }
    
    /**
     * Handle profile update response
     * @param success Whether update was successful
     */
    public void handleProfileUpdateResponse(boolean success) {
        if (success) {
            JOptionPane.showMessageDialog(this,
                    "Profile updated successfully.",
                    "Profile Updated",
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                    "Failed to update profile.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
} 