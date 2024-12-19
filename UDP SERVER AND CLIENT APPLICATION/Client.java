
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Client {

    private static InetAddress host;
    private static final int PORT = 2082;
    private static DatagramSocket datagramSocket;
    private static DatagramPacket inPacket, outPacket;
    private static byte[] buffer;

    public static void main(String[] args) {
        try {
            host = InetAddress.getLocalHost();
        } catch (UnknownHostException unknownHostException) {
            System.out.println("ERROR : " + unknownHostException.getMessage());
            System.exit(1);
        }
        accessServer();
    }

    private static void accessServer() {
        try {
            datagramSocket = new DatagramSocket();              //STEP 1
            Scanner userEntry = new Scanner(System.in);
            String message, response;
            do {
                System.out.println("Enter Message : ");
                message = userEntry.nextLine();
                if (!message.equals("***CLOSE***")) {
                    outPacket = new DatagramPacket(message.getBytes(), message.length(), host, PORT); //STEP 2
                    datagramSocket.send(outPacket); //STEP 3
                    buffer = new byte[256]; //STEP 5
                    inPacket = new DatagramPacket(buffer, buffer.length); //STEP 5
                    datagramSocket.receive(inPacket); //STEP 6
                    response = new String(inPacket.getData(), 0, inPacket.getLength()); //STEP 7
                    System.out.println("\n SERVER RESPONSE > " + response);
                    System.out.println(" MESSAGE : " + message);
                }

            } while (!message.equals("***CLOSE***"));
        } catch (IOException ioException) {
            System.out.println("An error occurred: " + ioException.getMessage());
            System.out.println("ERROR" + ioException.getMessage());

        } finally {
            System.out.println("\n * Closing connection ... *");
            datagramSocket.close();  // STEP 8

        }
    }

}
