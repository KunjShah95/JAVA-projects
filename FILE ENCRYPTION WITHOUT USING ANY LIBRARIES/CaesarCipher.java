
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
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

        byte[] fileData = Files.readAllBytes(Paths.get(inputFilePath)); // Read file as bytes
        byte[] encryptedData = caesarCipher(fileData, shift); // Encrypt data

        // Write metadata and encrypted data
        String metadata = "Shift:" + shift + "\nDate:" + LocalDateTime.now() + "\n\n";
        Files.write(Paths.get(outputFilePath), metadata.getBytes());
        Files.write(Paths.get(outputFilePath), encryptedData, java.nio.file.StandardOpenOption.APPEND);

        logEvent("Encryption completed. Output file: " + outputFilePath);

        // Display encrypted content
        logEvent("Encrypted file content: \n" + new String(encryptedData));
    }

    // Method to decrypt a file
    public static void decryptFile(String inputFilePath, String outputFilePath, int shift) throws IOException {
        logEvent("Starting decryption for file: " + inputFilePath);

        if (!validateFile(inputFilePath)) {
            return;
        }

        byte[] fileData = Files.readAllBytes(Paths.get(inputFilePath));

        // Skip metadata during decryption
        int metadataEndIndex = new String(fileData).indexOf("\n\n");
        if (metadataEndIndex != -1) {
            fileData = java.util.Arrays.copyOfRange(fileData, metadataEndIndex + 2, fileData.length);
        }

        byte[] decryptedData = caesarCipher(fileData, -shift); // Decrypt data
        Files.write(Paths.get(outputFilePath), decryptedData); // Write decrypted data to file

        logEvent("Decryption completed. Output file: " + outputFilePath);

        // Display decrypted content
        logEvent("Decrypted file content: \n" + new String(decryptedData));
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
        byte[] file1Data = Files.readAllBytes(Paths.get(filePath1));
        byte[] file2Data = Files.readAllBytes(Paths.get(filePath2));
        return java.util.Arrays.equals(file1Data, file2Data);
    }

    // Main method for user interaction
    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.print("Enter the path of the input file: ");
            String inputFile = scanner.nextLine();

            System.out.print("Enter the path to save the encrypted file: ");
            String encryptedFile = scanner.nextLine();

            System.out.print("Enter the path to save the decrypted file: ");
            String decryptedFile = scanner.nextLine();

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
        }
    }
}
