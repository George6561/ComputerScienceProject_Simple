package com.chess.stockfish;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ChessGame {

    private final StockfishConnector stockfish;
    private final ChessBoard board = new ChessBoard();
    private final List<String> rawMoves = new ArrayList<>();
    private final List<String> prettyMoves = new ArrayList<>();
    private final Scanner scanner = new Scanner(System.in);
    private boolean isWhiteToMove = true;
    private int turn = 1;

    public ChessGame() {
        this.stockfish = new StockfishConnector();
    }

    public void startGame() throws IOException {
        if (!stockfish.startEngine()) {
            System.out.println("Failed to start Stockfish engine.");
            return;
        }

        try {
            initializeStockfish();
            System.out.println("Game started. You are playing White.\n");

            while (true) {
                // --- USER MOVE ---
                System.out.print("Your move (e.g. e4, e2e4, 1. e4): ");
                String input = scanner.nextLine().trim();

                if (input.equalsIgnoreCase("quit")) {
                    System.out.println("Game exited.");
                    break;
                }

                String userMove = parseMove(input);
                if (userMove == null || userMove.length() != 4) {
                    System.out.println("Invalid move format.");
                    continue;
                }

                int[] userCoords = getCoords(userMove);
                int userPiece = board.getPieceAt(userCoords[0], userCoords[1]);
                int capturedPiece = board.getPieceAt(userCoords[2], userCoords[3]);
                boolean wasCapture = capturedPiece != 0;

                updateMoveHistory(userMove);
                applyMove(userCoords);
                prettyMoves.add(turn + ". " + board.toPGN(userMove, userPiece, wasCapture));
                isWhiteToMove = false;

                // --- STOCKFISH MOVE ---
                System.out.println("Sending to Stockfish: " + getMoveHistory());
                stockfish.updateGameState(getMoveHistory());
                stockfish.calculateBestMove(1000);
                String stockfishMove = stockfish.getBestMove();

                if (stockfishMove == null || stockfishMove.equals("(none)")) {
                    System.out.println("Stockfish resigns or is out of moves. Game over.");
                    break;
                }

                int[] sfCoords = getCoords(stockfishMove);
                int sfFromRow = sfCoords[0];
                int sfFromCol = sfCoords[1];
                int sfPiece = board.getPieceAt(sfFromRow, sfFromCol);
                int sfCaptured = board.getPieceAt(sfCoords[2], sfCoords[3]);
                boolean sfCapture = sfCaptured != 0;

                updateMoveHistory(stockfishMove);
                applyMove(sfCoords);
                prettyMoves.add(turn + "... " + board.toPGN(stockfishMove, sfPiece, sfCapture));
                isWhiteToMove = true;
                turn++;

                printMoveHistory();
            }

        } finally {
            stockfish.stopEngine();
        }
    }

    private void initializeStockfish() throws IOException {
        stockfish.sendCommand("uci");
        stockfish.getResponse();
        stockfish.sendCommand("isready");
        stockfish.getResponse();
        stockfish.sendCommand("position startpos");
    }

    private void applyMove(int[] coords) {
        board.movePiece(coords[0], coords[1], coords[2], coords[3]);
        board.nextMove();
    }

    public void updateMoveHistory(String move) {
        rawMoves.add(move);
    }

    public String getMoveHistory() {
        return String.join(" ", rawMoves);
    }

    private void printMoveHistory() {
        System.out.println("── Move History ──");
        for (int i = 0; i < prettyMoves.size(); i += 2) {
            System.out.print(prettyMoves.get(i));
            if (i + 1 < prettyMoves.size()) {
                System.out.print("   " + prettyMoves.get(i + 1));
            }
            System.out.println();
        }
        System.out.println();
    }

    private String parseMove(String input) {
        input = input.replaceAll("\\d+\\.", "").trim().toLowerCase();

        // UCI format input
        if (input.matches("[a-h][1-8][a-h][1-8]")) {
            return input;
        }

        // Pawn push like "e4"
        if (input.matches("[a-h][1-8]?")) {
            String file = input.substring(0, 1);
            String rank = input.length() == 2 ? input.substring(1) : "4";
            return file + "2" + file + rank;
        }

        ChessBoard.Player current = board.currentPlayer();
        boolean isWhite = current == ChessBoard.Player.WHITE;

        List<int[]> legalMoves = board.getAllLegalMoves(current);

        // Piece captures or regular moves like "Nxd4", "Nd4"
        if (input.matches("[nbrqk]x?[a-h][1-8]")) {
            char pieceLetter = input.charAt(0);
            boolean isCapture = input.contains("x");
            String destination = input.substring(isCapture ? 2 : 1);
            int destCol = destination.charAt(0) - 'a';
            int destRow = 8 - Character.getNumericValue(destination.charAt(1));

            int absPieceCode = switch (Character.toUpperCase(pieceLetter)) {
                case 'N' ->
                    3;
                case 'B' ->
                    4;
                case 'R' ->
                    2;
                case 'Q' ->
                    5;
                case 'K' ->
                    6;
                default ->
                    0;
            };
            int pieceCode = isWhite ? absPieceCode : -absPieceCode;

            for (int[] move : legalMoves) {
                int fromRow = move[0];
                int fromCol = move[1];
                int toRow = move[2];
                int toCol = move[3];

                if (toRow == destRow && toCol == destCol && board.getPieceAt(fromRow, fromCol) == pieceCode) {
                    return toUCI(fromRow, fromCol, toRow, toCol);
                }
            }
        }

        // Pawn captures like "exd5"
        if (input.matches("[a-h]x[a-h][1-8]")) {
            char fileFrom = input.charAt(0);
            char fileTo = input.charAt(2);
            char rankTo = input.charAt(3);

            int toCol = fileTo - 'a';
            int toRow = 8 - Character.getNumericValue(rankTo);
            int fromCol = fileFrom - 'a';

            for (int[] move : legalMoves) {
                int fromRow = move[0];
                int fromMoveCol = move[1];
                int toMoveRow = move[2];
                int toMoveCol = move[3];

                int movingPiece = board.getPieceAt(fromRow, fromMoveCol);

                if (Math.abs(movingPiece) == 1 // must be a pawn
                        && fromMoveCol == fromCol
                        && toMoveRow == toRow
                        && toMoveCol == toCol) {
                    return toUCI(fromRow, fromCol, toRow, toCol);
                }
            }
        }

        return null;
    }

    private int[] getCoords(String move) {
        return new int[]{
            8 - Character.getNumericValue(move.charAt(1)),
            move.charAt(0) - 'a',
            8 - Character.getNumericValue(move.charAt(3)),
            move.charAt(2) - 'a'
        };
    }

    private String toUCI(int fromRow, int fromCol, int toRow, int toCol) {
        return "" + (char) ('a' + fromCol) + (8 - fromRow)
                + (char) ('a' + toCol) + (8 - toRow);
    }
}
