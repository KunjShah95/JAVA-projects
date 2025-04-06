import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Scanner;

public class CaesarCipher {

    // Method to perform Caesar Cipher
    public static byte[] caesarCipher(byte[] data, int shift) {
        byte[] result = new byte[data.length];
        for (int i = 0; i < data.length; i++) {
            result[i] = (byte) (data[i] + shift);
        }
        return result;
    }

    // Method to encrypt a file
    public static void encryptFile(String inputFilePath, String outputFilePath, int shift) throws IOException {
        logEvent("Starting encryption for file: " + inputFilePath);

        if (!validateFile(inputFilePath)) {
            return;
        }

        // Ensure the output file's parent directories exist
        File outputFile = new File(outputFilePath);
        if (outputFile.getParentFile() != null && !outputFile.getParentFile().exists()) {
            if (!outputFile.getParentFile().mkdirs()) {
                logEvent("Error: Could not create directory - " + outputFile.getParent());
                return;
            }
        }

        byte[] fileData = Files.readAllBytes(Paths.get(inputFilePath)); // Read file as bytes
        byte[] encryptedData = caesarCipher(fileData, shift); // Encrypt data

        // Write metadata and encrypted data
        String metadata = "Shift:" + shift + "\nDate:" + LocalDateTime.now() + "\n\n";
        Files.write(Paths.get(outputFilePath), metadata.getBytes());
        Files.write(Paths.get(outputFilePath), encryptedData, StandardOpenOption.APPEND);

        logEvent("Encryption completed. Output file: " + outputFilePath);

        // Display encrypted content preview
        String previewContent = new String(encryptedData);
        if (previewContent.length() > 100) {
            previewContent = previewContent.substring(0, 100) + "... (output truncated)";
        }
        logEvent("Encrypted file content preview: \n" + previewContent);
    }

    // Method to decrypt a file
    public static void decryptFile(String inputFilePath, String outputFilePath, int shift) throws IOException {
        logEvent("Starting decryption for file: " + inputFilePath);

        if (!validateFile(inputFilePath)) {
            return;
        }

        // Ensure the output file's parent directories exist
        File outputFile = new File(outputFilePath);
        if (outputFile.getParentFile() != null && !outputFile.getParentFile().exists()) {
            if (!outputFile.getParentFile().mkdirs()) {
                logEvent("Error: Could not create directory - " + outputFile.getParent());
                return;
            }
        }

        byte[] fileData = Files.readAllBytes(Paths.get(inputFilePath));

        // Skip metadata during decryption
        int metadataEndIndex = new String(fileData).indexOf("\n\n");
        if (metadataEndIndex != -1) {
            fileData = Arrays.copyOfRange(fileData, metadataEndIndex + 2, fileData.length);
        }

        byte[] decryptedData = caesarCipher(fileData, -shift); // Decrypt data
        Files.write(Paths.get(outputFilePath), decryptedData); // Write decrypted data to file

        logEvent("Decryption completed. Output file: " + outputFilePath);

        // Display decrypted content preview
        String previewContent = new String(decryptedData);
        if (previewContent.length() > 100) {
            previewContent = previewContent.substring(0, 100) + "... (output truncated)";
        }
        logEvent("Decrypted file content preview: \n" + previewContent);
    }

    // Method to validate file existence and readability
    public static boolean validateFile(String filePath) {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            logEvent("Error: File does not exist - " + filePath);
            return false;
        }
        if (!Files.isReadable(path)) {
            logEvent("Error: File is not readable - " + filePath);
            return false;
        }
        return true;
    }

    // Method to log events
    public static void logEvent(String message) {
        System.out.println("[" + LocalDateTime.now() + "] " + message);
    }

    // Method to generate a shift key from a password
    public static int generateKeyFromPassword(String password) {
        int key = 0;
        for (char c : password.toCharArray()) {
            key += c; // Sum ASCII values of the characters
        }
        return key % 256; // Ensure the key stays within byte range
    }

    // Method to compare two files
    public static boolean compareFiles(String filePath1, String filePath2) throws IOException {
        if (!validateFile(filePath1) || !validateFile(filePath2)) {
            return false;
        }
        byte[] file1Data = Files.readAllBytes(Paths.get(filePath1));
        byte[] file2Data = Files.readAllBytes(Paths.get(filePath2));
        return Arrays.equals(file1Data, file2Data);
    }
    
    // Console menu method
    public static void consoleMenu() {
        Scanner scanner = new Scanner(System.in);
        boolean running = true;
        
        try {
            while (running) {
                System.out.println("\n--- File Encryption Utility ---");
                System.out.println("1. Encrypt a file");
                System.out.println("2. Decrypt a file");
                System.out.println("3. Encrypt and verify");
                System.out.println("4. Exit");
                System.out.print("Choose an option (1-4): ");
                
                String choice = scanner.nextLine().trim();
                
                switch (choice) {
                    case "1":
                        encryptFileConsole(scanner);
                        break;
                    case "2":
                        decryptFileConsole(scanner);
                        break;
                    case "3":
                        encryptAndVerifyConsole(scanner);
                        break;
                    case "4":
                        running = false;
                        System.out.println("Exiting program...");
                        break;
                    default:
                        System.out.println("Invalid option. Please try again.");
                }
            }
        } catch (Exception e) {
            System.err.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }
    
    // Helper method for console encryption
    private static void encryptFileConsole(Scanner scanner) {
        try {
            System.out.print("Enter the path of the input file: ");
            String inputFile = scanner.nextLine().trim();
            
            System.out.print("Enter the path to save the encrypted file: ");
            String outputFile = scanner.nextLine().trim();
            
            // Add extension if not specified
            if (!outputFile.contains(".")) {
                outputFile = outputFile + ".enc";
            }
            
            System.out.print("Enter a password for encryption: ");
            String password = scanner.nextLine();
            int shift = generateKeyFromPassword(password);
            
            try {
                encryptFile(inputFile, outputFile, shift);
            } catch (IOException e) {
                logEvent("Error: " + e.getMessage());
            }
        } catch (Exception e) {
            logEvent("Input error: " + e.getMessage());
        }
    }
    
    // Helper method for console decryption
    private static void decryptFileConsole(Scanner scanner) {
        try {
            System.out.print("Enter the path of the encrypted file: ");
            String inputFile = scanner.nextLine().trim();
            
            System.out.print("Enter the path to save the decrypted file: ");
            String outputFile = scanner.nextLine().trim();
            
            // Add extension if not specified
            if (!outputFile.contains(".")) {
                outputFile = outputFile + ".dec";
            }
            
            System.out.print("Enter the password for decryption: ");
            String password = scanner.nextLine();
            int shift = generateKeyFromPassword(password);
            
            try {
                decryptFile(inputFile, outputFile, shift);
            } catch (IOException e) {
                logEvent("Error: " + e.getMessage());
            }
        } catch (Exception e) {
            logEvent("Input error: " + e.getMessage());
        }
    }
    
    // Helper method for encrypting and verifying in console
    private static void encryptAndVerifyConsole(Scanner scanner) {
        try {
            System.out.print("Enter the path of the input file: ");
            String inputFile = scanner.nextLine().trim();
            
            System.out.print("Enter the path to save the encrypted file: ");
            String encryptedFile = scanner.nextLine().trim();
            
            // Add extension if not specified
            if (!encryptedFile.contains(".")) {
                encryptedFile = encryptedFile + ".enc";
            }
            
            System.out.print("Enter the path to save the decrypted file (for verification): ");
            String decryptedFile = scanner.nextLine().trim();
            
            // Add extension if not specified
            if (!decryptedFile.contains(".")) {
                decryptedFile = decryptedFile + ".dec";
            }
            
            System.out.print("Enter a password for encryption: ");
            String password = scanner.nextLine();
            int shift = generateKeyFromPassword(password);
            
            try {
                // Encrypt the file
                encryptFile(inputFile, encryptedFile, shift);
                
                // Decrypt the file
                decryptFile(encryptedFile, decryptedFile, shift);
                
                // Compare original and decrypted files
                if (compareFiles(inputFile, decryptedFile)) {
                    logEvent("Integrity check passed: The original and decrypted files match.");
                } else {
                    logEvent("Integrity check failed: The original and decrypted files do not match.");
                }
            } catch (IOException e) {
                logEvent("Error: " + e.getMessage());
            }
        } catch (Exception e) {
            logEvent("Input error: " + e.getMessage());
        }
    }

    // Main method
    public static void main(String[] args) {
        System.out.println("Welcome to Caesar Cipher File Encryption Utility");
        System.out.println("==============================================");
        consoleMenu();
    }
}