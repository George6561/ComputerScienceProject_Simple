## Features

### Core Functionality

#### **Game Mechanics:**
- Interactive chessboard implemented using `ChessBoard` and `ChessWindow` classes.
- Move generation and validation using Stockfish.
- Game states tracked and displayed in real-time.
- Support for special moves:
  - Castling
  - Pawn promotion

#### **Stockfish Integration:**
- Communicates with the Stockfish engine via the `StockfishConnector` class.
- Retrieves:
  - Optimal moves
  - Position evaluations
  - Legal moves
- Adjustable:
  - Analysis depth
  - Move time for flexible gameplay and analysis.

#### **Move Storage and Analysis:**
- Tracks move history using the `MoveStorage` class.
- Evaluates game states and stores position scores.

---

### User Interface

#### **JavaFX-Based GUI:**
- Visual representation of the chessboard and pieces in `ChessWindow` and `MainWindow`.
- Drag-and-drop functionality for piece movement.
- Dynamically updates the board and provides user feedback.

#### **Scalable Design:**
- Modular structure for easy extensibility.
- Separation of concerns:
  - Game logic (`ChessGame`)
  - UI (`ChessWindow`)
  - Engine communication (`StockfishConnector`)
