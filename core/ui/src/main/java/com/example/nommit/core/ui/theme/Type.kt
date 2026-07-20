package com.example.nommit.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.sp
import com.example.nommit.core.ui.R

/**
 * Three tiers, no more (style guide §02).
 *
 * Display  -- Bagel Fat One: brand, restaurant names, big counts. Headlines only.
 * Body/UI  -- Inter 400-900: everything that has to stay readable.
 * Accent   -- Permanent Marker: price tags, distance stamps, section labels.
 */

/** Bulbous and loud. Single weight by design -- never use it for body copy. */
val BagelFatOne = FontFamily(Font(R.font.bagel_fat_one_regular, FontWeight.Normal))

/**
 * Static instances rather than the variable font: variable-axis weight selection
 * needs FontVariation settings that only take effect on API 26+ inconsistently,
 * and shipping five static cuts is more predictable at minSdk 26.
 */
val Inter = FontFamily(
    Font(R.font.inter_400, FontWeight.Normal),
    Font(R.font.inter_500, FontWeight.Medium),
    Font(R.font.inter_700, FontWeight.Bold),
    Font(R.font.inter_800, FontWeight.ExtraBold),
    Font(R.font.inter_900, FontWeight.Black),
)

/** Hand-scrawled. Feels drawn onto the zine. */
val PermanentMarker = FontFamily(Font(R.font.permanent_marker_regular, FontWeight.Normal))

/**
 * Bagel Fat One is drawn with very little internal leading, and the comp sets it
 * at line-height 1.0-1.15 throughout. Compose's default trimming leaves a visible
 * gap above caps, so display styles trim the first baseline's top.
 */
private val TightDisplay = LineHeightStyle(
    alignment = LineHeightStyle.Alignment.Center,
    trim = LineHeightStyle.Trim.FirstLineTop,
)

private fun display(size: Int, lineHeight: Int) = TextStyle(
    fontFamily = BagelFatOne,
    fontWeight = FontWeight.Normal,
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
    lineHeightStyle = TightDisplay,
)

private fun accent(size: Int) = TextStyle(
    fontFamily = PermanentMarker,
    fontWeight = FontWeight.Normal,
    fontSize = size.sp,
    lineHeight = (size * 1.3).toInt().sp,
)

private fun body(size: Int, weight: FontWeight, lineHeightMultiplier: Double = 1.5) = TextStyle(
    fontFamily = Inter,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = (size * lineHeightMultiplier).toInt().sp,
)

/**
 * Material 3 slots mapped onto the zine's three tiers. The mapping is deliberate
 * so stock M3 components inherit sane styling, but Nommit's own composables reach
 * for [NommitType] instead, which names things the way the comp does.
 */
val NommitTypography = Typography(
    displayLarge = display(56, 56),
    displayMedium = display(46, 46),
    displaySmall = display(33, 34),
    headlineLarge = display(30, 33),
    headlineMedium = display(27, 30),
    headlineSmall = display(26, 27),
    titleLarge = display(24, 25),
    titleMedium = display(20, 21),
    titleSmall = display(16, 18),
    bodyLarge = body(15, FontWeight.Normal),
    bodyMedium = body(14, FontWeight.Normal, 1.55),
    bodySmall = body(12, FontWeight.Normal),
    labelLarge = body(14, FontWeight.Bold, 1.2),
    labelMedium = body(13, FontWeight.Bold, 1.2),
    labelSmall = body(11, FontWeight.Bold, 1.2),
)

/**
 * The comp's named text roles. Prefer these over MaterialTheme.typography inside
 * feature code -- they carry the intent ("this is a stamp") rather than a size.
 */
object NommitType {
    /** Splash wordmark. */
    val Wordmark = display(46, 46)

    /** "Nommit" in the top bar, the Nom button label. */
    val BrandLarge = display(26, 27)

    /** Detail-sheet restaurant name. */
    val TitleXl = display(33, 34)

    /** "12 spots", empty-state headline. */
    val TitleLarge = display(27, 30)

    /** Card restaurant name. */
    val CardTitle = display(20, 21)

    /** Rating numerals inside the turmeric sticker. */
    val RatingNumber = display(14, 15)

    /** The big "finding spots" counter. */
    val Counter = display(52, 52)

    /** Section headings: "How far you walkin'?", "What are you craving?" */
    val SectionLabel = accent(15)

    /** Distance stamps, radius tags, "$$" price marks. */
    val Stamp = accent(14)
    val StampSmall = accent(12)

    /** Chip labels. */
    val Chip = body(13, FontWeight.Bold, 1.2)

    /** Sort toggle labels. */
    val Toggle = body(12, FontWeight.ExtraBold, 1.2)

    /** Sticker text on cuisine tags. */
    val StickerLabel = body(11, FontWeight.Bold, 1.2)

    /** Paragraph copy in the detail sheet and empty states. */
    val Body = body(14, FontWeight.Normal, 1.55)

    /** Fact values in the detail sheet's open-now / nommed-by tiles. */
    val FactValue = body(14, FontWeight.Bold, 1.3)
}
