
import java.io.*;
import java.net.*;
import java.util.*;

public class TicTacToeHost {

    private static final int PORT = 5000;
    private static final char[][] board = new char[3][3];
    private static boolean isMyTurn = true;

    public static void main(String[] args) throws IOException {
        initializeBoard();
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started at IP: 127.0.0.1 and Port: " + PORT);
            System.out.println("Waiting for a player to join...");
            Socket clientSocket = serverSocket.accept();
            System.out.println("Player connected!");

            try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream())); PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

                System.out.println("Game started! You are 'X'. Waiting for your move...");
                while (true) {
                    if (isMyTurn) {
                        printBoard();
                        makeMove(out, 'X');
                    } else {
                        System.out.println("Waiting for opponent's move...");
                        String opponentMove = in.readLine();
                        if (opponentMove.startsWith("CHAT:")) {
                            System.out.println("Opponent: " + opponentMove.substring(5));
                        } else {
                            processMove(opponentMove, 'O');
                            isMyTurn = true;
                        }
                    }

                    if (isGameOver()) {
                        printBoard();
                        break;
                    }
                }
            } catch (IOException e) {
                System.err.println("Communication Error: " + e.getMessage());
                System.err.println("Server Error: " + e.getMessage());
            }
        }
    }

    private static void initializeBoard() {
        for (int i = 0; i < 3; i++) {
            Arrays.fill(board[i], '-');
        }
    }

    private static void printBoard() {
        System.out.println("\nCurrent Board:");
        for (char[] row : board) {
            System.out.println(Arrays.toString(row));
        }
    }

    private static void makeMove(PrintWriter out, char mark) throws IOException {
        Scanner scanner = new Scanner(System.in);
        int row, col;
        while (true) {
            System.out.print("Enter row (0-2) and column (0-2): ");
            row = scanner.nextInt();
            col = scanner.nextInt();
            if (row >= 0 && row < 3 && col >= 0 && col < 3 && board[row][col] == '-') {
                board[row][col] = mark;
                out.println(row + "," + col);
                break;
            } else {
                System.out.println("Invalid move, try again.");
            }
        }
        isMyTurn = false;
    }

    private static void processMove(String move, char mark) {
        String[] parts = move.split(",");
        if (parts.length != 2) {
            System.err.println("Invalid move format received: " + move);
            return;
        }
        int row, col;
        try {
            row = Integer.parseInt(parts[0]);
            col = Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            System.err.println("Invalid move coordinates received: " + move);
            return;
        }
        board[row][col] = mark;
    }

    private static boolean isGameOver() {
        if (checkWin('X')) {
            System.out.println("You win!");
            return true;
        }
        if (checkWin('O')) {
            System.out.println("You lose!");
            return true;
        }
        if (isBoardFull()) {
            System.out.println("It's a draw!");
            return true;
        }
        return false;
    }

    private static boolean checkWin(char mark) {
        for (int i = 0; i < 3; i++) {
            if (board[i][0] == mark && board[i][1] == mark && board[i][2] == mark) {
                return true;
            }
            if (board[0][i] == mark && board[1][i] == mark && board[2][i] == mark) {
                return true;
            }
        }
        return (board[0][0] == mark && board[1][1] == mark && board[2][2] == mark)
                || (board[0][2] == mark && board[1][1] == mark && board[2][0] == mark);
    }

    private static boolean isBoardFull() {
        for (char[] row : board) {
            for (char cell : row) {
                if (cell == '-') {
                    return false;
                }
            }
        }
        return true;
    }
}
