package com.chatapp.client;

import com.chatapp.model.Message;
import com.chatapp.model.Message.MessageType;
import com.chatapp.util.UserAuthentication;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Dialog for resetting a forgotten password
 */
public class ForgotPasswordDialog extends JDialog {
    private JTextField emailField;
    private JTextField codeField;
    private JPasswordField newPasswordField;
    private JPasswordField confirmPasswordField;
    private JButton requestCodeButton;
    private JButton resetPasswordButton;
    private JButton cancelButton;
    private JLabel statusLabel;
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private JPanel requestPanel;
    private JPanel resetPanel;
    private ChatClient client;
    
    /**
     * Create a new forgot password dialog
     * 
     * @param parent Parent frame
     * @param client Chat client
     */
    public ForgotPasswordDialog(Frame parent, ChatClient client) {
        super(parent, "Reset Password", true);
        this.client = client;
        
        setSize(400, 300);
        setLocationRelativeTo(parent);
        setResizable(false);
        
        initComponents();
        setupListeners();
    }
    
    /**
     * Initialize UI components
     */
    private void initComponents() {
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        
        // ===== Request Code Panel =====
        requestPanel = new JPanel(new BorderLayout());
        JPanel requestFieldsPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Title
        JLabel titleLabel = new JLabel("Forgot Password", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        
        // Email field
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 0.0;
        requestFieldsPanel.add(new JLabel("Email:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        emailField = new JTextField(20);
        requestFieldsPanel.add(emailField, gbc);
        
        // Request button
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.weightx = 0.5;
        requestCodeButton = new JButton("Request Code");
        requestFieldsPanel.add(requestCodeButton, gbc);
        
        // Cancel button
        gbc.gridx = 2;
        gbc.gridy = 1;
        cancelButton = new JButton("Cancel");
        requestFieldsPanel.add(cancelButton, gbc);
        
        // Status label
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 3;
        gbc.weightx = 1.0;
        statusLabel = new JLabel("Enter your email to receive a reset code", SwingConstants.CENTER);
        statusLabel.setForeground(Color.GRAY);
        requestFieldsPanel.add(statusLabel, gbc);
        
        // Add to panel
        requestPanel.add(titleLabel, BorderLayout.NORTH);
        requestPanel.add(requestFieldsPanel, BorderLayout.CENTER);
        requestPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // ===== Reset Password Panel =====
        resetPanel = new JPanel(new BorderLayout());
        JPanel resetFieldsPanel = new JPanel(new GridBagLayout());
        
        // Title
        JLabel resetTitleLabel = new JLabel("Reset Password", SwingConstants.CENTER);
        resetTitleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        resetTitleLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        
        // Code field
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 0.0;
        resetFieldsPanel.add(new JLabel("Reset Code:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        codeField = new JTextField(10);
        resetFieldsPanel.add(codeField, gbc);
        
        // New password field
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.weightx = 0.0;
        resetFieldsPanel.add(new JLabel("New Password:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        newPasswordField = new JPasswordField(20);
        resetFieldsPanel.add(newPasswordField, gbc);
        
        // Confirm password field
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.weightx = 0.0;
        resetFieldsPanel.add(new JLabel("Confirm Password:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        confirmPasswordField = new JPasswordField(20);
        resetFieldsPanel.add(confirmPasswordField, gbc);
        
        // Reset button
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        gbc.weightx = 0.5;
        resetPasswordButton = new JButton("Reset Password");
        resetFieldsPanel.add(resetPasswordButton, gbc);
        
        // Back button
        gbc.gridx = 2;
        gbc.gridy = 3;
        JButton backButton = new JButton("Back");
        backButton.addActionListener(e -> cardLayout.show(mainPanel, "request"));
        resetFieldsPanel.add(backButton, gbc);
        
        // Add to panel
        resetPanel.add(resetTitleLabel, BorderLayout.NORTH);
        resetPanel.add(resetFieldsPanel, BorderLayout.CENTER);
        resetPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Add panels to card layout
        mainPanel.add(requestPanel, "request");
        mainPanel.add(resetPanel, "reset");
        
        // Display request panel first
        cardLayout.show(mainPanel, "request");
        
        // Add to dialog
        add(mainPanel);
    }
    
    /**
     * Set up event listeners
     */
    private void setupListeners() {
        // Cancel button
        cancelButton.addActionListener(e -> dispose());
        
        // Request code button
        requestCodeButton.addActionListener(e -> {
            String email = emailField.getText().trim();
            
            // Validate email
            if (email.isEmpty() || !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                statusLabel.setText("Please enter a valid email address");
                statusLabel.setForeground(Color.RED);
                return;
            }
            
            // Send request to server
            if (client.isConnected()) {
                // Disable button to prevent multiple requests
                requestCodeButton.setEnabled(false);
                requestCodeButton.setText("Sending...");
                
                // Set message handler for reset code response
                client.addMessageHandler(msg -> {
                    if (msg.getType() == MessageType.STATUS && msg.getSender().equals("SERVER")) {
                        if (msg.getContent().startsWith("RESET_CODE:")) {
                            String[] parts = msg.getContent().split(":");
                            if (parts.length >= 2) {
                                String code = parts[1];
                                SwingUtilities.invokeLater(() -> {
                                    codeField.setText(code);  // Pre-fill the code for demo purposes
                                    cardLayout.show(mainPanel, "reset");
                                    requestCodeButton.setEnabled(true);
                                    requestCodeButton.setText("Request Code");
                                });
                            }
                        } else if (msg.getContent().equals("RESET_FAILED")) {
                            SwingUtilities.invokeLater(() -> {
                                statusLabel.setText("Email not found. Please try again.");
                                statusLabel.setForeground(Color.RED);
                                requestCodeButton.setEnabled(true);
                                requestCodeButton.setText("Request Code");
                            });
                        }
                    }
                });
                
                // Send the request
                try {
                    client.sendResetRequest(email);
                } catch (Exception ex) {
                    statusLabel.setText("Error: " + ex.getMessage());
                    statusLabel.setForeground(Color.RED);
                    requestCodeButton.setEnabled(true);
                    requestCodeButton.setText("Request Code");
                }
            }
        });
        
        // Reset password button
        resetPasswordButton.addActionListener(e -> {
            String code = codeField.getText().trim();
            String email = emailField.getText().trim();
            String newPassword = new String(newPasswordField.getPassword());
            String confirmPassword = new String(confirmPasswordField.getPassword());
            
            // Validate input
            if (code.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter the reset code", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter and confirm your new password", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (!newPassword.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(this, "Passwords do not match", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Send reset request to server
            if (client.isConnected()) {
                // Disable button to prevent multiple requests
                resetPasswordButton.setEnabled(false);
                resetPasswordButton.setText("Resetting...");
                
                // Set message handler for reset result
                client.addMessageHandler(msg -> {
                    if (msg.getType() == MessageType.STATUS && msg.getSender().equals("SERVER")) {
                        if (msg.getContent().equals("RESET_SUCCESS")) {
                            SwingUtilities.invokeLater(() -> {
                                JOptionPane.showMessageDialog(
                                    this,
                                    "Password has been reset successfully.\nYou can now login with your new password.",
                                    "Success",
                                    JOptionPane.INFORMATION_MESSAGE
                                );
                                dispose();
                            });
                        } else if (msg.getContent().equals("RESET_FAILED")) {
                            SwingUtilities.invokeLater(() -> {
                                JOptionPane.showMessageDialog(
                                    this,
                                    "Failed to reset password. Please check your code and try again.",
                                    "Error",
                                    JOptionPane.ERROR_MESSAGE
                                );
                                resetPasswordButton.setEnabled(true);
                                resetPasswordButton.setText("Reset Password");
                            });
                        }
                    }
                });
                
                // Send the request
                try {
                    client.sendPasswordReset(email, code, newPassword);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    resetPasswordButton.setEnabled(true);
                    resetPasswordButton.setText("Reset Password");
                }
            }
        });
    }
} 