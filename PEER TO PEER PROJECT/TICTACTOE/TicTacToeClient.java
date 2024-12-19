
import java.io.*;
import java.net.*;

public class TicTacToeClient {

    private static final char[][] board = new char[3][3];
    private static boolean gameActive = true;
    private static boolean isClientTurn = true; // Flag to indicate if it's the client's turn

    public static void main(String[] args) {
        initializeBoard();
        System.out.println("Enter the host's IP address: ");
        try (BufferedReader consoleInput = new BufferedReader(new InputStreamReader(System.in))) {
            String hostAddress = consoleInput.readLine();

            try (Socket socket = new Socket(hostAddress, 5000); BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream())); PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

                System.out.println("Connected to host! You are 'O'. Waiting for the host's move...");

                // Start a thread to listen for moves from the server
                new Thread(() -> {
                    while (gameActive) {
                        try {
                            String hostMove = in.readLine();
                            if (hostMove != null) {
                                processMove(hostMove, 'X');
                                printBoard();
                                if (isGameOver()) {
                                    System.out.println("Game Over! X wins!");
                                    gameActive = false;
                                } else {
                                    isClientTurn = true; // It's now the client's turn
                                }
                            }
                        } catch (IOException e) {
                            System.err.println("Error reading move from host: " + e.getMessage());
                        }
                    }
                }).start();

                // Main loop for the client to make moves
                while (gameActive) {
                    if (isClientTurn) {
                        makeMove(consoleInput, out, 'O');
                        if (isGameOver()) {
                            System.out.println("Game Over! O wins!");
                            gameActive = false;
                        } else {
                            isClientTurn = false; // It's now the host's turn
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Client Error: " + e.getMessage());
        }
    }

    private static void initializeBoard() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                board[i][j] = '-';
            }
        }
    }

    private static void printBoard() {
        for (char[] row : board) {
            System.out.println(new String(row));
        }
    }

    private static void makeMove(BufferedReader consoleInput, PrintWriter out, char mark) {
        int row, col;
        while (true) {
            try {
                System.out.print("Enter row (0-2): ");
                row = Integer.parseInt(consoleInput.readLine());
                System.out.print("Enter column (0-2): ");
                col = Integer.parseInt(consoleInput.readLine());

                if (row >= 0 && row < 3 && col >= 0 && col < 3 && board[row][col] == '-') {
                    board[row][col] = mark;
                    out.println(row + "," + col);
                    break;
                } else {
                    System.out.println("Invalid move, try again.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter valid integers for row and column.");
            } catch (IOException e) {
                System.err.println("Error reading input: " + e.getMessage());
            }
        }
    }

    private static void processMove(String move, char mark) {
        String[] parts = move.split(",");
        if (parts.length != 2) {
            System.err.println("Invalid move format received: " + move);
            return; // or handle the error appropriately
        }
        try {
            int row = Integer.parseInt(parts[0]);
            int col = Integer.parseInt(parts[1]);
            if (board[row][col] == '-') {
                board[row][col] = mark;
            } else {
                System.err.println("Received move for an already occupied cell.");
            }
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            System.err.println("Error processing move: " + e.getMessage());
        }
    }

    private static boolean isGameOver() {
        // Check rows for a win
        for (int i = 0; i < 3; i++) {
            if (board[i][0] == board[i][1] && board[i][1] == board[i][2] && board[i][0] != '-') {
                return true;
            }
        }

        // Check columns for a win
        for (int i = 0; i < 3; i++) {
            if (board[0][i] == board[1][i] && board[1][i] == board[2][i] && board[0][i] != '-') {
                return true;
            }
        }

        // Check diagonals for a win
        if (board[0][0] == board[1][1] && board[1][1] == board[2][2] && board[0][0] != '-') {
            return true;
        }
        if (board[0][2] == board[1][1] && board[1][1] == board[2][0] && board[0][2] != '-') {
            return true;
        }

        // Check for a draw (if the board is full)
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board[i][j] == '-') {
                    return false;
                }
            }
        }

        // If no win and the board is full, it's a draw
        System.out.println("Game Over! It's a draw!");
        return true;
    }
}
