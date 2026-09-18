# Changelog

> Mirrored in the in-app "Docs" page.

## v1.10.1 (2026-09-19)

- **Home-screen widget layout fix**: redesigned as a horizontal 3×1 layout (points + streak on the left, check-in button on the right); the button is no longer clipped
- **Widget i18n**: added English widget strings (previously fell back to Chinese on English devices)
- **Trademark cleanup**: "Wordle" → "Word Guess" and "Tetris" → "Falling Blocks" across UI, docs and changelog
- **Third-party notices**: added `THIRD_PARTY_NOTICES.md` (Apache 2.0 compliance) and an "Open Source Licenses" page in Settings

## v1.10.0 (2026-07-31)

- **New games ×3**:
  - Falling Blocks: 10×20 board, 7 classic tetrominoes, rotate / soft drop / hard drop, speed levels, line-clear scoring with high score
  - Gomoku: 15×15 board vs AI; threat-aware AI (always blocks open threes, kills open fours), 8-ply search + threat extension, Beginner / Ultimate difficulties, +100 points on win
  - Sudoku: auto-generated puzzles, Easy / Medium / Hard, check & erase, +40 points on completion
- **New tools ×3**: Anniversary (countdown to important dates), Sleep Calculator (90-minute cycle recommendations), Lottery Gen (5+2 lucky picks)
- **Home-screen widget**: shows points & check-in streak, one-tap check-in from the desktop
- **Achievements**: 10 achievement badges (games / tools / check-ins / points / Orca), progress viewable in Settings
- **Game sound effects**: new sound system (action / success / error / victory), toggle in Settings
- **Gomoku difficulties**: Beginner (win +20) / Ultimate (win +100, lose +2)
- **Theme shop expansion**: 12 new color themes, 50 points each
- **Matrix rain rewrite**: Canvas-based trailing rain (bright head + fading tail + random reset)
- **Theme state fix**: switching themes no longer leaves multiple "In use" states
- **Whack-a-Mole stability rewrite**: no animations/emoji dependencies, full exception protection
- **Sleep Calculator fix**: fixed crash (missing format string arguments)
- **Pre-release full audit fixes**:
  - Gomoku AI strengthened: 8-ply search + threat extension, 6:0 vs Beginner AI
  - Gomoku: AI stuck after restart fixed
  - Word Guess: physical keyboard input fixed
  - Whack-a-Mole: leaving the game mid-round no longer grants rewards
  - Snake: rapid direction changes no longer reverse the snake
  - Sudoku: puzzle generation / difficulty switching state fixed; high-score formatting fixed
  - World Clock: whole-screen freeze fixed; city cards now show country names in both languages
  - Biorhythm: date offset fixed (no longer serves stale cached data)
  - Anniversary: cross-timezone day-count offset fixed
  - Scientific Calculator: large numbers now shown in scientific notation
  - Morse Code: space mapping fixed both ways; Interval Timer timing fixed; Kaomoji corrupted characters fixed
- **Full i18n pass**: all hardcoded UI strings (Home / Browse / Search / Detail / Docs / Settings / Theme Shop / World Clock / Falling Blocks) now switch between Chinese and English
- Version bumped to 1.10.0 (73 tools total)

## v1.9.0 (2026-07-30)

- **Orca premium theme**: all-new pure black/white premium theme system
  - Unlock eligibility with a 7-day check-in streak (10,000 points required)
  - Forces dark mode when active, pure black background + pure white accents
  - Cards switch to black with white borders
  - Double points (×2) from all games and check-ins
  - Home title shows "NUSV Orca"
  - Unlocks 11 exclusive hidden tools: Matrix Rain, Secret Vault, Clipboard History, Habit Tracker, Text Encrypt, Speed Reader, Color Picker, Password Checker, Text Diff, CSS Gradient, Quick Notes
- **Flashlight tool**: toggle pure white / background color, simulates a flashlight
- **Battery info tool**: charge level, charging state, temperature, voltage
- **Quick Timer tool**: 1/3/5/10-minute presets, start/pause/reset
- **Full zh/en translation pass**: Chinese & English titles/descriptions for every tool
- Deep code refactor and stability improvements
- Version bumped to 1.9.0 (67 tools total)

## v1.8.0 (2026-07-30)

- **Earn points from games**: all 8 games award points for winning/finishing
  - Tic-Tac-Toe: +3 win / +1 draw
  - 2048: total score / 100 (min 1)
  - Minesweeper: +5 on win
  - Memory Match: +5 on completion
  - Snake: +1 per food
  - Word Guess: +5 on correct guess
  - Simon Says: +1 per round
  - Whack-a-Mole: +1 per 2 moles
- **Game high scores**: new GameStatsManager, best scores tracked per game, shown in-game as Best
- **Check-in streak**: consecutive-day tracking, +5 bonus after 7 consecutive days
- **Theme shop**: shows check-in streak (🔥 flame icon + streak reward hint)
- **Game animation fixes**: dice roll, coin flip, magic 8-ball shake now animate correctly
- **Morse Code fix**: removed non-standard `#` mapping conflicting with `@`
- **Word Guess dictionary expansion**: 56 → 540+ common 5-letter words
- **Word Guess keyboard position**: added 96dp bottom padding to avoid navigation bar overlap
- **Memory Match freeze fix**: game no longer gets stuck after two mismatched flips
- Version bumped to 1.8.0 (47 tools total)

## v1.7.1 (2026-07-30)

- Fixed multiple bugs introduced in 1.7.0
- Word Guess dictionary expanded from 56 to 540+ common words
- Word Guess keyboard bottom padding to avoid navigation bar overlap
- Fixed Memory Match freeze after two mismatched flips
- Version bumped to 1.7.1

## v1.7.0 (2026-07-30)

- 4 new games: Snake (Canvas body gradient + pulsing food), Word Guess (virtual keyboard + letter state colors), Simon Says (4 glowing buttons), Whack-a-Mole (30-second challenge)
- Game effects: TicTacToe spring placement + win pulse, Memory Match card pop-in + match highlight, Snake food particle rotation
- All games fully integrated with the language switching system (zh/en)
- Added 6 missing haptic feedback triggers
- Result sharing: random quotes, QR content, name generator, scientific calculator
- Discover layout switching (list / 2-col / 3-col grid)
- Discover search keyword highlighting (matched text becomes theme-colored + bold)
- More themes / theme shop fully localized
- Recent tools capped at 5, sorted by recency left-to-right
- Dark mode home card color fixes
- Version bumped to 1.7.0 (47 tools total)

## v1.6.5 (2026-07-30)

- Discover switched to list layout (replacing the grid)
- Recent tools capped at 5, newest on the left
- More themes / theme shop fully localized
- 5 missing haptic feedback triggers added
- Dark mode home card color fixes
- Version bumped to 1.6.5
- Settings layout switching (list / 2-col / 3-col grid)
- Discover search keyword highlighting
- Result sharing: random quotes, QR content, name generator, scientific calculator
- 4 new games: Snake, Word Guess, Simon Says, Whack-a-Mole
- Game effects: TicTacToe spring animation + win pulse, Memory Match card pop-in, Snake food particles

## v1.6.0 (2026-07-30)

- Daily check-in system (1-3 random points, once per day)
- Theme shop (10 color themes, 10 points each)
- Settings "More Themes" entry
- Discover search box (real-time name/description filtering)
- Home recent tools row
- App Shortcuts (long-press launcher icon: QR / Drawing Pad / Dice / Calculator)
- Light mode featured cards switched to purple

## v1.5.0 (2026-07-30)

- 6 new advanced tools:
  - Scientific Calculator (trig / log / parentheses / history)
  - Drawing Pad (freehand / colors / brushes / undo / save)
  - Markdown live preview (edit / preview / split)
  - Biorhythm (physical / emotional / intellectual curves)
  - Metronome (BPM / time signature / tap tempo / vibration)
  - Morse Code (text ↔ morse / vibration playback / copy)
- 4 new generative tools:
  - QR Code Generator (ZXing encoding / save to gallery)
  - Kaomoji Keyboard (8 categories, 300+ faces / search / one-tap copy)
  - Random Quotes (60 quotes / category filter / copy)
  - Name Generator (fantasy / Japanese / English / sci-fi / Chinese)
- Drawing Pad heavily enhanced: spray / marker / rainbow / calligraphy brushes, eraser, opacity slider, square brush, hue picker
- Discover recommendation ordering by usage frequency
- Tool count from 33 → 43
- Bug fixes and performance improvements

## v1.4.0 (2026-07-30)

- Language switching system (中文 / English)
- Discover category filter (All / Games / Utilities / Dev Tools / Other)
- Mini-app tool animations (dice bounce, coin spin, password fade, stopwatch pulse)
- Bug fixes: Settings scrolling, language switch not taking effect

## v1.3.0 (2026-07-29)

- Settings page: theme switching, haptic feedback toggle, sync button
- 33 mini-app tools:
  - Mini games: Dice, Coin Flip, Magic 8-Ball, Rock-Paper-Scissors, Slot Machine
  - Everyday: Tip Calculator, BMI Calculator, Random Number, Unit Converter, Stopwatch, Countdown Timer, Text Statistics, Age Calculator, Password Generator, Percentage, Base Converter, Bill Splitter, Date Difference
  - Productivity: Pomodoro, Decision Maker, Palindrome Checker, Todo List, JSON Formatter, Base64 Tool, Color Converter, UUID Generator, Hash Generator, Epoch Converter, URL Tool, Case Converter, Regex Tester, Lorem Ipsum, Breathing Exercise
- Page transition animations + scalePress feedback
- Splash screen

## v1.2.0 (2026-07-29)

- Discover page (Mini-app grid layout)
- Remote sync framework (SyncManager)
- HapticPrefs haptic feedback system

## v1.1.0 (2026-07-29)

- Docs page + Markdown rendering
- Tab navigation between pages
