package com.example.mezahub.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.Collections

/**
 * Loads card icons from assets/icons/<tagId>.{png,webp,jpg} — same drop-in workflow as the
 * cry clips under assets/cries/. A tagId with no matching file simply has no icon; callers
 * should fall back to a placeholder rather than treat a miss as an error.
 */
object PokemonIconRepository {
    private val EXTENSIONS = listOf("png", "webp", "jpg")

    // Icons are shown at ~112dp at most; decoding the source art (up to ~1100px) at full size
    // would cost ~100MB for the whole catalog, so decode down to roughly display size instead.
    private const val MAX_DECODE_PX = 384

    // Synchronized (not Concurrent) because a cached miss is stored as null.
    private val cache: MutableMap<String, ImageBitmap?> = Collections.synchronizedMap(HashMap())

    suspend fun load(context: Context, tagId: String): ImageBitmap? {
        synchronized(cache) {
            if (cache.containsKey(tagId)) return cache[tagId]
        }

        val bitmap = withContext(Dispatchers.IO) {
            for (ext in EXTENSIONS) {
                val decoded = try {
                    decodeDownsampled(context, "icons/$tagId.$ext")
                } catch (e: IOException) {
                    null // try next extension
                }
                if (decoded != null) return@withContext decoded.asImageBitmap()
            }
            null
        }

        cache[tagId] = bitmap
        return bitmap
    }

    private fun decodeDownsampled(context: Context, path: String): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.assets.open(path).use { BitmapFactory.decodeStream(it, null, bounds) }
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

        var sample = 1
        while (maxOf(bounds.outWidth, bounds.outHeight) / (sample * 2) >= MAX_DECODE_PX) sample *= 2

        val options = BitmapFactory.Options().apply { inSampleSize = sample }
        return context.assets.open(path).use { BitmapFactory.decodeStream(it, null, options) }
    }
}
