# Nommit — Update / Changeset for Claude Code

> The app is already scaffolded from the previous spec. Apply the three changes below to the **existing** project. Do NOT rebuild from scratch and do NOT re-architect — these are targeted edits.

---

## Change 1 — The API key is now real and present

`local.properties` now contains a **valid Google `AIzaSy…` key** in both `MAPS_API_KEY` and `PLACES_API_KEY`. Remove any placeholder-detection guards, mock fallbacks, or "key missing" build-time warnings you added earlier — assume the key is present and valid. Wire it through as designed (manifest placeholder for Maps, BuildConfig for the network layer).

Reminder to surface to the human (not a code task): the key's project needs an **Android-app restriction** matching the package (`com.example.nommit`) + the **debug SHA-1** (`./gradlew signingReport`), or the map renders grey even with a valid key.

---

## Change 2 — Run in "basic filtering, no billing" mode

**Constraint:** the key's Google Cloud project has **no billing account attached.** The app must degrade to a cuisine + distance experience and must NOT depend on paid/premium data fields.

Make these edits:

**2a. Drop the data-heavy filters and sorts.**
- Remove **"Top Nommed"** (rating sort) and **"Cheap Eats"** (price sort) from the sort control. Keep only **Nearest** (distance).
- Remove the **price ($–$$$$) filter** entirely.
- Keep the **cuisine chips** and **distance/radius** — those are the "basic filtering" that survives.

**2b. Trim the field mask to the cheapest (Essentials) tier.**
In the Places request `X-Goog-FieldMask`, request only:
```
places.id,places.displayName,places.formattedAddress,places.location,places.types,places.primaryType
```
Remove `rating`, `userRatingCount`, `priceLevel`, `currentOpeningHours`, and `photos`. Those pull higher billing tiers and are no longer used. The field mask determines which SKU/tier you're billed at, so trimming it is the actual cost lever, not just cosmetic.

**2c. Update the card and detail UI** to the reduced data: name, cuisine sticker, distance stamp. No rating star, no price tag, no photo (use a generated/placeholder zine tile from `core:ui` in the photo slot instead of a Places image). Keep the zine styling intact.

**2d. Handle the billing error explicitly.** Places API (New) and the Maps SDK both **require billing to be enabled to return ANY data** — a key alone is not enough. If the Places call returns a billing/authorization error (`REQUEST_DENIED`, `PERMISSION_DENIED`, or a body mentioning billing), do NOT show the generic network-error state. Show a distinct state: *"Restaurant search needs billing enabled on the Google Cloud project"* with a short line pointing the user to enable it. This makes the real cause obvious instead of looking like a dead network.

**2e. Keep the restaurant provider swappable.** The repository already talks to a `RemoteDataSource` interface — keep it that way. Add a `TODO` note that a **no-billing provider (OpenStreetMap Overpass)** can be dropped in behind the same interface if the project stays billing-free (Overpass gives cuisine + location with no key and no billing, but no ratings/price — which matches this basic mode). Do not implement it now; just leave the seam clean.

---

## Change 3 — Fix the radius control (it's a spring-return jog slider, not a plain slider)

The plain min-max `Slider` you used is wrong. The intended control is already drawn in the `/design` HTML — **re-read the radius/slider component there and match it.** Here is the interaction it's meant to have, since the static HTML can't show the motion:

**It's an "endless" jog slider that self-centers:**
- The thumb **rests at the horizontal center** of the track (neutral position).
- **Drag right → radius grows; drag left → radius shrinks.** The offset from center controls the **rate** the radius changes while you hold — the further from center, the faster it moves. (Hold near center = slow nudge; hold far = fast sweep.)
- **On release, the thumb springs back to center** with a spring animation, and the **radius keeps its new value.** The control is "endless" because it never hits an end stop — it always returns to neutral so you can keep adjusting.
- The **underlying radius is clamped** to a sensible range (e.g. **200 m – 5 km**) even though the control itself has no visible ends.
- The **live distance readout** ("1.2 km") updates continuously as it moves, and the radius circle on the map resizes in real time.

**Implementation notes (Compose):**
- Don't use Material `Slider`. Build a custom composable with `pointerInput` + `detectDragGestures`.
- Track the thumb's horizontal offset while dragging; map `offsetFraction` (−1…+1 from center) to a signed rate, and apply it to the radius on a frame tick (`LaunchedEffect` / `withFrameNanos`) while the drag is active.
- On drag end, `animate*AsState` the thumb offset back to 0 with a spring (springy/gummy, matching the app's motion language).
- Optional: light haptic tick as the radius crosses each 100 m.
- This **replaces** both the plain slider and the earlier circle-edge-handle idea — the jog slider is the answer to the "handle fights the map gesture" problem, since it lives in the bottom UI strip, not on the map.

---

## After applying

Confirm: builds, key wired, sort reduced to Nearest, field mask trimmed, billing-error state shows on a no-billing key, and the radius jog slider springs back to center while the radius holds. Flag anything in the existing code these changes leave orphaned (e.g. now-unused price/rating mappers) and remove it.