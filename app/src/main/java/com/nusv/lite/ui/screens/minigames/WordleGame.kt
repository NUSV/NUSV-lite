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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.nusv.lite.util.GameStatsManager
import com.nusv.lite.util.LocalAppStrings
import com.nusv.lite.util.PointsManager
import com.nusv.lite.util.performIfEnabled
import kotlinx.coroutines.delay

private val wordList = listOf(
    "APPLE", "BRAIN", "CRANE", "DRIVE", "EAGLE", "FLAME", "GRAPE", "HOUSE",
    "IMAGE", "JUICE", "KNIFE", "LEMON", "MOUSE", "NIGHT", "OCEAN", "PIANO",
    "QUEEN", "RIVER", "STONE", "TIGER", "UMBRE", "VIVID", "WATER", "YACHT",
    "BREAD", "CLOUD", "DANCE", "EARTH", "FROST", "GREEN", "HEART", "IVORY",
    "JELLY", "KOALA", "LIGHT", "MAGIC", "NOVEL", "OLIVE", "PEARL", "QUEST",
    "ROBOT", "SALAD", "TOWER", "ULTRA", "VENUS", "WHALE", "XENON", "ZEBRA",
    "ALBUM", "BASIC", "CHESS", "DELTA", "ELITE", "FLARE", "GHOST", "HAPPY",
    "ADULT", "ANGRY", "BEACH", "BIRTH", "BLACK", "BLAME", "BLIND", "BLOOM",
    "BOAST", "BONUS", "BRASS", "BRICK", "BROWN", "BURST", "CABIN", "CANDY",
    "CARGO", "CARRY", "CATCH", "CHAIR", "CHARM", "CHASE", "CHEAP", "CHECK",
    "CHEST", "CHIEF", "CHILD", "CHILL", "CIVIC", "CLEAN", "CLEAR", "CLICK",
    "CLIMB", "CLOCK", "CLOTH", "COACH", "COAST", "COLOR", "CORAL", "COUCH",
    "COULD", "COUNT", "COURT", "COVER", "CRAZY", "CREAM", "CROWD", "CROWN",
    "CRUDE", "CURVE", "CYCLE", "DAIRY", "DECAY", "DIGIT", "DIRTY", "DOUBT",
    "DOUGH", "DRAFT", "DRAWN", "DREAM", "DRESS", "DRIED", "DRINK", "DROVE",
    "DYING", "EAGER", "EARLY", "EIGHT", "ELECT", "EMPTY", "ENEMY", "ENJOY",
    "ENTER", "EQUAL", "ERROR", "EVENT", "EVERY", "EXACT", "EXIST", "EXTRA",
    "FAITH", "FALSE", "FANCY", "FAULT", "FENCE", "FIBER", "FIELD", "FIFTY",
    "FIGHT", "FINAL", "FLASH", "FLEET", "FLESH", "FLOAT", "FLOOD", "FLOOR",
    "FLOUR", "FOCUS", "FORCE", "FORTH", "FORTY", "FORUM", "FOUND", "FRAME",
    "FRANK", "FRAUD", "FRESH", "FRONT", "FRUIT", "FULLY", "FUNNY", "GIANT",
    "GLASS", "GLOBE", "GLORY", "GRAND", "GRASS", "GRAVE", "GREAT", "GRILL",
    "GROSS", "GROUP", "GUARD", "GUESS", "GUEST", "GUIDE", "GUILT", "HAPPY",
    "HARSH", "HASTE", "HAUNT", "HEAVY", "HENCE", "HONOR", "HORSE", "HOTEL",
    "HOURL", "HUMAN", "HUMOR", "HURRY", "IDEAL", "IGNOE", "INDEX", "INNER",
    "INPUT", "ISSUE", "JOKER", "JUDGE", "LABEL", "LASER", "LAUGH", "LAYER",
    "LEARN", "LEASE", "LEVEL", "LEVER", "LIMIT", "LINER", "LIVER", "LOCAL",
    "LOGIC", "LOOSE", "LOVER", "LOWER", "LOYAL", "LUCKY", "LUNAR", "LUNCH",
    "MAKER", "MANOR", "MARCH", "MATCH", "MERRY", "METAL", "MINOR", "MIXED",
    "MODEL", "MONEY", "MONTH", "MORAL", "MOTOR", "MOUNT", "MOVIE", "MUSIC",
    "NAIVE", "NERVE", "NEVER", "NICHE", "NURSE", "OCCUR", "OFFER", "OFTEN",
    "ONION", "OPERA", "ORBIT", "ORDER", "ORGAN", "OTHER", "OUGHT", "OUTER",
    "OWNER", "PAINT", "PANEL", "PAPER", "PARIS", "PARTY", "PASTE", "PATCH",
    "PAUSE", "PEACE", "PHASE", "PHONE", "PHOTO", "PIANO", "PIECE", "PILOT",
    "PITCH", "PIXEL", "PLACE", "PLAIN", "PLANE", "PLANT", "PLATE", "PLAZA",
    "PLEAD", "PLUCK", "PLUMB", "PLUME", "PLUMP", "POINT", "POLAR", "POUND",
    "POWER", "PRESS", "PRICE", "PRIDE", "PRIME", "PRINT", "PRIOR", "PRIZE",
    "PROBE", "PROOF", "PROSE", "PROUD", "PROVE", "PSALM", "PULSE", "PUNCH",
    "PUPIL", "PURSE", "QUEEN", "QUERY", "QUEST", "QUEUE", "QUICK", "QUIET",
    "QUITE", "QUOTA", "QUOTE", "RADAR", "RADIO", "RAISE", "RALLY", "RANCH",
    "RANGE", "RAPID", "RATIO", "REACH", "REACT", "READY", "REALM", "REBEL",
    "REFER", "REIGN", "RELAX", "REPLY", "RIDER", "RIDGE", "RIFLE", "RIGHT",
    "RIGID", "RISKY", "RIVAL", "ROBIN", "ROCKY", "ROUTE", "ROYAL", "RUGBY",
    "RUINE", "RULER", "RURAL", "SAINT", "SALON", "SALTY", "SANDY", "SCALE",
    "SCARE", "SCENE", "SCENT", "SCOPE", "SCORE", "SCOUT", "SCRAP", "SEIZE",
    "SENSE", "SERVE", "SEVEN", "SHADE", "SHAFT", "SHAKE", "SHALL", "SHAME",
    "SHAPE", "SHARE", "SHARK", "SHARP", "SHEEP", "SHEER", "SHEET", "SHELF",
    "SHELL", "SHIFT", "SHINE", "SHIRT", "SHOCK", "SHORE", "SHORT", "SHOUT",
    "SIGHT", "SILLY", "SINCE", "SIXTH", "SIXTY", "SKILL", "SKIRT", "SLAVE",
    "SLEEP", "SLICE", "SLIDE", "SLOPE", "SMALL", "SMART", "SMELL", "SMILE",
    "SMOKE", "SNAKE", "SOLAR", "SOLID", "SOLVE", "SORRY", "SOUND", "SOUTH",
    "SPACE", "SPARE", "SPARK", "SPEAK", "SPEED", "SPEND", "SPICE", "SPINE",
    "SPITE", "SPLIT", "SPOKE", "SPOON", "SPORT", "SPRAY", "SQUAD", "STACK",
    "STAFF", "STAGE", "STAKE", "STALE", "STALL", "STAMP", "STAND", "STARE",
    "START", "STATE", "STAYS", "STEAK", "STEAL", "STEAM", "STEEL", "STEEP",
    "STEER", "STERN", "STICK", "STIFF", "STILL", "STOCK", "STONE", "STOOD",
    "STOOL", "STORE", "STORM", "STORY", "STOVE", "STRAP", "STRAW", "STRIP",
    "STUCK", "STUDY", "STUFF", "STYLE", "SUGAR", "SUITE", "SUNNY", "SUPER",
    "SURGE", "SWAMP", "SWEAR", "SWEAT", "SWEEP", "SWEET", "SWEPT", "SWIFT",
    "SWING", "SWORE", "SWORN", "TABLE", "TASTE", "TEACH", "TEETH", "THANK",
    "THEME", "THERE", "THICK", "THIEF", "THING", "THINK", "THIRD", "THORN",
    "THOSE", "THREE", "THREW", "THROW", "THUMB", "TIGHT", "TIMER", "TITLE",
    "TODAY", "TOKEN", "TOPIC", "TORCH", "TOTAL", "TOUCH", "TOUGH", "TOWEL",
    "TOWER", "TOXIC", "TRACE", "TRACK", "TRADE", "TRAIL", "TRAIN", "TRAIT",
    "TRASH", "TREAT", "TREND", "TRIBE", "TRICK", "TRIED", "TROOP", "TRUCK",
    "TRULY", "TRUMP", "TRUST", "TRUTH", "TUMOR", "TWICE", "TWIST", "TYING",
    "ULTRA", "UNCLE", "UNDER", "UNION", "UNITE", "UNITY", "UNTIL", "UPPER",
    "UPSET", "URBAN", "USAGE", "USUAL", "VALID", "VALUE", "VALVE", "VAULT",
    "VERSE", "VIDEO", "VIGOR", "VIRAL", "VIRUS", "VISIT", "VITAL", "VIVID",
    "VOCAL", "VODKA", "VOICE", "VOTER", "WAGON", "WASTE", "WATCH", "WATER",
    "WEARY", "WEAVE", "WHEEL", "WHERE", "WHICH", "WHILE", "WHITE", "WHOLE",
    "WHOSE", "WIDEN", "WIDTH", "WITCH", "WOMAN", "WORLD", "WORRY", "WORSE",
    "WORST", "WORTH", "WOULD", "WOUND", "WRATH", "WRITE", "WRONG", "WROTE",
    "YIELD", "YOUNG", "YOURS", "YOUTH",
)

private val correctColor = Color(0xFF4CAF50)
private val presentColor = Color(0xFFFFC107)
private val absentColor = Color(0xFF666666)

private enum class KeyStatus { CORRECT, PRESENT, ABSENT }

private fun computeGuessStatuses(guess: String, answer: String): List<KeyStatus> {
    val remaining = answer.toCharArray().toMutableList()
    val result = MutableList(5) { KeyStatus.ABSENT }
    for (i in 0 until 5) {
        if (guess[i] == answer[i]) {
            result[i] = KeyStatus.CORRECT
            remaining[i] = ' '
        }
    }
    for (i in 0 until 5) {
        if (result[i] == KeyStatus.CORRECT) continue
        val idx = remaining.indexOf(guess[i])
        if (idx >= 0) {
            result[i] = KeyStatus.PRESENT
            remaining[idx] = ' '
        }
    }
    return result
}

private fun computeKeyboardStatus(guesses: List<String>, answer: String): Map<Char, KeyStatus> {
    val result = mutableMapOf<Char, KeyStatus>()
    for (guess in guesses) {
        val statuses = computeGuessStatuses(guess, answer)
        for (i in guess.indices) {
            val c = guess[i]
            val cur = result[c]
            val st = statuses[i]
            when {
                st == KeyStatus.CORRECT -> result[c] = KeyStatus.CORRECT
                cur == KeyStatus.CORRECT -> {}
                st == KeyStatus.PRESENT && cur != KeyStatus.CORRECT -> result[c] = KeyStatus.PRESENT
                cur == null -> result[c] = KeyStatus.ABSENT
            }
        }
    }
    return result
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun WordleGame(onBack: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    val strings = LocalAppStrings.current
    val ctx = LocalContext.current
    var answer by remember { mutableStateOf(wordList.random()) }
    var guesses by remember { mutableStateOf(listOf<String>()) }
    var currentGuess by remember { mutableStateOf("") }
    var gameOver by remember { mutableStateOf(false) }
    var won by remember { mutableStateOf(false) }
    var shakeRow by remember { mutableStateOf(-1) }
    var shakeAnimOffset by remember { mutableStateOf(0f) }
    var wins by remember { mutableIntStateOf(GameStatsManager.getHighScore(ctx, "wordle")) }
    var rewardMsg by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(shakeRow) {
        if (shakeRow >= 0) {
            for (i in 1..3) {
                shakeAnimOffset = 10f; delay(40)
                shakeAnimOffset = -10f; delay(40)
            }
            shakeAnimOffset = 0f
            shakeRow = -1
        }
    }

    val keyboardStatus = remember(guesses, answer) { computeKeyboardStatus(guesses, answer) }

    fun submitGuess() {
        if (currentGuess.length != 5) return
        if (currentGuess !in wordList) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            shakeRow = guesses.size
            return
        }
        haptic.performIfEnabled()
        val newGuesses = guesses + currentGuess
        guesses = newGuesses
        if (currentGuess == answer) {
            won = true
            gameOver = true
            val newWins = wins + 1
            wins = newWins
            GameStatsManager.setHighScore(ctx, "wordle", newWins)
            val pts = 5
            PointsManager.addPoints(ctx, pts)
            rewardMsg = strings.gameYouEarned.format(pts)
        } else if (newGuesses.size >= 6) {
            gameOver = true
        }
        currentGuess = ""
    }

    fun onKeyPress(key: Char) {
        if (!gameOver && currentGuess.length < 5) currentGuess += key
    }

    fun onDelete() {
        if (currentGuess.isNotEmpty()) currentGuess = currentGuess.dropLast(1)
    }

    fun reset() {
        answer = wordList.random()
        guesses = emptyList()
        currentGuess = ""
        gameOver = false
        won = false
        shakeRow = -1
        rewardMsg = null
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp)
            .onKeyEvent { event ->
            if (event.type == KeyEventType.KeyUp) {
                when (event.key) {
                    Key.Enter -> { submitGuess(); true }
                    Key.Backspace -> { onDelete(); true }
                    else -> {
                        val code = event.nativeKeyEvent.keyCode
                        val c = if (code in android.view.KeyEvent.KEYCODE_A..android.view.KeyEvent.KEYCODE_Z) {
                            'A' + (code - android.view.KeyEvent.KEYCODE_A)
                        } else null
                        if (c != null) { onKeyPress(c); true }
                        else false
                    }
                }
            } else false
        },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.size(36.dp).background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp)).clickable { haptic.performIfEnabled(); onBack() },
                contentAlignment = Alignment.Center,
            ) { Text("\u2190", style = MaterialTheme.typography.titleLarge) }
            Spacer(Modifier.width(12.dp))
            Text(strings.toolTitles["wordle"] ?: "Word Guess", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.weight(1f))
            Column(horizontalAlignment = Alignment.End) {
                Text(strings.gameBestScore.format(wins), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (rewardMsg != null) {
                    Text(rewardMsg!!, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            for (row in 0 until 6) {
                val guess = if (row < guesses.size) guesses[row] else if (row == guesses.size) currentGuess else ""
                val isShaking = shakeRow == row
                val statuses = if (row < guesses.size) computeGuessStatuses(guesses[row], answer) else null

                Row(
                    modifier = Modifier.offset(x = if (isShaking) shakeAnimOffset.dp else 0.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    for (col in 0 until 5) {
                        val letter = if (col < guess.length) guess[col].toString() else " "
                        val hasLetter = letter != " "
                        val determined = row < guesses.size
                        val bg = if (determined && statuses != null) {
                            when (statuses[col]) {
                                KeyStatus.CORRECT -> correctColor
                                KeyStatus.PRESENT -> presentColor
                                KeyStatus.ABSENT -> absentColor
                            }
                        } else if (hasLetter) MaterialTheme.colorScheme.surfaceVariant
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        val textColor = if (determined) Color.White else MaterialTheme.colorScheme.onSurface

                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .background(bg, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = if (hasLetter) letter else "",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = textColor,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        if (gameOver) {
            Text(
                text = if (won) strings.gameYouWin else strings.gameAnswer.format(answer),
                style = MaterialTheme.typography.titleLarge,
                color = if (won) correctColor else MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                    .clickable { haptic.performIfEnabled(); reset() }
                    .padding(horizontal = 32.dp, vertical = 12.dp),
            ) { Text(strings.gameNewGame, color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold) }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    for (rowIdx in 0 until 3) {
                        val rowLetters = when (rowIdx) {
                            0 -> "QWERTYUIOP"
                            1 -> "ASDFGHJKL"
                            2 -> "ZXCVBNM"
                            else -> ""
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            rowLetters.forEach { c ->
                                val status = keyboardStatus[c]
                                val bg = when (status) {
                                    KeyStatus.CORRECT -> correctColor
                                    KeyStatus.PRESENT -> presentColor
                                    KeyStatus.ABSENT -> absentColor
                                    null -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(bg, RoundedCornerShape(6.dp))
                                        .clickable { haptic.performIfEnabled(); onKeyPress(c) }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        c.toString(),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (status != null) Color.White else MaterialTheme.colorScheme.onSurface,
                                        textAlign = TextAlign.Center,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                        .clickable { haptic.performIfEnabled(); onDelete() }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center,
                ) { Text("DEL", fontWeight = FontWeight.Bold) }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
                        .clickable { haptic.performIfEnabled(); submitGuess() }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center,
                ) { Text("ENTER", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary) }
            }
        }
    }
}
