package com.lumo.store.viewmodel

import android.content.Context
import android.content.Intent
import android.os.Environment
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lumo.store.data.AppInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

data class DownloadState(
    val app: AppInfo? = null,
    val progress: Float = 0f,
    val isDownloading: Boolean = false,
    val isCompleted: Boolean = false
)

class DownloadViewModel : ViewModel() {

    private val _downloadState = MutableStateFlow(DownloadState())
    val downloadState: StateFlow<DownloadState> = _downloadState

    // फाइल का नाम यहाँ तय कर लिया ताकि इंस्टॉल के समय दिक्कत न आए
    private fun getFileName(app: AppInfo): String {
        return "${app.name.replace(" ", "_")}_v${app.version}.apk"
    }

    fun startDownload(context: Context, app: AppInfo) {
        if (app.downloadUrl.isBlank()) {
            android.util.Log.e("LumoStore", "Download URL is empty")
            return
        }

        _downloadState.value = DownloadState(app = app, isDownloading = true, progress = 0f)

        // बैकग्राउंड थ्रेड में डाउनलोड शुरू करो (बिना नोटिफिकेशन के)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val url = URL(app.downloadUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.connect()

                if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                    throw Exception("Server returned ${connection.responseCode}")
                }

                val fileLength = connection.contentLength
                val fileName = getFileName(app)

                // फाइल ऐप के अंदरूनी स्टोरेज (Downloads) में सेव होगी
                val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName)

                connection.inputStream.use { input ->
                    FileOutputStream(file).use { output ->
                        val buffer = ByteArray(4096)
                        var bytesRead: Int
                        var totalBytesRead = 0L

                        while (input.read(buffer).also { bytesRead = it } != -1) {
                            output.write(buffer, 0, bytesRead)
                            totalBytesRead += bytesRead

                            if (fileLength > 0) {
                                val progress = totalBytesRead.toFloat() / fileLength.toFloat()
                                _downloadState.value = _downloadState.value.copy(progress = progress)
                            }
                        }
                    }
                }

                // डाउनलोड पूरा हुआ
                _downloadState.value = _downloadState.value.copy(
                    isDownloading = false,
                    isCompleted = true,
                    progress = 1f
                )
            } catch (e: Exception) {
                android.util.Log.e("LumoStore", "Download failed: ${e.message}")
                _downloadState.value = DownloadState() // एरर आने पर रीसेट करो
            }
        }
    }

    fun dismiss() {
        _downloadState.value = DownloadState()
    }

    // इंस्टॉल के लिए फाइल खोलना
    fun installApk(context: Context, appName: String) {
        val currentApp = _downloadState.value.app ?: return
        val fileName = getFileName(currentApp)

        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName)

        if (file.exists()) {
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)

            // इंस्टॉलर खुलने के 5 सेकंड बाद फाइल डिलीट कर दो
            viewModelScope.launch(Dispatchers.IO) {
                kotlinx.coroutines.delay(5000)
                if (file.exists()) {
                    file.delete()
                }
            }
        } else {
            android.util.Log.e("LumoStore", "File not found to install")
        }
        dismiss()
    }
}