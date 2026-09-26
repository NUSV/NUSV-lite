package com.nusv.lite.util

/**
 * Utilities for comparing dotted version strings such as "1.10.1", "v2.0" or
 * "1.10.0-alpha". Comparison is numeric per segment so "1.10.0" is correctly
 * treated as newer than "1.9.9", unlike a plain string comparison.
 */
object VersionUtils {

    /** Returns true when [remote] is a newer version than [current]. */
    fun isNewer(remote: String, current: String): Boolean = compare(remote, current) > 0

    /**
     * Compares two version strings numerically.
     * Returns a negative value if [a] is older, 0 if equal, positive if newer.
     * Missing segments are treated as zero ("1.10" == "1.10.0") and any
     * pre-release / build metadata after '-' or '+' is ignored.
     */
    fun compare(a: String, b: String): Int {
        val pa = parse(a)
        val pb = parse(b)
        val size = maxOf(pa.size, pb.size)
        for (i in 0 until size) {
            val x = pa.getOrElse(i) { 0 }
            val y = pb.getOrElse(i) { 0 }
            if (x != y) return x.compareTo(y)
        }
        return 0
    }

    private fun parse(version: String): List<Int> {
        val core = version.trim().removePrefix("v").removePrefix("V")
            .substringBefore('-')
            .substringBefore('+')
        return core.split('.').map { segment ->
            segment.takeWhile { it.isDigit() }.toIntOrNull() ?: 0
        }
    }
}
