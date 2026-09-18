package com.nusv.lite.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nusv.lite.data.SyncManager
import com.nusv.lite.data.SyncResult
import com.nusv.lite.util.HapticPrefs
import com.nusv.lite.util.Lang
import com.nusv.lite.util.LanguagePrefs
import com.nusv.lite.util.LayoutMode
import com.nusv.lite.util.LayoutPrefs
import com.nusv.lite.util.LocalAppStrings
import com.nusv.lite.util.SoundManager
import com.nusv.lite.util.SoundPrefs
import com.nusv.lite.util.ossLibraries
import com.nusv.lite.BuildConfig
import com.nusv.lite.util.performIfEnabled
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SettingsScreen(
    syncManager: SyncManager,
    onThemeChange: ((Boolean?) -> Unit)? = null,
    onLanguageChange: ((Lang) -> Unit)? = null,
    onThemeShopClick: (() -> Unit)? = null,
    onAchievementsClick: (() -> Unit)? = null,
) {
    var themeOption by remember { mutableStateOf("system") }
    val haptic = LocalHapticFeedback.current
    var syncing by remember { mutableStateOf(false) }
    var syncStatus by remember { mutableStateOf<String?>(null) }
    var easterEggClicks by remember { mutableStateOf(0) }
    var showEasterEgg by remember { mutableStateOf(false) }
    var showLicenses by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val strings = LocalAppStrings.current
    var currentLang by remember { mutableStateOf(LanguagePrefs.get()) }

    val lastSync = syncManager.getLastSyncTime()
    val lastSyncText = if (lastSync > 0) {
        val date = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault())
            .format(Date(lastSync))
        "${strings.settingsLastSync}: $date"
    } else {
        strings.settingsNeverSync
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = 24.dp)
    ) {
        Text(
            text = strings.settingsTitle,
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(Modifier.height(32.dp))

        Text(
            text = strings.settingsTheme,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = 20.dp)
        )
        Spacer(Modifier.height(12.dp))
        ThemeOption(strings.settingsThemeSystem, "system", themeOption, haptic) {
            themeOption = it; onThemeChange?.invoke(null)
        }
        ThemeOption(strings.settingsThemeDark, "dark", themeOption, haptic) {
            themeOption = it; onThemeChange?.invoke(true)
        }
        ThemeOption(strings.settingsThemeLight, "light", themeOption, haptic) {
            themeOption = it; onThemeChange?.invoke(false)
        }

        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable { haptic.performIfEnabled(); onThemeShopClick?.invoke() }
                .padding(horizontal = 16.dp, vertical = 14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(strings.settingsMoreThemes, style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary)
                    Text(strings.settingsMoreThemesDesc, style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(">", style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable { haptic.performIfEnabled(); onAchievementsClick?.invoke() }
                .padding(horizontal = 16.dp, vertical = 14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(strings.achievementsTitle, style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary)
                    Text(strings.achievementsDesc, style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(">", style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Spacer(Modifier.height(32.dp))

        Text(
            text = strings.settingsLayout,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = 20.dp)
        )
        Spacer(Modifier.height(12.dp))
        val layoutMode = remember { mutableStateOf(LayoutPrefs.get()) }
        ThemeOption(strings.settingsLayoutList, "list", layoutMode.value.value, haptic) {
            layoutMode.value = LayoutMode.LIST; LayoutPrefs.set(LayoutMode.LIST)
        }
        ThemeOption(strings.settingsLayoutGrid2, "grid2", layoutMode.value.value, haptic) {
            layoutMode.value = LayoutMode.GRID_2; LayoutPrefs.set(LayoutMode.GRID_2)
        }
        ThemeOption(strings.settingsLayoutGrid3, "grid3", layoutMode.value.value, haptic) {
            layoutMode.value = LayoutMode.GRID_3; LayoutPrefs.set(LayoutMode.GRID_3)
        }

        Spacer(Modifier.height(32.dp))

        Text(
            text = strings.settingsHaptics,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = 20.dp)
        )
        Spacer(Modifier.height(8.dp))

        var hapticEnabled by remember { mutableStateOf(HapticPrefs.isEnabled()) }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = strings.settingsHaptics,
                style = MaterialTheme.typography.bodyLarge
            )
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        if (hapticEnabled) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant,
                        RoundedCornerShape(22.dp)
                    )
                    .clickable {
                        haptic.performIfEnabled()
                        hapticEnabled = !hapticEnabled
                        HapticPrefs.setEnabled(hapticEnabled)
                    },
                contentAlignment = if (hapticEnabled) Alignment.CenterEnd else Alignment.CenterStart
            ) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .padding(2.dp)
                        .background(
                            MaterialTheme.colorScheme.onPrimary,
                            RoundedCornerShape(9.dp)
                        )
                )
            }
        }

        Spacer(Modifier.height(32.dp))

        Text(
            text = strings.settingsSound,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = 20.dp)
        )
        Spacer(Modifier.height(8.dp))

        var soundEnabled by remember { mutableStateOf(SoundPrefs.isEnabled()) }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = strings.settingsSound,
                style = MaterialTheme.typography.bodyLarge
            )
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        if (soundEnabled) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant,
                        RoundedCornerShape(22.dp)
                    )
                    .clickable {
                        haptic.performIfEnabled()
                        soundEnabled = !soundEnabled
                        SoundPrefs.setEnabled(soundEnabled)
                        if (soundEnabled) SoundManager.playSuccess()
                    },
                contentAlignment = if (soundEnabled) Alignment.CenterEnd else Alignment.CenterStart
            ) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .padding(2.dp)
                        .background(
                            MaterialTheme.colorScheme.onPrimary,
                            RoundedCornerShape(9.dp)
                        )
                )
            }
        }

        Spacer(Modifier.height(32.dp))

        Text(
            text = strings.settingsLanguage,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = 20.dp)
        )
        Spacer(Modifier.height(8.dp))

        Lang.entries.forEach { lang ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = currentLang == lang,
                    onClick = {
                        haptic.performIfEnabled()
                        if (currentLang != lang) {
                            currentLang = lang
                            LanguagePrefs.set(lang)
                            onLanguageChange?.invoke(lang)
                        }
                    },
                    colors = RadioButtonDefaults.colors(
                        selectedColor = MaterialTheme.colorScheme.primary
                    )
                )
                Text(
                    text = lang.label,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }

        Text(
            text = strings.settingsLangBeta,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
        )

        Spacer(Modifier.height(32.dp))

        Text(
            text = strings.settingsSync,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = 20.dp)
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = lastSyncText,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 20.dp)
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "${strings.settingsSyncUrl}: ${syncManager.getSyncUrl()}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 20.dp)
        )
        Spacer(Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .height(44.dp)
                .background(
                    if (syncing) MaterialTheme.colorScheme.surfaceVariant
                    else MaterialTheme.colorScheme.primary,
                    RoundedCornerShape(10.dp)
                )
                .clickable(enabled = !syncing) {
                    haptic.performIfEnabled()
                    syncing = true
                    syncStatus = null
                    scope.launch {
                        val result = syncManager.syncAll()
                        syncStatus = when (result) {
                            SyncResult.SUCCESS -> strings.settingsSyncComplete
                            SyncResult.NO_UPDATE -> strings.settingsSyncNoUpdate
                            SyncResult.ERROR -> strings.settingsSyncFailed
                        }
                        syncing = false
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (syncing) strings.settingsSyncing else strings.settingsSyncNow,
                style = MaterialTheme.typography.titleSmall,
                color = if (syncing) MaterialTheme.colorScheme.onSurfaceVariant
                        else MaterialTheme.colorScheme.onPrimary
            )
        }

        if (syncStatus != null) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = syncStatus!!,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
        }

        Spacer(Modifier.height(32.dp))

        Text(
            text = strings.settingsAbout,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = 20.dp)
        )
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = strings.settingsVersion, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = BuildConfig.VERSION_NAME,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.clickable {
                    haptic.performIfEnabled()
                    easterEggClicks++
                    if (easterEggClicks >= 7) {
                        showEasterEgg = true
                        easterEggClicks = 0
                    }
                }
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { haptic.performIfEnabled(); showLicenses = true }
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = strings.settingsOpenSource, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = "\u203A",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(Modifier.weight(1f))

        Text(
            text = strings.settingsPowered,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 100.dp)
                .padding(horizontal = 20.dp),
        )
    }

    if (showLicenses) {
        AlertDialog(
            onDismissRequest = { showLicenses = false },
            title = {
                Text(
                    text = strings.ossTitle,
                    style = MaterialTheme.typography.headlineMedium
                )
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    ossLibraries.forEach { lib ->
                        Text(text = lib.name, style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = lib.license,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(10.dp))
                    }
                    Text(
                        text = strings.ossFooter,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(
                        text = strings.okLabel,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { haptic.performIfEnabled(); showLicenses = false }.padding(12.dp)
                    )
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(24.dp)
        )
    }

    if (showEasterEgg) {
        AlertDialog(
            onDismissRequest = { showEasterEgg = false },
            title = {
                Text(
                    text = strings.settingsEasterEgg,
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(
                        text = strings.okLabel,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { haptic.performIfEnabled(); showEasterEgg = false }.padding(12.dp)
                    )
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(24.dp)
        )
    }
}

@Composable
private fun ThemeOption(
    label: String,
    value: String,
    selectedValue: String,
    haptic: androidx.compose.ui.hapticfeedback.HapticFeedback,
    onSelect: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selectedValue == value,
            onClick = { haptic.performIfEnabled(); onSelect(value) },
            colors = RadioButtonDefaults.colors(
                selectedColor = MaterialTheme.colorScheme.primary
            )
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}
