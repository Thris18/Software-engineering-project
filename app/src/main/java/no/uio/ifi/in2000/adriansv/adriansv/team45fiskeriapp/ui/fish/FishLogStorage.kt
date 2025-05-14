package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.fish

import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.model.fish.FishLog
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

private const val TAG = "FishLogRepository" // For logging..


class FishLogStorage(private val context: Context) {
    private val _fishLogs = MutableStateFlow<List<FishLog>>(emptyList())
    val fishLogs: Flow<List<FishLog>> = _fishLogs.asStateFlow()

    init {
        loadFishLogs()
    }

    private fun loadFishLogs() {
        try {
            val file = File(context.filesDir, "fish_logs.dat")
            if (file.exists()) {
                FileInputStream(file).use { fis ->
                    ObjectInputStream(fis).use { ois ->
                        @Suppress("UNCHECKED_CAST")
                        val logs = ois.readObject() as List<FishLog>
                        _fishLogs.value = logs
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Feil ved lasting av fiskelogger: ${e.message}")
        }
    }

    private fun saveFishLogs() {
        try {
            val file = File(context.filesDir, "fish_logs.dat")
            FileOutputStream(file).use { fos ->
                ObjectOutputStream(fos).use { oos ->
                    oos.writeObject(_fishLogs.value)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Feil ved lagring av fiskelogger: ${e.message}")
        }
    }

    fun addFishLog(fishLog: FishLog) {
        _fishLogs.value += fishLog
        saveFishLogs()
    }

    fun clearFishLogs() {
        _fishLogs.value = emptyList()
        saveFishLogs()
    }

    fun removeFishLog(fishLog: FishLog) {
        val currentLogs = _fishLogs.value.toMutableList()
        currentLogs.removeIf { it.timestamp == fishLog.timestamp }
        _fishLogs.value = currentLogs
        saveFishLogs()
    }
}