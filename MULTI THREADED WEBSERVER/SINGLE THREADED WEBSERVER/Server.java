
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public class Server {

    public void run() throws IOException, UnknownHostException {
        int port = 8090;
        ServerSocket socket = new ServerSocket(port);
        socket.setSoTimeout(20000);
        while (true) {
            System.out.println("Server is listening on port: " + port);
            Socket acceptedConnection = socket.accept();
            System.out.println("Connected to " + acceptedConnection.getRemoteSocketAddress());
            PrintWriter toClient = new PrintWriter(acceptedConnection.getOutputStream(), true);
            BufferedReader fromClient = new BufferedReader(new InputStreamReader(acceptedConnection.getInputStream()));
            String clientInput;
            while ((clientInput = fromClient.readLine()) != null) {
                System.out.println("Received from client: " + clientInput);
                toClient.println("Echo: " + clientInput);
            }
        }
    }

    public static void main(String[] args) {
        Server server = new Server();
        try {
            server.run();
        } catch (UnknownHostException ex) {
            System.err.println("UnknownHostException: " + ex.getMessage());
        } catch (IOException ex) {
            System.err.println("IOException: " + ex.getMessage());
        }
    }

}
