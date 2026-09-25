package com.example.mezahub.data

import androidx.annotation.StringRes
import com.example.mezahub.R

// Declaration order is display order (rarest first) — StarTier.entries is used for lists.
enum class StarTier(val stars: Int, @StringRes val labelRes: Int) {
    SUPERSTAR(6, R.string.tier_superstar),
    STAR(5, R.string.tier_star),
    FOUR(4, R.string.tier_four),
    THREE(3, R.string.tier_three),
    TWO(2, R.string.tier_two),
    REGULAR(0, R.string.tier_regular),
}
