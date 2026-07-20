package com.example.nommit.core.ui.component

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.nommit.core.ui.theme.CuisineStyle
import com.example.nommit.core.ui.theme.NommitColors
import com.example.nommit.core.ui.theme.NommitType
import com.example.nommit.core.ui.theme.Zine

/**
 * Shared cut-outs. Everything here is a direct translation of a component in
 * Nommit-Style-Guide.dc.html §04 -- same fill, outline, shadow, radius and tilt.
 */

/**
 * Gummy press feedback (style guide §05 "Gummy motion"): the element squashes
 * toward its shadow rather than fading. Returns a scale to feed a graphicsLayer.
 */
@Composable
private fun pressScale(pressed: Boolean, squash: Float = 0.96f): Float {
    val scale by animateFloatAsState(
        targetValue = if (pressed) squash else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "pressScale",
    )
    return scale
}

/**
 * The primary action button -- "Nom it 🔍". Chili fill, 4px ink outline, 6px hard
 * shadow, Bagel Fat One label.
 *
 * On press the button drops onto its own shadow (offset shrinks) so it reads as
 * a physical key being pushed, which is the comp's motion language.
 */
@Composable
fun ZineButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = NommitColors.Chili,
    contentColor: Color = NommitColors.Cream,
    textStyle: TextStyle = NommitType.BrandLarge,
    shadowOffset: Dp = Zine.ShadowMedium,
    cornerRadius: Dp = Zine.RadiusButton,
    tilt: Float = 0f,
    enabled: Boolean = true,
    contentPadding: Dp = 14.dp,
    content: @Composable RowScope.() -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    // Travel the button downward by the amount its shadow shrinks, so the top edge
    // moves and the bottom edge stays put -- a real key press, not a slide.
    val travel by animateFloatAsState(
        targetValue = if (pressed && enabled) 1f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessHigh),
        label = "buttonTravel",
    )
    val sink = shadowOffset * 0.6f

    Row(
        modifier = modifier
            .rotate(tilt)
            .graphicsLayer { translationY = travel * sink.toPx() }
            .zineSurface(
                // Disabled is drawn as unprinted paper -- cream fill, ghosted ink.
                // Fading the fill instead would let the ink shadow bleed through and
                // read as a muddy, broken colour rather than an inactive control.
                background = if (enabled) containerColor else NommitColors.Cream,
                cornerRadius = cornerRadius,
                borderWidth = Zine.BorderThick,
                borderColor = if (enabled) NommitColors.Ink else NommitColors.InkGhost,
                shadowOffset = shadowOffset - (sink * travel),
                shadowColor = if (enabled) NommitColors.Ink else NommitColors.InkGhost,
            )
            .clickableNoRipple(interaction, enabled, onClick)
            .padding(contentPadding),
        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CompositionLocalProvider(
            LocalTextStyle provides textStyle.copy(
                color = if (enabled) contentColor else NommitColors.InkGhost,
            ),
            content = { content() },
        )
    }
}

/**
 * A cuisine chip. Inactive: cream fill, count badge. Active: the cuisine's own
 * saturated colour, a marker-pen tick, and a -2deg tilt with a jiggle on toggle.
 */
@Composable
fun ZineCuisineChip(
    label: String,
    style: CuisineStyle,
    selected: Boolean,
    count: Int?,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    // Chips jiggle when they become active (style guide §05). Driving the rotation
    // off `selected` means the spring plays on selection and settles at the tilt.
    val tilt by animateFloatAsState(
        targetValue = if (selected) -Zine.TILT_SUBTLE else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioHighBouncy,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "chipTilt",
    )
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.03f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioHighBouncy),
        label = "chipScale",
    )

    Row(
        modifier = modifier
            .graphicsLayer {
                rotationZ = tilt
                scaleX = scale
                scaleY = scale
            }
            .zineSurface(
                background = if (selected) style.color else NommitColors.Cream,
                cornerRadius = Zine.RadiusChip,
                borderWidth = if (compact) Zine.BorderThin else Zine.BorderNormal,
                shadowOffset = if (compact) Zine.ShadowTiny else Zine.ShadowSmall,
            )
            .toggleable(
                value = selected,
                role = Role.Checkbox,
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onValueChange = { onToggle() },
            )
            .padding(
                horizontal = if (compact) 10.dp else 12.dp,
                vertical = if (compact) 5.dp else 7.dp,
            ),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(style.emoji, style = NommitType.Chip)
        Text(
            text = label,
            style = NommitType.Chip,
            color = if (selected) style.onColor else NommitColors.Ink,
        )
        if (selected) {
            Text("✓", style = NommitType.StampSmall, color = style.onColor)
        } else if (count != null) {
            Text(
                text = count.toString(),
                style = NommitType.StickerLabel,
                color = NommitColors.Ink.copy(alpha = 0.5f),
            )
        }
    }
}

/**
 * A sticker: the small tilted tag used for cuisine labels, price marks and the
 * rating badge. Leaf element only -- never wrap running text in one.
 */
@Composable
fun ZineSticker(
    modifier: Modifier = Modifier,
    background: Color = NommitColors.Cream,
    contentColor: Color = NommitColors.Ink,
    textStyle: TextStyle = NommitType.StickerLabel,
    tilt: Float = 0f,
    cornerRadius: Dp = Zine.RadiusSticker,
    borderWidth: Dp = Zine.BorderHairline,
    shadowOffset: Dp = 0.dp,
    horizontalPadding: Dp = 7.dp,
    verticalPadding: Dp = 1.dp,
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier = modifier
            .rotate(tilt)
            .zineSurface(
                background = background,
                cornerRadius = cornerRadius,
                borderWidth = borderWidth,
                shadowOffset = shadowOffset,
            )
            .padding(horizontal = horizontalPadding, vertical = verticalPadding),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CompositionLocalProvider(
            LocalTextStyle provides textStyle.copy(color = contentColor),
            content = { content() },
        )
    }
}

/** The turmeric star badge that appears on every card and the detail hero. */
@Composable
fun ZineRatingBadge(
    rating: Double,
    modifier: Modifier = Modifier,
    tilt: Float = 1.5f,
    large: Boolean = false,
) {
    ZineSticker(
        modifier = modifier,
        background = NommitColors.Turmeric,
        tilt = tilt,
        cornerRadius = if (large) Zine.RadiusControl else Zine.RadiusTag,
        borderWidth = if (large) Zine.BorderNormal else Zine.BorderHairline,
        shadowOffset = Zine.ShadowTiny,
        horizontalPadding = if (large) 11.dp else 8.dp,
        verticalPadding = if (large) 3.dp else 1.dp,
    ) {
        Text("⭐", style = if (large) NommitType.Stamp else NommitType.StickerLabel)
        Text(
            text = String.format(java.util.Locale.US, "%.1f", rating),
            style = if (large) NommitType.CardTitle else NommitType.RatingNumber,
            color = NommitColors.Ink,
        )
    }
}

/**
 * A cream panel with a heavy outline -- the bottom control card, the fact tiles,
 * anything that groups controls.
 */
@Composable
fun ZinePanel(
    modifier: Modifier = Modifier,
    background: Color = NommitColors.Cream,
    cornerRadius: Dp = Zine.RadiusSheet,
    borderWidth: Dp = Zine.BorderThick,
    shadowOffset: Dp = Zine.ShadowXl,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier.zineSurface(
            background = background,
            cornerRadius = cornerRadius,
            borderWidth = borderWidth,
            shadowOffset = shadowOffset,
        ),
    ) {
        content()
    }
}

/**
 * Clicks without Material's ripple: the zine's feedback is squash-and-overshoot,
 * and a ripple on a hard-outlined cut-out looks like a rendering bug.
 */
private fun Modifier.clickableNoRipple(
    interactionSource: MutableInteractionSource,
    enabled: Boolean,
    onClick: () -> Unit,
): Modifier = this.clickable(
    interactionSource = interactionSource,
    indication = null,
    enabled = enabled,
    onClick = onClick,
)
