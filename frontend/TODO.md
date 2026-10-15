# Frontend TODO

Remaining items from the project audit (2026-10-08). Items 1–4 of that audit are done:
Wear OS dependency removed, deprecations fixed, duplicate components deleted,
touch targets raised to 48dp.

---

## 1. Enable R8 for release builds

`app/build.gradle.kts` currently disables optimization entirely:

```kotlin
buildTypes {
    release {
        optimization { enable = false }
    }
}
```

No shrinking, no obfuscation, no resource stripping. Fine while marking, wrong before
any real distribution.

- [ ] Turn optimization back on for `release`
- [ ] Enable resource shrinking (depends on code shrinking being on)
- [ ] Add a `proguard-rules.pro` — check whether the kotlinx.serialization
      serializers for `Screen` survive; `@Serializable` classes usually need a keep rule
- [ ] Build a release APK and walk the whole flow, specifically navigation
      (serializer stripping shows up as a runtime crash, not a build failure)

**Do this before item 2** — resource shrinking is what makes the unused assets a
non-issue automatically.

## 2. Prune unused assets

Lint reports 14 unused resources.

- [ ] `drawable-nodpi/` holds 18 party logos; `SampleParties` references 5
      (`anc`, `da`, `eff`, `ifp`, `actionsa`). Either finish the party list or delete
      the other 13
- [ ] `drawable/success_check.jpg` is referenced nowhere — delete
- [ ] `IconLocation` warning: a bitmap sitting in density-independent `drawable/`
      (same file, so deleting it clears this too)
- [ ] `IconXmlAndPng` warning: `ic_launcher` exists as both `.xml` and `.webp`
      in `mipmap-xxxhdpi` — pick one

## 3. Extract hardcoded strings

`res/values/strings.xml` has 2 entries; every other piece of UI copy is inline in
Kotlin.

- [ ] Move user-facing text to `strings.xml`
- [ ] **The language pill in `HomeHeader` currently promises something the app
      cannot do** — "EN ⌄" implies a language picker, but with hardcoded strings
      there is nothing to switch to. Either wire up real locales
      (`values-af/`, `values-zu/`, `values-xh/`) or drop the pill until it works
- [ ] Add `tools:locale` and check RTL layout — `supportsRtl="true"` is already
      set in the manifest, so it is claimed but untested

## 4. Replace the test stubs

`app/src/test` and `app/src/androidTest` contain only generated examples.

- [ ] Unit tests: `RegistrationViewModel` state transitions, `sampleLookup`
      receipt matching
- [ ] UI tests worth having: registration back navigation, the three `HeroState`
      renders on home, party selection → review passing the right party

## 5. Build a real Settings screen

`Screen.Settings` renders `InfoScreen` as a placeholder, but the bottom bar
advertises it as its own destination.

- [x] Decide what belongs there (language, notifications, sign out, about)
- [x] Implemented SettingsScreen with Account, Language, Notifications, Theme, Biometrics, Privacy and Log Out options

## 6. Dependency updates (low priority)

Lint flags these; none are urgent and each risks churn.

- [ ] `coreKtx` 1.10.1 — well behind
- [ ] AGP and Gradle wrapper versions flagged as obsolete
- [ ] Check before upgrading whether anything depends on current behaviour

---

## Smaller things noticed

- [ ] `ui/Components/` uses a capital-C directory name while every other package
      is lowercase — inconsistent, and an IDE rename is cheap
- [ ] `AppFontFamily` in `ui/theme/Type.kt` is `FontFamily.SansSerif`. Dropping a
      `.ttf` into `res/font/` and changing that one line moves the whole app onto
      a brand typeface
- [ ] `Screen.Review` is a `data object`, with the selected party held in graph
      state. A `data class Review(val partyId: String)` would survive process
      death — see the Obsidian note "Jetpack Compose Navigation - NavHost Recipe"
