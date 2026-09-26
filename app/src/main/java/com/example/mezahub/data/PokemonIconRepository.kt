package com.example.mezahub.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.Collections

/**
 * Loads card icons from assets/versions/v<N>/icons/<tagId>.{png,webp,jpg} — same drop-in
 * workflow as the cry clips under assets/versions/v<N>/cries/. A tagId with no matching file
 * simply has no icon; callers should fall back to a placeholder rather than treat a miss as an error.
 */
object PokemonIconRepository {
    private val EXTENSIONS = listOf("png", "webp", "jpg")

    // Icons are shown at ~112dp at most; decoding the source art (up to ~1100px) at full size
    // would cost ~100MB for the whole catalog, so decode down to roughly display size instead.
    private const val MAX_DECODE_PX = 384

    // A full catalog is ~32MB of decoded icons per version, so keep only the most recently shown
    // ones: an eighth of the app's memory budget, measured in KB. Evicted icons are re-decoded.
    private val cache = object : LruCache<String, ImageBitmap>((Runtime.getRuntime().maxMemory() / 8 / 1024).toInt()) {
        override fun sizeOf(key: String, value: ImageBitmap): Int = value.asAndroidBitmap().allocationByteCount / 1024
    }

    // Tags with no icon file, so a miss isn't re-probed for every extension each time.
    private val missing: MutableSet<String> = Collections.synchronizedSet(HashSet())

    suspend fun load(context: Context, version: Int, tagId: String): ImageBitmap? {
        // Keyed by version too: regular tags like "R-1-1" can reappear in several versions.
        val key = "$version/$tagId"
        cache.get(key)?.let { return it }
        if (key in missing) return null
        val iconsDir = MezastarVersion.fromNumber(version).iconsDir

        val bitmap = withContext(Dispatchers.IO) {
            for (ext in EXTENSIONS) {
                val decoded = try {
                    decodeDownsampled(context, "$iconsDir/$tagId.$ext")
                } catch (e: IOException) {
                    null // try next extension
                }
                if (decoded != null) return@withContext decoded.asImageBitmap()
            }
            null
        }

        if (bitmap != null) cache.put(key, bitmap) else missing += key
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
