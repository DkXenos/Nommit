package com.example.nommit.feature.discovery.domain

import com.example.nommit.feature.discovery.domain.model.Cuisine

/**
 * Turns Places API types into the app's cuisine vocabulary.
 *
 * The API hands back a bag of types per place -- typically a specific one
 * ("ramen_restaurant") alongside generic ones ("restaurant", "food",
 * "point_of_interest", "establishment"). The generic ones are useless as chips, so
 * the resolver looks for the most specific known type and only falls back to a
 * generic label when nothing better exists.
 */
object CuisineResolver {

    /**
     * Types that describe *what* the food is. Ordered by specificity: the first
     * match wins, so "ramen_restaurant" beats a co-occurring "japanese_restaurant".
     */
    private val cuisineTypes: List<Pair<String, Cuisine>> = listOf(
        "ramen_restaurant" to Cuisine("ramen", "Ramen"),
        "sushi_restaurant" to Cuisine("sushi", "Sushi"),
        "pizza_restaurant" to Cuisine("pizza", "Pizza"),
        "hamburger_restaurant" to Cuisine("burger", "Burgers"),
        "barbecue_restaurant" to Cuisine("bbq", "BBQ"),
        "steak_house" to Cuisine("steak", "Steak"),
        "seafood_restaurant" to Cuisine("seafood", "Seafood"),
        "sandwich_shop" to Cuisine("sandwich", "Sandwiches"),
        "ice_cream_shop" to Cuisine("ice_cream", "Ice Cream"),
        "dessert_shop" to Cuisine("dessert", "Dessert"),
        "dessert_restaurant" to Cuisine("dessert", "Dessert"),
        "bakery" to Cuisine("bakery", "Bakery"),
        "breakfast_restaurant" to Cuisine("breakfast", "Breakfast"),
        "brunch_restaurant" to Cuisine("breakfast", "Brunch"),
        "thai_restaurant" to Cuisine("thai", "Thai"),
        "korean_restaurant" to Cuisine("korean", "Korean"),
        "japanese_restaurant" to Cuisine("japanese", "Japanese"),
        "chinese_restaurant" to Cuisine("chinese", "Chinese"),
        "vietnamese_restaurant" to Cuisine("vietnamese", "Vietnamese"),
        "indonesian_restaurant" to Cuisine("indonesian", "Indonesian"),
        "indian_restaurant" to Cuisine("indian", "Indian"),
        "italian_restaurant" to Cuisine("italian", "Italian"),
        "mexican_restaurant" to Cuisine("mexican", "Mexican"),
        "french_restaurant" to Cuisine("french", "French"),
        "greek_restaurant" to Cuisine("greek", "Greek"),
        "spanish_restaurant" to Cuisine("spanish", "Spanish"),
        "turkish_restaurant" to Cuisine("middle_eastern", "Turkish"),
        "lebanese_restaurant" to Cuisine("middle_eastern", "Lebanese"),
        "middle_eastern_restaurant" to Cuisine("middle_eastern", "Middle Eastern"),
        "mediterranean_restaurant" to Cuisine("mediterranean", "Mediterranean"),
        "brazilian_restaurant" to Cuisine("steak", "Brazilian"),
        "american_restaurant" to Cuisine("burger", "American"),
        "vegan_restaurant" to Cuisine("vegetarian", "Vegan"),
        "vegetarian_restaurant" to Cuisine("vegetarian", "Vegetarian"),
        "fast_food_restaurant" to Cuisine("fast_food", "Fast Food"),
        "coffee_shop" to Cuisine("cafe", "Coffee"),
        "cafe" to Cuisine("cafe", "Cafe"),
        "bar" to Cuisine("bar", "Bar"),
        "meal_takeaway" to Cuisine("fast_food", "Takeaway"),
    )

    private val byType = cuisineTypes.toMap()

    /** Last resort so a place is never dropped for lack of a recognisable type. */
    private val generic = Cuisine("restaurant", "Eats")

    /**
     * @param primaryType the API's own `primaryType`, trusted first when it is a
     *   cuisine we know -- it is Google's judgement of what the place mainly is.
     * @param primaryTypeDisplayName Google's localised label, used to name an
     *   unknown-but-specific type rather than showing it as generic "Eats".
     */
    fun resolve(
        primaryType: String?,
        primaryTypeDisplayName: String?,
        types: List<String>,
    ): Cuisine {
        byType[primaryType]?.let { return it }

        // Fall back to the most specific type present, using the declared order.
        cuisineTypes.firstOrNull { (type, _) -> type in types }?.let { return it.second }

        // A specific type we don't have a mapping for still beats "Eats", provided
        // Google gave us something human-readable to put on the chip.
        if (!primaryType.isNullOrBlank() && !primaryTypeDisplayName.isNullOrBlank()) {
            return Cuisine(primaryType, primaryTypeDisplayName)
        }
        return generic
    }

    /** All cuisine keys a place could be filtered under, primary first. */
    fun allCuisines(primaryType: String?, types: List<String>): Set<String> =
        buildSet {
            byType[primaryType]?.let { add(it.key) }
            types.forEach { type -> byType[type]?.let { add(it.key) } }
        }
}
