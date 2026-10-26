# JavaFX Tetris

A feature-rich Tetris game built in **Java 21** and **JavaFX**, with an AI player, a networked external player, two-player mode, JSON persistence and a fully tested game engine. Designed around **MVC**, **SOLID/GRASP** principles and classic **OOP design patterns**.

> Team project for a university Object-Oriented Software Development course (2026) — graded **100/100** (Milestone 1) and **96/100** (Final).

<p align="center">
  <img src="docs/gameplay.gif" alt="Two AI players competing in two-player mode" width="640">
</p>

📺 **Demo videos:** [Milestone 1](https://www.youtube.com/watch?v=NxlvT8MpbKM) · [Final submission](https://www.youtube.com/watch?v=xKUyLLbiOf4)

---

## Features

- **Classic gameplay** — 7 tetromino types, rotation, soft drop, multi-row clearing, pause/resume
- **Scoring & levels** — 100 / 300 / 600 / 1000 points for 1–4 lines, level progression, live score/level/lines display
- **AI player** — evaluates placements and plays automatically; can be toggled mid-game
- **External player** — connects to an external game server over TCP (`localhost:3000`), runs network calls off the UI thread, warns when the server is unavailable and resumes control when it comes back
- **Two-player mode** — two boards running simultaneously with an identical piece sequence; each side can be Human, AI or External
- **Dynamic board size** — configurable width (5–15), height (15–30) and starting level (1–10); the window resizes and re-centres automatically
- **Music & sound effects** — background music and effects, toggled in settings or in-game (`M` / `S`)
- **Persistence** — settings and top-10 high scores saved to JSON and restored across runs

## Tech Stack

| Area | Tools |
|---|---|
| Language | Java 21 |
| UI | JavaFX 21 (controls, media, inline CSS styling) |
| Build | Maven |
| Testing | JUnit 5 (incl. parameterized tests), Mockito |
| Version control | Git / GitHub (feature branches, pull requests, code review, tags) |

## Getting Started

**Requirements:** JDK 21+ and Maven 3.9+

```bash
git clone https://github.com/vy-trg/javafx-tetris.git
cd javafx-tetris
mvn clean javafx:run   # run the game
mvn test               # run the test suite
```

> The External Player mode needs a compatible Tetris server listening on `localhost:3000`. The server used during the course is not included in this repository; all other modes work without it.

### Controls (single player)

| Key | Action |
|---|---|
| ← / → | Move left / right |
| ↓ | Soft drop |
| ↑ | Rotate |
| P | Pause / resume |
| M | Toggle music |
| S | Toggle sound effects |
| A | Toggle AI play |

## Architecture

The project follows **Model–View–Controller**. `GameController` contains all game rules and has **no JavaFX dependency** — the View calls its command methods (`handleMoveLeft()`, `handleRotate()`, `tick()`…) and the Controller reports events back only through `Runnable` / `IntConsumer` callbacks, so the game engine can be unit-tested without a UI.

![MVC overview](docs/mvc-diagram.png)

<details>
<summary><b>Full class diagram</b></summary>

![Class diagram](docs/class-diagram.png)

</details>

### Design patterns

![State pattern](docs/state-pattern.png)

| Pattern | Where | Why |
|---|---|---|
| **State** | `GameState` → `PlayingState`, `PausedState`, `GameOverState` | Removes `if (paused) / else if (gameOver)` branching; each input method in `GameController` is a one-line delegation, and new phases can be added without touching existing code |
| **Factory** | `TetrominoFactory` | Centralises creation of the 7 piece types from a `TetrominoType` enum |
| **Singleton** | `ConfigurationManager`, `HighScoreManager`, `SoundManager`, state objects | One shared source of settings, scores and audio state |
| **Facade** | `SoundManager` | Hides JavaFX `Media`, `MediaPlayer` and `AudioClip` behind a few simple methods and handles missing audio files gracefully |

### Other Java features used

- **Abstract classes & interfaces** — `Tetromino` (abstract) implements `Rotatable`; 7 concrete piece classes
- **Enums & records** — `TetrominoType` carries each piece's colour; `RowClearResult` record for row-clear results
- **Streams, lambdas & `Comparator`** — sorting and capping the top-10 high scores; event callbacks
- **Generics & thread safety** — `TetrominoSequence` uses a `synchronized` `List<TetrominoType>` so both players always receive the same pieces
- **Concurrency & networking** — `ExternalPlayer` uses a TCP `Socket` with connect/read timeouts, a single daemon thread `ExecutorService` and `CompletableFuture`, so the game loop never blocks on the network
- **File I/O & JSON** — settings and scores persisted between sessions

## Testing

**16 tests, all passing** (`mvn test`), targeting the pure-logic classes so they run in milliseconds without a UI:

- `BoardTest` — default and custom grid sizes, row clearing and scoring, reset
- `ConfigurationManagerTest` — validated setters (e.g. rejecting out-of-range widths)
- `HighScoreManagerTest` — descending sort, top-10 cap, blank-name handling
- `GameControllerAdvancedTest`
  - **Parameterized test** (`@ParameterizedTest` + `@CsvSource`) across 5×15, 10×20 and 15×30 boards
  - **Hand-written stub** recording how many times the game-over callback fires
  - **Mockito mock** verifying the same behaviour

## Project Structure

```
src/
├── main/java/
│   ├── Main.java, SplashScreen.java, MainMenu.java,
│   │   ConfigurationScreen.java, HighScoresScreen.java
│   └── org/oosd/
│       ├── GameScreen.java, TwoPlayerGameScreen.java
│       ├── ConfigurationManager.java, HighScoreManager.java
│       ├── audio/        SoundManager
│       ├── controller/   GameController, GameState + 3 states,
│       │                 AIPlayer, ExternalPlayer, TetrominoSequence
│       └── model/        Board, Tetromino, Rotatable, TetrominoFactory,
│                         TetrominoType, I/J/L/O/S/T/Z pieces
└── test/java/org/oosd/   Board, ConfigurationManager, HighScoreManager,
                          GameController tests
```

## Team & Contributions

Built by a team of three:

**Vy Truong** ([@vy-trg](https://github.com/vy-trg))
- Core gameplay engine: `Board`, the `Tetromino` hierarchy (abstract class + `Rotatable` interface + 7 pieces) and `GameScreen` rendering, controls, pause and row clearing
- Two-player mode with a shared, thread-safe piece sequence (`TetrominoSequence`)
- AI player and dynamic board sizing
- State pattern (`GameState` and its implementations) and the MVC design
- The full test suite, including parameterized tests, a hand-written stub and Mockito mocks
- Delivered gameplay through a feature branch and pull request with code review

**Aiden Lucas** ([@alucas-gu](https://github.com/alucas-gu)) — team lead and repository manager; music and sound (`SoundManager` facade), external player networking

**Tasmia Faizal Aboo** ([@tas-mia121](https://github.com/tas-mia121)) — menus and screens, configuration screen, high scores and scoring rules

---

*Originally developed in a shared team repository*
