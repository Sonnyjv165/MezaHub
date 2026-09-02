package com.example.mezahub.data

import android.content.Context
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

/**
 * Loads card icons from assets/icons/<tagId>.{png,webp,jpg} — same drop-in workflow as the
 * cry clips under assets/cries/. A tagId with no matching file simply has no icon; callers
 * should fall back to a placeholder rather than treat a miss as an error.
 */
object PokemonIconRepository {
    private val EXTENSIONS = listOf("png", "webp", "jpg")
    private val cache = mutableMapOf<String, ImageBitmap?>()

    suspend fun load(context: Context, tagId: String): ImageBitmap? {
        cache[tagId]?.let { return it }
        if (cache.containsKey(tagId)) return null

        val bitmap = withContext(Dispatchers.IO) {
            for (ext in EXTENSIONS) {
                try {
                    context.assets.open("icons/$tagId.$ext").use { stream ->
                        val decoded = BitmapFactory.decodeStream(stream)
                        if (decoded != null) return@withContext decoded.asImageBitmap()
                    }
                } catch (e: IOException) {
                    // try next extension
                }
            }
            null
        }

        cache[tagId] = bitmap
        return bitmap
    }
}
