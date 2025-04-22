package com.chess.stockfish;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

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
 * Class: StockfishConnector
 *
 * This class provides an interface for communicating with the Stockfish chess engine.
 * It is used to start, stop, and interact with the engine to evaluate positions, calculate 
 * best moves, and perform other operations that enhance the chess-playing capabilities of 
 * the application.
 *
 * Key functionalities include:
 * - Starting and stopping the Stockfish engine.
 * - Sending commands to and receiving responses from the Stockfish engine.
 * - Retrieving the best move and other analytical information from the engine.
 * - Setting options for the engine such as analysis depth, number of threads, and debug mode.
 * - Querying legal moves and verifying the validity of a given move.
 * - Handling game state updates to keep Stockfish informed of the current position on the chessboard.
 *
 * Dependencies:
 * - The Stockfish engine executable, referenced by `ENGINE_SOURCE`. Ensure that the executable path is correctly specified.
 * - Java I/O classes (`BufferedReader`, `BufferedWriter`, etc.) for interacting with Stockfish.
 *
 * Usage:
 * - `startEngine()`: Starts the Stockfish engine process.
 * - `sendCommand(String command)`: Sends a specific UCI command to the engine.
 * - `getBestMove()`: Retrieves the best move suggested by Stockfish based on the current game state.
 * - `stopEngine()`: Stops the engine and releases all related resources.
 *
 * Notes:
 * - The engine operates using the Universal Chess Interface (UCI), which allows for clear command and response protocols.
 * - Some methods rely on a proper sequence of commands to ensure the engine is in the correct state before processing further instructions.
 * - Use `isEngineReady()` to ensure the engine is ready for a new command after initialization or heavy operations.
 */
public class StockfishConnector {

    private Process stockfish;
    private BufferedReader input;
    private BufferedWriter output;
    private static final String ENGINE_SOURCE = "stockfish/stockfish-windows-x86-64-avx2";
    private double rating;

    /**
     * Starts the Stockfish engine process and initializes input and output
     * streams for communication.
     *
     * @return true if the engine starts successfully, false if an error occurs
     * during the startup process.
     */
    public boolean startEngine() {
        try {
            stockfish = new ProcessBuilder(ENGINE_SOURCE).start();
            input = new BufferedReader(new InputStreamReader(stockfish.getInputStream()));
            output = new BufferedWriter(new OutputStreamWriter(stockfish.getOutputStream()));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Sends a specified command to the Stockfish engine for processing.
     *
     * @param command The UCI command to be sent to the engine.
     * @throws IOException If an I/O error occurs during communication with the
     * engine.
     */
    public void sendCommand(String command) throws IOException {
        output.write(command + "\n");
        output.flush();
    }

    /**
     * Reads and returns the response from the Stockfish engine until a
     * termination keyword is found.
     *
     * @return A string containing the response from the engine, up to and
     * including specific keywords such as "uciok", "bestmove", or "readyok".
     * @throws IOException If an I/O error occurs while reading from the
     * engine's output stream.
     */
    public String getResponse() throws IOException {
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = input.readLine()) != null) {
            sb.append(line).append("\n");
            if (line.equals("uciok") || line.startsWith("bestmove") || line.equals("readyok")) {
                break;
            }
        }
        return sb.toString();
    }

    /**
     * Retrieves the best move suggested by the Stockfish engine based on the
     * current game state.
     *
     * @return A string representing the best move in standard algebraic
     * notation.
     * @throws IOException If an I/O error occurs while reading from the
     * engine's output stream.
     */
    public String getBestMove() throws IOException {
        String bestMove = null;
        String line;
        long startTime = System.currentTimeMillis();

        while ((line = input.readLine()) != null) {
            // System.out.println("Stockfish response: " + line); // Debug log

            // Check for and parse the evaluation score if it contains "cp"
            if (line.contains("cp")) {
                String[] parts = line.split(" ");
                for (int i = 0; i < parts.length; i++) {
                    if (parts[i].equals("cp")) {
                        this.rating = Double.parseDouble(parts[i + 1]) / 100.0;
                        // System.out.println("Updated rating: " + this.rating); // Debug log
                    }
                }
            }

            // Stop reading when the "bestmove" line is found
            if (line.startsWith("bestmove")) {
                String[] parts = line.split(" ");
                bestMove = parts[1];
                break;
            }

            // Exit the loop if it takes too long to prevent potential infinite looping
            if (System.currentTimeMillis() - startTime > 5000) {
                // System.out.println("Timeout reached while reading response for best move.");
                break;
            }
        }

        return bestMove;
    }

    /**
     * Stops the Stockfish engine and releases all associated resources.
     *
     * Sends the "quit" command to gracefully terminate the engine process and
     * destroys the process to ensure that it is properly closed. Catches and
     * logs any IOException that occurs during the process.
     */
    public void stopEngine() {
        try {
            sendCommand("quit");
            stockfish.destroy();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Updates the game state in the Stockfish engine to reflect the current
     * position based on the provided move history.
     *
     * @param moveHistory A string representing the sequence of moves in
     * standard UCI format. If null or empty, the engine sets the position to
     * the starting position.
     * @throws IOException If an I/O error occurs while sending the command to
     * the engine.
     */
    public void updateGameState(String moveHistory) throws IOException {
        if (moveHistory == null || moveHistory.isEmpty()) {
            sendCommand("position startpos");
        } else {
            sendCommand("position startpos moves " + moveHistory);
        }
    }

    /**
     * Instructs the Stockfish engine to calculate the best move within a
     * specified time limit.
     *
     * @param timeLimitMillis The time limit in milliseconds for the engine to
     * perform the calculation.
     * @throws IOException If an I/O error occurs while sending the command to
     * the engine.
     */
    public void calculateBestMove(int timeLimitMillis) throws IOException {
        sendCommand("go movetime " + timeLimitMillis);
    }

    /**
     * Checks if the Stockfish engine is ready to receive new commands.
     *
     * @return true if the engine responds with "readyok", indicating it is
     * ready; false otherwise.
     * @throws IOException If an I/O error occurs while sending the command or
     * reading the response from the engine.
     */
    public boolean isEngineReady() throws IOException {
        sendCommand("isready");
        String response = getResponse();
        return response.contains("readyok");
    }

    /**
     * Sets the analysis depth for the Stockfish engine, instructing it to
     * search up to a specific depth.
     *
     * @param depth The number of plies (half-moves) for the engine to analyze.
     * @throws IOException If an I/O error occurs while sending the command to
     * the engine.
     */
    public void setAnalysisDepth(int depth) throws IOException {
        sendCommand("go depth " + depth);
    }

    /**
     * Sets a specific option for the Stockfish engine with a given value.
     *
     * @param option The name of the option to be configured in the engine.
     * @param value The value to be assigned to the specified option.
     * @throws IOException If an I/O error occurs while sending the command to
     * the engine.
     */
    public void setOption(String option, String value) throws IOException {
        sendCommand("setoption name " + option + " value " + value);
    }

    /**
     * Retrieves a list of legal moves from the current position in the
     * Stockfish engine.
     *
     * @return A list of strings representing all legal moves available from the
     * current position.
     * @throws IOException If an I/O error occurs while sending the command or
     * reading the response from the engine.
     */
    public List<String> getLegalMoves() throws IOException {
        sendCommand("d");
        List<String> legalMoves = new ArrayList<>();
        String line;
        while ((line = input.readLine()) != null) {
            if (line.startsWith("Legal moves:")) {
                String[] moves = line.replace("Legal moves: ", "").split(" ");
                for (String move : moves) {
                    legalMoves.add(move);
                }
                break;
            }
        }
        return legalMoves;
    }

    /**
     * Enables or disables debug mode for the Stockfish engine by setting the
     * debug log file.
     *
     * @param enable A boolean indicating whether to enable (true) or disable
     * (false) debug mode.
     * @throws IOException If an I/O error occurs while sending the command to
     * the engine.
     */
    public void setDebugMode(boolean enable) throws IOException {
        sendCommand("setoption name Debug Log File value " + (enable ? "debug.log" : ""));
    }

    /**
     * Retrieves the full analysis output from the Stockfish engine up to the
     * point where it finds the best move.
     *
     * @return A string containing the detailed analysis output from the engine.
     * @throws IOException If an I/O error occurs while reading the response
     * from the engine.
     */
    public String getAnalysisOutput() throws IOException {
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = input.readLine()) != null) {
            sb.append(line).append("\n");
            if (line.startsWith("bestmove")) {
                break;
            }
        }
        return sb.toString();
    }

    /**
     * Sets the number of threads to be used by the Stockfish engine for
     * processing.
     *
     * @param threads The number of threads to be allocated for the engine's
     * operations.
     * @throws IOException If an I/O error occurs while sending the command to
     * the engine.
     */
    public void setThreads(int threads) throws IOException {
        sendCommand("setoption name Threads value " + threads);
    }

    /**
     * Clears the hash table in the Stockfish engine, effectively starting a new
     * game.
     *
     * @throws IOException If an I/O error occurs while sending the command to
     * the engine.
     */
    public void clearHash() throws IOException {
        sendCommand("ucinewgame");
    }

    /**
     * Checks if a given move is valid based on the current game state in the
     * Stockfish engine.
     *
     * @param move A string representing the move in standard algebraic
     * notation.
     * @return true if the move is legal; false otherwise.
     * @throws IOException If an I/O error occurs while retrieving legal moves
     * from the engine.
     */
    public boolean isValidMove(String move) throws IOException {
        List<String> legalMoves = getLegalMoves();
        return legalMoves.contains(move);
    }

    /**
     * Retrieves the most recent evaluation rating of the move from the
     * Stockfish engine.
     *
     * @return A double representing the evaluation rating of the move, with
     * positive values favoring White and negative values favoring Black.
     */
    public double getMoveRating() {
        return this.rating;
    }


}



