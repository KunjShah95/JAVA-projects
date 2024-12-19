
import java.io.*;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PeerServer {

    private static final int PORT = 1234; // Port for the server
    private static final String FILES_DIR = "shared_files"; // Directory where files are stored
    private static final Logger logger = Logger.getLogger(PeerServer.class.getName());
    private static ExecutorService threadPool;

    public static void main(String[] args) {
        File sharedDir = new File(FILES_DIR);
        if (!sharedDir.exists()) {
            sharedDir.mkdir(); // Create the shared directory if it does not exist
        }

        threadPool = Executors.newFixedThreadPool(10); // Create a thread pool

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            logger.info("Server is waiting for clients to connect...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                logger.info(String.format("Client connected: %s", clientSocket.getInetAddress()));
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Server error: ", e);
        } finally {
            threadPool.shutdown(); // Shutdown the thread pool
        }
    }
}

class ClientHandler implements Runnable {

    private final Socket clientSocket;
    private static final int CHUNK_SIZE = 4096; // Chunk size for file transfer
    private final File sharedDir;

    public ClientHandler(Socket clientSocket, File sharedDir) {
        this.clientSocket = clientSocket;
        this.sharedDir = sharedDir;
    }

    @Override
    public void run() {
        try (DataInputStream input = new DataInputStream(clientSocket.getInputStream()); DataOutputStream output = new DataOutputStream(clientSocket.getOutputStream())) {

            // Read action from the client (UPLOAD, DOWNLOAD, etc.)
            String action = input.readUTF();
            switch (action) {
                case "UPLOAD" ->
                    receiveFile(input); // Receive file from the client
                case "DOWNLOAD" ->
                    sendFile(input, output); // Send file to the client
                default ->
                    output.writeUTF("INVALID ACTION");
            }

        } catch (IOException e) {
            Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, "Client error: ", e);
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, "Error closing client socket: ", e);
            }
        }
    }

    private void receiveFile(DataInputStream input) throws IOException {
        String fileName = input.readUTF(); // Read file name from client
        long fileSize = input.readLong(); // Read file size from client

        // Prevent overwriting existing files
        File file = new File(sharedDir, fileName);
        if (file.exists()) {
            Logger.getLogger(ClientHandler.class.getName()).log(Level.WARNING, "File already exists: {0}", fileName);
            return; // Optionally, you could send a response back to the client
        }

        try (FileOutputStream fos = new FileOutputStream(file); BufferedOutputStream bos = new BufferedOutputStream(fos)) {

            byte[] buffer = new byte[CHUNK_SIZE];
            int bytesRead;
            long totalBytesRead = 0;

            // Read the file in chunks
            while (totalBytesRead < fileSize) {
                bytesRead = input.read(buffer, 0, CHUNK_SIZE);
                if (bytesRead == -1) {
                    break;
                }
                bos.write(buffer, 0, bytesRead);
                totalBytesRead += bytesRead;
                Logger.getLogger(ClientHandler.class.getName()).info(String.format("Receiving: %d%%", (totalBytesRead * 100 / fileSize)));
            }

            Logger.getLogger(ClientHandler.class.getName()).log(Level.INFO, "File uploaded: {0}", fileName);
        }
    }

    private void sendFile(DataInputStream input, DataOutputStream output) throws IOException {
        String fileName = input.readUTF(); // Get the file name from the client
        File file = new File(sharedDir, fileName);

        if (file.exists()) {
            output.writeUTF("EXISTS"); // Let client know that the file exists
            output.writeLong(file.length()); // Send file size to client

            try (FileInputStream fis = new FileInputStream(file); BufferedInputStream bis = new BufferedInputStream(fis)) {

                byte[] buffer = new byte[CHUNK_SIZE];
                int bytesRead;

                // Send the file in chunks to the client
                while ((bytesRead = bis.read(buffer)) != -1) {
                    Logger.getLogger(ClientHandler.class.getName()).log(Level.INFO, "File sent: {0}", fileName);
                    Logger.getLogger(ClientHandler.class.getName()).info(String.format("File sent: %s", fileName));
                    output.write(buffer, 0, bytesRead);
                }

                Logger.getLogger(ClientHandler.class.getName()).log(Level.INFO, "File sent: {0}", fileName);
            }
        } else {
            output.writeUTF("NOT FOUND"); // Let client know that the file was not found
        }
    }

    public Socket getClientSocket() {
        return clientSocket;
    }
}
