package com.chess.stockfish;

import java.io.IOException;

/**
 * Entry point for the console-based chess application.
 * 
 * This version launches a CLI chess game where the user plays White
 * and Stockfish responds as Black. Moves are input via System.in.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("Welcome to KingFischer Console Edition ♟");
        System.out.println("Type your move in formats like 'e4', 'e2e4', or '1. e4'. Type 'quit' to exit.\n");

        ChessGame game = new ChessGame();
        try {
            game.startGame();
        } catch (IOException e) {
            System.err.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
