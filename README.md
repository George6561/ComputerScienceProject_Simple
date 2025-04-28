## Features

### Core Functionality

#### **Game Mechanics:**
- Console-based chess game implemented using `ChessBoard` and `ChessGame` classes.
- Human plays as White, Stockfish plays as Black.
- Move input parsing supports various formats:
  - Simple pawn pushes (e.g., `e4`)
  - Full UCI format (e.g., `e2e4`)
  - Algebraic notation (e.g., `Nf3`, `exd5`)
- Move generation and validation handled through internal logic.
- Special moves supported:
  - Castling
  - En passant
  - Pawn promotion

#### **Stockfish Integration:**
- Communicates with the Stockfish engine using the `StockfishConnector` class.
- Retrieves:
  - Best moves
  - Position evaluations
  - Legal move lists
- Configurable engine behavior:
  - Search depth
  - Move time per turn
  - Thread management for performance tuning

#### **Move Storage and Analysis:**
- Tracks move history internally in `ChessGame`.
- Displays real-time move history in PGN-like format after every turn.
- Automatically updates and sends move sequences to Stockfish for evaluation.

---

### User Interface

#### **Console-Based Interaction:**
- Pure text input/output via terminal.
- Clean and intuitive prompts for player moves.
- Instant feedback and updated board state after every move.

#### **Scalable Design:**
- Modular structure designed for easy extension:
  - Game logic (`ChessGame`)
  - Board state management (`ChessBoard`)
  - Engine communication (`StockfishConnector`)
- Easily expandable for features like full PGN export, advanced analysis, or GUI integration.
