package com.chatapp.client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

/**
 * Panel for handling password reset functionality
 */
public class ForgotPasswordPanel extends JPanel {
    private JTextField emailField;
    private JTextField resetCodeField;
    private JPasswordField newPasswordField;
    private JPasswordField confirmPasswordField;
    private JButton requestCodeButton;
    private JButton resetPasswordButton;
    private JButton backButton;
    private JLabel statusLabel;
    
    private ChatClient client;
    private LoginFrame loginFrame;
    
    /**
     * Create a new forgot password panel
     * @param client The chat client
     * @param loginFrame The login frame
     */
    public ForgotPasswordPanel(ChatClient client, LoginFrame loginFrame) {
        this.client = client;
        this.loginFrame = loginFrame;
        
        setLayout(new BorderLayout());
        
        // Create header
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(59, 89, 182));
        JLabel headerLabel = new JLabel("Reset Password");
        headerLabel.setForeground(Color.WHITE);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 18));
        headerPanel.add(headerLabel);
        add(headerPanel, BorderLayout.NORTH);
        
        // Create main panel
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Add email field
        JLabel emailLabel = new JLabel("Email Address:");
        mainPanel.add(emailLabel, gbc);
        
        emailField = new JTextField(20);
        mainPanel.add(emailField, gbc);
        
        // Add request code button
        requestCodeButton = new JButton("Request Reset Code");
        requestCodeButton.setBackground(new Color(59, 89, 182));
        requestCodeButton.setForeground(Color.WHITE);
        mainPanel.add(requestCodeButton, gbc);
        
        // Add separator
        JSeparator separator = new JSeparator();
        gbc.insets = new Insets(20, 5, 20, 5);
        mainPanel.add(separator, gbc);
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Add reset code field
        JLabel resetCodeLabel = new JLabel("Reset Code:");
        mainPanel.add(resetCodeLabel, gbc);
        
        resetCodeField = new JTextField(20);
        mainPanel.add(resetCodeField, gbc);
        
        // Add new password field
        JLabel newPasswordLabel = new JLabel("New Password:");
        mainPanel.add(newPasswordLabel, gbc);
        
        newPasswordField = new JPasswordField(20);
        mainPanel.add(newPasswordField, gbc);
        
        // Add confirm password field
        JLabel confirmPasswordLabel = new JLabel("Confirm Password:");
        mainPanel.add(confirmPasswordLabel, gbc);
        
        confirmPasswordField = new JPasswordField(20);
        mainPanel.add(confirmPasswordField, gbc);
        
        // Add reset password button
        resetPasswordButton = new JButton("Reset Password");
        resetPasswordButton.setBackground(new Color(59, 89, 182));
        resetPasswordButton.setForeground(Color.WHITE);
        mainPanel.add(resetPasswordButton, gbc);
        
        // Add status label
        statusLabel = new JLabel(" ");
        statusLabel.setForeground(Color.RED);
        mainPanel.add(statusLabel, gbc);
        
        add(mainPanel, BorderLayout.CENTER);
        
        // Add back button
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        backButton = new JButton("Back to Login");
        bottomPanel.add(backButton);
        add(bottomPanel, BorderLayout.SOUTH);
        
        // Add action listeners
        addActionListeners();
    }
    
    /**
     * Add action listeners to buttons
     */
    private void addActionListeners() {
        // Request code button
        requestCodeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String email = emailField.getText().trim();
                if (email.isEmpty()) {
                    statusLabel.setText("Please enter your email address");
                    return;
                }
                
                // Validate email format
                if (!isValidEmail(email)) {
                    statusLabel.setText("Please enter a valid email address");
                    return;
                }
                
                // Request reset code
                try {
                    client.sendResetRequest(email);
                    statusLabel.setText("Reset code sent to your email. Check your inbox.");
                    statusLabel.setForeground(new Color(0, 128, 0));
                } catch (IOException ex) {
                    statusLabel.setText("Error: " + ex.getMessage());
                    statusLabel.setForeground(Color.RED);
                }
            }
        });
        
        // Reset password button
        resetPasswordButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String email = emailField.getText().trim();
                String resetCode = resetCodeField.getText().trim();
                String newPassword = new String(newPasswordField.getPassword());
                String confirmPassword = new String(confirmPasswordField.getPassword());
                
                // Validate inputs
                if (email.isEmpty() || resetCode.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                    statusLabel.setText("All fields are required");
                    statusLabel.setForeground(Color.RED);
                    return;
                }
                
                if (!newPassword.equals(confirmPassword)) {
                    statusLabel.setText("Passwords do not match");
                    statusLabel.setForeground(Color.RED);
                    return;
                }
                
                if (newPassword.length() < 6) {
                    statusLabel.setText("Password must be at least 6 characters");
                    statusLabel.setForeground(Color.RED);
                    return;
                }
                
                // Send password reset request
                try {
                    client.sendPasswordReset(email, resetCode, newPassword);
                    statusLabel.setText("Password reset request sent");
                    statusLabel.setForeground(new Color(0, 128, 0));
                } catch (IOException ex) {
                    statusLabel.setText("Error: " + ex.getMessage());
                    statusLabel.setForeground(Color.RED);
                }
            }
        });
        
        // Back button
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loginFrame.showLoginPanel();
            }
        });
    }
    
    /**
     * Handle server response to reset request
     * @param success Whether the reset was successful
     */
    public void handlePasswordResetResponse(boolean success) {
        if (success) {
            JOptionPane.showMessageDialog(this, 
                    "Password has been reset successfully. You can now login with your new password.",
                    "Password Reset Successful", 
                    JOptionPane.INFORMATION_MESSAGE);
            loginFrame.showLoginPanel();
        } else {
            statusLabel.setText("Password reset failed. Please check your email and reset code.");
            statusLabel.setForeground(Color.RED);
        }
    }
    
    /**
     * Handle server response to reset code request
     * @param resetCode The reset code from the server
     */
    public void handleResetCodeResponse(String resetCode) {
        // In a real application, the code would be sent to the user's email
        // For testing purposes, we can show it in a dialog
        JOptionPane.showMessageDialog(this, 
                "Your password reset code is: " + resetCode + "\n" +
                "(In a real application, this would be sent to your email)",
                "Reset Code", 
                JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Check if email is valid format
     * @param email Email to check
     * @return true if valid, false otherwise
     */
    private boolean isValidEmail(String email) {
        // Simple email validation
        return email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }
    
    /**
     * Reset all fields
     */
    public void resetFields() {
        emailField.setText("");
        resetCodeField.setText("");
        newPasswordField.setText("");
        confirmPasswordField.setText("");
        statusLabel.setText(" ");
    }
} 