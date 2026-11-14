# AxolotlRendering (Makar's Fork)

Fork of [AxolotlClient-rendering](https://codeberg.org/AxolotlClient/AxolotlClient-rendering) with a lightweight SVG pipeline, modern graphical effects (LiquidGlass & GaussianBlur), and full Forge / NeoForge / Fabric multi-loader support across Minecraft versions from 1.8.9 to 1.21+.

---

## What's Changed in this Fork

- **Lightweight SVG Engine**: Completely cut out Apache Batik (which dragged 16+ jar dependencies and ~10 MB of bloat) and replaced it with **[JSVG](https://github.com/weisJ/jsvg)** — an ultra-lightweight (~580 KB), pure-Java vector graphics engine with zero external dependencies and crisp antialiasing.
- **LiquidGlass Shader Effect**: Added Apple/macOS-style fluid frosted glass rendering (`axolotlclient_rendering$liquidGlass`), complete with backdrop sampling, normal-based edge refraction, chromatic aberration (RGB dispersion), directional specular rim highlights, and antialiased rounded borders.
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
- **LiquidGlass**:
  - Frosted glassmorphism panels with customizable tint, border color, outline width, blur radius, and refraction distortion.
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

### 1.8.9 – 1.12.2 (Forge & Fabric)
Use `DrawUtil.get()` to access the rendering interface:

```java
import io.github.axolotlclient.rendering.DrawUtil;

var draw = DrawUtil.get();

// Rounded rectangle with 8px corner radius
draw.axolotlclient_rendering$roundedRect(50, 50, 200, 120, 0xCC1E1E2E, 8f);

// Gaussian blur backdrop under a panel
draw.axolotlclient_rendering$roundedGaussianBlur(50, 50, 200, 120, 8f, 6f, 3.5f);

// LiquidGlass panel (frosted glass, refraction, chromatic aberration, specular rim)
draw.axolotlclient_rendering$liquidGlass(
    50, 50, 200, 120,   // x, y, width, height
    10f,                // corner rounding
    0x25FFFFFF,         // frosted tint color
    0x60FFFFFF,         // rim highlight color
    1.2f,               // border width
    5.0f,               // blur radius
    4.0f                // refraction distortion
);

// SVG vector image
draw.axolotlclient_rendering$blitSVG(new Identifier("mymod", "textures/gui/icon.svg"), 60, 60, 32, 32, -1);
```

### 1.20.1+ (Forge, NeoForge, Fabric)
`GuiGraphics` automatically implements `GuiGraphicsExtension`:

```java
// Inside any screen render(...) or HUD render callback:
guiGraphics.axolotlclient_rendering$roundedRect(x, y, x + width, y + height, 0xDD181825, 6f);

// LiquidGlass card
guiGraphics.axolotlclient_rendering$liquidGlass(
    x, y, width, height, 8f, 0x20FFFFFF, 0x40FFFFFF, 1.0f, 6.0f, 4.0f
);

// Gaussian backdrop blur
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

- This fork is licensed under the **GNU Lesser General Public License v3.0 or later** ([LGPL-3.0-or-later](LICENSE)).
- Original codebase: [AxolotlClient-rendering](https://codeberg.org/AxolotlClient/AxolotlClient-rendering) by [moehreag](https://github.com/moehreag) and contributors.
- SVG rendering powered by [JSVG](https://github.com/weisJ/jsvg) by Jannis Weis (MIT License).
