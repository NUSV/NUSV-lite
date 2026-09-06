# NUSV LITE

<p align="center"><img src="banner.svg" alt="NUSV LITE" width="100%"></p>

<p align="center">
  <a href="https://github.com/NUSV/NUSV-lite/releases"><img src="https://img.shields.io/github/v/release/NUSV/NUSV-lite?label=release" alt="Latest release"></a>
  <a href="LICENSE"><img src="https://img.shields.io/github/license/NUSV/NUSV-lite" alt="License"></a>
  <a href="https://github.com/NUSV/NUSV-lite"><img src="https://img.shields.io/github/stars/NUSV/NUSV-lite" alt="Stars"></a>
  <a href="https://github.com/NUSV/NUSV-lite"><img src="https://img.shields.io/github/languages/top/NUSV/NUSV-lite?label=language" alt="Language"></a>
  <img src="https://img.shields.io/badge/platform-Android-brightgreen?logo=android" alt="Platform">
</p>

Official Android client for NUSV, putting the entire NUSV ecosystem in your pocket.

## Features

- **Content hub**: browse the latest posts across all NUSV sites, organized by category for quick access
- **11 built-in games**: Tic-Tac-Toe, 2048, Minesweeper, Memory Match, Snake, Wordle, Simon Says, Whack-a-Mole, Tetris, Gomoku (vs. a threat-aware AI with two difficulty levels), Sudoku — earn points on wins, per-game high scores
- **60+ built-in tools**:
  - *Everyday*: dice & coin flips, tip / BMI / age calculators, unit & currency converters, world clock, stopwatch, countdown, pomodoro, bill splitter, date difference, expense tracker, quick timer, flashlight, battery info, anniversary countdown, sleep calculator, lottery generator
  - *Dev*: JSON formatter, Base64, hash, UUID, epoch & URL & color & case converters, regex tester, Markdown preview, QR code generator, text statistics, lorem ipsum, password generator, text encryption
  - *Creative*: drawing pad, kaomoji keyboard, name generator, random quotes, biorhythm charts, Morse code, metronome, breathing exercise
- **Points & check-ins**: daily check-in (1–3 pts) with streak rewards, points from games, redeemable in the theme shop
- **Theme shop**: color themes unlockable with points, including the **Orca premium theme** (pure black/white UI, ×2 points, 11 exclusive hidden tools) earned through long-term check-ins
- **Home-screen widget**: points & check-in streak at a glance, one-tap check-in
- **Achievements**: 10 badges covering games, tools, check-ins and points
- **Game sound effects**, haptic feedback, remote sync
- 中文 / English language switching, dark / light themes, list / 2-column / 3-column layouts

## Tech Stack

- Kotlin + Jetpack Compose
- Material 3 design language
- Room local database
- Inspired by Arc / Linear / Vercel

## Build

```bash
# Requires JDK 17
./gradlew assembleDebug
# Output: app/build/outputs/apk/debug/app-debug.apk
```

Release APKs live in the `release/` directory.

## Documentation

- [CHANGELOG](CHANGELOG.md) (complete version history from v1.1.0)
- Mirrored in the in-app "Docs" page
