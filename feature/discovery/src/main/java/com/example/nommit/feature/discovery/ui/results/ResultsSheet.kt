package com.example.nommit.feature.discovery.ui.results

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.nommit.core.common.formatDistance
import com.example.nommit.core.ui.component.ZineCuisineChip
import com.example.nommit.core.ui.component.ZineRatingBadge
import com.example.nommit.core.ui.component.ZineSticker
import com.example.nommit.core.ui.component.tornPaperHatching
import com.example.nommit.core.ui.component.zineSurface
import com.example.nommit.core.ui.theme.CuisineStyles
import com.example.nommit.core.ui.theme.NommitColors
import com.example.nommit.core.ui.theme.NommitType
import com.example.nommit.core.ui.theme.Zine
import com.example.nommit.feature.discovery.domain.model.Cuisine
import com.example.nommit.feature.discovery.domain.model.PriceLevel
import com.example.nommit.feature.discovery.domain.model.Restaurant
import com.example.nommit.feature.discovery.domain.model.SortMode
import androidx.compose.foundation.shape.RoundedCornerShape

/**
 * The results sheet: count, sort toggle, price filter, cuisine chips and the card
 * list. Rendered inside the draggable sheet frame, which owns the height detents.
 */
@Composable
fun ResultsSheet(
    results: List<Restaurant>,
    radiusMeters: Double,
    cuisines: List<Pair<Cuisine, Int>>,
    selectedCuisines: Set<String>,
    sortMode: SortMode,
    priceFilter: PriceLevel?,
    photoUrlFor: (Restaurant) -> String?,
    onSortChange: (SortMode) -> Unit,
    onPriceChange: (PriceLevel?) -> Unit,
    onCuisineToggle: (String) -> Unit,
    onRestaurantClick: (Restaurant) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp)) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "${results.size} spots",
                    style = NommitType.TitleLarge,
                    color = NommitColors.Ink,
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "within ${formatDistance(radiusMeters)}",
                    style = NommitType.Stamp,
                    color = NommitColors.Chili,
                )
            }

            Spacer(Modifier.height(10.dp))
            SortBar(sortMode = sortMode, onSortChange = onSortChange)

            Spacer(Modifier.height(9.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                PriceFilter(selected = priceFilter, onSelect = onPriceChange)
            }

            if (cuisines.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                androidx.compose.foundation.layout.FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    cuisines.forEach { (cuisine, count) ->
                        ZineCuisineChip(
                            label = cuisine.displayName,
                            style = CuisineStyles.of(cuisine.key),
                            selected = cuisine.key in selectedCuisines,
                            count = count,
                            onToggle = { onCuisineToggle(cuisine.key) },
                            compact = true,
                        )
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 6.dp,
                bottom = 26.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            itemsIndexed(results, key = { _, item -> item.id }) { index, restaurant ->
                RestaurantCard(
                    restaurant = restaurant,
                    photoUrl = photoUrlFor(restaurant),
                    // Tilt by list position, not at random, so a card doesn't jump
                    // to a new angle every time the list re-sorts.
                    tilt = Zine.CardTilts[index % Zine.CardTilts.size],
                    onClick = { onRestaurantClick(restaurant) },
                )
            }
        }
    }
}

@Composable
private fun SortBar(sortMode: SortMode, onSortChange: (SortMode) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
        SortMode.entries.forEach { mode ->
            val active = mode == sortMode
            Box(
                modifier = Modifier
                    .weight(1f)
                    .rotate(if (active) -1.5f else 0f)
                    .zineSurface(
                        background = if (active) NommitColors.Chili else NommitColors.Cream,
                        cornerRadius = Zine.RadiusControl,
                        borderWidth = Zine.BorderThin,
                        shadowOffset = Zine.ShadowTiny,
                    )
                    .clickable { onSortChange(mode) }
                    .padding(vertical = 7.dp, horizontal = 4.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = mode.label,
                    style = NommitType.Toggle,
                    color = if (active) NommitColors.Cream else NommitColors.Ink,
                    maxLines = 1,
                )
            }
        }
    }
}

/**
 * Segmented $ / $$ / $$$ control. Tapping the active tier clears the filter, as in
 * the comp -- there is no separate "all" button.
 */
@Composable
private fun PriceFilter(selected: PriceLevel?, onSelect: (PriceLevel?) -> Unit) {
    val tiers = listOf(PriceLevel.Inexpensive, PriceLevel.Moderate, PriceLevel.Expensive)
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(Zine.RadiusControl))
            .zineSurface(
                background = NommitColors.Cream,
                cornerRadius = Zine.RadiusControl,
                borderWidth = Zine.BorderThin,
                shadowOffset = Zine.ShadowTiny,
            ),
    ) {
        tiers.forEach { tier ->
            val active = tier == selected
            Box(
                modifier = Modifier
                    .background(if (active) NommitColors.Turmeric else NommitColors.Cream)
                    .clickable { onSelect(tier) }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = tier.symbol.orEmpty(),
                    style = NommitType.Stamp,
                    color = NommitColors.Ink,
                )
            }
        }
    }
}

/**
 * A result card: photo panel, name, cuisine sticker, price, rating and distance.
 * Tilted a couple of degrees so the list reads as hand-dealt.
 */
@Composable
fun RestaurantCard(
    restaurant: Restaurant,
    photoUrl: String?,
    tilt: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val cuisineStyle = CuisineStyles.of(restaurant.cuisine.key)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .rotate(tilt)
            .zineSurface(
                background = NommitColors.CardWhite,
                cornerRadius = Zine.RadiusCard,
                borderWidth = Zine.BorderNormal,
                shadowOffset = Zine.ShadowMedium,
            )
            .clickable(onClick = onClick)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(13.dp),
    ) {
        PhotoPanel(
            photoUrl = photoUrl,
            fallbackColor = cuisineStyle.color,
            label = restaurant.cuisine.displayName.lowercase(),
            modifier = Modifier.size(96.dp),
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = restaurant.name,
                style = NommitType.CardTitle,
                color = NommitColors.Ink,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(7.dp))

            androidx.compose.foundation.layout.FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                ZineSticker(
                    background = cuisineStyle.color,
                    contentColor = cuisineStyle.onColor,
                    tilt = -Zine.TILT_SUBTLE,
                ) {
                    Text(cuisineStyle.emoji)
                    Text(restaurant.cuisine.displayName)
                }
                // No price stamp at all when the API has no price data -- inventing
                // one would be worse than the small gap it leaves.
                restaurant.priceLevel.symbol?.let { symbol ->
                    ZineSticker(textStyle = NommitType.StampSmall) { Text(symbol) }
                }
            }

            Spacer(Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                restaurant.rating?.let { ZineRatingBadge(rating = it) }
                Text(
                    text = formatDistance(restaurant.distanceMeters),
                    style = NommitType.Stamp,
                    color = NommitColors.Chili,
                )
            }
        }
    }
}

/**
 * The photo panel. Falls back to the cuisine colour with the comp's torn-paper
 * hatching whenever there is no photo -- which is common for street-food places,
 * so the fallback has to look intentional rather than broken.
 */
@Composable
fun PhotoPanel(
    photoUrl: String?,
    fallbackColor: androidx.compose.ui.graphics.Color,
    label: String,
    modifier: Modifier = Modifier,
    cornerRadius: androidx.compose.ui.unit.Dp = 11.dp,
) {
    Box(
        modifier = modifier
            .zineSurface(
                background = fallbackColor,
                cornerRadius = cornerRadius,
                borderWidth = Zine.BorderNormal,
                shadowOffset = 0.dp,
            )
            .clip(RoundedCornerShape(cornerRadius)),
        contentAlignment = Alignment.BottomStart,
    ) {
        if (photoUrl != null) {
            AsyncImage(
                model = photoUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Box(modifier = Modifier.fillMaxSize().tornPaperHatching())
        }

        Text(
            text = label,
            style = NommitType.StickerLabel,
            color = NommitColors.Ink,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
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
