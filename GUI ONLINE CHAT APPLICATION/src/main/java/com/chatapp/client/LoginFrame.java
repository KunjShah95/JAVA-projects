package com.chatapp.client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Frame for user login and registration
 */
public class LoginFrame extends JFrame {
    private JPanel loginPanel;
    private JPanel registerPanel;
    private ForgotPasswordPanel forgotPasswordPanel;
    private CardLayout cardLayout;
    private JPanel cardPanel;
    
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;
    private JLabel statusLabel;
    private JTextField registerUsernameField;
    private JPasswordField registerPasswordField;
    private JPasswordField confirmPasswordField;
    private JTextField emailField;
    private JButton createAccountButton;
    private JButton backToLoginButton;
    private JLabel registerStatusLabel;
    private JLabel forgotPasswordLabel;
    
    private ChatClient client;
    
    /**
     * Create a new login frame
     * @param client The chat client
     */
    public LoginFrame(ChatClient client) {
        this.client = client;
        
        setTitle("Chat Application - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 450);
        setLocationRelativeTo(null);
        
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        
        createLoginPanel();
        createRegisterPanel();
        createForgotPasswordPanel();
        
        cardPanel.add(loginPanel, "login");
        cardPanel.add(registerPanel, "register");
        cardPanel.add(forgotPasswordPanel, "forgotPassword");
        
        getContentPane().add(cardPanel);
        
        cardLayout.show(cardPanel, "login");
    }
    
    /**
     * Create the login panel
     */
    private void createLoginPanel() {
        loginPanel = new JPanel(new BorderLayout());
        
        // Header panel
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(59, 89, 182));
        headerPanel.setPreferredSize(new Dimension(400, 60));
        JLabel titleLabel = new JLabel("Chat Application");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);
        
        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Username
        JLabel usernameLabel = new JLabel("Username:");
        usernameField = new JTextField(20);
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(usernameLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 0;
        formPanel.add(usernameField, gbc);
        
        // Password
        JLabel passwordLabel = new JLabel("Password:");
        passwordField = new JPasswordField(20);
        
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(passwordLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 1;
        formPanel.add(passwordField, gbc);
        
        // Login button
        loginButton = new JButton("Login");
        loginButton.setBackground(new Color(59, 89, 182));
        loginButton.setForeground(Color.WHITE);
        
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(15, 5, 5, 5);
        formPanel.add(loginButton, gbc);
        
        // Register button
        registerButton = new JButton("Register New Account");
        
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(5, 5, 5, 5);
        formPanel.add(registerButton, gbc);
        
        // Forgot password link
        forgotPasswordLabel = new JLabel("Forgot Password?");
        forgotPasswordLabel.setForeground(Color.BLUE);
        forgotPasswordLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        formPanel.add(forgotPasswordLabel, gbc);
        
        // Status label
        statusLabel = new JLabel(" ");
        statusLabel.setForeground(Color.RED);
        
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 5, 5, 5);
        formPanel.add(statusLabel, gbc);
        
        loginPanel.add(headerPanel, BorderLayout.NORTH);
        loginPanel.add(formPanel, BorderLayout.CENTER);
        
        // Add action listeners
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                login();
            }
        });
        
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(cardPanel, "register");
            }
        });
        
        forgotPasswordLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                cardLayout.show(cardPanel, "forgotPassword");
                forgotPasswordPanel.resetFields();
            }
        });
    }
    
    /**
     * Create the register panel
     */
    private void createRegisterPanel() {
        registerPanel = new JPanel(new BorderLayout());
        
        // Header panel
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(59, 89, 182));
        headerPanel.setPreferredSize(new Dimension(400, 60));
        JLabel titleLabel = new JLabel("Create New Account");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);
        
        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Username
        JLabel usernameLabel = new JLabel("Username:");
        registerUsernameField = new JTextField(20);
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(usernameLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 0;
        formPanel.add(registerUsernameField, gbc);
        
        // Email
        JLabel emailLabel = new JLabel("Email:");
        emailField = new JTextField(20);
        
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(emailLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 1;
        formPanel.add(emailField, gbc);
        
        // Password
        JLabel passwordLabel = new JLabel("Password:");
        registerPasswordField = new JPasswordField(20);
        
        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(passwordLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 2;
        formPanel.add(registerPasswordField, gbc);
        
        // Confirm Password
        JLabel confirmPasswordLabel = new JLabel("Confirm Password:");
        confirmPasswordField = new JPasswordField(20);
        
        gbc.gridx = 0;
        gbc.gridy = 3;
        formPanel.add(confirmPasswordLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 3;
        formPanel.add(confirmPasswordField, gbc);
        
        // Create Account button
        createAccountButton = new JButton("Create Account");
        createAccountButton.setBackground(new Color(59, 89, 182));
        createAccountButton.setForeground(Color.WHITE);
        
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(15, 5, 5, 5);
        formPanel.add(createAccountButton, gbc);
        
        // Back to Login button
        backToLoginButton = new JButton("Back to Login");
        
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(5, 5, 5, 5);
        formPanel.add(backToLoginButton, gbc);
        
        // Status label
        registerStatusLabel = new JLabel(" ");
        registerStatusLabel.setForeground(Color.RED);
        
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 5, 5, 5);
        formPanel.add(registerStatusLabel, gbc);
        
        registerPanel.add(headerPanel, BorderLayout.NORTH);
        registerPanel.add(formPanel, BorderLayout.CENTER);
        
        // Add action listeners
        createAccountButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                register();
            }
        });
        
        backToLoginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(cardPanel, "login");
            }
        });
    }
    
    /**
     * Create the forgot password panel
     */
    private void createForgotPasswordPanel() {
        forgotPasswordPanel = new ForgotPasswordPanel(client, this);
    }
    
    /**
     * Show the login panel
     */
    public void showLoginPanel() {
        cardLayout.show(cardPanel, "login");
    }
    
    /**
     * Show the register panel
     */
    public void showRegisterPanel() {
        cardLayout.show(cardPanel, "register");
    }
    
    /**
     * Show the forgot password panel
     */
    public void showForgotPasswordPanel() {
        cardLayout.show(cardPanel, "forgotPassword");
    }
    
    /**
     * Handle the login process
     */
    private void login() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        
        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Username and password are required");
            return;
        }
        
        statusLabel.setText("Logging in...");
        client.login(username, password);
    }
    
    /**
     * Handle the registration process
     */
    private void register() {
        String username = registerUsernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = new String(registerPasswordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());
        
        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            registerStatusLabel.setText("All fields are required");
            return;
        }
        
        if (!password.equals(confirmPassword)) {
            registerStatusLabel.setText("Passwords do not match");
            return;
        }
        
        if (password.length() < 6) {
            registerStatusLabel.setText("Password must be at least 6 characters");
            return;
        }
        
        // Validate email format
        if (!isValidEmail(email)) {
            registerStatusLabel.setText("Please enter a valid email address");
            return;
        }
        
        registerStatusLabel.setText("Creating account...");
        client.register(username, password, email);
    }
    
    /**
     * Handle login response from server
     * @param success Whether login was successful
     * @param message Message from server
     */
    public void handleLoginResponse(boolean success, String message) {
        if (success) {
            dispose(); // Close login frame
        } else {
            statusLabel.setText(message);
        }
    }
    
    /**
     * Handle registration response from server
     * @param success Whether registration was successful
     * @param message Message from server
     */
    public void handleRegisterResponse(boolean success, String message) {
        if (success) {
            JOptionPane.showMessageDialog(this, 
                    "Registration successful! You can now login.",
                    "Registration Successful", 
                    JOptionPane.INFORMATION_MESSAGE);
            cardLayout.show(cardPanel, "login");
            registerUsernameField.setText("");
            emailField.setText("");
            registerPasswordField.setText("");
            confirmPasswordField.setText("");
        } else {
            registerStatusLabel.setText(message);
        }
    }
    
    /**
     * Handle password reset response from server
     * @param success Whether reset was successful
     * @param message Message from server
     */
    public void handlePasswordResetResponse(boolean success, String message) {
        if (success) {
            forgotPasswordPanel.handlePasswordResetResponse(true);
        } else {
            forgotPasswordPanel.handlePasswordResetResponse(false);
        }
    }
    
    /**
     * Handle reset code response from server
     * @param resetCode Reset code from server
     */
    public void handleResetCodeResponse(String resetCode) {
        forgotPasswordPanel.handleResetCodeResponse(resetCode);
    }
    
    /**
     * Validate email format
     * @param email Email to validate
     * @return Whether email is valid
     */
    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }
} 