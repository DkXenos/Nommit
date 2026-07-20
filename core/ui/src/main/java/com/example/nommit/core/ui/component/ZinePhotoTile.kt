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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.nommit.core.ui.theme.CuisineStyles
import com.example.nommit.core.ui.theme.NommitColors
import com.example.nommit.core.ui.theme.NommitType
import com.example.nommit.core.ui.theme.Zine

/**
 * The generated stand-in for a restaurant photo.
 *
 * Many street-food places have no Places photo at all, so the drawn tile is the
 * base layer and a real photo is painted over it when one exists: cuisine colour,
 * the torn-paper hatching from the style guide, the cuisine emoji as the subject,
 * and a caption strip. Because the colour and emoji come from the cuisine, the
 * fallback matches that place's chip and map pin -- it reads as a deliberate
 * illustration rather than a failed image load.
 */
@Composable
fun ZinePhotoTile(
    cuisineKey: String,
    label: String,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 11.dp,
    emojiStyle: androidx.compose.ui.text.TextStyle = NommitType.TitleLarge,
    /**
     * A real Places photo, when the place has one. The drawn tile underneath stays
     * in place as the loading and error state, so a slow or missing image degrades
     * into the zine illustration rather than a grey rectangle.
     */
    photoUrl: String? = null,
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

        if (photoUrl != null) {
            // Drawn over the tile rather than replacing it, so there is never a
            // blank frame between layout and the image arriving.
            AsyncImage(
                model = photoUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }

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
