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
  - **NeoForge**: 1.21.1+
  - **Fabric / Quilt**: 1.8.9 – 1.21+
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

## Getting Started

### 1. JitPack (Easiest)

Add the JitPack repository and the dependency to your `build.gradle` or `build.gradle.kts`:

#### Gradle (Kotlin DSL)
```kotlin
repositories {
    maven("https://jitpack.io")
}

dependencies {
    // Replace <mc-version> with 1.8.9, 1.20, 1.21, etc.
    implementation("com.github.Makar986:AxolotlRenderingMakarsFork:1.0.0")
}
```

#### Gradle (Groovy)
```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.Makar986:AxolotlRenderingMakarsFork:1.0.0'
}
```

### 2. GitHub Packages Maven

```kotlin
repositories {
    maven {
        name = "GitHubPackages"
        url = uri("https://maven.pkg.github.com/Makar986/AxolotlRenderingMakarsFork")
        credentials {
            username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_ACTOR")
            password = project.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")
        }
    }
}
```

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
