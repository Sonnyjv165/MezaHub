package com.example.mezahub.data

/**
 * A Mezastar card set. Regions run different versions, so the user picks the one their arcade
 * machine uses; matching and the Pokédex are scoped to it. Tag IDs follow `1-<number>-<card>`
 * (e.g. Version 4's first card is 1-4-001). Assets live under `assets/versions/v<number>/`.
 */
enum class MezastarVersion(val number: Int) {
    V1(1),
    V2(2),
    V3(3),
    V4(4),
    ;

    val assetDir: String get() = "versions/v$number"
    val criesDir: String get() = "$assetDir/cries"
    val iconsDir: String get() = "$assetDir/icons"

    companion object {
        /** Version 3 is the set MezaHub was built and tuned against. */
        val DEFAULT = V3

        fun fromNumber(number: Int): MezastarVersion = entries.firstOrNull { it.number == number } ?: DEFAULT
    }
}
