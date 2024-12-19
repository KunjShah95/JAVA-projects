
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class Main {

    private static ServerSocket serverSocket;
    private static final int PORT = 1234;

    public static void main(String[] args) {

        System.out.println("OPENING PORT : " + PORT + "\n");
        try {
            serverSocket = new ServerSocket(PORT); // STEP 1
        } catch (IOException ioException) {
            System.out.println("UNABLE TO ATTACH TO PORT ! EXCEPTION : " + ioException.getMessage());
            System.exit(1);
        }
        do {
            handleClient();
        } while (true);

    }

    private static void handleClient() {
        Socket link = null;                 //STEP 2
        try {
            link = serverSocket.accept();   //STEP 2

            Scanner input = new Scanner(link.getInputStream()); //STEP 3
            PrintWriter output = new PrintWriter(link.getOutputStream(), true);  //STEP 3

            int numMessages = 0;
            String message = input.nextLine(); //STEP 4
            while (!message.equals("***CLOSES***")) {
                System.out.println("MESSAGE RECEIVED : " + message);
                numMessages++;
                output.println("MESSAGE NUM  " + numMessages + " : " + message); //STEP 4
                message = input.nextLine();
            }
            output.println(numMessages + " messages received"); //STEP 4

        } catch (IOException ioException) {
            System.out.println("ERROR: " + ioException.getMessage());
        } finally {
            if (link != null) {
                try {
                    System.out.println("\n* CLOSING CONNECTION . . . *");
                    link.close(); //STEP 5
                } catch (IOException e) {
                    System.out.println("UNABLE TO DISCONNECT ! : " + e.getMessage());
                    System.exit(1);
                }
            }
        }
    }

}
