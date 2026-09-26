package com.nusv.lite.util

import android.content.Context
import com.nusv.lite.widget.NusvWidgetProvider
import java.util.Calendar

object PointsManager {
    private const val PREFS_NAME = "points"
    private const val KEY_BALANCE = "balance"
    private const val KEY_LAST_CHECKIN = "last_checkin"
    private const val KEY_UNLOCKED = "unlocked"
    private const val KEY_SELECTED = "selected_theme"
    private const val KEY_STREAK = "streak"
    private const val KEY_LAST_STREAK_DATE = "last_streak_date"
    const val FREE_THEME = "Default (Pink)"
    const val ORCA_THEME = "Orca"
    private const val ORCA_PRICE = 10000
    private const val KEY_ORCA_PURCHASED = "orca_purchased"
    private const val STREAK_BONUS_THRESHOLD = 7
    private const val STREAK_BONUS_POINTS = 5

    private fun prefs(c: Context) = c.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getBalance(c: Context): Int = prefs(c).getInt(KEY_BALANCE, 0)

    fun getPointsMultiplier(c: Context): Int =
        if (getSelectedTheme(c) == ORCA_THEME && isOrcaPurchased(c)) 2 else 1

    fun addPoints(c: Context, amount: Int) {
        val balance = getBalance(c) + amount * getPointsMultiplier(c)
        prefs(c).edit().putInt(KEY_BALANCE, balance).apply()
        NusvWidgetProvider.refresh(c)
    }

    fun isOrcaPurchased(c: Context): Boolean = prefs(c).getBoolean(KEY_ORCA_PURCHASED, false)

    private fun setOrcaPurchased(c: Context, v: Boolean) {
        prefs(c).edit().putBoolean(KEY_ORCA_PURCHASED, v).apply()
    }

    fun isOrcaActive(c: Context): Boolean =
        getSelectedTheme(c) == ORCA_THEME && isOrcaPurchased(c)

    fun isOrcaEligible(c: Context): Boolean = getStreak(c) >= 7

    fun purchaseOrcaTheme(c: Context): Boolean {
        if (isOrcaPurchased(c)) return false
        if (!isOrcaEligible(c)) return false
        val balance = getBalance(c)
        if (balance < ORCA_PRICE) return false
        prefs(c).edit().putInt(KEY_BALANCE, balance - ORCA_PRICE).apply()
        setOrcaPurchased(c, true)
        unlock(c, ORCA_THEME)
        NusvWidgetProvider.refresh(c)
        return true
    }

    fun getSelectedTheme(c: Context): String = prefs(c).getString(KEY_SELECTED, FREE_THEME) ?: FREE_THEME

    fun setSelectedTheme(c: Context, name: String) {
        prefs(c).edit().putString(KEY_SELECTED, name).apply()
    }

    fun getUnlocked(c: Context): Set<String> =
        prefs(c).getStringSet(KEY_UNLOCKED, setOf(FREE_THEME)) ?: setOf(FREE_THEME)

    fun isUnlocked(c: Context, theme: String): Boolean = theme in getUnlocked(c)

    fun unlock(c: Context, theme: String) {
        val unlocked = getUnlocked(c).toMutableSet()
        unlocked.add(theme)
        prefs(c).edit().putStringSet(KEY_UNLOCKED, unlocked).apply()
    }

    fun getStreak(c: Context): Int = prefs(c).getInt(KEY_STREAK, 0)

    fun canCheckIn(c: Context): Boolean {
        val last = prefs(c).getLong(KEY_LAST_CHECKIN, 0L)
        if (last == 0L) return true
        val lastCal = Calendar.getInstance().apply { timeInMillis = last }
        val today = Calendar.getInstance()
        return lastCal.get(Calendar.DAY_OF_YEAR) != today.get(Calendar.DAY_OF_YEAR) ||
               lastCal.get(Calendar.YEAR) != today.get(Calendar.YEAR)
    }

    /** Returns points earned today (0 if already checked in) */
    fun checkIn(c: Context): Int {
        if (!canCheckIn(c)) return 0
        val raw = (1..3).random()
        val multiplier = getPointsMultiplier(c)
        val points = raw * multiplier
        val balance = getBalance(c) + points
        val now = System.currentTimeMillis()
        val last = prefs(c).getLong(KEY_LAST_CHECKIN, 0L)

        var streak = 1
        var bonus = 0
        if (last != 0L) {
            val lastCal = Calendar.getInstance().apply { timeInMillis = last }
            val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
            if (lastCal.get(Calendar.DAY_OF_YEAR) == yesterday.get(Calendar.DAY_OF_YEAR) &&
                lastCal.get(Calendar.YEAR) == yesterday.get(Calendar.YEAR)) {
                streak = getStreak(c) + 1
            }
        }

        if (streak >= STREAK_BONUS_THRESHOLD) {
            bonus = STREAK_BONUS_POINTS
        }

        prefs(c).edit()
            .putInt(KEY_BALANCE, balance + bonus)
            .putLong(KEY_LAST_CHECKIN, now)
            .putInt(KEY_STREAK, streak)
            .putLong(KEY_LAST_STREAK_DATE, now)
            .apply()
        NusvWidgetProvider.refresh(c)
        return points + bonus
    }

    /** Returns true if purchase succeeded */
    fun purchaseTheme(c: Context, theme: String): Boolean {
        if (isUnlocked(c, theme)) return false
        if (theme == ORCA_THEME) return false
        val price = 10
        val balance = getBalance(c)
        if (balance < price) return false
        prefs(c).edit().putInt(KEY_BALANCE, balance - price).apply()
        unlock(c, theme)
        NusvWidgetProvider.refresh(c)
        return true
    }
}
