package com.nusv.lite.data

import android.content.Context
import android.content.SharedPreferences
import com.nusv.lite.model.Category
import com.nusv.lite.model.ContentJson
import com.nusv.lite.model.Doc
import com.nusv.lite.model.DocsJson
import com.nusv.lite.model.Item
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.time.Instant
import java.util.concurrent.TimeUnit

enum class SyncResult {
    SUCCESS, NO_UPDATE, ERROR
}

class SyncManager(context: Context, private val db: AppDatabase) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("sync_meta", Context.MODE_PRIVATE)

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val json = Json { ignoreUnknownKeys = true }

    fun getContentVersion(): Int = prefs.getInt("content_version", 0)
    fun getDocsVersion(): Int = prefs.getInt("docs_version", 0)
    fun getLastSyncTime(): Long = prefs.getLong("last_sync_at", 0L)
    fun getSyncUrl(): String = prefs.getString("sync_url", DEFAULT_SYNC_URL) ?: DEFAULT_SYNC_URL

    fun setSyncUrl(url: String) {
        prefs.edit().putString("sync_url", url).apply()
    }

    suspend fun syncAll(): SyncResult = withContext(Dispatchers.IO) {
        val contentResult = syncContent()
        val docsResult = syncDocs()
        when {
            contentResult == SyncResult.ERROR || docsResult == SyncResult.ERROR -> SyncResult.ERROR
            contentResult == SyncResult.NO_UPDATE && docsResult == SyncResult.NO_UPDATE -> SyncResult.NO_UPDATE
            else -> {
                prefs.edit().putLong("last_sync_at", System.currentTimeMillis()).apply()
                SyncResult.SUCCESS
            }
        }
    }

    private suspend fun syncContent(): SyncResult {
        return try {
            val url = "${getSyncUrl()}/content.json"
            val request = Request.Builder().url(url).get().build()
            val response = client.newCall(request).execute()

            if (!response.isSuccessful) return SyncResult.ERROR

            val body = response.body.string()
            val remote = json.decodeFromString<ContentJson>(body)

            val localVersion = getContentVersion()
            if (remote.version <= localVersion) return SyncResult.NO_UPDATE

            db.categoryDao().deleteAll()
            db.categoryDao().insertAll(
                remote.categories.mapIndexed { i, c ->
                    Category(
                        id = c.id,
                        name = c.name,
                        color = c.color.removePrefix("#").toLong(16),
                        sortOrder = i
                    )
                }
            )

            db.itemDao().deleteAll()
            db.itemDao().insertAll(
                remote.items.map { item ->
                    Item(
                        id = item.id,
                        title = item.title,
                        description = item.description,
                        url = item.url,
                        categoryId = item.categoryId,
                        tags = item.tags.joinToString(","),
                        isFeatured = item.isFeatured,
                        createdAt = try {
                            Instant.parse(item.createdAt).toEpochMilli()
                        } catch (_: Exception) {
                            System.currentTimeMillis()
                        }
                    )
                }
            )

            prefs.edit().putInt("content_version", remote.version).apply()
            SyncResult.SUCCESS
        } catch (_: Exception) {
            SyncResult.ERROR
        }
    }

    private suspend fun syncDocs(): SyncResult {
        return try {
            val url = "${getSyncUrl()}/docs.json"
            val request = Request.Builder().url(url).get().build()
            val response = client.newCall(request).execute()

            if (!response.isSuccessful) return SyncResult.ERROR

            val body = response.body.string()
            val remote = json.decodeFromString<DocsJson>(body)

            val localVersion = getDocsVersion()
            if (remote.version <= localVersion) return SyncResult.NO_UPDATE

            db.docDao().deleteAll()
            db.docDao().insertAll(
                remote.docs.map { doc ->
                    Doc(
                        id = doc.id,
                        title = doc.title,
                        content = doc.content,
                        createdAt = try {
                            Instant.parse(doc.createdAt).toEpochMilli()
                        } catch (_: Exception) {
                            System.currentTimeMillis()
                        }
                    )
                }
            )

            prefs.edit().putInt("docs_version", remote.version).apply()
            SyncResult.SUCCESS
        } catch (_: Exception) {
            SyncResult.ERROR
        }
    }

    suspend fun checkForUpdate(): UpdateInfo? = withContext(Dispatchers.IO) {
        return@withContext try {
            val url = "${getSyncUrl()}/version.json"
            val request = Request.Builder().url(url).get().build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) return@withContext null
            val body = response.body.string()
            json.decodeFromString<UpdateInfo>(body)
        } catch (_: Exception) {
            null
        }
    }

    companion object {
        const val DEFAULT_SYNC_URL = "https://raw.githubusercontent.com/NUSV/nusv-lite-sync/main"
    }
}

@Serializable
data class UpdateInfo(
    val latestVersion: String,
    val downloadUrl: String,
    val changelog: String = ""
)
