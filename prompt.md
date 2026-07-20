# Nommit — Build Spec for Claude Code

> Paste this whole file into Claude Code at the root of a fresh Android Studio project.
> It is the source of truth for **what to build**, **how to architect it**, and **how to wire the API**.
> The **visual design is NOT described here in detail** — it lives in the exported HTML from Claude Design (see §7). Read that HTML and translate it faithfully.

---

## 1. What Nommit is

A **single-function** food discovery app. No login, no accounts, no profiles, no saved lists, no super-app features. It does exactly one thing, the way a focused Apple utility app does:

1. On open, it shows a **map centered on the user's current location** with a **draggable radius circle**.
2. A **checklist of cuisine genres** sits at the bottom — populated dynamically from what the API actually returns for that area (don't show cuisines that have zero results nearby).
3. A big **"Nom 🔍"** button runs the search.
4. Results appear as a **draggable bottom sheet over the same map** (the map never leaves), with **sort** (Nearest / Top Nommed / Cheap Eats) and **price filter** controls.
5. Tapping a result **expands a detail sheet** over the map; a "Directions" button hands off to Google/Apple Maps. That's the whole app.

Do NOT add: onboarding carousels, sign-in, settings beyond a location-permission re-prompt, favorites, sharing, reviews-writing, ordering. Resist scope creep. One function.

---

## 2. Tech stack (use exactly this)

- **Language:** Kotlin
- **UI:** Jetpack Compose (Material 3)
- **Map:** `maps-compose` (Google Maps Compose) + Google Maps SDK for Android
- **Location:** `FusedLocationProviderClient` (play-services-location)
- **Networking:** Retrofit + OkHttp + kotlinx.serialization
- **Images:** Coil (`coil-compose`)
- **Local cache:** Room
- **DI:** Hilt
- **Async:** Coroutines + Flow
- **Min SDK:** 26 · **Target SDK:** latest stable

---

## 3. Architecture — feature-based, multi-module

**Feature-based ≠ "many features."** This app has essentially **one feature** (`discovery`). Feature-based means the code is organized **by feature**, each owning its full `data / domain / ui` stack, instead of package-by-layer (one global `data/`, one global `ui/`). Do NOT invent fake features (no separate `search`, `results`, `detail` modules that all share the same map and data source) — that's over-engineering. Search, results, and detail are **sub-flows of the single `discovery` feature** and live as packages inside it.

Gradle multi-module layout:

```
:app                      // Application, navigation host, DI setup, MainActivity
:core:common              // Result wrapper, dispatchers, Haversine distance util, constants
:core:ui                  // Compose theme + design tokens (from Claude Design HTML), shared composables
:core:network             // Retrofit/OkHttp setup, API key interceptor, base DTOs
:core:database            // Room db, cache entities + DAO
:core:location            // FusedLocation wrapper exposing current location as a suspend/Flow
:feature:discovery        // THE feature
    data/
        remote/           // Places API service, DTOs, field-mask constants
        local/            // Room-backed cache source
        mapper/           // DTO -> domain model mappers
        DiscoveryRepositoryImpl.kt
    domain/
        model/            // Restaurant, Cuisine, PriceLevel, SortMode, SearchQuery
        DiscoveryRepository.kt   // interface
        usecase/          // SearchNearbyRestaurants, GetAvailableCuisines
    ui/
        map/              // MapScreen: location + draggable radius + cuisine checklist + Nom button
        results/          // ResultsSheet: list, sort bar, price filter, restaurant cards
        detail/           // DetailSheet: hero, tags, hours, Directions CTA
        DiscoveryViewModel.kt
        DiscoveryUiState.kt
```

**Rules:**
- Each feature depends on `core:*`, never on another feature.
- `ui` depends on `domain`; `data` implements `domain`; `domain` depends on nothing Android-specific.
- One `DiscoveryViewModel` owns a single `DiscoveryUiState` (sealed/`data class`) driving all three sub-flows, since they all sit over one map and share the same result set. Expose it as `StateFlow`.
- Use `Result`-style wrapper (`Loading / Success / Error / Empty`) from `core:common`; render the Empty/Error/Loading states described in §6.

---

## 4. Getting the third-party API (Google Places API — New)

**This is a manual step the human must do — Claude Code cannot create the key. Instruct the user to do this, then read the key from `local.properties`.**

Human setup steps:
1. Go to **Google Cloud Console** → create a project (e.g. `nommit`).
2. **Enable billing** (required even for free-tier usage; there is a recurring monthly free allotment, and with caching this app should stay within it — confirm current numbers on Google's Places API pricing page before shipping).
3. **APIs & Services → Enable APIs** → enable **Places API (New)** and **Maps SDK for Android**.
4. **Credentials → Create credentials → API key.** Create **one key** (it can serve both APIs) or two separate keys.
5. **Restrict the key** (important — a key shipped in an APK is extractable):
   - Application restriction: **Android apps** → add the app's **package name** + **SHA-1** (from `./gradlew signingReport`).
   - API restriction: limit to **Places API (New)** + **Maps SDK for Android** only.
6. Put the key in `local.properties` (which is git-ignored):
   ```
   MAPS_API_KEY=xxxxxxxx
   PLACES_API_KEY=xxxxxxxx   # can be the same value
   ```

Claude Code wiring:
- In `app/build.gradle.kts`, read the keys via a `Properties` load of `local.properties` and expose them:
  - `MAPS_API_KEY` → inject into the manifest via `manifestPlaceholders` for the Maps SDK meta-data tag.
  - `PLACES_API_KEY` → expose via `buildConfigField` (BuildConfig) for the network layer.
- **Never hardcode keys in source. Never commit them.** Ensure `local.properties` is in `.gitignore`.

---

## 5. Talking to the Places API (New) — REST via Retrofit

Use **REST**, not the Places SDK's `searchNearby`. Reason: REST gives clean DTO → domain mapping and makes Room caching and testing straightforward, which suits the feature-based layering. (The Android SDK is the alternative if REST becomes a blocker — note it but default to REST.)

### 5a. Nearby Search request

```
POST https://places.googleapis.com/v1/places:searchNearby
Headers:
  Content-Type: application/json
  X-Goog-Api-Key: <PLACES_API_KEY>
  X-Goog-FieldMask: places.id,places.displayName,places.formattedAddress,places.location,places.types,places.primaryType,places.priceLevel,places.rating,places.userRatingCount,places.currentOpeningHours.openNow,places.photos
Body:
{
  "includedTypes": ["restaurant","cafe","bar","bakery","meal_takeaway"],
  "maxResultCount": 20,
  "rankPreference": "DISTANCE",
  "locationRestriction": {
    "circle": {
      "center": { "latitude": <userLat>, "longitude": <userLng> },
      "radius": <radiusInMeters>
    }
  }
}
```

- The `X-Goog-FieldMask` header controls **which fields are returned AND what you pay for** — request only what's listed above. Put it in an OkHttp interceptor or per-call header.
- `priceLevel` enum values: `PRICE_LEVEL_FREE`, `PRICE_LEVEL_INEXPENSIVE`, `PRICE_LEVEL_MODERATE`, `PRICE_LEVEL_EXPENSIVE`, `PRICE_LEVEL_VERY_EXPENSIVE`, `PRICE_LEVEL_UNSPECIFIED`. Map to `$`–`$$$$` in the domain layer; treat `UNSPECIFIED` as "unknown," don't drop the place.

### 5b. ⚠️ The 20-result cap (design-critical)

**Nearby Search (New) returns a maximum of 20 results and has NO pagination.** For a small radius this is fine. To genuinely "show all restaurants in the area" for larger radii, use **Text Search (New)** instead, which supports up to **60 results** via `nextPageToken` paging:

```
POST https://places.googleapis.com/v1/places:searchText
Body:
{
  "textQuery": "restaurants",
  "includedType": "restaurant",
  "locationRestriction": { "circle": { "center": {...}, "radius": <meters> } },
  "pageSize": 20
}
```
Loop on `nextPageToken` (up to 3 pages / 60 results). **Implement Text Search as the primary search path** so the radius can grow without silently capping at 20; keep the code structured so swapping the endpoint is trivial. Document this choice in a comment.

### 5c. Photos

Response gives `places[].photos[].name` (a resource path, not a URL). Build the image URL:
```
https://places.googleapis.com/v1/{photo.name}/media?maxWidthPx=600&key=<PLACES_API_KEY>
```
Feed that straight into Coil. Only fetch the first photo per card to control cost.

### 5d. Filtering & sorting (do it in OUR layer, not the API)

The New API filters by type and ranks by distance/popularity only — it has **no price/rating filter params**. So:
- **Cuisine checklist** → derive available cuisines from the `types` / `primaryType` of returned places, dedupe, and show only those as chips. Selecting chips filters the in-memory list (and/or constrains `includedTypes` on the next search).
- **Sort modes** (client-side, in a use case):
  - `Nearest` → sort by Haversine distance from user (util in `core:common`).
  - `Top Nommed` → sort by a rating score, e.g. `rating` weighted by `log(userRatingCount)` so a 5.0 with 2 reviews doesn't beat a 4.6 with 900.
  - `Cheap Eats` → sort ascending by `priceLevel`, unknowns last.
- **Price filter** ($–$$$$) → simple predicate on `priceLevel`.

### 5e. Caching (keeps you in the free tier)

- Cache each search result set in **Room**, keyed by a coarse grid cell of (lat, lng) + rounded radius + selected cuisines.
- TTL ~24h (restaurant data barely changes). On search: check cache first, hit network only on miss/expiry.
- This is the single biggest cost saver — implement it, don't skip it.

---

## 6. Behavior spec per sub-flow

**Map screen (`ui/map`)**
- Request `ACCESS_FINE_LOCATION` (+ coarse) at runtime on first open. If denied → show the **location-denied state**: friendly message + a button that opens app settings. No login, this is the only permission moment.
- Center map on current location; draw a **radius circle** overlay. Provide a **draggable radius control** — implement as a draggable handle on the circle's edge; if that's too fiddly in `maps-compose`, fall back to a styled slider. Show the live distance ("1.2 km") on the circle/handle.
- **Cuisine checklist**: horizontally scrollable chips at the bottom, populated from the API for the current area; multi-select; selected chips get a checked/stamped state.
- **"Nom 🔍" button**: chunky, prominent, magnifying-glass icon beside the label. On tap → run search → show loading state → slide up the results sheet.
- Keep the radius control and the cuisine chips from fighting for the same thumb zone — put the distance readout ON the circle, reserve the bottom strip for chips + Nom.

**Results sheet (`ui/results`)** — draggable bottom sheet over the map, three heights: **Peek** (map full, sheet summary + one swipeable card row synced to pins), **Half** (map + scrollable card list), **Full** (cards dominate, map as a thin strip). Sort toggle + price filter pinned at the top of the sheet. Each restaurant is a **card** (see HTML for exact styling): photo, name, cuisine tag, price ($$), distance, rating. Selecting a card → detail.

**Detail sheet (`ui/detail`)** — expands over the map: hero photo, name, cuisine tags, price, opening-hours (openNow), rating, distance. **"Directions"** button fires an `Intent` to Google Maps (`google.navigation:q=lat,lng` or a maps geo URI) — do NOT rebuild navigation. Swipe down to collapse.

**Required states (design all of them):**
- **Loading** — the "finding spots… N" counter + pins dropping animation (doubles as the discovery payoff).
- **Empty** — no results in radius: doodle empty-plate + "Nothing to nom here — widen your radius" + a button that grows the circle.
- **Error** — network/API failure: friendly retry.
- **Location denied** — as above.

---

## 7. UI / UX — pull it from the Claude Design HTML (do NOT invent your own styling)

The visual design already exists as **exported HTML/CSS from Claude Design**. It will be placed in a **`/design`** folder at the repo root (ask the user for the exact path if it's not there). **That HTML is the single source of truth for all visuals.** Your job is to translate it into Compose, not to redesign it.

Steps:
1. **Read every file in `/design`.** Extract:
   - **Design tokens** → color palette (hex), typography (font families, weights, sizes, line-heights), spacing scale, corner radii, border widths, shadow/offset values.
   - **Component styles** → the restaurant card, cuisine chips, the Nom button, the sort/price controls, the radius readout tag, sheets, empty/loading art.
   - **Motion** → any transitions/keyframes (chomp, card deal, chip jiggle, pin drop, parallax).
2. **Build `core:ui` theme from those tokens** — a Material 3 theme whose `ColorScheme`, `Typography`, and shapes mirror the HTML exactly. Add the custom fonts to `res/font`. Encode non-Material tokens (hard offset shadows, ink outlines, sticker rotations) as reusable Compose modifiers.
3. **Recreate each component in Compose to match the HTML** — same colors, same font, same outline/shadow/rotation, same proportions. The maximalist "night-market zine" look (thick ink outlines, hard drop shadows, warm cream base, saturated cards, slight ±2–4° rotations) must survive the translation. If the HTML and this spec ever disagree on visuals, **the HTML wins**.
4. **Reproduce the animations** described in the HTML with Compose (`animate*AsState`, `AnimatedVisibility`, `updateTransition`, spring specs). Springy/gummy squash-and-overshoot is the motion language — the "Nom" chomp and the pin-drop-into-radius reveal are the hero moments.
5. Match light/dark handling to whatever the HTML defines; if it's single-theme, ship single-theme.

Treat the HTML as a design comp, not runnable code — **you are not embedding a WebView.** Everything becomes native Compose.

---

## 8. Build order for Claude Code

1. Scaffold the multi-module Gradle project (§3) with version catalogs; add all dependencies (§2); set up Hilt.
2. Wire keys from `local.properties` → manifest placeholder + BuildConfig (§4).
3. `core:*` foundations: theme from `/design` HTML (§7), network client + field-mask interceptor, Room cache, location wrapper, Haversine util.
4. `feature:discovery` domain: models, repository interface, use cases.
5. `feature:discovery` data: Places REST service (Text Search primary, §5b), DTOs, mappers, cache-first repository (§5e).
6. `feature:discovery` ui: map screen → results sheet → detail sheet, all driven by one ViewModel/UiState, styled from the HTML.
7. All four states (§6). Then the animations (§7.4).
8. Verify: builds, runs, requests location, searches, caches, sorts/filters, hands off directions.

**Ask me before:** adding any dependency not listed here, adding any feature beyond §1, or making a visual choice not answered by the `/design` HTML.