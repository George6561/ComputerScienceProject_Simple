/*
 * Copyright (c) 2024 
 * George Miller
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 * ----------------------------------------------------------------------------
 *
 * Class: ChessBoard
 *
 * This class represents a chessboard and provides methods to manage its state.
 * The chessboard is stored as an 8x8 integer array, where positive numbers represent 
 * white pieces and negative numbers represent black pieces.
 *
 * Key functionalities include:
 * - Retrieving the board state as a 2D array or a 1D array.
 * - Moving pieces on the board, handling special moves such as castling.
 * - Adding and removing pieces from specific positions on the board.
 * - Managing player turns and determining the current player.
 */
package com.chess.stockfish;

import java.util.ArrayList;
import java.util.List;

public class ChessBoard {

    // The chessboard is represented as an 8x8 2D array.
    private static final int[][] board = new int[][]{
        {-2, -3, -4, -5, -6, -4, -3, -2}, // Row 0: Black's major pieces
        {-1, -1, -1, -1, -1, -1, -1, -1}, // Row 1: Black's pawns
        {0, 0, 0, 0, 0, 0, 0, 0}, // Empty squares
        {0, 0, 0, 0, 0, 0, 0, 0}, // Empty squares
        {0, 0, 0, 0, 0, 0, 0, 0}, // Empty squares
        {0, 0, 0, 0, 0, 0, 0, 0}, // Empty squares
        {1, 1, 1, 1, 1, 1, 1, 1}, // White's pawns
        {2, 3, 4, 5, 6, 4, 3, 2} // Row 7: White's major pieces
    };

    private int[] lastMove;

    // Enum to represent the player's turn
    public enum Player {
        WHITE, BLACK
    }

    // The current player (whose turn it is)
    private static Player move = Player.WHITE;

    /**
     * Returns a copy of the current chessboard as a 2D array.
     *
     * This method returns a deep copy of the 2D array to prevent direct
     * modification of the internal board state by other classes.
     *
     * @return A copy of the chessboard as a 2D integer array.
     */
    public int[][] getBoard() {
        int[][] boardCopy = new int[board.length][];
        for (int i = 0; i < board.length; i++) {
            boardCopy[i] = board[i].clone(); // Clone each row for deep copy
        }
        return boardCopy;
    }

    /**
     * Returns the chessboard as a one-dimensional array with the rating of the
     * position as the last element. This can be used for machine learning
     * models.
     *
     * @return The board in elements 0-63 and the move score in element 64.
     */
    public int[] getBoardArray() {
        int[] oneDimensionalBoard = new int[65];
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                oneDimensionalBoard[(8 * y) + x] = board[y][x];
            }
        }
        return oneDimensionalBoard;
    }

    /**
     * Moves a piece from one square to another on the chessboard.
     *
     * Special moves like castling are handled separately.
     *
     * @param fromRow The starting row of the piece.
     * @param fromCol The starting column of the piece.
     * @param toRow The destination row of the piece.
     * @param toCol The destination column of the piece.
     */
    public void movePiece(int fromRow, int fromCol, int toRow, int toCol) {
        if (fromRow >= 0 && fromRow < 8 && fromCol >= 0 && fromCol < 8
                && toRow >= 0 && toRow < 8 && toCol >= 0 && toCol < 8) {

            int piece = board[fromRow][fromCol];
            if (piece == 0) {
                return;  // No piece to move
            }

            // Handle castling (king moving two squares to either side)
            if (piece == 6 || piece == -6) {  // King
                if (fromCol == 4 && (toCol == 6 || toCol == 2)) {
                    if (toCol == 6) {  // Kingside castling
                        board[fromRow][4] = 0;  // Move king
                        board[fromRow][6] = piece;
                        board[fromRow][7] = 0;  // Move rook
                        board[fromRow][5] = (piece > 0) ? 2 : -2;  // Add rook
                    } else if (toCol == 2) {  // Queenside castling
                        board[fromRow][4] = 0;  // Move king
                        board[fromRow][2] = piece;
                        board[fromRow][0] = 0;  // Move rook
                        board[fromRow][3] = (piece > 0) ? 2 : -2;  // Add rook
                    }
                    return;
                }
            }

            // Regular move
            board[fromRow][fromCol] = 0;  // Clear original square
            board[toRow][toCol] = piece;  // Move piece to destination

            // Handle pawn promotion
            if ((piece == 1 && toRow == 0) || (piece == -1 && toRow == 7)) {
                promotePawn(toRow, toCol, piece > 0);  // Promote white or black pawn
            }
        }
    }

    /**
     * Promotes a pawn that reaches the end of the board.
     *
     * @param row The row where the pawn is located.
     * @param col The column where the pawn is located.
     * @param isWhite True if the pawn is white, false if black.
     */
    private void promotePawn(int row, int col, boolean isWhite) {
        String choice = "Q";  // Automatically promote to a queen for now

        int newPiece;
        switch (choice) {
            case "Q" ->
                newPiece = isWhite ? 5 : -5;  // Queen
            case "R" ->
                newPiece = isWhite ? 2 : -2;  // Rook
            case "N" ->
                newPiece = isWhite ? 3 : -3;  // Knight
            case "B" ->
                newPiece = isWhite ? 4 : -4;  // Bishop
            default ->
                newPiece = isWhite ? 5 : -5;  // Default to Queen
        }

        board[row][col] = newPiece;
    }

    /**
     * Removes a piece from the specified square on the chessboard.
     *
     * @param row The row of the piece to remove.
     * @param col The column of the piece to remove.
     */
    public void removePiece(int row, int col) {
        if (row >= 0 && row < 8 && col >= 0 && col < 8) {
            board[row][col] = 0;  // Set the square to empty (0)
        }
    }

    /**
     * Adds a piece to the specified square on the chessboard.
     *
     * @param row The row where the piece will be placed.
     * @param col The column where the piece will be placed.
     * @param piece The piece to add (use positive values for white, negative
     * for black).
     */
    public void addPiece(int row, int col, int piece) {
        if (row >= 0 && row < 8 && col >= 0 && col < 8) {
            board[row][col] = piece;  // Place the piece on the board
        }
    }

    /**
     * Switches the turn to the next player (White to Black or Black to White).
     */
    public void nextMove() {
        move = (move == Player.WHITE) ? Player.BLACK : Player.WHITE;
    }

    /**
     * Determines which player is currently allowed to move.
     *
     * @return The current player (WHITE or BLACK).
     */
    public Player currentPlayer() {
        return move;
    }

    /**
     * Returns a string representation of the chessboard.
     *
     * @return A string representing the current state of the chessboard.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                int piece = board[row][col];
                switch (piece) {
                    case -1 ->
                        sb.append('p');  // Black Pawn
                    case -2 ->
                        sb.append('r');  // Black Rook
                    case -3 ->
                        sb.append('n');  // Black Knight
                    case -4 ->
                        sb.append('b');  // Black Bishop
                    case -5 ->
                        sb.append('q');  // Black Queen
                    case -6 ->
                        sb.append('k');  // Black King
                    case 1 ->
                        sb.append('P');   // White Pawn
                    case 2 ->
                        sb.append('R');   // White Rook
                    case 3 ->
                        sb.append('N');   // White Knight
                    case 4 ->
                        sb.append('B');   // White Bishop
                    case 5 ->
                        sb.append('Q');   // White Queen
                    case 6 ->
                        sb.append('K');   // White King
                    default ->
                        sb.append('*');  // Empty square
                }
            }
            sb.append('\n');  // New line after each row
        }
        return sb.toString();
    }

    /**
     * Returns all legal moves for the current player. Each move is represented
     * as an array: [fromRow, fromCol, toRow, toCol].
     *
     * @param player The player whose moves are being calculated.
     * @return A list of arrays representing legal moves for the current player.
     */
    public List<int[]> getAllLegalMoves(Player player) {
        List<int[]> legalMoves = new ArrayList<>();
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                int piece = board[row][col];
                if ((player == Player.WHITE && piece > 0) || (player == Player.BLACK && piece < 0)) {
                    List<int[]> pieceMoves = getMovesForPiece(row, col, piece);
                    for (int[] move : pieceMoves) {
                        int capturedPiece = board[move[2]][move[3]];
                        movePiece(move[0], move[1], move[2], move[3]);
                        if (!isInCheck(player)) {
                            legalMoves.add(move);  // Only add the move if it doesn't leave the king in check
                        }
                        board[move[0]][move[1]] = piece;
                        board[move[2]][move[3]] = capturedPiece;
                    }
                }
            }
        }
        return legalMoves;
    }

    /**
     * Returns a list of potential moves for a given piece. This function does
     * not check for checks or pins.
     *
     * @param row The row of the piece.
     * @param col The column of the piece.
     * @param piece The piece to find moves for.
     * @return A list of arrays representing potential moves for the piece.
     */
    private List<int[]> getMovesForPiece(int row, int col, int piece) {
        List<int[]> moves = new ArrayList<>();
        boolean isWhite = piece > 0;
        switch (Math.abs(piece)) {
            case 1 ->
                addPawnMoves(row, col, isWhite, moves);  // Pawn
            case 2 ->
                addRookMoves(row, col, isWhite, moves);  // Rook
            case 3 ->
                addKnightMoves(row, col, isWhite, moves);  // Knight
            case 4 ->
                addBishopMoves(row, col, isWhite, moves);  // Bishop
            case 5 ->
                addQueenMoves(row, col, isWhite, moves);  // Queen
            case 6 ->
                addKingMoves(row, col, isWhite, moves);  // King
        }
        return moves;
    }

    // Helper methods for each type of piece
    /**
     * Adds possible pawn moves to the moves list.
     *
     * @param row The row of the pawn.
     * @param col The column of the pawn.
     * @param isWhite True if the pawn is white, false if black.
     * @param moves The list to add the moves to.
     */
    private void addPawnMoves(int row, int col, boolean isWhite, List<int[]> moves) {
        int direction = isWhite ? -1 : 1;
        int startRow = isWhite ? 6 : 1;

        if (isValidMove(row + direction, col) && board[row + direction][col] == 0) {
            moves.add(new int[]{row, col, row + direction, col});
            if (row == startRow && board[row + 2 * direction][col] == 0) {
                moves.add(new int[]{row, col, row + 2 * direction, col});
            }
        }

        if (isValidMove(row + direction, col - 1) && isOpponentPiece(row + direction, col - 1, isWhite)) {
            moves.add(new int[]{row, col, row + direction, col - 1});
        }
        if (isValidMove(row + direction, col + 1) && isOpponentPiece(row + direction, col + 1, isWhite)) {
            moves.add(new int[]{row, col, row + direction, col + 1});
        }
    }

    /**
     * Adds possible rook moves to the moves list.
     *
     * @param row The row of the rook.
     * @param col The column of the rook.
     * @param isWhite True if the rook is white, false if black.
     * @param moves The list to add the moves to.
     */
    private void addRookMoves(int row, int col, boolean isWhite, List<int[]> moves) {
        addLinearMoves(row, col, isWhite, moves, new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}});
    }

    /**
     * Adds possible knight moves to the moves list.
     *
     * @param row The row of the knight.
     * @param col The column of the knight.
     * @param isWhite True if the knight is white, false if black.
     * @param moves The list to add the moves to.
     */
    private void addKnightMoves(int row, int col, boolean isWhite, List<int[]> moves) {
        int[][] knightMoves = {{-2, -1}, {-2, 1}, {-1, -2}, {-1, 2}, {1, -2}, {1, 2}, {2, -1}, {2, 1}};
        for (int[] move : knightMoves) {
            int newRow = row + move[0];
            int newCol = col + move[1];
            if (isValidMove(newRow, newCol) && !isFriendlyPiece(newRow, newCol, isWhite)) {
                moves.add(new int[]{row, col, newRow, newCol});
            }
        }
    }

    /**
     * Adds possible bishop moves to the moves list.
     *
     * @param row The row of the bishop.
     * @param col The column of the bishop.
     * @param isWhite True if the bishop is white, false if black.
     * @param moves The list to add the moves to.
     */
    private void addBishopMoves(int row, int col, boolean isWhite, List<int[]> moves) {
        addLinearMoves(row, col, isWhite, moves, new int[][]{{1, 1}, {1, -1}, {-1, 1}, {-1, -1}});
    }

    /**
     * Adds possible queen moves to the moves list.
     *
     * @param row The row of the queen.
     * @param col The column of the queen.
     * @param isWhite True if the queen is white, false if black.
     * @param moves The list to add the moves to.
     */
    private void addQueenMoves(int row, int col, boolean isWhite, List<int[]> moves) {
        addLinearMoves(row, col, isWhite, moves, new int[][]{
            {1, 0}, {-1, 0}, {0, 1}, {0, -1}, {1, 1}, {1, -1}, {-1, 1}, {-1, -1}});
    }

    /**
     * Adds possible king moves to the moves list.
     *
     * @param row The row of the king.
     * @param col The column of the king.
     * @param isWhite True if the king is white, false if black.
     * @param moves The list to add the moves to.
     */
    private void addKingMoves(int row, int col, boolean isWhite, List<int[]> moves) {
        int[][] kingMoves = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}, {1, 1}, {1, -1}, {-1, 1}, {-1, -1}};
        for (int[] move : kingMoves) {
            int newRow = row + move[0];
            int newCol = col + move[1];
            if (isValidMove(newRow, newCol) && !isFriendlyPiece(newRow, newCol, isWhite)) {
                moves.add(new int[]{row, col, newRow, newCol});
            }
        }
    }

    // Helper methods
    // Helper methods
    /**
     * Adds linear moves in specified directions for a piece.
     *
     * @param row The starting row of the piece.
     * @param col The starting column of the piece.
     * @param isWhite True if the piece is white, false if black.
     * @param moves The list to add the moves to.
     * @param directions The array of directions to add moves for.
     */
    private void addLinearMoves(int row, int col, boolean isWhite, List<int[]> moves, int[][] directions) {
        for (int[] direction : directions) {
            int newRow = row + direction[0];
            int newCol = col + direction[1];
            while (isValidMove(newRow, newCol) && !isFriendlyPiece(newRow, newCol, isWhite)) {
                moves.add(new int[]{row, col, newRow, newCol});
                if (board[newRow][newCol] != 0) { // Stop at the first opponent piece
                    break;
                }
                newRow += direction[0];
                newCol += direction[1];
            }
        }
    }

    /**
     * Checks if the given coordinates are within the bounds of the chessboard.
     *
     * @param row The row index to check.
     * @param col The column index to check.
     * @return True if the coordinates are valid, false otherwise.
     */
    private boolean isValidMove(int row, int col) {
        return row >= 0 && row < 8 && col >= 0 && col < 8;
    }

    /**
     * Determines if a piece at the given coordinates is friendly to the current
     * player.
     *
     * @param row The row index of the piece.
     * @param col The column index of the piece.
     * @param isWhite True if checking for a white piece, false for a black
     * piece.
     * @return True if the piece is friendly, false otherwise.
     */
    private boolean isFriendlyPiece(int row, int col, boolean isWhite) {
        int piece = board[row][col];
        return (isWhite && piece > 0) || (!isWhite && piece < 0);
    }

    /**
     * Determines if a piece at the given coordinates is an opponent's piece.
     *
     * @param row The row index of the piece.
     * @param col The column index of the piece.
     * @param isWhite True if checking for a white piece's opponent, false for
     * black.
     * @return True if the piece is an opponent's piece, false otherwise.
     */
    private boolean isOpponentPiece(int row, int col, boolean isWhite) {
        int piece = board[row][col];
        return (isWhite && piece < 0) || (!isWhite && piece > 0);
    }

    /**
     * Prints the current state of the chessboard with indices for reference.
     */
    public void printBoardWithIndices() {
        ////System.out.println("  0 1 2 3 4 5 6 7");  // Column indices for reference
        for (int row = 0; row < 8; row++) {
            //System.out.print(row + " ");  // Row index
            for (int col = 0; col < 8; col++) {
                int piece = board[row][col];
                char displayChar;
                displayChar = switch (piece) {
                    case -1 ->
                        'p';  // Black Pawn
                    case -2 ->
                        'r';  // Black Rook
                    case -3 ->
                        'n';  // Black Knight
                    case -4 ->
                        'b';  // Black Bishop
                    case -5 ->
                        'q';  // Black Queen
                    case -6 ->
                        'k';  // Black King
                    case 1 ->
                        'P';   // White Pawn
                    case 2 ->
                        'R';   // White Rook
                    case 3 ->
                        'N';   // White Knight
                    case 4 ->
                        'B';   // White Bishop
                    case 5 ->
                        'Q';   // White Queen
                    case 6 ->
                        'K';   // White King
                    default ->
                        '.';  // Empty square
                };
                //System.out.print(displayChar + " ");
            }
            ////System.out.println();  // New line after each row
        }
    }

    /**
     * Converts board coordinates into standard chess notation (e.g., "e4").
     *
     * @param row The row index on the board (0-7).
     * @param col The column index on the board (0-7).
     * @return A string representing the position in chess notation.
     */
    public String toChessNotation(int row, int col) {
        char file = (char) ('a' + col);
        int rank = 8 - row;
        return "" + file + rank;
    }

    boolean checkingForCheck = false;

    /**
     * Checks if the king of the specified player is in check.
     *
     * @param player The player to check (WHITE or BLACK).
     * @return True if the king is in check, false otherwise.
     */
    public boolean isInCheck(Player player) {
        if (checkingForCheck) {
            return false; // Prevent further recursion if already checking for check
        }

        checkingForCheck = true; // Set the flag to indicate check detection is in progress

        // Find the king's position
        int kingRow = -1, kingCol = -1;
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                int piece = board[row][col];
                if ((player == Player.WHITE && piece == 6) || (player == Player.BLACK && piece == -6)) {
                    kingRow = row;
                    kingCol = col;
                    break;
                }
            }
            if (kingRow != -1) {
                break;  // Exit loop once the king is found
            }
        }

        // Check if the king is attacked by any opponent piece
        boolean inCheck = false;
        List<int[]> opponentMoves = getAllLegalMoves(player == Player.WHITE
                ? Player.BLACK : Player.WHITE);
        for (int[] move : opponentMoves) {
            if (move[2] == kingRow && move[3] == kingCol) {
                inCheck = true;  // The king is in check
                break;
            }
        }

        checkingForCheck = false; // Reset the flag after check detection
        return inCheck;
    }

    /**
     * Checks if the specified player is in checkmate.
     *
     * @param player The player to check (WHITE or BLACK).
     * @return True if the player is in checkmate, false otherwise.
     */
    public boolean isCheckmate(Player player) {
        // First, check if the king is in check
        if (!isInCheck(player)) {
            return false; // If the king is not in check, it can't be checkmate
        }

        // Get all legal moves for the player
        List<int[]> legalMoves = getAllLegalMoves(player);

        // If there are no legal moves left, the player is in checkmate
        return legalMoves.isEmpty();
    }

    /**
     * Resets the chessboard to the initial starting position.
     */
    public void resetBoard() {
        // Set the board to the initial starting position
        int[][] initialBoard = {
            {-2, -3, -4, -5, -6, -4, -3, -2}, // Row 0: Black's major pieces
            {-1, -1, -1, -1, -1, -1, -1, -1}, // Row 1: Black's pawns
            {0, 0, 0, 0, 0, 0, 0, 0}, // Empty squares
            {0, 0, 0, 0, 0, 0, 0, 0}, // Empty squares
            {0, 0, 0, 0, 0, 0, 0, 0}, // Empty squares
            {0, 0, 0, 0, 0, 0, 0, 0}, // Empty squares
            {1, 1, 1, 1, 1, 1, 1, 1}, // Row 6: White's pawns
            {2, 3, 4, 5, 6, 4, 3, 2} // Row 7: White's major pieces
        };

        // Copy the initial board setup to the current board
        for (int i = 0; i < board.length; i++) {
            board[i] = initialBoard[i].clone();
        }

        // Reset the move tracking and set the current player to White
        move = Player.WHITE;
        lastMove = null; // Clear the last move tracking if necessary

        System.out.println("Board has been reset to the starting position.");
    }

    /**
     * Converts a UCI move like "e2e4" or "g1f3" into PGN notation like "e4" or
     * "Nf3". Basic version — only uses destination square + piece type.
     */
    public String toPGN(String uciMove, int pieceCode, boolean wasCapture) {
        if (uciMove == null || uciMove.length() < 4) {
            return uciMove;
        }

        String from = uciMove.substring(0, 2);
        String to = uciMove.substring(2, 4);
        int fromCol = from.charAt(0) - 'a';

        String captureSymbol = wasCapture ? "x" : "";

        String move;
        switch (Math.abs(pieceCode)) {
            case 1 -> { // Pawn
                if (wasCapture) {
                    move = (char) ('a' + fromCol) + "x" + to;
                } else {
                    move = to;
                }
            }
            case 2 ->
                move = "R" + captureSymbol + to;
            case 3 ->
                move = "N" + captureSymbol + to;
            case 4 ->
                move = "B" + captureSymbol + to;
            case 5 ->
                move = "Q" + captureSymbol + to;
            case 6 ->
                move = "K" + captureSymbol + to;
            default ->
                move = to;
        }

        return move;
    }

    /**
     * Returns the piece located at the specified position on the board.
     *
     * @param row The row index (0-7) of the piece.
     * @param col The column index (0-7) of the piece.
     * @return The integer value representing the piece at the given position,
     * or 0 if the position is empty.
     */
    public int getPieceAt(int row, int col) {
        if (row >= 0 && row < 8 && col >= 0 && col < 8) {
            return board[row][col];
        } else {
            throw new IllegalArgumentException("Position out of bounds");
        }
    }

}
