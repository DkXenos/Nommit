package com.example.nommit.core.common

object NommitConstants {
    /** Radius bounds for the draggable circle, in metres. */
    const val MIN_RADIUS_METERS = 200.0
    const val MAX_RADIUS_METERS = 5_000.0
    const val DEFAULT_RADIUS_METERS = 1_200.0

    /** How much the "widen the radius" button in the empty state grows the circle. */
    const val RADIUS_GROW_STEP_METERS = 800.0

    /** Cache lifetime. Restaurant metadata barely changes, so a day is plenty (§5e). */
    const val CACHE_TTL_MILLIS = 24L * 60 * 60 * 1000

    /**
     * Cache keys bucket the user's position into a coarse grid so that small GPS
     * jitter still hits the same cached search. ~0.005 deg is roughly 500 m.
     */
    const val CACHE_GRID_DEGREES = 0.005

    /** Text Search (New) returns at most 20 per page, 3 pages, 60 total (§5b). */
    const val PAGE_SIZE = 20
    const val MAX_PAGES = 3
}
