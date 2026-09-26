package com.nusv.lite.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VersionUtilsTest {

    @Test
    fun numericSegmentsAreComparedAsNumbers() {
        assertTrue(VersionUtils.isNewer("1.10.0", "1.9.9"))
        assertTrue(VersionUtils.isNewer("1.2.0", "1.10.0").not())
        assertTrue(VersionUtils.isNewer("2.0.0", "10.0.0").not())
        assertTrue(VersionUtils.isNewer("10.0.0", "2.0.0"))
    }

    @Test
    fun equalVersionsAreNotNewer() {
        assertFalse(VersionUtils.isNewer("1.10.1", "1.10.1"))
        assertEquals(0, VersionUtils.compare("1.10.1", "1.10.1"))
    }

    @Test
    fun missingSegmentsCountAsZero() {
        assertEquals(0, VersionUtils.compare("1.10", "1.10.0"))
        assertEquals(0, VersionUtils.compare("1", "1.0.0"))
        assertFalse(VersionUtils.isNewer("1.10.0", "1.10"))
        assertTrue(VersionUtils.isNewer("1.10.1", "1.10"))
        assertEquals(0, VersionUtils.compare("1.10.0", "1.10"))
    }

    @Test
    fun leadingVAndPreReleaseSuffixAreIgnored() {
        assertEquals(0, VersionUtils.compare("v1.10.1", "1.10.1"))
        assertEquals(0, VersionUtils.compare("1.10.0-alpha", "1.10.0"))
        assertTrue(VersionUtils.isNewer("v1.11.0-beta", "v1.10.1"))
    }

    @Test
    fun nonNumericSegmentsFallBackToZero() {
        assertEquals(0, VersionUtils.compare("abc", "0"))
        assertTrue(VersionUtils.isNewer("1.0.1", "abc"))
    }
}
