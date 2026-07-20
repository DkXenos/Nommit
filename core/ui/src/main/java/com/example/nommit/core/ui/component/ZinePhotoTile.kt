package com.example.nommit.core.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.nommit.core.ui.theme.CuisineStyles
import com.example.nommit.core.ui.theme.NommitColors
import com.example.nommit.core.ui.theme.NommitType
import com.example.nommit.core.ui.theme.Zine

/**
 * The generated stand-in for a restaurant photo.
 *
 * Places photos sit above the Essentials billing tier, so the app doesn't fetch
 * them at all. Rather than leave a hole, each place gets a drawn zine tile: its
 * cuisine colour, the torn-paper hatching from the style guide, the cuisine emoji
 * as the subject, and a caption strip. Because the colour and emoji come from the
 * cuisine, the tile matches that place's chip and map pin -- it reads as a
 * deliberate illustration rather than a failed image load.
 */
@Composable
fun ZinePhotoTile(
    cuisineKey: String,
    label: String,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 11.dp,
    emojiStyle: androidx.compose.ui.text.TextStyle = NommitType.TitleLarge,
) {
    val style = CuisineStyles.of(cuisineKey)

    Box(
        modifier = modifier
            .zineSurface(
                background = style.color,
                cornerRadius = cornerRadius,
                borderWidth = Zine.BorderNormal,
                shadowOffset = 0.dp,
            )
            .clip(RoundedCornerShape(cornerRadius)),
        contentAlignment = Alignment.Center,
    ) {
        Box(modifier = Modifier.fillMaxSize().tornPaperHatching())

        Text(
            text = style.emoji,
            style = emojiStyle,
            modifier = Modifier.padding(bottom = 12.dp),
        )

        Text(
            text = label,
            style = NommitType.StickerLabel,
            color = NommitColors.Ink,
            maxLines = 1,
            textAlign = TextAlign.Center,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .zineSurface(
                    background = NommitColors.Cream,
                    cornerRadius = 0.dp,
                    borderWidth = 0.dp,
                    shadowOffset = 0.dp,
                )
                .padding(horizontal = 5.dp, vertical = 2.dp),
        )
    }
}
