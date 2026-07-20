package com.example.nommit.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.nommit.core.ui.theme.NommitColors
import com.example.nommit.core.ui.theme.Zine

/**
 * The one treatment that makes something a Nommit cut-out: hard offset shadow,
 * thick ink border, cream/colour fill (style guide §05 "Outline everything").
 *
 * The shadow is drawn *outside* the element's bounds, exactly like the CSS
 * `box-shadow: 0 Npx 0 #1E1712` it comes from. That means it does not reserve
 * layout space -- parents must leave [shadowOffset] of room below, which is why
 * the comp's columns use gaps a little larger than they look.
 *
 * @param shadowOffset vertical drop, no blur. Pass 0.dp for a flush element.
 */
fun Modifier.zineSurface(
    background: Color = NommitColors.Cream,
    cornerRadius: Dp = Zine.RadiusCard,
    borderWidth: Dp = Zine.BorderNormal,
    borderColor: Color = NommitColors.Ink,
    shadowOffset: Dp = Zine.ShadowSmall,
    shadowColor: Color = NommitColors.Ink,
): Modifier {
    val shape = RoundedCornerShape(cornerRadius)
    return this
        .then(
            if (shadowOffset > 0.dp) {
                Modifier.drawBehind {
                    val radius = CornerRadius(cornerRadius.toPx())
                    // The shadow is the silhouette of the element, shifted down.
                    // Drawing it at full size (not inset by the border) keeps the
                    // ink border and the shadow reading as one solid slab.
                    drawRoundRect(
                        color = shadowColor,
                        topLeft = Offset(0f, shadowOffset.toPx()),
                        size = Size(size.width, size.height),
                        cornerRadius = radius,
                    )
                }
            } else {
                Modifier
            },
        )
        .background(background, shape)
        .border(borderWidth, borderColor, shape)
}

/**
 * Shadow only, for elements that supply their own fill (e.g. a photo panel whose
 * background is an image rather than a colour).
 */
fun Modifier.zineShadow(
    cornerRadius: Dp = Zine.RadiusCard,
    shadowOffset: Dp = Zine.ShadowSmall,
    shadowColor: Color = NommitColors.Ink,
): Modifier = this.drawBehind {
    drawRoundRect(
        color = shadowColor,
        topLeft = Offset(0f, shadowOffset.toPx()),
        size = Size(size.width, size.height),
        cornerRadius = CornerRadius(cornerRadius.toPx()),
    )
}

/**
 * The torn-paper diagonal hatching the comp lays over every photo placeholder
 * (`repeating-linear-gradient(-45deg, ...)`). Also used as a loading shimmer
 * substrate before a real photo arrives.
 */
fun Modifier.tornPaperHatching(
    stripeWidth: Dp = 7.dp,
    color: Color = NommitColors.Ink.copy(alpha = 0.14f),
): Modifier = this.drawBehind {
    val step = stripeWidth.toPx()
    val strokeW = step
    // -45 degree stripes: walk the diagonal from the top-left past the bottom-right,
    // drawing every other band so half the surface stays transparent.
    var x = -size.height
    while (x < size.width + size.height) {
        drawLine(
            color = color,
            start = Offset(x, size.height),
            end = Offset(x + size.height, 0f),
            strokeWidth = strokeW,
        )
        x += step * 2
    }
}
