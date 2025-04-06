package com.chatapp.client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Login window for the chat application
 */
public class LoginWindow extends JFrame {
    private ChatClient client;
    
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JTextField emailField;
    private JButton loginButton;
    private JButton registerButton;
    private JLabel statusLabel;
    private JLabel forgotPasswordLink;
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private JPanel loginPanel;
    private JPanel registerPanel;
    
    /**
     * Create a new login window
     */
    public LoginWindow() {
        setTitle("Chat Application - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setSize(400, 300);
        setLocationRelativeTo(null);
        
        // Initialize client
        client = new ChatClient();
        
        // Set up UI
        initComponents();
        setupListeners();
        
        // Connect to server
        new Thread(() -> {
            if (!client.connect()) {
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("Failed to connect to server");
                });
            }
        }).start();
        
        setVisible(true);
    }
    
    /**
     * Initialize UI components
     */
    private void initComponents() {
        // Main panel with card layout
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        
        // Login panel
        loginPanel = new JPanel(new BorderLayout());
        JPanel loginFieldsPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Username field
        gbc.gridx = 0;
        gbc.gridy = 0;
        loginFieldsPanel.add(new JLabel("Username:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        usernameField = new JTextField(15);
        loginFieldsPanel.add(usernameField, gbc);
        
        // Password field
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.0;
        loginFieldsPanel.add(new JLabel("Password:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        passwordField = new JPasswordField(15);
        loginFieldsPanel.add(passwordField, gbc);
        
        // Login button
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.weightx = 0.0;
        gbc.anchor = GridBagConstraints.CENTER;
        JPanel buttonPanel = new JPanel(new FlowLayout());
        
        loginButton = new JButton("Login");
        buttonPanel.add(loginButton);
        
        JButton switchToRegisterButton = new JButton("Register New Account");
        buttonPanel.add(switchToRegisterButton);
        
        loginFieldsPanel.add(buttonPanel, gbc);
        
        // Status label
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        statusLabel = new JLabel("Connecting to server...");
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        loginFieldsPanel.add(statusLabel, gbc);
        
        // Add Forgot Password link
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        forgotPasswordLink = new JLabel("<html><a href=''>Forgot Password?</a></html>");
        forgotPasswordLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
        forgotPasswordLink.setHorizontalAlignment(SwingConstants.CENTER);
        loginFieldsPanel.add(forgotPasswordLink, gbc);
        
        // Add padding
        JPanel paddedLoginPanel = new JPanel(new BorderLayout());
        paddedLoginPanel.add(loginFieldsPanel, BorderLayout.CENTER);
        paddedLoginPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Add title
        JLabel titleLabel = new JLabel("Chat Application Login", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        paddedLoginPanel.add(titleLabel, BorderLayout.NORTH);
        
        loginPanel.add(paddedLoginPanel, BorderLayout.CENTER);
        
        // Register panel
        registerPanel = new JPanel(new BorderLayout());
        JPanel registerFieldsPanel = new JPanel(new GridBagLayout());
        
        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Username field
        gbc.gridx = 0;
        gbc.gridy = 0;
        registerFieldsPanel.add(new JLabel("Username:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        JTextField regUsernameField = new JTextField(15);
        registerFieldsPanel.add(regUsernameField, gbc);
        
        // Password field
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.0;
        registerFieldsPanel.add(new JLabel("Password:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        JPasswordField regPasswordField = new JPasswordField(15);
        registerFieldsPanel.add(regPasswordField, gbc);
        
        // Email field
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.0;
        registerFieldsPanel.add(new JLabel("Email:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        emailField = new JTextField(15);
        registerFieldsPanel.add(emailField, gbc);
        
        // Register button
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.weightx = 0.0;
        gbc.anchor = GridBagConstraints.CENTER;
        JPanel regButtonPanel = new JPanel(new FlowLayout());
        
        registerButton = new JButton("Register");
        regButtonPanel.add(registerButton);
        
        JButton switchToLoginButton = new JButton("Back to Login");
        regButtonPanel.add(switchToLoginButton);
        
        registerFieldsPanel.add(regButtonPanel, gbc);
        
        // Add padding
        JPanel paddedRegisterPanel = new JPanel(new BorderLayout());
        paddedRegisterPanel.add(registerFieldsPanel, BorderLayout.CENTER);
        paddedRegisterPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Add title
        JLabel regTitleLabel = new JLabel("Create New Account", SwingConstants.CENTER);
        regTitleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        paddedRegisterPanel.add(regTitleLabel, BorderLayout.NORTH);
        
        registerPanel.add(paddedRegisterPanel, BorderLayout.CENTER);
        
        // Add panels to card layout
        mainPanel.add(loginPanel, "login");
        mainPanel.add(registerPanel, "register");
        
        // Show login panel by default
        cardLayout.show(mainPanel, "login");
        
        // Add panel to frame
        add(mainPanel);
        
        // Switch between panels
        switchToRegisterButton.addActionListener(e -> cardLayout.show(mainPanel, "register"));
        switchToLoginButton.addActionListener(e -> cardLayout.show(mainPanel, "login"));
        
        // Login with user credentials
        loginButton.addActionListener(e -> {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            
            if (username.isBlank() || password.isBlank()) {
                statusLabel.setText("Username and password cannot be empty");
                return;
            }
            
            statusLabel.setText("Logging in...");
            loginButton.setEnabled(false);
            
            new Thread(() -> {
                client.login(username, password);
            }).start();
        });
        
        // Register button action
        registerButton.addActionListener(e -> {
            String username = regUsernameField.getText();
            String password = new String(regPasswordField.getPassword());
            String email = emailField.getText();
            
            if (username.isBlank() || password.isBlank() || email.isBlank()) {
                statusLabel.setText("All fields are required");
                return;
            }
            
            statusLabel.setText("Registering...");
            registerButton.setEnabled(false);
            
            new Thread(() -> {
                client.register(username, password, email);
            }).start();
        });
    }
    
    /**
     * Set up event listeners
     */
    private void setupListeners() {
        // Window close event
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (client != null) {
                    client.disconnect();
                }
            }
        });
        
        // Connection status updates
        client.addConnectionHandler(status -> {
            SwingUtilities.invokeLater(() -> {
                statusLabel.setText(status);
                loginButton.setEnabled(true);
                registerButton.setEnabled(true);
            });
        });
        
        // Message handler
        client.addMessageHandler(message -> {
            if (message.getType() == com.chatapp.model.Message.MessageType.STATUS) {
                if (message.getContent().equals("Login successful")) {
                    SwingUtilities.invokeLater(() -> {
                        // Open main chat window
                        new ChatWindow(client);
                        dispose(); // Close login window
                    });
                } else if (message.getContent().equals("Registration successful")) {
                    SwingUtilities.invokeLater(() -> {
                        // Switch to login panel
                        cardLayout.show(mainPanel, "login");
                        statusLabel.setText("Registration successful. Please login.");
                        loginButton.setEnabled(true);
                        registerButton.setEnabled(true);
                    });
                } else {
                    SwingUtilities.invokeLater(() -> {
                        statusLabel.setText(message.getContent());
                        loginButton.setEnabled(true);
                        registerButton.setEnabled(true);
                    });
                }
            }
        });
        
        // Forgot password link action
        forgotPasswordLink.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                ForgotPasswordDialog dialog = new ForgotPasswordDialog(LoginWindow.this, client);
                dialog.setVisible(true);
            }
        });
    }
    
    /**
     * Main method to start the application
     */
    public static void main(String[] args) {
        try {
            // Set look and feel to the system look and feel
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> {
            new LoginWindow();
        });
    }
} 