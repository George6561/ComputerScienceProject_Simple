package com.chess.stockfish;

import java.io.IOException;

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
 * Interface: ChessEngine
 *
 * This interface defines the core method that must be implemented by any class 
 * that acts as a bridge between a chess application and an external chess 
 * engine. Implementing classes should ensure compatibility with engine protocols 
 * such as UCI (Universal Chess Interface).
 *
 * Key method:
 * - `getNextMove()`: Returns the best move as calculated by the engine.
 *
 * Dependencies:
 * - Java I/O (`IOException`) for handling potential input/output operations 
 * that may arise during communication with the engine.
 *
 * Usage:
 * - `getNextMove()`: This method should be called after the engine has been 
 * initialized and provided with the current game state to calculate and return 
 * the best move.
 */
public interface ChessEngine {
    
    /**
     * Retrieves the next move suggested by the chess engine based on the 
     * current game state.
     *
     * @return A string representing the best move in standard algebraic notation.
     * @throws IOException If an I/O error occurs during the interaction with the 
     * engine, such as when reading from or writing to its input/output streams.
     */
    public String getNextMove() throws IOException;
    
}
