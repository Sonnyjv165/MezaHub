package com.example.mezahub.data

import android.content.Context
import com.example.mezahub.model.CryOutcome
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID

data class DetectionRecord(
    val id: String = UUID.randomUUID().toString(),
    val outcomes: List<CryOutcome>,
    val timestampMillis: Long = System.currentTimeMillis(),
) {
    /** A detection only ever matches within one version's index, so all outcomes share it. */
    val version: Int get() = outcomes.first().version
}

/**
 * Log of real cry detections (newest first), persisted as a flat JSON file in app-private
 * storage so history survives app restarts. No real database — a plain file is plenty at this
 * scale (a handful of detections per session).
 */
object DetectionHistoryRepository {
    private const val FILE_NAME = "detection_history.json"
    private const val LEGACY_VERSION = 3

    private val _records = MutableStateFlow<List<DetectionRecord>>(emptyList())
    val records: StateFlow<List<DetectionRecord>> = _records.asStateFlow()

    // Serializes load/modify/save so a detection landing while History is deleting (or
    // loading) can't interleave and drop one of the writes.
    private val mutex = Mutex()
    @Volatile private var loaded = false

    suspend fun ensureLoaded(context: Context) {
        if (loaded) return
        mutex.withLock { ensureLoadedLocked(context) }
    }

    suspend fun record(context: Context, outcomes: List<CryOutcome>) = mutex.withLock {
        ensureLoadedLocked(context)
        _records.value = listOf(DetectionRecord(outcomes = outcomes)) + _records.value
        persist(context)
    }

    suspend fun remove(context: Context, id: String) = mutex.withLock {
        ensureLoadedLocked(context)
        _records.value = _records.value.filterNot { it.id == id }
        persist(context)
    }

    /** Puts a previously removed record back in its original chronological position (Undo). */
    suspend fun restore(context: Context, record: DetectionRecord) = mutex.withLock {
        ensureLoadedLocked(context)
        if (_records.value.none { it.id == record.id }) {
            _records.value = (_records.value + record).sortedByDescending { it.timestampMillis }
            persist(context)
        }
    }

    private suspend fun ensureLoadedLocked(context: Context) {
        if (loaded) return
        _records.value = withContext(Dispatchers.IO) { readFromDisk(context) }
        loaded = true
    }

    private suspend fun persist(context: Context) {
        withContext(Dispatchers.IO) { writeToDisk(context, _records.value) }
    }

    private fun readFromDisk(context: Context): List<DetectionRecord> {
        val file = File(context.filesDir, FILE_NAME)
        if (!file.exists()) return emptyList()
        return try {
            val array = JSONArray(file.readText())
            (0 until array.length()).mapNotNull { i -> parseRecord(array.optJSONObject(i)) }
        } catch (e: Exception) {
            // Keep the unreadable file around instead of letting the next save overwrite it,
            // so a corrupt history is recoverable rather than silently wiped.
            file.renameTo(File(context.filesDir, "$FILE_NAME.corrupt-${System.currentTimeMillis()}"))
            emptyList()
        }
    }

    private fun parseRecord(obj: JSONObject?): DetectionRecord? {
        if (obj == null) return null
        val outcomesArray = obj.optJSONArray("outcomes") ?: return null
        val outcomes = (0 until outcomesArray.length()).mapNotNull { j ->
            parseOutcome(outcomesArray.optJSONObject(j))
        }
        if (outcomes.isEmpty()) return null
        return DetectionRecord(
            id = obj.optString("id"),
            outcomes = outcomes,
            timestampMillis = obj.optLong("timestampMillis"),
        )
    }

    private fun parseOutcome(obj: JSONObject?): CryOutcome? {
        if (obj == null) return null
        val tier = try {
            StarTier.valueOf(obj.optString("tier"))
        } catch (e: IllegalArgumentException) {
            return null
        }
        return CryOutcome(
            tagId = obj.optString("tagId"),
            speciesName = obj.optString("speciesName"),
            tier = tier,
            confidencePercent = obj.optInt("confidencePercent"),
            // Records saved before multi-version support were all Version 3 cards.
            version = obj.optInt("version", LEGACY_VERSION),
        )
    }

    private fun writeToDisk(context: Context, records: List<DetectionRecord>) {
        val array = JSONArray()
        for (record in records) {
            val outcomesArray = JSONArray()
            for (outcome in record.outcomes) {
                outcomesArray.put(
                    JSONObject()
                        .put("tagId", outcome.tagId)
                        .put("speciesName", outcome.speciesName)
                        .put("tier", outcome.tier.name)
                        .put("confidencePercent", outcome.confidencePercent)
                        .put("version", outcome.version),
                )
            }
            array.put(
                JSONObject()
                    .put("id", record.id)
                    .put("timestampMillis", record.timestampMillis)
                    .put("outcomes", outcomesArray),
            )
        }
        try {
            // Write-then-rename so a crash or power loss mid-save can't leave a half-written file.
            val tmp = File(context.filesDir, "$FILE_NAME.tmp")
            tmp.writeText(array.toString())
            if (!tmp.renameTo(File(context.filesDir, FILE_NAME))) tmp.delete()
        } catch (e: Exception) {
            // Best-effort persistence — a failed write just means this change isn't saved.
        }
    }
}
