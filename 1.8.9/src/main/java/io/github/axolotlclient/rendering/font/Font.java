package io.github.axolotlclient.rendering.font;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tessellator;
import io.github.axolotlclient.rendering.ExRenderPipelines;
import io.github.axolotlclient.rendering.RenderingCommon;
import io.github.axolotlclient.rendering.StyleExtension;
import io.github.axolotlclient.rendering.util.ARGB;
import it.unimi.dsi.fastutil.floats.Float2ObjectMap;
import it.unimi.dsi.fastutil.floats.Float2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.longs.*;
import it.unimi.dsi.fastutil.objects.*;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.Window;
import net.minecraft.client.render.texture.AbstractTexture;
import net.minecraft.client.resource.manager.ResourceManager;
import net.minecraft.resource.Identifier;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.PointerBuffer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.freetype.FT_Face;
import org.lwjgl.util.freetype.FT_MM_Var;
import org.lwjgl.util.freetype.FT_Vector;
import org.lwjgl.util.freetype.TT_OS2;

import static org.lwjgl.util.freetype.FreeType.*;

@SuppressWarnings({"resource", "DataFlowIssue", "unused"})
@Slf4j
public class Font implements AutoCloseable {

	public static final long TAG_WEIGHT = makeVariantTag('w', 'g', 'h', 't');
	public static final long TAG_WIDTH = makeVariantTag('w', 'h', 't', 'h');
	public static final long TAG_ITALIC = makeVariantTag('i', 't', 'a', 'l');
	public static final float WEIGHT_REGULAR = 400;
	public static final float WEIGHT_BOLD = 700;
	public static final float ITALIC_ENABLED = 1;
	public static final float ITALIC_DISABLED = 0;
	private static final List<WeakReference<Font>> fonts = new ArrayList<>();

	@ApiStatus.Internal
	public static void reload() {
		Font.fonts.removeIf(w -> w.refersTo(null));
		Font.fonts.forEach(w -> {
			Font f = w.get();
			f.clearPageMap();
			f.loadGlyphDimensions();
		});
	}

	public static void onScaleChange() {
		window = new Window(Minecraft.getInstance());
		Font.fonts.removeIf(w -> w.refersTo(null));
		Font.fonts.forEach(w -> {
			Font font = w.get();
			font.resizeInternal(font.mcSize);
		});
	}

	public static void closeAll() {
		Font.fonts.removeIf(w -> w.refersTo(null));
		Font.fonts.forEach(w -> w.get().close());
	}

	private static Window window = new Window(Minecraft.getInstance());
	private static long ftLib;
	private static int ftRefCount;
	private static final Object LIBRARY_LOCK = new Object();
	private static final Random RANDOM = new Random();

	private static long getLibrary() {
		synchronized (LIBRARY_LOCK) {
			if (ftLib == 0L) {
				try (MemoryStack memoryStack = MemoryStack.stackPush()) {
					PointerBuffer buf = memoryStack.mallocPointer(1);
					if (FT_Init_FreeType(buf) != FT_Err_Ok) {
						throw new IllegalStateException("initializing FreeType lib");
					}
					ftLib = buf.get();
				}
			}
			return ftLib;
		}
	}

	/**
	 * Create a font from this file.
	 *
	 * @param font the font file
	 * @param size the font size in pixels
	 * @throws IOException if an I/O exception occurs
	 */
	public static Font read(Path font, int size) throws IOException {
		var in = Files.readAllBytes(font);
		return read(in, size);
	}

	/**
	 * Create a font from this file.
	 *
	 * @param font the font file
	 * @param size the font size in pixels
	 * @throws IOException if an I/O exception occurs
	 */
	public static Font read(InputStream font, int size) throws IOException {
		return read(font.readAllBytes(), size);
	}

	/**
	 * Create a font from this file.
	 *
	 * @param font the font file
	 * @param size the font size in pixels
	 * @throws IOException if an I/O exception occurs
	 */
	public static Font read(byte[] font, int size) throws IOException {
		var buf = MemoryUtil.memAlloc(font.length);
		buf.put(font).flip();
		return read(buf, size);
	}

	/**
	 * Create a font from this file.
	 *
	 * @param font the font file. <strong>This memory will be freed on {@link #close()}</strong>.
	 * @param size the font size in pixels
	 * @throws IOException if an I/O exception occurs
	 */
	public static Font read(ByteBuffer font, int size) throws IOException {
		return new Font(font, true, size, 0);
	}

	ByteBuffer fontMem;
	FT_Face face;
	private boolean hasKerningInfo;
	private int mcSize;
	private int lineHeight;
	private final Long2ObjectMap<ObjectFloatPair<VariationAxis>> variations = new Long2ObjectLinkedOpenHashMap<>();
	private final StringSplitter splitter = new StringSplitter(this::getWidth);
	private final ObjectList<SubFont> subFonts = new ObjectArrayList<>();
	private final Float2ObjectMap<IntList> glyphsByWidth = new Float2ObjectOpenHashMap<>();
	private Font activeSubFont = this;

	private final Long2ObjectMap<Page> pageMap = new Long2ObjectOpenHashMap<>();

	Font(ByteBuffer font, boolean ownsFontMem, int size, long faceIndex) throws IOException {
		fonts.add(new WeakReference<>(this));
		this.fontMem = ownsFontMem ? font : null;
		try (var stack = MemoryStack.stackPush()) {
			var faceBuf = stack.mallocPointer(1);
			var error = FT_New_Memory_Face(getLibrary(), font, faceIndex, faceBuf);
			if (error != FT_Err_Ok) {
				if (error == FT_Err_Unknown_File_Format) {
					throw new IOException("Unknown format for font");
				}
				throw new IOException("Error reading font");
			}
			face = FT_Face.create(faceBuf.get());
			if (!"TrueType".equals(FT_Get_Font_Format(face))) {
				FT_Done_Face(face);
				throw new IOException("Unsupported font format");
			}

			if (FT_HAS_MULTIPLE_MASTERS(face)) {
				var mmVarBuf = stack.mallocPointer(1);
				FT_Get_MM_Var(face, mmVarBuf);
				var variants = FT_MM_Var.create(mmVarBuf.get());

				for (var a : variants.axis()) {
					variations.put(a.tag(), new ObjectFloatMutablePair<>(new VariationAxis(true, f16Dot16ToFP32(a.minimum()), f16Dot16ToFP32(a.def()), f16Dot16ToFP32(a.maximum()), a.tag(), a.strid()), a.def()));
				}
				synchronized (LIBRARY_LOCK) {
					FT_Done_MM_Var(getLibrary(), variants);
				}
				if (hasVariant(TAG_WEIGHT)) {
					setVariant(TAG_WEIGHT, WEIGHT_REGULAR);
				}
			}

			if (FT_Select_Charmap(face, FT_ENCODING_UNICODE) != FT_Err_Ok) {
				FT_Done_Face(face);
				throw new IOException("No unicode charmap in font");
			}

			try {
				resize(size);
			} catch (RuntimeException e) {
				FT_Done_Face(face);
				throw e;
			}

			ftRefCount++;
		}
	}

	public static int makeVariantTag(String tag) {
		if (tag.length() != 4) {
			throw new IllegalArgumentException("Tags must have 4 characters");
		}
		return makeVariantTag(tag.charAt(0), tag.charAt(1), tag.charAt(2), tag.charAt(3));
	}

	public static int makeVariantTag(char x1, char x2, char x3, char x4) {
		return FT_MAKE_TAG(x1, x2, x3, x4);
	}

	private static float f16Dot16ToFP32(final long value) {
		return (float) ((1.0 / (1 << 16)) * (double) value);
	}

	private static long fp32ToF16Dot16(final float value) {
		return (long) (value * (1 << 16));
	}

	@SuppressWarnings("UnusedReturnValue")
	public Font addSubFont(long tag, float value, Font font) {
		var map = new Long2FloatOpenHashMap();
		map.put(tag, value);
		return addSubFont(map, font);
	}

	public Font addSubFont(Long2FloatMap variants, Font font) {
		for (var s : subFonts) {
			if (s.variant().equals(variants)) {
				throw new IllegalArgumentException("subfont with variant already registered!");
			}
		}
		subFonts.add(new SubFont(variants, font));
		return this;
	}

	private Font getSubFont(long tag, float value) {
		return getSubFont(Long2FloatMaps.singleton(tag, value));
	}

	private Font getSubFont(Long2FloatMap variant) {
		for (var s : subFonts) {
			if (s.variant().equals(variant)) {
				return s.font();
			}
		}
		return this;
	}

	private Long2FloatMap getSubFontProperties(Font font) {
		for (var s : subFonts) {
			if (s.font == font) {
				return s.variant();
			}
		}
		return null;
	}

	private void setVariants() {
		try (var stack = MemoryStack.stackPush()) {
			var buf = stack.mallocCLong(variations.size());
			variations.values().forEach(p -> buf.put(fp32ToF16Dot16(p.valueFloat())));
			buf.flip();
			FT_Set_Var_Design_Coordinates(face, buf);
		}
		lineHeight = (int) (FT_MulFix(face.height(), face.size().metrics().y_scale()) / 64);
	}

	public void setVariant(long tag, float value) {
		setVariantInternal(tag, value);
		setVariants();
	}

	public boolean hasVariant(long tag) {
		return variations.containsKey(tag);
	}

	public float getVariant(long tag) {
		var entry = variations.get(tag);
		if (entry == null) return 0;
		return entry.valueFloat();
	}

	private void setVariantInternal(long tag, float value) {
		for (var e : variations.values()) {
			if (e.key().tag() == tag) {
				e.value(value);
				break;
			}
		}
	}

	public void resetVariants() {
		if (FT_IS_VARIATION(face)) {
			for (var e : variations.values()) {
				e.value(e.key().def());
			}
			setVariants();
		}
	}

	private long getPageIndex(int no) {
		int variation = Arrays.hashCode(variations.values().stream().mapToLong(p -> fp32ToF16Dot16(p.valueFloat())).toArray());
		return (long) variation << 32 | no;
	}

	public void resize(int mcSize) {
		subFonts.forEach(f -> f.font.resize(mcSize));
		resizeInternal(mcSize);
	}

	private void resizeInternal(int mcSize) {
		this.mcSize = mcSize;
		var size = mcSize * window.getScale();
		if (FT_Set_Pixel_Sizes(face, 0, size) != FT_Err_Ok) {
			throw new RuntimeException("Can't set font size " + size);
		}

		lineHeight = (int) (FT_MulFix(face.height(), face.size().metrics().y_scale()) / 64);

		hasKerningInfo = FT_HAS_KERNING(face);

		clearPageMap();
		loadGlyphDimensions();
	}

	private void loadGlyphDimensions() {
		for (var i = 0; i < 0xFFF; i += 256) {
			getPage(i, false);
		}
	}

	private void clearPageMap() {
		for (var page : pageMap.values()) {
			Minecraft.getInstance().getTextureManager().close(page.tex);
		}
		pageMap.clear();
		glyphsByWidth.clear();
	}

	@Override
	public void close() {
		subFonts.forEach(s -> s.font.close());
		subFonts.clear();
		clearPageMap();

		synchronized (LIBRARY_LOCK) {
			if (face != null) {
				FT_Done_Face(face);
				face = null;
			}
			if (--ftRefCount == 0) {
				FT_Done_FreeType(ftLib);
				ftLib = 0;
			}
		}
		if (fontMem != null) {
			MemoryUtil.memFree(fontMem);
			fontMem = null;
		}
	}

	public void renderText(String text, float x, float y, int color, boolean shadow, int alignFlags) {
		var built = buildText(color, shadow, s -> StringDecomposer.iterateFormatted(text, new Style(), s));
		renderTextCommon(x, y, alignFlags, built);
	}

	public void renderText(String text, float x, float y, int color, boolean shadow) {
		renderText(text, x, y, color, shadow, 0);
	}

	public void renderText(String text, float x, float y, int color) {
		renderText(text, x, y, color, true);
	}

	public void renderText(Text text, float x, float y, int color, boolean shadow, int alignFlags) {
		var built = buildText(color, shadow, s -> StringDecomposer.iterateFormatted(text, new Style(), s));
		renderTextCommon(x, y, alignFlags, built);
	}

	public void renderText(Text text, float x, float y, int color, boolean shadow) {
		renderText(text, x, y, color, shadow, 0);
	}

	public void renderText(Text text, float x, float y, int color) {
		renderText(text, x, y, color, true);
	}

	public float width(String text) {
		return activeSubFont.splitter.stringWidth(text);
	}

	public float width(Text text) {
		return activeSubFont.splitter.stringWidth(text);
	}

	public int lineHeight() {
		return activeSubFont.lineHeight;
	}

	private float getWidth(int codePt, Style style) {
		updateVariant(style);
		var page = getPage(codePt, false);
		var c = page.charInfo[codePt & 0xFF];
		return c.advanceX;
	}

	private BuiltText buildText(int color, boolean shadow, Consumer<FormattedCharSink> sink) {
		final float[] penX = {0};
		final float[] penY = {0};
		var screenAndTexCoords = new Object2ObjectOpenHashMap<Identifier, List<CoordData>>();
		var effects = new ObjectArrayList<EffectData>();
		var fontBox = new float[]{Float.MAX_VALUE, Float.MAX_VALUE, Float.MIN_VALUE, Float.MIN_VALUE};

		final int[] prevGlyphI = {0};

		sink.accept((index, style, codePt) -> {
			if (codePt == '\n') {
				penX[0] = 0;
				penY[0] += lineHeight();
				prevGlyphI[0] = 0;
				return true;
			} else if (codePt == '\r') {
				penX[0] = 0;
				prevGlyphI[0] = 0;
				return true;
			}

			updateVariant(style);
			var styleColor = ((StyleExtension) style).axolotlclient_rendering$getColor();
			int textColor;
			if (styleColor != null) {
				textColor = ARGB.color(ARGB.alpha(color), styleColor.getValue());
			} else if (style.getColor() != null && style.getColor().isColor()) {
				textColor = ARGB.color(ARGB.alpha(color), TextRenderUtils.getColor(style.getColor()));
			} else textColor = color;

			int shadowColor = shadow ? ARGB.scaleRGB(textColor, 0.25f) : 0;

			var page = getPage(codePt, true);

			var c = page.charInfo[codePt & 0xFF];

			float charAdvanceX = c.advanceX;
			float charAdvanceY = c.advanceY;
			int charX0 = c.x0;
			int charY0 = c.y0;
			int charX1 = c.x1;
			int charY1 = c.y1;
			if (style.isObfuscated() && codePt != ' ') {
				var codePts = activeSubFont.glyphsByWidth.get(charAdvanceX);
				codePt = codePts.getInt(RANDOM.nextInt(codePts.size()));
				page = getPage(codePt, true);

				c = page.charInfo[codePt & 0xFF];
			}

			if (hasKerningInfo && prevGlyphI[0] > 0 && c.glyphI > 0) {
				try (FT_Vector kerning = FT_Vector.create()) {
					FT_Get_Kerning(activeSubFont.face, prevGlyphI[0], c.glyphI, FT_KERNING_DEFAULT, kerning);
					penX[0] += kerning.x() / 64f;
					penY[0] = kerning.y() / 64f;
				}
			}

			var list = screenAndTexCoords.computeIfAbsent(page.tex, unused -> new ObjectArrayList<>());

			var hasShadow = ARGB.alpha(shadowColor) != 0;
			var shadowOffset = 1f * window.getScale();
			if (hasShadow) {
				list.add(new CoordData(penX[0] + charX0 + shadowOffset, penY[0] - charY0 + shadowOffset,
					c.u0, c.v0, c.u1, c.v1,
					c.width(), c.height(), shadowColor));
			}

			list.add(new CoordData(penX[0] + charX0, penY[0] - charY0,
				c.u0, c.v0, c.u1, c.v1,
				c.width(), c.height(), textColor));

			if (style.isStrikethrough() || style.isUnderlined()) {
				var effectX = penX[0];
				if (style.isStrikethrough()) {
					var table = FT_Get_Sfnt_Table(activeSubFont.face, FT_SFNT_OS2);
					var strikeoutpos = lineHeight() / 2f + shadowOffset;
					var strikeoutThicknessFT = activeSubFont.face.underline_thickness();
					if (table != 0) {
						var t = TT_OS2.create(table);
						strikeoutThicknessFT = t.yStrikeoutSize();
						strikeoutpos = FT_MulFix(t.yStrikeoutPosition(), activeSubFont.face.size().metrics().y_scale()) / 64f;
					}
					var strikeoutThickness = FT_MulFix(strikeoutThicknessFT, activeSubFont.face.size().metrics().y_scale()) / 64f;
					if (hasShadow) {
						effects.add(new EffectData(effectX + shadowOffset, penY[0] - strikeoutpos + shadowOffset,
							charAdvanceX, strikeoutThickness, shadowColor));
					}
					effects.add(new EffectData(effectX, penY[0] - strikeoutpos,
						charAdvanceX, strikeoutThickness, textColor));
				}
				if (style.isUnderlined()) {
					var underlinePos = FT_MulFix(activeSubFont.face.underline_position(), activeSubFont.face.size().metrics().y_scale()) / 64f;
					var underlineThickness = FT_MulFix(activeSubFont.face.underline_thickness(), activeSubFont.face.size().metrics().y_scale()) / 64f;
					if (hasShadow) {
						effects.add(new EffectData(effectX + shadowOffset, penY[0] - underlinePos + shadowOffset,
							charAdvanceX, underlineThickness, shadowColor));
					}
					effects.add(new EffectData(effectX, penY[0] - underlinePos,
						charAdvanceX, underlineThickness, textColor));
				}
			}

			fontBox[0] = Math.min(fontBox[0], penX[0] + charX0);
			fontBox[1] = Math.min(fontBox[1], penY[0] - charY0);
			fontBox[2] = Math.max(fontBox[2], penX[0] + charX1);
			fontBox[3] = Math.max(fontBox[3], penY[0] - charY1);

			penX[0] += charAdvanceX;
			penY[0] += charAdvanceY;

			prevGlyphI[0] = c.glyphI;
			return true;
		});


		return new BuiltText(screenAndTexCoords, effects, fontBox);
	}

	private @NotNull Page getPage(int codePt, boolean forRender) {
		long pgIndex = activeSubFont.getPageIndex(codePt >> 8);
		var page = activeSubFont.pageMap.get(pgIndex);
		if (page == null) {
			page = activeSubFont.loadPageGlyphDimensions(pgIndex);
		}
		if (forRender && !page.rendered) {
			activeSubFont.loadPageForRender(pgIndex, page);
		}
		return page;
	}

	private void updateVariant(Style style) {
		boolean bold = style.isBold();
		boolean italic = style.isItalic();
		var isBold = getVariant(TAG_WEIGHT) == WEIGHT_BOLD;
		var isItalic = getVariant(TAG_ITALIC) == ITALIC_ENABLED;
		var subProps = getSubFontProperties(activeSubFont);
		if (subProps != null) {
			isBold |= subProps.getOrDefault(TAG_WEIGHT, WEIGHT_REGULAR) == WEIGHT_BOLD;
			isItalic |= subProps.getOrDefault(TAG_ITALIC, ITALIC_DISABLED) == ITALIC_ENABLED;
		}
		var variantsChanged = false;
		if (bold && !isBold) {
			if (hasVariant(TAG_WEIGHT)) {
				setVariantInternal(TAG_WEIGHT, WEIGHT_BOLD);
				variantsChanged = true;
			} else {
				activeSubFont = getSubFont(TAG_WEIGHT, WEIGHT_BOLD);
			}
		} else if (!bold && isBold) {
			if (hasVariant(TAG_WEIGHT)) {
				setVariantInternal(TAG_WEIGHT, WEIGHT_REGULAR);
				variantsChanged = true;
			} else {
				activeSubFont = getSubFont(TAG_WEIGHT, WEIGHT_REGULAR);
			}
		}
		if (italic && !isItalic) {
			if (hasVariant(TAG_ITALIC)) {
				setVariantInternal(TAG_ITALIC, ITALIC_ENABLED);
				variantsChanged = true;
			} else {
				activeSubFont = getSubFont(TAG_ITALIC, ITALIC_ENABLED);
			}
		} else if (!italic && isItalic) {
			if (hasVariant(TAG_ITALIC)) {
				setVariantInternal(TAG_ITALIC, ITALIC_DISABLED);
				variantsChanged = true;
			} else {
				activeSubFont = getSubFont(TAG_ITALIC, ITALIC_DISABLED);
			}
		}
		if (variantsChanged) {
			setVariants();
		}
	}

	record BuiltText(Object2ObjectOpenHashMap<Identifier, List<CoordData>> coordData,
					 List<EffectData> effects, float[] textBox) {
	}

	private Page loadPageGlyphDimensions(long pageIndex) {
		var page = new Page();
		pageMap.put(pageIndex, page);
		int tWidth, tHeight;
		var pageNo = (int) pageIndex;
		int rowWidth = 0, maxRowWidth = 0, rowHeight = 0, totalHeight = 0;
		for (int codePt = pageNo << 8; codePt < (pageNo + 1) << 8; codePt++) {
			int tblRow = (codePt >> 4) & 0xF;
			var glyphI = FT_Get_Char_Index(face, codePt);
			if (FT_Load_Glyph(face, glyphI, FT_LOAD_NO_BITMAP | FT_LOAD_BITMAP_METRICS_ONLY) != FT_Err_Ok) {
				log.atError().setMessage(() -> "Error loading glyph for while getting dimensions: {} ({})").addArgument(Integer.toHexString(codePt)).addArgument(Character.toString(codePt)).log();
				continue;
			}

			var slot = face.glyph();
			var bmp = slot.bitmap();

			var c = page.charInfo[codePt & 0xFF] = new CharInfo();
			c.x0 = slot.bitmap_left();
			c.y0 = slot.bitmap_top();
			c.x1 = bmp.width() + slot.bitmap_left();
			c.y1 = slot.bitmap_top() + bmp.rows();
			c.advanceX = slot.advance().x() / 64f;
			c.advanceY = slot.advance().y() / 64f;
			c.glyphI = glyphI;
			glyphsByWidth.computeIfAbsent(c.advanceX, unused -> new IntArrayList()).add(codePt);

			rowWidth += bmp.width() + 2;
			rowHeight = Math.max(bmp.rows(), rowHeight);
			if (((codePt + 1 >> 4) & 0xF) != tblRow) {
				maxRowWidth = Math.max(rowWidth, maxRowWidth);
				rowWidth = 0;
				totalHeight += rowHeight + 2;
				rowHeight = 0;
			}
		}
		tWidth = maxRowWidth + 2;
		tHeight = totalHeight + 2;
		page.texWidth = tWidth;
		page.texHeight = tHeight;
		return page;
	}

	private void loadPageForRender(long pageIndex, Page page) {
		int tWidth = page.texWidth;
		int tHeight = page.texHeight;
		var pageNo = (int) pageIndex;
		page.rendered = true;

		var texData = MemoryUtil.memAlloc(tWidth * tHeight);
		var pointer = MemoryUtil.memAddress(texData);
		MemoryUtil.memSet(pointer, 0, (long) tWidth * tHeight);
		var slot = face.glyph();

		int curX = 2;
		int curY = 2;
		int rowHeight = 0;
		for (int codePt = pageNo << 8; codePt < (pageNo + 1) << 8; codePt++) {
			int tblRow = (codePt >> 4) & 0xF;

			var c = page.charInfo[codePt & 0xFF];
			if (FT_Load_Glyph(face, c.glyphI, FT_LOAD_RENDER) != FT_Err_Ok) {
				log.error("Error loading glyph for render: {} ({})", Integer.toHexString(codePt), Character.toString(codePt));
				continue;
			}
			var bmp = slot.bitmap();
			final int width = bmp.width();
			final int height = bmp.rows();

			var tblImgY = curY;
			var tblImgX = curX;

			c.u0 = (float) (tblImgX) / tWidth;
			c.v0 = (float) (tblImgY) / tHeight;
			c.u1 = (float) (tblImgX + bmp.width()) / tWidth;
			c.v1 = (float) (tblImgY + bmp.rows()) / tHeight;

			curX += width + 2;
			rowHeight = Math.max(rowHeight, height);
			{
				var buffer = bmp.buffer(bmp.width() * bmp.rows());
				if (buffer == null) {
					continue;
				}
				long src = MemoryUtil.memAddress(buffer);
				for (int y = 0; y < height; y++) {
					MemoryUtil.memCopy(src + ((long) y * width), pointer + (tblImgX + (long) (tblImgY + y) * tWidth), width);
				}
			}

			if (((codePt + 1 >> 4) & 0xF) != tblRow) {
				curX = 2;
				curY += rowHeight + 2;
				rowHeight = 0;
			}
		}

		page.tex = new Identifier(RenderingCommon.MODID,
			"font_" + face.family_nameString().replace(" ", "_")
				.toLowerCase(Locale.ROOT) + "/" + face.style_nameString().replace(" ", "_").toLowerCase(Locale.ROOT) + "/page_" + pageIndex);
		var tex = new AbstractTexture() {
			@Override
			public void load(ResourceManager resourceManager) {

			}
		};
		var texI = tex.getGlId();
		GlStateManager.bindTexture(texI);
		GL11.glTexParameteri(0xde1, 33085, 0);
		GL11.glTexParameteri(0xde1, 33082, 0);
		GL11.glTexParameteri(0xde1, 33083, 0);
		GL11.glTexParameterf(0xde1, 34049, 0.0F);

		GL11.glTexImage2D(0xde1, 0, 6408, tWidth, tHeight, 0, 0x1908, 0x1401, (IntBuffer) null);

		GL11.glPixelStorei(3314, 0);

		GL11.glPixelStorei(3316, 0);
		GL11.glPixelStorei(3315, 0);
		GL11.glPixelStorei(3317, 1);
		GL11.glTexSubImage2D(0xde1, 0, 0, 0, tWidth, tHeight, 0x1903, 5121, pointer);

		MemoryUtil.memFree(texData);
		Minecraft.getInstance().getTextureManager().register(page.tex, tex);
	}

	private void renderTextCommon(float x, float y, int alignFlags, BuiltText builtText) {
		float[] startOffset = new float[2];
		var textBox = builtText.textBox();

		int horizAlign = alignFlags & 0x3;
		startOffset[0] = switch (horizAlign) {
			case ORIGIN_HORIZ_LEFT -> textBox[0];
			case ORIGIN_HORIZ_RIGHT -> textBox[2];
			case ORIGIN_HORIZ_CENTER -> textBox[0] + (textBox[2] - textBox[0]) / 2f;
			//case ORIGIN_HORIZ_BASELINE -> {}
			default -> 0;
		};
		var vertAlign = alignFlags & 0xC;
		startOffset[1] = switch (vertAlign) {
			case ORIGIN_VERT_TOP -> textBox[1];
			case ORIGIN_VERT_BOTTOM -> textBox[3];
			case ORIGIN_VERT_CENTER -> textBox[1] + (textBox[3] - textBox[1]) / 2f;
			//case ORIGIN_VERT_BASELINE
			default -> 0;
		};

		GlStateManager.pushMatrix();
		GlStateManager.translatef(x, y + 7, 0);
		GlStateManager.translatef(-startOffset[0], -startOffset[1], 0);
		var scale = (float) window.getScale();
		GlStateManager.scalef(1f / scale, 1f / scale, 1);

		renderTextCommon(builtText);
		GlStateManager.popMatrix();
	}

	private void renderTextCommon(BuiltText builtText) {
		Tessellator tess = Tessellator.getInstance();
		GlStateManager.enableBlend();
		ExRenderPipelines.FONT_PROGRAM.bind();
		var vertexConsumer = tess.getBuilder();
		for (var pageGlyphs : builtText.coordData().object2ObjectEntrySet().stream().skip(0).toList()) {
			Minecraft.getInstance().getTextureManager().bind(pageGlyphs.getKey());
			vertexConsumer.begin(GL11.GL_QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
			for (var g : pageGlyphs.getValue()) {
				var color = g.color();
				vertexConsumer.vertex(g.x(), g.y(), 0).texture(g.u(), g.v())
					.color((color >> 16 & 0xFF) / 255f, (color >> 8 & 0xFF) / 255f,
						(color & 0xFF) / 255f, (color >> 24 & 0xFF) / 255f).nextVertex();
				vertexConsumer.vertex(g.x(), g.y() + g.height(), 0).texture(g.u(), g.v1())
					.color((color >> 16 & 0xFF) / 255f, (color >> 8 & 0xFF) / 255f,
						(color & 0xFF) / 255f, (color >> 24 & 0xFF) / 255f).nextVertex();
				vertexConsumer.vertex(g.x() + g.width(), g.y() + g.height(), 0).texture(g.u1(), g.v1())
					.color((color >> 16 & 0xFF) / 255f, (color >> 8 & 0xFF) / 255f,
						(color & 0xFF) / 255f, (color >> 24 & 0xFF) / 255f).nextVertex();
				vertexConsumer.vertex(g.x() + g.width(), g.y(), 0).texture(g.u1(), g.v())
					.color((color >> 16 & 0xFF) / 255f, (color >> 8 & 0xFF) / 255f,
						(color & 0xFF) / 255f, (color >> 24 & 0xFF) / 255f).nextVertex();
			}
			tess.end();
		}
		ExRenderPipelines.FONT_PROGRAM.unbind();
		GlStateManager.color3f(1, 1, 1);
		GlStateManager.disableTexture();
		if (!builtText.effects().isEmpty()) {
			vertexConsumer.begin(GL11.GL_QUADS, DefaultVertexFormat.POSITION_COLOR);
			for (var e : builtText.effects()) {
				var color = e.color();
				vertexConsumer.vertex(e.x(), e.y(), 0).color((color >> 16 & 0xFF) / 255f,
					(color >> 8 & 0xFF) / 255f, (color & 0xFF) / 255f, (color >> 24 & 0xFF) / 255f).nextVertex();
				vertexConsumer.vertex(e.x(), e.y() + e.height(), 0).color((color >> 16 & 0xFF) / 255f,
					(color >> 8 & 0xFF) / 255f, (color & 0xFF) / 255f, (color >> 24 & 0xFF) / 255f).nextVertex();
				vertexConsumer.vertex(e.x() + e.width(), e.y() + e.height(), 0)
					.color((color >> 16 & 0xFF) / 255f, (color >> 8 & 0xFF) / 255f,
						(color & 0xFF) / 255f, (color >> 24 & 0xFF) / 255f).nextVertex();
				vertexConsumer.vertex(e.x() + e.width(), e.y(), 0).color((color >> 16 & 0xFF) / 255f,
					(color >> 8 & 0xFF) / 255f, (color & 0xFF) / 255f, (color >> 24 & 0xFF) / 255f).nextVertex();
			}
			tess.end();
		}
		GlStateManager.disableBlend();
	}

	public static final int ORIGIN_HORIZ_BASELINE = 0x00;
	public static final int ORIGIN_HORIZ_LEFT = 0x01;
	public static final int ORIGIN_HORIZ_RIGHT = 0x02;
	public static final int ORIGIN_HORIZ_CENTER = 0x03;

	public static final int ORIGIN_VERT_BASELINE = 0x00;
	public static final int ORIGIN_VERT_TOP = 0x04;
	public static final int ORIGIN_VERT_BOTTOM = 0x08;
	public static final int ORIGIN_VERT_CENTER = 0x0C;

	@AllArgsConstructor
	@RequiredArgsConstructor
	static class CharInfo {
		float advanceX, advanceY;
		int x0, y0, x1, y1;
		int glyphI;
		float u0, v0, u1, v1;

		public int width() {
			return x1 - x0;
		}

		public int height() {
			return y1 - y0;
		}
	}

	static class Page {
		Identifier tex;
		CharInfo[] charInfo = new CharInfo[256];
		int texWidth, texHeight;
		boolean rendered;
	}


	public record CoordData(float x, float y, float u, float v, float u1, float v1, int width, int height, int color) {
	}

	public record EffectData(float x, float y, float width, float height, int color) {
	}

	private record SubFont(Long2FloatMap variant, Font font) {
	}

	private record VariationAxis(boolean supported, float min, float def, float max, float tag, int strid) {
	}
}
