# AxolotlClient Rendering

Small(-ish) rendering library integrated into Minecraft's rendering

Current features:

- Shader-based rendering of rounded rectangles (and circles)
	- Also textured & outlined
- Convenient rendering of TrueType fonts using FreeType
- Convenient rendering of static SVG images using Apache Batik

This library is published to our maven:
https://moehreag.duckdns.org/maven/#/releases/io/github/axolotlclient/rendering/AxolotlClient-rendering

### Examples

Examples on how to use the library can be found in the respective test source set for each version.

#### General Usage:
On 1.8.9, use `DrawUtil.get()` to obtain an instance.
Otherwise, use `GuiGraphics` directly and the interface methods from
`GuiGraphicsExtension` (prefixed with `axolotlclient_rendering`)

Fonts can be loaded using `Font.read(...)`. If you're using a variant font
this will automatically read the variants and make use of them appropriately,
however the font features supported by FreeType directly are rather limited.
If a specific feature is provided by a separate font file (for example an *italic* or
**bold** variant) they can be added using `Font#addSubFont`, specifying in which conditions it should be used
(some tags and values can be found as static definitions in the `Font` class).

This library includes the *[Inter](https://rsms.me/inter)* font by default.

### TODO

- svg rasterization with fade-in after so there aren't any jarring jumps
  (currently it pops in, which is mostly only noticeable on larger/more complicated svgs)

