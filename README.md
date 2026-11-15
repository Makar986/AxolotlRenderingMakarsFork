# AxolotlRendering (Makar's Fork)

Fork of [AxolotlClient-rendering](https://codeberg.org/AxolotlClient/AxolotlClient-rendering) with a lightweight SVG pipeline, modern graphical effects (ReGlass-grade LiquidGlass & GaussianBlur), high-performance MSDF font rendering, and full Forge / NeoForge / Fabric multi-loader support across Minecraft versions from 1.8.9 to 1.21+.

---

## What's Changed in this Fork

- **Lightweight SVG Engine**: Completely cut out Apache Batik (which dragged 16+ jar dependencies and ~10 MB of bloat) and replaced it with **[JSVG](https://github.com/weisJ/jsvg)** — an ultra-lightweight (~580 KB), pure-Java vector graphics engine with zero external dependencies and crisp antialiasing.
- **Physical LiquidGlass (ReGlass Parity)**: Re-engineered liquid glass rendering based on [ReGlass](https://github.com/RedxAx/ReGlass) optics:
  - Exact box distance gradient (`sdgBox`) surface normals for smooth refractive curvature.
  - Snell's Law optical refraction with physical IOR distortion.
  - Physical RGB chromatic dispersion (aberration) across separate color channels.
  - Exponential Fresnel edge reflections and directional specular glare.
  - Full configuration via the fluent `LiquidGlassStyle` builder.
- **MSDF (Multi-channel Signed Distance Field) Font Engine**:
  - Built-in resolution-independent font rendering using MSDF shaders (`median(r,g,b)` with derivative antialiasing).
  - Bundled default **Montserrat** MSDF font.
  - Dynamic loading of any `msdf-atlas-gen` font directly from `.zip` archives or input streams (`MsdfFont.fromZip(...)`).
  - Subpixel kerning, custom sizing, text measurements, and drop shadow support.
- **GaussianBlur Shader Effect**: Added separable Gaussian blur filter (`axolotlclient_rendering$roundedGaussianBlur`) supporting arbitrary blur radii, sigma control, tinting, and smooth rounded corners.
- **Multi-Loader Compatibility**:
  - **Forge**: 1.8.9, 1.12.2, 1.16.5, 1.20.1
  - **NeoForge**: 1.21.1, 26.1, 26.2, 26.3
  - **Fabric / Quilt / Ornithe**: 1.8.9, 1.12.2, 1.16.5, 1.20.1, 1.21+, 26.x
- **Artifact Size**: Library footprint reduced drastically by stripping heavy XML/Batik runtimes.

---

## Features

- **Shader-based Rounded Geometry**:
  - Rounded rectangles, pills, and circles (`roundedRect`, `circleBlit`)
  - Outline strokes with customizable width (`outlineRoundedRect`)
  - Directional linear color gradients (horizontal & vertical)
  - Variable per-corner rounding (`roundedRectVarying`)
- **LiquidGlass (ReGlass Optics)**:
  - Frosted glassmorphism panels with Snell refraction, chromatic dispersion, Fresnel reflection, and specular glare.
- **MSDF Fonts**:
  - Crisp rendering at 8px, 24px, 72px or higher without pixelation or blurriness.
- **GaussianBlur**:
  - Fast backdrop blur with rounded scissor masks.
- **TrueType Font Rendering**:
  - High quality font rendering backed by FreeType.
  - Bundled with the *[Inter](https://rsms.me/inter)* variable typeface.
- **Vector Graphics (SVG)**:
  - High-performance rasterization to native texture format with automatic caching.

---

## Supported Versions & Subprojects

| Subproject | Minecraft Version | Loader | Direct IDE / CLI Run Task |
|---|---|---|---|
| `:1.8.9.fabric` | **1.8.9** | **Fabric / Ornithe** | `JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew :1.8.9.fabric:runClient` |
| `:1.8.9.forge` | **1.8.9** | **Forge** | `JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew :1.8.9.forge:runClient` |
| `:1.12.2.forge` | **1.12.2** | **Forge** | `JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew :1.12.2.forge:runClient` |
| `:1.16.5.forge` | **1.16.5** | **Forge** | `JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew :1.16.5.forge:runClient` |
| `:1.20.fabric` | **1.20.1** | **Fabric** | `JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew :1.20.fabric:runClient` |
| `:1.20.forge` | **1.20.1** | **Forge** | `JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew :1.20.forge:runClient` |
| `:1.21.fabric` | **1.21.1** | **Fabric** | `JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew :1.21.fabric:runClient` |
| `:1.21.neoforge` | **1.21.1** | **NeoForge** | `JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew :1.21.neoforge:runClient` |
| `:26.1.neoforge` | **26.1** | **NeoForge** | `JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew :26.1.neoforge:runClient` |
| `:26.2.neoforge` | **26.2** | **NeoForge** | `JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew :26.2.neoforge:runClient` |
| `:26.3.neoforge` | **26.3** | **NeoForge** | `JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew :26.3.neoforge:runClient` |

### Standalone Version Folders
Every version folder in the repository is explicitly namespaced by its modloader:
- `1.21.4.fabric/` — Minecraft 1.21.4 (Fabric)
- `1.21.11.fabric/` — Minecraft 1.21.11 (Fabric / RenderPipelines)
- `26.1.neoforge/` — Minecraft 26.1 (NeoForge)
- `26.1.x.fabric/` — Minecraft 26.1 (Fabric)
- `26.2.neoforge/` — Minecraft 26.2 (NeoForge)
- `26.2.fabric/` — Minecraft 26.2 (Fabric)
- `26.3.neoforge/` — Minecraft 26.3 (NeoForge)
- `26.3.fabric/` — Minecraft 26.3 (Fabric)
- `1.16.5.fabric/` — Minecraft 1.16.5 (Fabric)
- `1.12.2.fabric/` — Minecraft 1.12.2 (Fabric / Ornithe)

---

## Getting Started (JitPack)

Add the JitPack repository and the desired version artifact to your `build.gradle` or `build.gradle.kts`:

#### Gradle (Kotlin DSL)
```kotlin
repositories {
    maven("https://jitpack.io")
}

dependencies {
    // 1.21.1 NeoForge / Fabric:
    implementation("com.github.Makar986.AxolotlRenderingMakarsFork:1.21.neoforge:v1.0.0")
    // or Fabric:
    // implementation("com.github.Makar986.AxolotlRenderingMakarsFork:1.21.fabric:v1.0.0")

    // 1.20.1 Forge / Fabric:
    // implementation("com.github.Makar986.AxolotlRenderingMakarsFork:1.20.forge:v1.0.0")
    // implementation("com.github.Makar986.AxolotlRenderingMakarsFork:1.20.fabric:v1.0.0")

    // 1.8.9:
    // implementation("com.github.Makar986.AxolotlRenderingMakarsFork:1.8.9.fabric:v1.0.0")
}
```

#### Gradle (Groovy DSL)
```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    // 1.21.1 NeoForge / Fabric:
    implementation 'com.github.Makar986.AxolotlRenderingMakarsFork:1.21.neoforge:v1.0.0'
    // or Fabric:
    // implementation 'com.github.Makar986.AxolotlRenderingMakarsFork:1.21.fabric:v1.0.0'

    // 1.20.1 Forge / Fabric:
    // implementation 'com.github.Makar986.AxolotlRenderingMakarsFork:1.20.forge:v1.0.0'
    // implementation 'com.github.Makar986.AxolotlRenderingMakarsFork:1.20.fabric:v1.0.0'

    // 1.8.9:
    // implementation 'com.github.Makar986.AxolotlRenderingMakarsFork:1.8.9.fabric:v1.0.0'
}
```

---

## Testing & Launching

Each loader/version can be launched directly in your IDE (IntelliJ IDEA run configurations) or via Gradle CLI:

```bash
# Launch Fabric 1.20.1 client directly:
JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew :1.20.fabric:runClient

# Launch Forge 1.20.1 client directly:
JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew :1.20.forge:runClient

# Launch Fabric 1.21.1 client directly:
JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew :1.21.fabric:runClient

# Launch NeoForge 1.21.1 client directly:
JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew :1.21.neoforge:runClient

# Launch 1.8.9 Fabric client directly:
JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew :1.8.9.fabric:runClient
```

### 2. Testing on NeoForge 1.21.1 & Forge Profiles
Every built jar already includes loader metadata (`META-INF/neoforge.mods.toml` for NeoForge, `META-INF/mods.toml` for Forge) along with Mixin configs:

1. Build the mod JAR:
   ```bash
   ./gradlew :1.21.neoforge:build
   # or for Fabric:
   ./gradlew :1.21.fabric:build
   ```
2. Take the resulting jar from the subproject `build/libs/` directory (e.g. `1.21.neoforge/build/libs/axolotlclient-rendering-1.0.0+1.21.1.jar`).
3. Place it into the `mods/` directory of your **NeoForge 1.21.1** profile in Prism Launcher, Modrinth App, CurseForge, or the Minecraft Launcher. NeoForge will detect and load it automatically.
4. For Forge 1.20.1, build with `./gradlew :1.20.forge:build`.

---

## Usage Examples

### 1. LiquidGlass (ReGlass Optics)

```java
import io.github.axolotlclient.rendering.LiquidGlassStyle;

// Configure physical optics
LiquidGlassStyle glass = LiquidGlassStyle.create()
    .tint(0xFFFFFF, 0.15f)               // tint color & opacity
    .refractionThickness(13.15f)         // refraction depth
    .refractionFactor(1.4f)              // Snell index of refraction
    .refractionDispersion(7.0f)          // chromatic dispersion
    .fresnel(39.71f, 0.2f, 0.2f)        // Fresnel range, hardness, factor
    .glare(30.0f, 0.2f, 0.9f)            // Specular glare range, hardness, factor
    .blurRadius(8.0f)                    // Frosted blur radius
    .border(0x80FFFFFF, 1.2f);           // Border rim highlight

// 1.20+ / 1.21+ (GuiGraphics)
guiGraphics.axolotlclient_rendering$liquidGlass(x, y, width, height, 12f, glass);

// 1.8.9 (DrawUtil)
DrawUtil.get().axolotlclient_rendering$liquidGlass(x, y, width, height, 12f, glass);
```

### 2. MSDF Font Rendering

```java
import io.github.axolotlclient.rendering.font.msdf.DefaultMsdfFont;
import io.github.axolotlclient.rendering.font.msdf.MsdfFont;

// Default bundled Montserrat MSDF font
MsdfFont font = DefaultMsdfFont.montserrat();
guiGraphics.axolotlclient_rendering$drawMsdfString(font, "Crisp Text at Any Scale!", x, y, 16f, 0xFFFFFFFF, true);

// Or load any font directly from an msdf-atlas-gen ZIP bundle:
MsdfFont customFont = MsdfFont.fromZip(new File("/path/to/fonts.zip"), "BoldRubik");
guiGraphics.axolotlclient_rendering$drawMsdfString(customFont, "Custom Font from ZIP", x, y, 20f, 0xFF55FF55, true);
```

### 3. Rounded Rectangles & Gaussian Blur

```java
// 1.8.9 – 1.12.2 (DrawUtil)
var draw = DrawUtil.get();
draw.axolotlclient_rendering$roundedRect(50, 50, 200, 120, 0xCC1E1E2E, 8f);
draw.axolotlclient_rendering$roundedGaussianBlur(50, 50, 200, 120, 8f, 6f, 3.5f);

// 1.20+ / 1.21+ (GuiGraphics)
guiGraphics.axolotlclient_rendering$roundedRect(x, y, x + width, y + height, 0xDD181825, 6f);
guiGraphics.axolotlclient_rendering$roundedGaussianBlur(x, y, width, height, 8f, 5f, 3f);
```

---

## Building from Source

Requirements: **JDK 21** or newer.

```bash
git clone https://github.com/Makar986/AxolotlRenderingMakarsFork.git
cd AxolotlRenderingMakarsFork
./gradlew build
```

To publish to your local maven cache (`~/.m2/repository`):

```bash
./gradlew publishToMavenLocal
```

---

## License & Credits

- Licensed under the **GNU Lesser General Public License v3.0 or later** ([LGPL-3.0-or-later](LICENSE)).
- Original codebase: [AxolotlClient-rendering](https://codeberg.org/AxolotlClient/AxolotlClient-rendering) by [moehreag](https://github.com/moehreag) and contributors.
- LiquidGlass optical model inspired by [ReGlass](https://github.com/RedxAx/ReGlass) by RedxAx.
- Vector graphics powered by [JSVG](https://github.com/weisJ/jsvg) by Jannis Weis (MIT License).
