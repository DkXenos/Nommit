package com.example.nommit.core.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * The zine palette, lifted verbatim from Nommit-Style-Guide.dc.html §01.
 *
 * Cream + Ink are "the constant frame" -- every surface is cream, every outline,
 * shadow and glyph is ink. The saturated set carries cards and chips; the pop
 * accents are stickers only.
 */
object NommitColors {
    /** Base paper. Every sheet, panel and chip background starts here. */
    val Cream = Color(0xFFFDF3E7)

    /** Outlines, hard shadows, and nearly all text. Never pure black. */
    val Ink = Color(0xFF1E1712)

    /** Cards sit on white rather than cream so they lift off the paper. */
    val CardWhite = Color(0xFFFFFFFF)

    // --- saturated set -------------------------------------------------------
    /** Primary. The Nom action, the user dot, distance stamps. */
    val Chili = Color(0xFFFF4D2E)
    val ChiliDark = Color(0xFFE23C1F)

    /** Secondary. Ratings, the radius fill, the "widen" CTA. */
    val Turmeric = Color(0xFFFFB627)

    /** Tertiary. Confirmations and the Directions CTA. */
    val Pandan = Color(0xFF3DD68C)

    // --- pop accents: stickers only, use sparingly ---------------------------
    val Magenta = Color(0xFFFF3E9A)
    val ElectricTeal = Color(0xFF00B3C4)
    val Grape = Color(0xFF7B4BFF)

    // --- text tiers ----------------------------------------------------------
    /** Body copy -- softer than Ink so paragraphs don't shout. */
    val InkBody = Color(0xFF3A2E24)

    /** Secondary/caption text. */
    val InkMuted = Color(0xFF6A5C4C)

    /** Disclaimers and hints. */
    val InkFaint = Color(0xFF9A8B78)

    /** The "?" on the empty plate -- barely there. */
    val InkGhost = Color(0xFFC9BBA6)

    // --- map surface ---------------------------------------------------------
    val MapWater = Color(0xFFBFE9E4)
    val MapPark = Color(0xFFBFE9CF)
    val MapBuildingLight = Color(0xFFF1E1C6)
    val MapBuildingDark = Color(0xFFEAD7B4)
    val MapStreet = Color(0xFFFBEAD2)

    /** Scrim behind modal dialogs -- ink at 45%. */
    val Scrim = Color(0x731E1712)
}

/**
 * How one cuisine is stamped throughout the app: a chip, its map pin and its card
 * photo panel all share this colour and emoji so they read as the same sticker.
 *
 * [onColor] is the text colour that stays legible on [color] -- the guide uses
 * cream on the darker fills (Grape/Teal/Chili) and ink on the brighter ones.
 */
data class CuisineStyle(
    val color: Color,
    val onColor: Color,
    val emoji: String,
)

/**
 * Resolves a cuisine key to its sticker.
 *
 * The style guide illustrates eight cuisines (Satay, Noodles, Dumplings, Thai,
 * Korean, BBQ, Dessert, Ramen), but those are a night-market fantasy set -- the
 * Places API returns its own vocabulary (`pizza_restaurant`, `sushi_restaurant`,
 * and so on), and the build spec requires the chip list be built from what the API
 * actually returns. So the guide's eight are honoured exactly where they overlap,
 * the palette is extended to the rest of the Places food types using only the six
 * approved saturated colours, and anything unrecognised still gets a stable
 * colour rather than falling out of the design.
 */
object CuisineStyles {

    private val Chili = CuisineStyle(NommitColors.Chili, NommitColors.Cream, "")
    private val Turmeric = CuisineStyle(NommitColors.Turmeric, NommitColors.Ink, "")
    private val Pandan = CuisineStyle(NommitColors.Pandan, NommitColors.Ink, "")
    private val Grape = CuisineStyle(NommitColors.Grape, NommitColors.Cream, "")
    private val Teal = CuisineStyle(NommitColors.ElectricTeal, NommitColors.Cream, "")
    private val Magenta = CuisineStyle(NommitColors.Magenta, NommitColors.Ink, "")

    /** The six fills the guide permits, in the order the fallback cycles them. */
    private val palette = listOf(Chili, Turmeric, Pandan, Grape, Teal, Magenta)

    /**
     * Keys are the app's own cuisine keys (see the discovery feature's cuisine
     * resolver), not raw Places types, so this table stays readable.
     */
    private val table: Map<String, CuisineStyle> = mapOf(
        // --- the style guide's eight, verbatim ---------------------------------
        "satay" to Chili.copy(emoji = "🍢"),
        "noodles" to Turmeric.copy(emoji = "🍜"),
        "dumplings" to Pandan.copy(emoji = "🥟"),
        "thai" to Grape.copy(emoji = "🌶️"),
        "korean" to Teal.copy(emoji = "🥘"),
        "bbq" to Magenta.copy(emoji = "🔥"),
        "dessert" to Turmeric.copy(emoji = "🍧"),
        "ramen" to Chili.copy(emoji = "🍥"),
        // --- extended to the rest of the Places food vocabulary ---------------
        "sushi" to Teal.copy(emoji = "🍣"),
        "japanese" to Teal.copy(emoji = "🍱"),
        "chinese" to Chili.copy(emoji = "🥡"),
        "vietnamese" to Pandan.copy(emoji = "🍲"),
        "indonesian" to Chili.copy(emoji = "🍛"),
        "indian" to Turmeric.copy(emoji = "🍛"),
        "pizza" to Chili.copy(emoji = "🍕"),
        "italian" to Pandan.copy(emoji = "🍝"),
        "burger" to Turmeric.copy(emoji = "🍔"),
        "mexican" to Magenta.copy(emoji = "🌮"),
        "seafood" to Teal.copy(emoji = "🦐"),
        "steak" to Magenta.copy(emoji = "🥩"),
        "breakfast" to Turmeric.copy(emoji = "🍳"),
        "bakery" to Turmeric.copy(emoji = "🥐"),
        "cafe" to Grape.copy(emoji = "☕"),
        "bar" to Grape.copy(emoji = "🍺"),
        "sandwich" to Pandan.copy(emoji = "🥪"),
        "vegetarian" to Pandan.copy(emoji = "🥗"),
        "middle_eastern" to Turmeric.copy(emoji = "🥙"),
        "mediterranean" to Teal.copy(emoji = "🫒"),
        "french" to Grape.copy(emoji = "🥖"),
        "greek" to Teal.copy(emoji = "🥙"),
        "spanish" to Magenta.copy(emoji = "🥘"),
        "fast_food" to Chili.copy(emoji = "🍟"),
        "ice_cream" to Magenta.copy(emoji = "🍦"),
        "restaurant" to Chili.copy(emoji = "🍽️"),
    )

    /**
     * Unknown cuisines cycle the palette by name hash rather than picking at
     * random: the pin, the chip and the card for one cuisine must always agree,
     * including across process restarts.
     */
    fun of(key: String): CuisineStyle {
        val normalised = key.lowercase()
        return table[normalised] ?: palette[
            Math.floorMod(normalised.hashCode(), palette.size),
        ].copy(emoji = "🍽️")
    }
}
