package com.example.nommit.feature.discovery.ui.results

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.nommit.core.common.formatDistance
import com.example.nommit.core.ui.component.ZineCuisineChip
import com.example.nommit.core.ui.component.rememberDealIn
import com.example.nommit.core.ui.component.ZinePhotoTile
import com.example.nommit.core.ui.component.ZineSticker
import com.example.nommit.core.ui.component.zineSurface
import com.example.nommit.core.ui.theme.CuisineStyles
import com.example.nommit.core.ui.theme.NommitColors
import com.example.nommit.core.ui.theme.NommitType
import com.example.nommit.core.ui.theme.Zine
import com.example.nommit.feature.discovery.domain.model.Cuisine
import com.example.nommit.feature.discovery.domain.model.Restaurant

/**
 * The results sheet: count, cuisine chips and the card list.
 *
 * The sort toggle and price filter that used to sit here are gone. Both depended
 * on paid fields (rating, review count, price level) that the Essentials field mask
 * no longer returns; a sort control offering a single option would be furniture.
 * Results are always nearest-first, and cuisine chips carry the filtering.
 */
@Composable
fun ResultsSheet(
    results: List<Restaurant>,
    radiusMeters: Double,
    cuisines: List<Pair<Cuisine, Int>>,
    selectedCuisines: Set<String>,
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

            Spacer(Modifier.height(4.dp))
            Text(
                text = "nearest first",
                style = NommitType.StampSmall,
                color = NommitColors.InkFaint,
            )

            if (cuisines.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                FlowRow(
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
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 26.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            itemsIndexed(results, key = { _, item -> item.id }) { index, restaurant ->
                RestaurantCard(
                    restaurant = restaurant,
                    dealInIndex = index,
                    // Tilt by list position, not at random, so a card doesn't jump
                    // to a new angle every time the list re-filters.
                    tilt = Zine.CardTilts[index % Zine.CardTilts.size],
                    onClick = { onRestaurantClick(restaurant) },
                )
            }
        }
    }
}

/**
 * A result card: generated zine tile, name, cuisine sticker, distance stamp.
 *
 * No rating badge, price tag or photo -- all three needed billed fields. The card
 * keeps its outline, hard shadow and tilt, so the reduced data reads as a simpler
 * card rather than a broken one.
 */
@Composable
fun RestaurantCard(
    restaurant: Restaurant,
    tilt: Float,
    dealInIndex: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val cuisineStyle = CuisineStyles.of(restaurant.cuisine.key)
    // The deal-in owns the rotation so the card lands on its tilt rather than
    // holding it while separately fading in.
    val dealIn = rememberDealIn(index = dealInIndex, settleRotation = tilt)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(dealIn)
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
        ZinePhotoTile(
            cuisineKey = restaurant.cuisine.key,
            label = restaurant.cuisine.displayName.lowercase(),
            modifier = Modifier.size(96.dp),
            emojiStyle = NommitType.TitleLarge,
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

            ZineSticker(
                background = cuisineStyle.color,
                contentColor = cuisineStyle.onColor,
                tilt = -Zine.TILT_SUBTLE,
            ) {
                Text(cuisineStyle.emoji)
                Text(restaurant.cuisine.displayName)
            }

            Spacer(Modifier.height(10.dp))
            Text(
                text = formatDistance(restaurant.distanceMeters),
                style = NommitType.Stamp,
                color = NommitColors.Chili,
            )
        }
    }
}
