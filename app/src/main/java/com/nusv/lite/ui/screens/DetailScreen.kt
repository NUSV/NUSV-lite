package com.nusv.lite.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nusv.lite.BuildConfig
import com.nusv.lite.data.SyncManager
import com.nusv.lite.data.UpdateInfo
import com.nusv.lite.util.LocalAppStrings
import com.nusv.lite.util.VersionUtils
import com.nusv.lite.util.performIfEnabled
import kotlinx.coroutines.launch
import com.nusv.lite.model.Category
import com.nusv.lite.model.Item
import com.nusv.lite.repository.AppRepository
import com.nusv.lite.ui.components.NusvChip
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DetailScreen(
    itemId: String,
    repository: AppRepository,
    syncManager: SyncManager,
    onBack: () -> Unit
) {
    val strings = LocalAppStrings.current
    val item by repository.getItemById(itemId).collectAsState(initial = null)
    val categories by repository.allCategories.collectAsState(initial = emptyList())

    val currentItem = item
    if (currentItem == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(strings.detailLoading)
        }
        return
    }

    val category = categories.find { it.id == currentItem.categoryId }
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(category?.color ?: 0xFFFF2D78).copy(alpha = 0.8f),
                            MaterialTheme.colorScheme.background
                        ),
                        start = Offset.Zero,
                        end = Offset(0f, Float.POSITIVE_INFINITY)
                    )
                ),
            contentAlignment = Alignment.TopStart
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 48.dp, start = 8.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { haptic.performIfEnabled(); onBack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = strings.cdBack,
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            if (category != null) {
                NusvChip(text = category.name, selected = true)
                Spacer(Modifier.height(12.dp))
            }

            Text(
                text = currentItem.title,
                style = MaterialTheme.typography.headlineLarge
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = currentItem.description,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (currentItem.tags.isNotBlank()) {
                Spacer(Modifier.height(20.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    currentItem.tags.split(",").forEach { tag ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(100.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = tag.trim(),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Text(
                text = strings.detailAddedOn.format(formatDate(currentItem.createdAt)),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(32.dp))

            if (currentItem.id == "4") {
                var showUpdateDialog by remember { mutableStateOf(false) }
                var updateInfo by remember { mutableStateOf<UpdateInfo?>(null) }
                var checking by remember { mutableStateOf(false) }
                var checkError by remember { mutableStateOf(false) }
                val scope = rememberCoroutineScope()

                Button(
                    onClick = {
                        haptic.performIfEnabled()
                        checking = true
                        checkError = false
                        updateInfo = null
                        scope.launch {
                            val info = syncManager.checkForUpdate()
                            if (info == null) {
                                checkError = true
                            } else {
                                updateInfo = info
                            }
                            checking = false
                            showUpdateDialog = true
                        }
                    },
                    enabled = !checking,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.SystemUpdate,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = if (checking) strings.detailChecking else strings.detailCheckUpdate,
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                if (showUpdateDialog) {
                    val info = updateInfo
                    val currentVer = BuildConfig.VERSION_NAME
                    val hasUpdate = info != null && VersionUtils.isNewer(info.latestVersion, currentVer)
                    AlertDialog(
                        onDismissRequest = { showUpdateDialog = false },
                        title = { Text("NUSV LITE") },
                        text = {
                            if (checkError) {
                                Text(strings.updateCheckFailed.format("NUSV Portal"))
                            } else if (info != null) {
                                Column {
                                    Text(
                                        if (hasUpdate) strings.updateAvailable.format(info.latestVersion)
                                        else strings.updateUpToDate.format(currentVer)
                                    )
                                    if (info.changelog.isNotBlank()) {
                                        Spacer(Modifier.height(8.dp))
                                        Text(
                                            text = info.changelog,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            } else {
                                Text(strings.updateManual.format("NUSV Portal"))
                            }
                        },
                        confirmButton = {
                            Button(onClick = { haptic.performIfEnabled(); showUpdateDialog = false }) {
                                Text(strings.okLabel)
                            }
                        },
                        dismissButton = {
                            if (info != null && hasUpdate && info.downloadUrl.isNotBlank()) {
                                Button(onClick = {
                                    haptic.performIfEnabled()
                                    showUpdateDialog = false
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(info.downloadUrl))
                                    context.startActivity(intent)
                                }) {
                                    Text(strings.downloadLabel)
                                }
                            }
                        }
                    )
                }
            } else {
                Button(
                    onClick = {
                        haptic.performIfEnabled()
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(currentItem.url))
                        context.startActivity(intent)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = strings.detailOpenLink,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            Spacer(Modifier.height(100.dp))
        }
    }
}

private fun formatDate(epochMillis: Long): String {
    return SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(epochMillis))
}
