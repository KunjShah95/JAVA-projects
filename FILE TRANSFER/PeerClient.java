
import java.io.*;
import java.net.*;

public class PeerClient {

    private static final String SERVER_ADDRESS = "localhost"; // Server IP address
    private static final int PORT = 1234;
    private static final int CHUNK_SIZE = 4096; // Same chunk size as in the server

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_ADDRESS, PORT); DataInputStream input = new DataInputStream(socket.getInputStream()); DataOutputStream output = new DataOutputStream(socket.getOutputStream())) {

            System.out.println("1. Upload a file\n2. Download a file");
            System.out.print("Select option: ");
            int choice = Integer.parseInt(new BufferedReader(new InputStreamReader(System.in)).readLine());

            switch (choice) {
                case 1 ->
                    uploadFile(output); // Upload file to server
                case 2 ->
                    downloadFile(input, output); // Download file from server
            }
        } catch (IOException e) {
            System.err.println("An error occurred: " + e.getMessage());
        }
    }

    // Upload file to the server in chunks
    private static void uploadFile(DataOutputStream output) throws IOException {
        System.out.print("Enter the path of the file to upload: ");
        String filePath = new BufferedReader(new InputStreamReader(System.in)).readLine();
        File file = new File(filePath);
        if (file.exists()) {
            output.writeUTF("UPLOAD"); // Notify server to upload
            output.writeUTF(file.getName()); // Send file name
            output.writeLong(file.length()); // Send file size

            FileInputStream fis = new FileInputStream(file);
            BufferedInputStream bis = new BufferedInputStream(fis);

            byte[] buffer = new byte[CHUNK_SIZE];
            int bytesRead;

            // Send the file in chunks to the server
            while ((bytesRead = bis.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
            }

            System.out.println("File uploaded successfully.");
        } else {
            System.out.println("File not found.");
        }
    }

    // Download file from the server in chunks
    private static void downloadFile(DataInputStream input, DataOutputStream output) throws IOException {
        System.out.print("Enter the file name to download: ");
        String fileName = new BufferedReader(new InputStreamReader(System.in)).readLine();

        output.writeUTF("DOWNLOAD"); // Notify server to download
        output.writeUTF(fileName); // Send file name to server

        String serverResponse = input.readUTF(); // Check if file exists
        if ("EXISTS".equals(serverResponse)) {
            long fileSize = input.readLong(); // Get file size from the server
            File file = new File("received_" + fileName); // Local file to save the downloaded file

            try (FileOutputStream fos = new FileOutputStream(file); BufferedOutputStream bos = new BufferedOutputStream(fos)) {

                byte[] buffer = new byte[CHUNK_SIZE];
                int bytesRead;
                long totalBytesRead = 0;

                // Receive the file in chunks from the server
                while (totalBytesRead < fileSize) {
                    bytesRead = input.read(buffer);
                    bos.write(buffer, 0, bytesRead);
                    totalBytesRead += bytesRead;
                    System.out.println("Downloading: " + (totalBytesRead * 100 / fileSize) + "%");
                }
            }

            System.out.println("File downloaded successfully.");
        } else {
            System.out.println("File not found on the server.");
        }
    }

    public static int getCHUNK_SIZE() {
        return CHUNK_SIZE;
    }
}
