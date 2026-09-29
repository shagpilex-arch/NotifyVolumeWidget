# Notif Volume — home screen widget

A single-purpose Android home screen widget. No app icon, no settings
screen, no other features:

- **Off** button — sets notification volume to 0%
- **−** / **+** with a bar — moves the notification volume in clean 10%
  steps (0, 10, 20 ... 100), never a value in between

The stepper always rounds to the nearest 10% first, then moves a further
10% from there — so from 58% it reads as 60% and **−** takes it to 50%;
from 54% it reads as 50% and **−** takes it to 40%. This only changes the
**notification** volume stream. It never touches ringer mode, silent mode,
vibrate or Do Not Disturb, and needs no special permission for that reason.

## Design notes

**A stepper, not a drag-slider.** Android's widget system (`RemoteViews`)
cannot host a slider you drag — the only draggable-style inputs it supports
are checkboxes, switches and radio buttons. The −/+ stepper reaches every
level by taps, which the platform does support.

**No app icon.** There is no launcher activity; find the widget in the
widget picker (long-press home screen → Widgets). The app still appears in
Settings → Apps.

**Button styling.** The buttons are plain `TextView`s with their own
background and centered text, not platform `<Button>`s. A `<Button>` inside
a widget is drawn using the *launcher's* theme, not this app's, so its
padding and text position vary by device/launcher and isn't reliably
centered — that's what made the buttons look off in the first version.

**Security.** The code that changes the volume lives in a
`BroadcastReceiver` that is *not* exported, reachable only through this
app's own PendingIntents. Another installed app cannot address it to change
your volume. The widget-drawing component is a separate exported receiver
(widgets must be) but never touches audio settings.

**The one edge case you might hit.** On the large majority of phones,
notification volume is independent of the ringer, and changing it needs no
permission at all — this is Android's default behaviour and what the app
assumes. On a minority of phones, the OS links notification volume to the
ringer/Do Not Disturb state, and lowering it (especially to exactly 0) can
be refused unless the app has Do Not Disturb access. Rather than asking
every user to grant this up front for a small minority who need it, the
app declares the permission (so it appears in Settings → Notifications →
Special app access → Do Not Disturb access, if you ever need to switch it
on) but never asks for it. If **Off**/**−** ever visibly does nothing, a
toast explains this and points at that Settings screen; everyone else will
never see it.

## Getting the APK

**A. GitHub Actions — no local tools needed.** Create a GitHub repository
from this folder and push. The *Build APK* workflow
(`.github/workflows/build-apk.yml`) builds it; open the run under the
**Actions** tab and download the `notif-volume-debug-apk` artifact
(`app-debug.apk`). You can also start it manually via *Run workflow*.

**B. Android Studio.** Open this folder, let it sync, then
Build → Build Bundle(s)/APK(s) → Build APK(s). The APK is written to
`app/build/outputs/apk/debug/app-debug.apk`.

**C. Command line.** With JDK 17+ and the Android SDK installed
(`ANDROID_HOME` set): `./gradlew assembleDebug`
(Windows: `gradlew.bat assembleDebug`).

Copy the APK to your phone and install it (you'll need to allow installs
from the app you open it with). The debug APK is signed with the standard
debug key, which is fine for personal use. Then long-press the home screen
→ Widgets → **Notif Volume**.

## Versions

minSdk 26 (Android 8.0), compileSdk/targetSdk 36 (Android 16) · Android
Gradle Plugin 8.13.2 · Kotlin 2.1.20 · Gradle 8.13 · JDK 17. No library
dependencies. `ACCESS_NOTIFICATION_POLICY` is declared but never requested
by the app itself (see "the one edge case" above).

## Status

The Kotlin has been compiled (warnings as errors) with Kotlin 2.1.20 and
2.3.0 against the Android API 34 stubs, all XML/resource references have
been cross-checked, and the rounding/stepping logic in `VolumeLevels.kt`
has been run standalone against the exact examples above plus a range of
realistic (coarse) device volume ranges. The full Gradle/AGP build and
on-device appearance/behaviour have **not** been exercised yet.
