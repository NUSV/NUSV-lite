package com.nusv.lite.util

data class OssLibrary(
    val name: String,
    val license: String,
)

val ossLibraries = listOf(
    OssLibrary("AndroidX Compose (UI, Material 3, Animation, Material Icons)", "Apache License 2.0"),
    OssLibrary("AndroidX Activity, Core SplashScreen, Lifecycle, Navigation", "Apache License 2.0"),
    OssLibrary("AndroidX Room", "Apache License 2.0"),
    OssLibrary("Kotlin Standard Library, kotlinx.coroutines, kotlinx.serialization", "Apache License 2.0"),
    OssLibrary("OkHttp, Okio", "Apache License 2.0"),
    OssLibrary("Coil", "Apache License 2.0"),
    OssLibrary("ZXing Core", "Apache License 2.0"),
    OssLibrary("JUnit 4 (test scope only)", "Eclipse Public License 1.0"),
)
