package com.nusv.lite.ui.screens.minigames

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nusv.lite.util.GameStatsManager
import com.nusv.lite.util.LocalAppStrings
import com.nusv.lite.util.PointsManager
import com.nusv.lite.util.SoundManager
import com.nusv.lite.util.performIfEnabled
import kotlinx.coroutines.delay
import kotlin.random.Random

private const val TETRIS_COLS = 10
private const val TETRIS_ROWS = 20

private data class TPoint(val x: Int, val y: Int)

private class Tetromino(val shape: List<List<Int>>, val color: Color) {
    val cells: List<TPoint> = buildList {
        shape.forEachIndexed { r, row ->
            row.forEachIndexed { c, v ->
                if (v == 1) add(TPoint(c, r))
            }
        }
    }
}

private val TETRIS_SHAPES = listOf(
    listOf(listOf(1, 1, 1, 1)),
    listOf(listOf(1, 1), listOf(1, 1)),
    listOf(listOf(0, 1, 0), listOf(1, 1, 1)),
    listOf(listOf(0, 1, 1), listOf(1, 1, 0)),
    listOf(listOf(1, 1, 0), listOf(0, 1, 1)),
    listOf(listOf(1, 0, 0), listOf(1, 1, 1)),
    listOf(listOf(0, 0, 1), listOf(1, 1, 1)),
)

private val TETRIS_COLORS = listOf(
    Color(0xFF00BCD4), Color(0xFFFFEB3B), Color(0xFF9C27B0),
    Color(0xFF4CAF50), Color(0xFFF44336), Color(0xFF2196F3), Color(0xFFFF9800),
)

@Composable
fun Tetris(onBack: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    val strings = LocalAppStrings.current
    val ctx = LocalContext.current

    var board by remember { mutableStateOf(Array(TETRIS_ROWS) { Array<Color?>(TETRIS_COLS) { null } }) }
    var current by remember { mutableStateOf<Pair<TPoint, Tetromino>?>(null) }
    var score by remember { mutableIntStateOf(0) }
    var highScore by remember { mutableIntStateOf(GameStatsManager.getHighScore(ctx, "tetris")) }
    var gameOver by remember { mutableStateOf(false) }
    var isPaused by remember { mutableStateOf(false) }
    var hasAwarded by remember { mutableStateOf(false) }
    var rewardMsg by remember { mutableStateOf<String?>(null) }

    fun level() = score / 500 + 1

    fun collides(b: Array<Array<Color?>>, cells: List<TPoint>, offset: TPoint): Boolean {
        cells.forEach { c ->
            val x = c.x + offset.x
            val y = c.y + offset.y
            if (x < 0 || x >= TETRIS_COLS || y >= TETRIS_ROWS) return true
            if (y >= 0 && b[y][x] != null) return true
        }
        return false
    }

    fun spawn() {
        val idx = Random.nextInt(TETRIS_SHAPES.size)
        val t = Tetromino(TETRIS_SHAPES[idx], TETRIS_COLORS[idx])
        val start = TPoint(TETRIS_COLS / 2 - 2, -2)
        if (collides(board, t.cells, start)) {
            gameOver = true
            if (!hasAwarded) {
                hasAwarded = true
                GameStatsManager.setHighScore(ctx, "tetris", score)
                highScore = maxOf(highScore, score)
                val pts = (score / 20).coerceIn(1, 60)
                PointsManager.addPoints(ctx, pts)
                rewardMsg = strings.gameYouEarned.format(pts)
            }
        } else {
            current = start to t
        }
    }

    fun lock() {
        val cur = current ?: return
        val cells = cur.second.cells
        val offset = cur.first
        val newBoard: MutableList<Array<Color?>> =
            Array(TETRIS_ROWS) { r -> board[r].copyOf() }.toMutableList()
        cells.forEach { c ->
            val y = c.y + offset.y
            val x = c.x + offset.x
            if (y >= 0 && y < TETRIS_ROWS && x in 0 until TETRIS_COLS) {
                newBoard[y][x] = cur.second.color
            }
        }
        var cleared = 0
        var y = TETRIS_ROWS - 1
        while (y >= 0) {
            if (newBoard[y].all { it != null }) {
                newBoard.removeAt(y)
                newBoard.add(0, Array<Color?>(TETRIS_COLS) { null })
                cleared++
            } else {
                y--
            }
        }
        board = newBoard.toTypedArray()
        current = null
        if (cleared > 0) {
            val linePts = listOf(10, 30, 60, 100).getOrElse(cleared - 1) { 120 }
            score += linePts
            SoundManager.playWin()
            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
        } else {
            SoundManager.playSuccess()
        }
        spawn()
    }

    fun move(dx: Int) {
        val cur = current ?: return
        val newOff = TPoint(cur.first.x + dx, cur.first.y)
        if (!collides(board, cur.second.cells, newOff)) {
            current = newOff to cur.second
            SoundManager.playTap()
        }
    }

    fun rotate() {
        val cur = current ?: return
        val h = cur.second.shape.size
        val w = cur.second.shape[0].size
        val rotated = List(w) { r -> List(h) { c -> cur.second.shape[h - 1 - c][r] } }
        val t = Tetromino(rotated, cur.second.color)
        val offsets = listOf(TPoint(0, 0), TPoint(1, 0), TPoint(-1, 0), TPoint(0, -1), TPoint(2, 0), TPoint(-2, 0))
        for (off in offsets) {
            val newOff = TPoint(cur.first.x + off.x, cur.first.y + off.y)
            if (!collides(board, t.cells, newOff)) {
                current = newOff to t
                SoundManager.playTap()
                return
            }
        }
    }

    fun softDrop(): Boolean {
        val cur = current ?: return false
        val newOff = TPoint(cur.first.x, cur.first.y + 1)
        if (!collides(board, cur.second.cells, newOff)) {
            current = newOff to cur.second
            return true
        }
        lock()
        return false
    }

    fun hardDrop() {
        val cur = current ?: return
        var y = cur.first.y
        while (!collides(board, cur.second.cells, TPoint(cur.first.x, y + 1))) y++
        current = TPoint(cur.first.x, y) to cur.second
        lock()
    }

    fun restart() {
        board = Array(TETRIS_ROWS) { Array<Color?>(TETRIS_COLS) { null } }
        score = 0
        gameOver = false
        isPaused = false
        hasAwarded = false
        rewardMsg = null
        spawn()
    }

    LaunchedEffect(Unit) { spawn() }

    LaunchedEffect(gameOver, isPaused) {
        if (gameOver || isPaused) return@LaunchedEffect
        while (true) {
            delay((500 - (level() - 1) * 45).toLong().coerceAtLeast(100))
            if (gameOver || isPaused) return@LaunchedEffect
            softDrop()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = { onBack() }) { Text("‹") }
            Text(strings.toolTitles["tetris"] ?: "Falling Blocks", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            TextButton(onClick = { if (!gameOver) { haptic.performIfEnabled(); isPaused = !isPaused } }) {
                Text(if (isPaused) "▶" else "⏸")
            }
        }

        Text(
            text = "${strings.gameScore.format(score)}    ${strings.gameHighScore} $highScore    ${strings.lvLabel}${level()}",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(8.dp))

        val cellSize = 18.dp
        val cur = current
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                .padding(4.dp)
        ) {
            (0 until TETRIS_ROWS).forEach { y ->
                Row {
                    (0 until TETRIS_COLS).forEach { x ->
                        val curColor = cur?.let { (off, piece) ->
                            val px = x - off.x
                            val py = y - off.y
                            if (py in 0 until 4 && px in 0 until 4 &&
                                piece.shape.getOrNull(py)?.getOrNull(px) == 1
                            ) piece.color else null
                        }
                        Box(
                            modifier = Modifier
                                .size(cellSize)
                                .padding(1.dp)
                                .background(
                                    curColor ?: board[y][x]
                                        ?: MaterialTheme.colorScheme.background,
                                    RoundedCornerShape(2.dp)
                                )
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        if (gameOver) {
            Text(strings.gameGameOver, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.error)
            rewardMsg?.let {
                Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(8.dp))
            Button(onClick = { haptic.performIfEnabled(); restart() }) { Text(strings.gamePlayAgain) }
            Spacer(Modifier.height(8.dp))
        } else if (isPaused) {
            Text(strings.gamePaused, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ControlBtn("◀", enabled = !gameOver && !isPaused) { move(-1) }
            ControlBtn("▶", enabled = !gameOver && !isPaused) { move(1) }
            ControlBtn("⟳", enabled = !gameOver && !isPaused) { rotate() }
            ControlBtn("▼", enabled = !gameOver && !isPaused) { softDrop() }
            ControlBtn("⤓", enabled = !gameOver && !isPaused) { hardDrop() }
        }

        Spacer(Modifier.height(8.dp))
        TextButton(onClick = { haptic.performIfEnabled(); restart() }) { Text(strings.gameNewGame) }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun ControlBtn(label: String, enabled: Boolean, onClick: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    Box(
        modifier = Modifier
            .size(52.dp)
            .background(
                if (enabled) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceVariant,
                RoundedCornerShape(12.dp)
            )
            .clickable(enabled = enabled) { haptic.performIfEnabled(); onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            style = MaterialTheme.typography.titleLarge,
            color = if (enabled) MaterialTheme.colorScheme.onPrimary
            else MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
