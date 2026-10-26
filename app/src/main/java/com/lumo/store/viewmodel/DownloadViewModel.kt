package com.lumo.store.viewmodel

import android.content.Context
import android.content.Intent
import android.os.Environment
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lumo.store.data.ApkVariant
import com.lumo.store.data.AppInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

enum class DownloadStatus {
    Idle,
    Downloading,
    Downloaded,
    Installing,
    Completed,
    Error
}

data class DownloadState(
    val app: AppInfo? = null,
    val variant: ApkVariant? = null,
    val progress: Float = 0f,
    val bytesDownloaded: Long = 0L,
    val totalBytes: Long = 0L,
    val speedBytesPerSec: Long = 0L,
    val etaSeconds: Int = 0,
    val status: DownloadStatus = DownloadStatus.Idle,
    val isDownloading: Boolean = false,
    val isCompleted: Boolean = false,
    val errorMessage: String? = null
)

class DownloadViewModel : ViewModel() {

    private val _downloadState = MutableStateFlow(DownloadState())
    val downloadState: StateFlow<DownloadState> = _downloadState

    fun startDownload(
        context: Context,
        app: AppInfo,
        variant: ApkVariant? = null
    ) {
        val url = (variant?.downloadUrl ?: app.downloadUrl).ifBlank { return }
        val fileName = variant?.filename?.ifBlank { null } ?: getFileName(app)

        _downloadState.value = DownloadState(
            app = app,
            variant = variant,
            status = DownloadStatus.Downloading,
            isDownloading = true,
            progress = 0f,
            totalBytes = variant?.size ?: 0L
        )

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val connection = URL(url).openConnection() as HttpURLConnection
                connection.connectTimeout = 15000
                connection.readTimeout = 30000
                connection.connect()

                if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                    throw Exception("Server returned ${connection.responseCode}")
                }

                val declared = connection.contentLengthLong
                val totalBytes = if (declared > 0) declared else (variant?.size ?: 0L)

                val file = File(
                    context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
                    fileName
                )

                var lastUpdate = System.currentTimeMillis()
                var lastBytes = 0L
                var speed = 0L

                connection.inputStream.use { input ->
                    FileOutputStream(file).use { output ->
                        val buffer = ByteArray(8192)
                        var bytesRead: Int
                        var totalRead = 0L

                        while (input.read(buffer).also { bytesRead = it } != -1) {
                            output.write(buffer, 0, bytesRead)
                            totalRead += bytesRead

                            val now = System.currentTimeMillis()
                            val elapsed = now - lastUpdate
                            if (elapsed >= 250) {
                                speed = ((totalRead - lastBytes) * 1000 / elapsed)
                                lastUpdate = now
                                lastBytes = totalRead

                                val progress = if (totalBytes > 0)
                                    (totalRead.toFloat() / totalBytes).coerceIn(0f, 1f)
                                else 0f

                                val eta = if (speed > 0 && totalBytes > 0)
                                    ((totalBytes - totalRead) / speed).toInt()
                                else 0

                                _downloadState.value = _downloadState.value.copy(
                                    progress = progress,
                                    bytesDownloaded = totalRead,
                                    totalBytes = totalBytes,
                                    speedBytesPerSec = speed,
                                    etaSeconds = eta
                                )
                            }
                        }
                    }
                }

                _downloadState.value = _downloadState.value.copy(
                    progress = 1f,
                    status = DownloadStatus.Downloaded,
                    isDownloading = false,
                    isCompleted = true,
                    speedBytesPerSec = 0L,
                    etaSeconds = 0
                )
            } catch (e: Exception) {
                android.util.Log.e("LumoStore", "Download failed: ${e.message}")
                _downloadState.value = _downloadState.value.copy(
                    status = DownloadStatus.Error,
                    isDownloading = false,
                    errorMessage = e.message
                )
            }
        }
    }

    fun dismiss() {
        _downloadState.value = DownloadState()
    }

    // ---------- INSTALL (FIXED: no immediate dismiss) ----------
    fun installApk(context: Context, appName: String) {
        val state = _downloadState.value
        val currentApp = state.app ?: return
        val fileName = state.variant?.filename?.ifBlank { null } ?: getFileName(currentApp)

        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName)

        if (!file.exists()) {
            android.util.Log.e("LumoStore", "APK file not found: $fileName")
            dismiss()
            return
        }

        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)

            _downloadState.value = _downloadState.value.copy(
                status = DownloadStatus.Installing
            )

            // Delete APK after 60s
            viewModelScope.launch(Dispatchers.IO) {
                kotlinx.coroutines.delay(60000)
                if (file.exists()) file.delete()
            }

            // Auto-dismiss DownloadBar after 8s
            viewModelScope.launch {
                kotlinx.coroutines.delay(8000)
                if (_downloadState.value.status == DownloadStatus.Installing) {
                    dismiss()
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("LumoStore", "Install failed: ${e.message}")
            _downloadState.value = _downloadState.value.copy(
                status = DownloadStatus.Error,
                errorMessage = "Cannot open installer"
            )
        }
    }

    private fun getFileName(app: AppInfo): String {
        return "${app.name.replace(" ", "_")}_v${app.version}.apk"
    }
}