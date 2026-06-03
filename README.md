# imageeditor

A Compose-first, WeChat-style image editor for Android: **crop / rotate, freehand
draw, mosaic, text, and stickers** — with a single entry composable and a clean,
business-agnostic input/output contract.

It fills a real gap in the ecosystem: a Jetpack Compose editor with a built-in
**mosaic brush** (most maintained libraries either are View-based, lack mosaic,
or are unmaintained).

## Features

- ✂️ **Crop / rotate / flip** — powered by [CanHub/Android-Image-Cropper](https://github.com/CanHub/Android-Image-Cropper) (runs first, skippable)
- ✏️ **Draw** — color palette, brush width, eraser
- ▦ **Mosaic** — self-implemented pixelate brush (downsampled `BitmapShader` along the stroke)
- 🅣 **Text** — draggable / scalable / rotatable layers, double-tap to edit
- 😀 **Sticker** — host-supplied assets via `StickerProvider`
- ↩ **Undo** — single button, coordinated across annotation and mosaic
- EXIF orientation handled on decode; output flattened in one pass

Drawing/text/sticker are powered by [burhanrashid52/PhotoEditor](https://github.com/burhanrashid52/PhotoEditor),
bridged into Compose via `AndroidView`.

## Install

### Via Maven Central

```kotlin
implementation("io.github.robeshell:compose-image-editor:0.1.0")
```

> Requires the maintainer to have published the release — see [PUBLISHING.md](./PUBLISHING.md).

### Via JitPack

Add JitPack to your repositories (`settings.gradle.kts`):

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
}
```

Then depend on the library:

```kotlin
implementation("com.github.robeshell:compose-image-editor:0.1.0")
```

### As a Gradle module / git submodule

Alternatively include the source as a module (e.g. via git submodule). The
library lives in the `imageeditor/` subdirectory of this repo, so point your
host `settings.gradle.kts` at it:

```kotlin
include(":imageeditor")
project(":imageeditor").projectDir = file("path/to/compose-image-editor/imageeditor")
```

```kotlin
implementation(project(":imageeditor"))
```

### ⚠️ Required setup — AppCompat theme for the crop activity

The crop screen (`com.canhub.cropper.CropImageActivity`) extends
`AppCompatActivity` and renders its **Done / rotate actions in the system
ActionBar**. You **must** declare it in your app manifest with an AppCompat
theme that **has an ActionBar**, otherwise it either crashes
(`You need to use a Theme.AppCompat theme`) or shows the crop frame with no
buttons:

```xml
<activity
    android:name="com.canhub.cropper.CropImageActivity"
    android:theme="@style/Theme.AppCompat.Light.DarkActionBar" />
```

### Transitive dependency note

This module pulls `androidx.appcompat` (via the cropper), which may **bump
`androidx.core` / `androidx.activity`** to newer versions than your app
currently resolves. Watch for signature changes such as
`Activity.onRequestPermissionsResult(..., permissions: Array<String>, ...)`.

## Run the sample

A runnable demo lives in `:sample` (pick an image → edit → see the result):

```bash
./gradlew :sample:installDebug
```

## Building from source

This is a standard Android Gradle project. Cloning it standalone requires a
`local.properties` at the repo root pointing to your Android SDK (Android Studio
creates this automatically on first open):

```properties
sdk.dir=/path/to/Android/sdk
```

## Usage

`ImageEditorScreen` is a composable — place it in your navigation or as a
full-screen overlay. It takes an `ImageEditorRequest` and reports back through
`onResult`. It knows nothing about your app, sending, or networking — it only
produces a local image `Uri`.

```kotlin
var editingUri by remember { mutableStateOf<Uri?>(null) }

editingUri?.let { uri ->
    ImageEditorScreen(
        request = ImageEditorRequest(
            sourceUri = uri,
            config = ImageEditorConfig(
                theme = EditorTheme(primary = Color(0xFF1E6FFF)),
                // optional: stickerProvider, brushColors, mosaicBlockSize, output, tools...
            ),
        ),
        onResult = { result ->
            editingUri = null
            when (result) {
                is ImageEditorResult.Success -> handleEditedImage(result.uri)
                is ImageEditorResult.Cancelled -> { /* user backed out */ }
                is ImageEditorResult.Failed -> showError(result.cause)
            }
        },
    )
}
```

### Configuration (`ImageEditorConfig`)

| Field | Default | Purpose |
|---|---|---|
| `tools` | all | Which tools to show, and order; include `CROP` to enable the crop pre-stage |
| `cropFirst` | `true` | Launch crop before annotation (skippable by cancelling the cropper) |
| `brushColors` | 8-color palette | Draw / text palette |
| `brushWidths` | `[8, 16, 28]` | Brush width steps (px) |
| `mosaicBlockSize` | `24` | Mosaic block edge (px); larger = blockier |
| `stickerProvider` | `null` | Supplies sticker `Uri`s; `null` hides the sticker tool |
| `output` | JPEG q95 | Output format / quality / target dir (defaults to cacheDir) |
| `theme` | blue | Toolbar colors |

### Result

`ImageEditorResult` is `Success(uri)`, `Cancelled`, or `Failed(cause)`. The
output `Uri` is a local file in the configured directory (or `cacheDir`). The
exported image already has correct pixel orientation (no EXIF rotation tag).

## Known limitations

- **Mosaic is drag-only** — a single tap does not paint (uses drag gestures).
- **Undo only, no redo.**
- **Text has no outline** — light text on light photos can be hard to read.
- **Stickers are host-provided** — the module ships no assets.

## Roadmap

- [x] Sample app
- [x] JitPack publishing
- [x] Unit tests (EXIF, mosaic coordinate mapping, undo routing)
- [x] Text shadow for readability
- [x] Mosaic tap support + redo
- [~] Maven Central publishing — configured; needs a Sonatype namespace + GPG key to release (see [PUBLISHING.md](./PUBLISHING.md))

## License

[MIT](./LICENSE).

Built on [CanHub/Android-Image-Cropper](https://github.com/CanHub/Android-Image-Cropper)
(Apache-2.0) and [burhanrashid52/PhotoEditor](https://github.com/burhanrashid52/PhotoEditor)
(MIT). The mosaic brush is an original implementation.
