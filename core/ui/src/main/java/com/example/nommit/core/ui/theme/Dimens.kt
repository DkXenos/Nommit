package com.example.nommit.core.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Non-Material tokens from the style guide's "Rules of the Zine" (§05).
 *
 * The look is built from exactly three ingredients applied together: a thick ink
 * border, a hard offset shadow with no blur, and a small rotation. These constants
 * are the vocabulary; [com.example.nommit.core.ui.component.zineSurface] applies them.
 */
object Zine {
    /** Ink border weights. Bigger elements carry heavier outlines. */
    val BorderHairline = 2.dp
    val BorderThin = 2.5.dp
    val BorderNormal = 3.dp
    val BorderThick = 4.dp
    val BorderHeavy = 5.dp

    /**
     * Hard shadow offsets -- "0 4-9px 0 #1E1712", no blur. The offset scales with
     * the border weight so a cut-out always looks like it's the same thickness of
     * paper lifted off the page.
     */
    val ShadowTiny = 3.dp
    val ShadowSmall = 4.dp
    val ShadowMedium = 6.dp
    val ShadowLarge = 7.dp
    val ShadowXl = 9.dp

    /** Corner radii. */
    val RadiusSticker = 7.dp
    val RadiusTag = 9.dp
    val RadiusChip = 13.dp
    val RadiusControl = 11.dp
    val RadiusCard = 16.dp
    val RadiusButton = 18.dp
    val RadiusPanel = 22.dp
    val RadiusSheet = 26.dp

    /** Spacing scale, as used by the comp's padding values. */
    val SpaceXs = 4.dp
    val SpaceSm = 8.dp
    val SpaceMd = 12.dp
    val SpaceLg = 16.dp
    val SpaceXl = 22.dp
    val SpaceXxl = 30.dp

    /**
     * Tilt range for stickers and cards. The guide is explicit: decoration is
     * chaotic, reading order is dead straight -- so only leaf elements rotate,
     * never containers that hold running text.
     */
    const val TILT_SUBTLE = 2f
    const val TILT_NORMAL = 3f
    const val TILT_STRONG = 4f

    /**
     * Per-card rotations, cycled by index so a result list looks hand-dealt but
     * stays deterministic across recompositions (a random tilt would jump every
     * time the list re-sorted).
     */
    val CardTilts = listOf(-3f, 2.5f, -2f, 3f, -3.5f, 2f, -2.5f, 3.5f)
}

val NommitShapes = Shapes(
    extraSmall = RoundedCornerShape(Zine.RadiusSticker),
    small = RoundedCornerShape(Zine.RadiusControl),
    medium = RoundedCornerShape(Zine.RadiusCard),
    large = RoundedCornerShape(Zine.RadiusButton),
    extraLarge = RoundedCornerShape(Zine.RadiusSheet),
)
