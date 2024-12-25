
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {

    public void run() throws IOException {
        int port = 8090;
        InetAddress address = InetAddress.getByName("localhost");
        try (Socket socket = new Socket(address, port); PrintWriter toSocket = new PrintWriter(socket.getOutputStream(), true); BufferedReader fromSocket = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            toSocket.println("Hello World from socket " + socket.getLocalSocketAddress());
            String line = fromSocket.readLine();
            System.out.println("Received from server: " + line);
        }
    }

    public static void main(String[] args) {
        Client singleThreadedWebServer_Client = new Client();
        try {
            singleThreadedWebServer_Client.run();
        } catch (UnknownHostException ex) {
            System.err.println("Unknown host: " + ex.getMessage());
        } catch (IOException ex) {
            System.err.println("I/O error: " + ex.getMessage());
        }
    }
}
