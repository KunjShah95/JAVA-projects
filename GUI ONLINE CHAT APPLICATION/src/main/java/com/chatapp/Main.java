package com.chatapp;

import com.chatapp.client.LoginWindow;
import com.chatapp.server.ChatServer;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Main class to start the chat application
 */
public class Main {
    /**
     * Main metho
     * 
     

     d
     * 
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        // Check arguments
        final boolean startServer;
        
        if (args.length > 0 && args[0].equalsIgnoreCase("--server")) {
            startServer = true;
        } else {
            startServer = false;
        }
        
        if (startServer) {
            // Start the server
            System.out.println("Starting server...");
            new Thread(() -> {
                ChatServer.main(new String[0]);
            }).start();
        }
        
        // Set look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Start the client application
        SwingUtilities.invokeLater(() -> {
            LoginWindow loginWindow = new LoginWindow();
            
            if (startServer) {
                // If we're also running the server, make sure it shuts down when the window closes
                loginWindow.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent e) {
                        System.exit(0);
                    }
                });
            }
        });
    }
} 