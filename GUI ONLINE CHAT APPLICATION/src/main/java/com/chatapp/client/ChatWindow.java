package com.chatapp.client;

import com.chatapp.model.Message;
import com.chatapp.model.Message.MessageType;
import com.chatapp.util.EmojiUtil;
import com.chatapp.util.MessageHistory;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Main chat window for the chat application
 */
public class ChatWindow extends JFrame {
    private ChatClient client;
    
    private JTabbedPane chatTabs;
    private Map<String, JTextPane> chatAreas;
    private Map<String, JScrollPane> chatScrollPanes;
    private JTextField messageField;
    private JButton sendButton;
    private JButton fileButton;
    private JButton emojiButton;
    private JList<String> userList;
    private DefaultListModel<String> userListModel;
    private JLabel statusLabel;
    private String selectedUser = "ALL";
    private JPopupMenu emojiMenu;
    private JToggleButton darkModeButton;
    private boolean isDarkMode = false;
    
    // Dark mode colors
    private final Color DARK_BG = new Color(43, 43, 43);
    private final Color DARK_TEXT = new Color(220, 220, 220);
    private final Color LIGHT_BG = Color.WHITE;
    private final Color LIGHT_TEXT = Color.BLACK;
    
    /**
     * Create a new chat window
     * 
     * @param client Chat client
     */
    public ChatWindow(ChatClient client) {
        this.client = client;
        this.chatAreas = new HashMap<>();
        this.chatScrollPanes = new HashMap<>();
        
        setTitle("Chat Application - " + client.getUsername());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 700);
        setLocationRelativeTo(null);
        
        // Load message history
        MessageHistory.loadHistory();
        
        // Set up UI
        initComponents();
        setupListeners();
        
        setVisible(true);
    }
    
    /**
     * Initialize UI components
     */
    private void initComponents() {
        // Main layout with split pane
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(200);
        splitPane.setDividerSize(5);
        splitPane.setContinuousLayout(true);
        
        // Set up chat tabs (right side)
        chatTabs = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
        JPanel mainChatPanel = new JPanel(new BorderLayout());
        
        // Create default "All" tab
        JPanel allChatPanel = createChatPanel("ALL");
        chatTabs.addTab("Everyone", allChatPanel);
        
        // Message input area
        JPanel messagePanel = new JPanel(new BorderLayout());
        messageField = new JTextField();
        messageField.addActionListener(e -> sendMessage());
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        
        fileButton = new JButton("📎"); // File attachment
        fileButton.setToolTipText("Send File");
        fileButton.addActionListener(e -> chooseAndSendFile());
        
        emojiButton = new JButton("😊"); // Emoji selector
        emojiButton.setToolTipText("Insert Emoji");
        emojiButton.addActionListener(e -> showEmojiMenu());
        
        sendButton = new JButton("Send");
        
        buttonPanel.add(fileButton);
        buttonPanel.add(emojiButton);
        buttonPanel.add(sendButton);
        
        messagePanel.add(messageField, BorderLayout.CENTER);
        messagePanel.add(buttonPanel, BorderLayout.EAST);
        
        mainChatPanel.add(chatTabs, BorderLayout.CENTER);
        mainChatPanel.add(messagePanel, BorderLayout.SOUTH);
        
        // Create emoji menu
        emojiMenu = new JPopupMenu();
        JPanel emojiPanel = EmojiUtil.createEmojiPanel(emojiCode -> {
            messageField.setText(messageField.getText() + " " + emojiCode + " ");
            messageField.requestFocus();
            emojiMenu.setVisible(false);
        });
        emojiMenu.add(emojiPanel);
        
        // Set up user panel (left side)
        JPanel userPanel = new JPanel(new BorderLayout());
        
        // User list
        userListModel = new DefaultListModel<>();
        userList = new JList<>(userListModel);
        userList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userList.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        userList.setCellRenderer(new UserListCellRenderer());
        
        // Add ALL option for broadcasting
        userListModel.addElement("ALL");
        
        JScrollPane userScrollPane = new JScrollPane(userList);
        
        // User panel header with profile button
        JPanel userPanelHeader = new JPanel(new BorderLayout());
        
        JLabel userListTitle = new JLabel("Online Users", SwingConstants.CENTER);
        userListTitle.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        userListTitle.setFont(new Font("Arial", Font.BOLD, 14));
        
        JButton profileButton = new JButton("👤 Profile");
        profileButton.addActionListener(e -> showProfileDialog());
        
        darkModeButton = new JToggleButton("🌙");
        darkModeButton.setToolTipText("Toggle Dark Mode");
        darkModeButton.addActionListener(e -> toggleDarkMode());
        
        JPanel headerButtons = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        headerButtons.add(profileButton);
        headerButtons.add(darkModeButton);
        
        userPanelHeader.add(userListTitle, BorderLayout.NORTH);
        userPanelHeader.add(headerButtons, BorderLayout.CENTER);
        
        userPanel.add(userPanelHeader, BorderLayout.NORTH);
        userPanel.add(userScrollPane, BorderLayout.CENTER);
        
        // Status bar
        statusLabel = new JLabel("Connected as " + client.getUsername());
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        
        // Set up the split pane
        splitPane.setLeftComponent(userPanel);
        splitPane.setRightComponent(mainChatPanel);
        
        // Add components to frame
        setLayout(new BorderLayout());
        add(splitPane, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);
        
        // Add menu bar
        JMenuBar menuBar = new JMenuBar();
        
        JMenu fileMenu = new JMenu("File");
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> {
            client.disconnect();
            dispose();
            System.exit(0);
        });
        fileMenu.add(exitItem);
        
        JMenu editMenu = new JMenu("Edit");
        JMenuItem clearHistoryItem = new JMenuItem("Clear Chat History");
        clearHistoryItem.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(
                this, 
                "Are you sure you want to clear all chat history?", 
                "Confirm Clear History", 
                JOptionPane.YES_NO_OPTION
            );
            
            if (confirm == JOptionPane.YES_OPTION) {
                MessageHistory.clearHistory();
                refreshAllChats();
            }
        });
        editMenu.add(clearHistoryItem);
        
        JMenu helpMenu = new JMenu("Help");
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(e -> JOptionPane.showMessageDialog(
            this,
            "Chat Application v2.0\nCreated with Java Swing",
            "About",
            JOptionPane.INFORMATION_MESSAGE
        ));
        helpMenu.add(aboutItem);
        
        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(helpMenu);
        
        setJMenuBar(menuBar);
        
        // Initial welcome message
        appendMessage("ALL", "Welcome to the chat, " + client.getUsername() + "!", "Server");
        
        // Send button action
        sendButton.addActionListener(e -> sendMessage());
    }
    
    /**
     * Create a chat panel for a specific user
     * 
     * @param username Username
     * @return Chat panel
     */
    private JPanel createChatPanel(String username) {
        JPanel chatPanel = new JPanel(new BorderLayout());
        
        // Chat area
        JTextPane chatArea = new JTextPane();
        chatArea.setEditable(false);
        
        // Set up document styles for chat messages
        StyledDocument doc = chatArea.getStyledDocument();
        
        // Server message style
        Style serverStyle = chatArea.addStyle("Server", null);
        StyleConstants.setForeground(serverStyle, Color.BLUE);
        StyleConstants.setBold(serverStyle, true);
        
        // User message style
        Style userStyle = chatArea.addStyle("User", null);
        StyleConstants.setForeground(userStyle, Color.BLACK);
        
        // Self message style
        Style selfStyle = chatArea.addStyle("Self", null);
        StyleConstants.setForeground(selfStyle, Color.GREEN.darker());
        StyleConstants.setItalic(selfStyle, true);
        
        // Timestamp style
        Style timestampStyle = chatArea.addStyle("Timestamp", null);
        StyleConstants.setForeground(timestampStyle, Color.GRAY);
        StyleConstants.setFontSize(timestampStyle, 10);
        
        // File message style
        Style fileStyle = chatArea.addStyle("File", null);
        StyleConstants.setForeground(fileStyle, Color.MAGENTA);
        StyleConstants.setBold(fileStyle, true);
        
        JScrollPane scrollPane = new JScrollPane(chatArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        
        chatPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Store references
        chatAreas.put(username, chatArea);
        chatScrollPanes.put(username, scrollPane);
        
        return chatPanel;
    }
    
    /**
     * Set up event listeners
     */
    private void setupListeners() {
        // Window close event
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                client.disconnect();
            }
        });
        
        // User list selection
        userList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selected = userList.getSelectedValue();
                if (selected != null) {
                    selectedUser = selected;
                    switchToTab(selectedUser);
                }
            }
        });
        
        // Chat tab changed
        chatTabs.addChangeListener((ChangeEvent e) -> {
            int selectedIndex = chatTabs.getSelectedIndex();
            if (selectedIndex >= 0) {
                String tabTitle = chatTabs.getTitleAt(selectedIndex);
                if (tabTitle.equals("Everyone")) {
                    selectedUser = "ALL";
                    userList.setSelectedValue("ALL", true);
                } else {
                    selectedUser = tabTitle;
                    userList.setSelectedValue(selectedUser, true);
                }
                
                statusLabel.setText("Messaging: " + (selectedUser.equals("ALL") ? "Everyone" : selectedUser));
            }
        });
        
        // Tab closing
        chatTabs.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isMiddleMouseButton(e)) {
                    int index = chatTabs.indexAtLocation(e.getX(), e.getY());
                    if (index != -1 && !chatTabs.getTitleAt(index).equals("Everyone")) {
                        chatTabs.remove(index);
                    }
                }
            }
        });
        
        // Message handler
        client.addMessageHandler(message -> {
            if (message.getType() == MessageType.CHAT || message.getType() == MessageType.FILE) {
                // Add to message history
                MessageHistory.addMessage(message);
                
                // Format timestamp
                String timestamp = message.getTimestamp().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                
                String sender = message.getSender();
                String content = message.getContent();
                
                // Convert emoji codes if present
                if (EmojiUtil.containsEmoji(content)) {
                    content = EmojiUtil.convertToEmojis(content);
                }
                
                // Determine which tab to display in
                String targetUser;
                if (message.getReceiver().equals("ALL")) {
                    targetUser = "ALL";
                } else if (sender.equals(client.getUsername())) {
                    targetUser = message.getReceiver();
                } else {
                    targetUser = sender;
                }
                
                final String displayContent = content;
                SwingUtilities.invokeLater(() -> {
                    // Create tab if it doesn't exist (except for broadcast messages)
                    if (!chatAreas.containsKey(targetUser)) {
                        createTabForUser(targetUser);
                    }
                    
                    if (message.getType() == MessageType.FILE) {
                        if (sender.equals(client.getUsername())) {
                            appendMessage(targetUser, "[" + timestamp + "] You sent a file: " + message.getFileName() + 
                                    " (" + message.getFileSize() + ") - Click to save", "File");
                        } else {
                            appendMessage(targetUser, "[" + timestamp + "] " + sender + " sent a file: " + 
                                    message.getFileName() + " (" + message.getFileSize() + ") - Click to save", "File");
                            
                            // Flash the tab if not selected
                            if (!targetUser.equals(selectedUser)) {
                                highlightTab(targetUser);
                            }
                        }
                    } else {
                        if (sender.equals("SERVER")) {
                            appendMessage(targetUser, "[" + timestamp + "] " + displayContent, "Server");
                        } else if (sender.equals(client.getUsername())) {
                            appendMessage(targetUser, "[" + timestamp + "] You: " + displayContent, "Self");
                        } else {
                            appendMessage(targetUser, "[" + timestamp + "] " + sender + ": " + displayContent, "User");
                            
                            // Flash the tab if not selected
                            if (!targetUser.equals(selectedUser)) {
                                highlightTab(targetUser);
                            }
                        }
                    }
                });
            } else if (message.getType() == MessageType.STATUS) {
                // Handle status messages (like user list updates)
                if (message.getSender().equals("SERVER") && message.getContent().contains(",")) {
                    SwingUtilities.invokeLater(() -> {
                        updateUserList(message.getContent().split(","));
                    });
                }
            }
        });
        
        // Connection status handler
        client.addConnectionHandler(status -> {
            SwingUtilities.invokeLater(() -> {
                if (status.equals("Connection to server lost")) {
                    JOptionPane.showMessageDialog(this, 
                        "Connection to server lost. The application will now close.", 
                        "Connection Error", 
                        JOptionPane.ERROR_MESSAGE);
                    dispose();
                    System.exit(0);
                }
            });
        });
    }
    
    /**
     * Send a message to the selected recipient
     */
    private void sendMessage() {
        String content = messageField.getText().trim();
        if (!content.isEmpty()) {
            client.sendChatMessage(content, selectedUser);
            messageField.setText("");
        }
    }
    
    /**
     * Choose and send a file to the selected recipient
     */
    private void chooseAndSendFile() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(this);
        
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                if (selectedFile.length() > 1024 * 1024 * 10) { // 10MB limit
                    JOptionPane.showMessageDialog(this,
                        "File is too large. Maximum file size is 10MB.",
                        "File Too Large",
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                byte[] fileData = Files.readAllBytes(selectedFile.toPath());
                client.sendFileMessage(selectedFile.getName(), fileData, selectedUser);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this,
                    "Error reading file: " + e.getMessage(),
                    "File Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    /**
     * Show emoji selection menu
     */
    private void showEmojiMenu() {
        emojiMenu.show(emojiButton, 0, emojiButton.getHeight());
    }
    
    /**
     * Show user profile dialog
     */
    private void showProfileDialog() {
        // Create and show profile dialog
        JDialog profileDialog = new JDialog(this, "User Profile", true);
        profileDialog.setLayout(new BorderLayout());
        profileDialog.setSize(400, 300);
        profileDialog.setLocationRelativeTo(this);
        
        JPanel profilePanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Username (non-editable)
        gbc.gridx = 0;
        gbc.gridy = 0;
        profilePanel.add(new JLabel("Username:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        JTextField usernameField = new JTextField(client.getUsername());
        usernameField.setEditable(false);
        profilePanel.add(usernameField, gbc);
        
        // Status
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.0;
        profilePanel.add(new JLabel("Status:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        String[] statuses = {"Available", "Away", "Busy", "Do Not Disturb"};
        JComboBox<String> statusComboBox = new JComboBox<>(statuses);
        profilePanel.add(statusComboBox, gbc);
        
        // Bio
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.0;
        profilePanel.add(new JLabel("Bio:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        JTextArea bioArea = new JTextArea(3, 20);
        bioArea.setLineWrap(true);
        bioArea.setWrapStyleWord(true);
        JScrollPane bioScrollPane = new JScrollPane(bioArea);
        profilePanel.add(bioScrollPane, gbc);
        
        // Buttons
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.weightx = 0.0;
        gbc.anchor = GridBagConstraints.CENTER;
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");
        
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        profilePanel.add(buttonPanel, gbc);
        
        // Button actions
        saveButton.addActionListener(e -> {
            // In a real implementation, this would update the user profile on the server
            JOptionPane.showMessageDialog(profileDialog, "Profile updated", "Success", JOptionPane.INFORMATION_MESSAGE);
            profileDialog.dispose();
        });
        
        cancelButton.addActionListener(e -> profileDialog.dispose());
        
        profileDialog.add(profilePanel, BorderLayout.CENTER);
        profileDialog.setVisible(true);
    }
    
    /**
     * Toggle dark mode
     */
    private void toggleDarkMode() {
        isDarkMode = darkModeButton.isSelected();
        
        Color bgColor = isDarkMode ? DARK_BG : LIGHT_BG;
        Color fgColor = isDarkMode ? DARK_TEXT : LIGHT_TEXT;
        
        // Update UI colors
        for (JTextPane chatArea : chatAreas.values()) {
            chatArea.setBackground(bgColor);
            chatArea.setForeground(fgColor);
            
            // Update styles
            if (isDarkMode) {
                StyleConstants.setForeground(chatArea.getStyle("Server"), Color.CYAN);
                StyleConstants.setForeground(chatArea.getStyle("User"), DARK_TEXT);
                StyleConstants.setForeground(chatArea.getStyle("Self"), Color.GREEN);
                StyleConstants.setForeground(chatArea.getStyle("Timestamp"), Color.GRAY);
                StyleConstants.setForeground(chatArea.getStyle("File"), Color.MAGENTA);
            } else {
                StyleConstants.setForeground(chatArea.getStyle("Server"), Color.BLUE);
                StyleConstants.setForeground(chatArea.getStyle("User"), LIGHT_TEXT);
                StyleConstants.setForeground(chatArea.getStyle("Self"), Color.GREEN.darker());
                StyleConstants.setForeground(chatArea.getStyle("Timestamp"), Color.GRAY);
                StyleConstants.setForeground(chatArea.getStyle("File"), Color.MAGENTA);
            }
        }
        
        userList.setBackground(bgColor);
        userList.setForeground(fgColor);
        messageField.setBackground(bgColor);
        messageField.setForeground(fgColor);
        
        // Refresh UI
        SwingUtilities.updateComponentTreeUI(this);
    }
    
    /**
     * Append a message to the chat area with the specified style
     * 
     * @param targetUser Target user tab
     * @param message Message to append
     * @param styleType Style type (Server, User, Self, or File)
     */
    private void appendMessage(String targetUser, String message, String styleType) {
        JTextPane chatArea = chatAreas.get(targetUser);
        if (chatArea == null) {
            return;
        }
        
        StyledDocument doc = chatArea.getStyledDocument();
        try {
            doc.insertString(doc.getLength(), message + "\n", chatArea.getStyle(styleType));
            
            // If it's a file message, add a mouse listener to handle saving
            if (styleType.equals("File")) {
                final int offset = doc.getLength() - message.length() - 1;
                final int length = message.length();
                
                chatArea.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        int pos = chatArea.viewToModel2D(e.getPoint());
                        if (pos >= offset && pos <= offset + length) {
                            saveFileDialog(message);
                        }
                    }
                });
            }
            
            // Scroll to bottom
            JScrollPane scrollPane = chatScrollPanes.get(targetUser);
            if (scrollPane != null) {
                JScrollBar verticalBar = scrollPane.getVerticalScrollBar();
                verticalBar.setValue(verticalBar.getMaximum());
            }
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Show dialog to save a received file
     * 
     * @param message Message containing file info
     */
    private void saveFileDialog(String message) {
        // For a real implementation, this would extract the file data and save it
        JOptionPane.showMessageDialog(this,
            "File saving would be implemented here.",
            "Save File",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Create a new tab for a user
     * 
     * @param username Username
     */
    private void createTabForUser(String username) {
        if (!chatAreas.containsKey(username)) {
            JPanel chatPanel = createChatPanel(username);
            String tabTitle = username.equals("ALL") ? "Everyone" : username;
            chatTabs.addTab(tabTitle, chatPanel);
        }
    }
    
    /**
     * Switch to a tab for a specific user
     * 
     * @param username Username
     */
    private void switchToTab(String username) {
        if (!chatAreas.containsKey(username)) {
            createTabForUser(username);
        }
        
        for (int i = 0; i < chatTabs.getTabCount(); i++) {
            String tabTitle = chatTabs.getTitleAt(i);
            if ((username.equals("ALL") && tabTitle.equals("Everyone")) || tabTitle.equals(username)) {
                chatTabs.setSelectedIndex(i);
                statusLabel.setText("Messaging: " + (username.equals("ALL") ? "Everyone" : username));
                break;
            }
        }
    }
    
    /**
     * Highlight a tab to indicate new messages
     * 
     * @param username Username
     */
    private void highlightTab(String username) {
        for (int i = 0; i < chatTabs.getTabCount(); i++) {
            String tabTitle = chatTabs.getTitleAt(i);
            if ((username.equals("ALL") && tabTitle.equals("Everyone")) || tabTitle.equals(username)) {
                if (i != chatTabs.getSelectedIndex()) {
                    chatTabs.setForegroundAt(i, Color.RED);
                }
                break;
            }
        }
    }
    
    /**
     * Update the list of online users
     * 
     * @param users Array of usernames
     */
    private void updateUserList(String[] users) {
        // Store current selection
        String currentSelection = userList.getSelectedValue();
        
        // Clear the list but keep "ALL" option
        userListModel.removeAllElements();
        userListModel.addElement("ALL");
        
        // Add online users
        for (String user : users) {
            if (!user.equals(client.getUsername())) {
                userListModel.addElement(user);
            }
        }
        
        // Restore selection or select "ALL" if previous selection is no longer available
        if (currentSelection != null && userListModel.contains(currentSelection)) {
            userList.setSelectedValue(currentSelection, true);
        } else {
            userList.setSelectedValue("ALL", true);
            selectedUser = "ALL";
        }
    }
    
    /**
     * Refresh all chat tabs with current message history
     */
    private void refreshAllChats() {
        for (String username : chatAreas.keySet()) {
            JTextPane chatArea = chatAreas.get(username);
            chatArea.setText("");
        }
    }
    
    /**
     * Custom cell renderer for the user list
     */
    private class UserListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            
            String username = (String) value;
            if (username.equals("ALL")) {
                label.setText("Everyone");
                label.setIcon(new ImageIcon(getClass().getResource("/icons/group.png"))); // You would need to add this resource
            } else {
                label.setText(username);
                // In a real implementation, you could show online status here
            }
            
            return label;
        }
    }
}