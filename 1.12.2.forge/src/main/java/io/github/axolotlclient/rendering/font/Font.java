/*
 * Copyright © 2025 moehreag <moehreag@gmail.com> & Contributors
 *
 * This file is part of AxolotlClient (Rendering Mod).
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 * For more information, see the LICENSE file.
 */

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

import com.google.common.io.ByteStreams;
import io.github.axolotlclient.rendering.StyleExtension;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.BufferBuilder;
import io.github.axolotlclient.rendering.ExRenderPipelines;
import io.github.axolotlclient.rendering.Rendering;
import io.github.axolotlclient.rendering.RenderingCommon;
import io.github.axolotlclient.rendering.util.ARGB;
import it.unimi.dsi.fastutil.floats.Float2ObjectMap;
import it.unimi.dsi.fastutil.floats.Float2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.longs.*;
import it.unimi.dsi.fastutil.objects.*;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.math.MathHelper;
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
public class Font implements Closeable {

	public static final long TAG_WEIGHT = makeVariantTag('w', 'g', 'h', 't');
	public static final long TAG_WIDTH = makeVariantTag('w', 'd', 't', 'h');
	public static final long TAG_ITALIC = makeVariantTag('i', 't', 'a', 'l');
	public static final float WEIGHT_REGULAR = 400;
	public static final float WEIGHT_BOLD = 700;
	public static final float ITALIC_ENABLED = 1;
	public static final float ITALIC_DISABLED = 0;
	private static final List<WeakReference<Font>> fonts = new ArrayList<>();

	@ApiStatus.Internal
	public static void reload() {
		Font.fonts.removeIf(w -> w.get() == null);
		Font.fonts.forEach(w -> {
			Font f = w.get();
			f.clearPageMap();
			f.loadGlyphDimensions();
		});
	}

	public static void onScaleChange() {
		window = new ScaledResolution(Minecraft.getMinecraft());
		Font.fonts.removeIf(w -> w.get() == null);
		Font.fonts.forEach(w -> {
			Font font = w.get();
			font.resizeInternal(font.mcSize);
		});
	}

	public static void closeAll() {
		Font.fonts.removeIf(w -> w.get() == null);
		Font.fonts.forEach(w -> w.get().close());
	}

	private static ScaledResolution window = new ScaledResolution(Minecraft.getMinecraft());
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
		byte[] in = Files.readAllBytes(font);
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
		return read(ByteStreams.toByteArray(font), size);
	}

	/**
	 * Create a font from this file.
	 *
	 * @param font the font file
	 * @param size the font size in pixels
	 * @throws IOException if an I/O exception occurs
	 */
	public static Font read(byte[] font, int size) throws IOException {
		ByteBuffer buf = MemoryUtil.memAlloc(font.length);
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
	private int mcSize, size;
	private int lineHeight;
	private final Long2ObjectMap<ObjectFloatPair<VariationAxis>> variations = new Long2ObjectLinkedOpenHashMap<>();
	private final StringSplitter splitter = new StringSplitter(this::getWidth);
	private final ObjectList<SubFont> subFonts = new ObjectArrayList<>();
	private final Int2ObjectMap<Float2ObjectMap<IntList>> glyphsByWidth = new Int2ObjectOpenHashMap<>();
	private Font activeSubFont = this;

	private final Int2ObjectMap<Long2ObjectMap<Page>> pageMap = new Int2ObjectOpenHashMap<>();

	Font(ByteBuffer font, boolean ownsFontMem, int size, long faceIndex) throws IOException {
		fonts.add(new WeakReference<>(this));
		this.fontMem = ownsFontMem ? font : null;
		try (MemoryStack stack = MemoryStack.stackPush()) {
			PointerBuffer faceBuf = stack.mallocPointer(1);
			int error = FT_New_Memory_Face(getLibrary(), font, faceIndex, faceBuf);
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
				PointerBuffer mmVarBuf = stack.mallocPointer(1);
				FT_Get_MM_Var(face, mmVarBuf);
				FT_MM_Var variants = FT_MM_Var.create(mmVarBuf.get());

				for (org.lwjgl.util.freetype.FT_Var_Axis a : variants.axis()) {
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
		Long2FloatMap map = new Long2FloatOpenHashMap();
		map.put(tag, value);
		return addSubFont(map, font);
	}

	public Font addSubFont(Long2FloatMap variants, Font font) {
		for (SubFont s : subFonts) {
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
		for (SubFont s : subFonts) {
			if (s.variant().equals(variant)) {
				return s.font();
			}
		}
		return this;
	}

	private Long2FloatMap getSubFontProperties(Font font) {
		for (SubFont s : subFonts) {
			if (s.font == font) {
				return s.variant();
			}
		}
		return null;
	}

	private void setVariants() {
		try (MemoryStack stack = MemoryStack.stackPush()) {
			org.lwjgl.CLongBuffer buf = stack.mallocCLong(variations.size());
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
		ObjectFloatPair<VariationAxis> entry = variations.get(tag);
		if (entry == null) return 0;
		return entry.valueFloat();
	}

	private void setVariantInternal(long tag, float value) {
		for (ObjectFloatPair<VariationAxis> e : variations.values()) {
			if (e.key().tag() == tag) {
				e.value(value);
				break;
			}
		}
	}

	public void resetVariants() {
		if (FT_IS_VARIATION(face)) {
			for (ObjectFloatPair<VariationAxis> e : variations.values()) {
				e.value(e.key().def());
			}
			setVariants();
		}
	}

	private long getPageIndex(int no) {
		int variation = Arrays.hashCode(variations.values().stream().mapToLong(p -> fp32ToF16Dot16(p.valueFloat())).toArray());
		return (long) variation << 32 | no;
	}

	/**
	 * Set the font size
	 *
	 * @param mcSize the new size, in minecraft scale
	 * @implNote Font sizes are stored in separate atlases and cached for as long as possible
	 */
	public void resize(int mcSize) {
		subFonts.forEach(f -> f.font.resize(mcSize));
		resizeInternal(mcSize);
	}

	public void withSize(int mcSize, Runnable drawer) {
		int origSize = this.mcSize;
		resize(mcSize);
		drawer.run();
		resize(origSize);
	}

	private void resizeInternal(int mcSize) {
		this.mcSize = mcSize;
		size = mcSize * window.getScaleFactor();
		if (FT_Set_Pixel_Sizes(face, 0, size) != FT_Err_Ok) {
			throw new RuntimeException("Can't set font size " + size);
		}

		lineHeight = (int) (FT_MulFix(face.height(), face.size().metrics().y_scale()) / 64);

		hasKerningInfo = FT_HAS_KERNING(face);

		loadGlyphDimensions();
	}

	private void loadGlyphDimensions() {
		for (int i = 0; i < 0xFFF; i += 256) {
			getPage(i, false);
		}
	}

	private void clearCurrentSizePages() {
		if (pageMap.containsKey(size)) {
			for (Page page : pageMap.get(size).values()) {
				Minecraft.getMinecraft().getTextureManager().deleteTexture(page.tex);
			}
			pageMap.clear();
		}
		if (glyphsByWidth.containsKey(size)) {
			glyphsByWidth.get(size).clear();
		}
	}

	private void clearPageMap() {
		for (Int2ObjectMap.Entry<Long2ObjectMap<Page>> pages : pageMap.int2ObjectEntrySet()) {
			for (Page page : pages.getValue().values()) {
				Minecraft.getMinecraft().getTextureManager().deleteTexture(page.tex);
			}
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
		BuiltText built = buildText(color, shadow, s -> StringDecomposer.iterateFormatted(text, new Style(), s));
		renderTextCommon(x, y, alignFlags, built);
	}

	public void renderText(ITextComponent text, float x, float y, int color, boolean shadow, int alignFlags) {
		BuiltText built = buildText(color, shadow, s -> StringDecomposer.iterateFormatted(text, new Style(), s));
		renderTextCommon(x, y, alignFlags, built);
	}

	private void renderTextCommon(float x, float y, int alignFlags, BuiltText builtText) {
		float[] startOffset = getOffset(alignFlags, builtText);
		float scale = (float) window.getScaleFactor();

		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y - 1f, 0);
		GlStateManager.scale(1f / scale, 1f / scale, 1);
		GlStateManager.translate(0, ascender(), 0);
		GlStateManager.translate(-startOffset[0], -startOffset[1], 0);

		renderTextCommon(builtText);
		GlStateManager.popMatrix();
	}

	float ascender() {
		return MathHelper.floor(FT_MulFix(face.ascender(), face.size().metrics().y_scale()) / 64f);
	}

	private static float @NotNull [] getOffset(int alignFlags, BuiltText builtText) {
		float[] startOffset = new float[2];
		float[] textBox = builtText.textBox();

		int horizAlign = alignFlags & 0x3;
		if (horizAlign == ORIGIN_HORIZ_LEFT) {
			startOffset[0] = textBox[0];
		} else if (horizAlign == ORIGIN_HORIZ_RIGHT) {
			startOffset[0] = textBox[2];
		} else if (horizAlign == ORIGIN_HORIZ_CENTER) {
			startOffset[0] = textBox[0] + (textBox[2] - textBox[0]) / 2f;
		} else {
			startOffset[0] = 0;
		}
		int vertAlign = alignFlags & 0xC;
		if (vertAlign == ORIGIN_VERT_TOP) {
			startOffset[1] = textBox[1];
		} else if (vertAlign == ORIGIN_VERT_BOTTOM) {
			startOffset[1] = textBox[3];
		} else if (vertAlign == ORIGIN_VERT_CENTER) {
			startOffset[1] = textBox[1] + (textBox[3] - textBox[1]) / 2f;
		} else {
			startOffset[1] = 0;
		}
		return startOffset;
	}

	private void renderTextCommon(BuiltText builtText) {
		Tessellator tess = Tessellator.getInstance();
		GlStateManager.enableBlend();
		ExRenderPipelines.FONT_PROGRAM.bind();
		GlStateManager.color(1, 1, 1);
		BufferBuilder vertexConsumer = tess.getBuffer();
		for (Map.Entry<ResourceLocation, List<CoordData>> pageGlyphs : builtText.coordData().entrySet()) {
			Minecraft.getMinecraft().getTextureManager().bindTexture(pageGlyphs.getKey());
			vertexConsumer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
			for (CoordData g : pageGlyphs.getValue()) {
				int color = Rendering.scaleARGB(g.color());
				vertexConsumer.pos(g.x(), g.y(), 0).tex(g.u(), g.v())
					.color((color >> 16 & 0xFF) / 255f, (color >> 8 & 0xFF) / 255f,
						(color & 0xFF) / 255f, (color >> 24 & 0xFF) / 255f).endVertex();
				vertexConsumer.pos(g.x(), g.y() + g.height(), 0).tex(g.u(), g.v1())
					.color((color >> 16 & 0xFF) / 255f, (color >> 8 & 0xFF) / 255f,
						(color & 0xFF) / 255f, (color >> 24 & 0xFF) / 255f).endVertex();
				vertexConsumer.pos(g.x() + g.width(), g.y() + g.height(), 0).tex(g.u1(), g.v1())
					.color((color >> 16 & 0xFF) / 255f, (color >> 8 & 0xFF) / 255f,
						(color & 0xFF) / 255f, (color >> 24 & 0xFF) / 255f).endVertex();
				vertexConsumer.pos(g.x() + g.width(), g.y(), 0).tex(g.u1(), g.v())
					.color((color >> 16 & 0xFF) / 255f, (color >> 8 & 0xFF) / 255f,
						(color & 0xFF) / 255f, (color >> 24 & 0xFF) / 255f).endVertex();
			}
			tess.draw();
		}
		ExRenderPipelines.FONT_PROGRAM.unbind();
		GlStateManager.color(1, 1, 1);
		GlStateManager.disableTexture2D();
		if (!builtText.effects().isEmpty()) {
			vertexConsumer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
			for (EffectData e : builtText.effects()) {
				int color = Rendering.scaleARGB(e.color());
				vertexConsumer.pos(e.x(), e.y(), 0).color((color >> 16 & 0xFF) / 255f,
					(color >> 8 & 0xFF) / 255f, (color & 0xFF) / 255f, (color >> 24 & 0xFF) / 255f).endVertex();
				vertexConsumer.pos(e.x(), e.y() + e.height(), 0).color((color >> 16 & 0xFF) / 255f,
					(color >> 8 & 0xFF) / 255f, (color & 0xFF) / 255f, (color >> 24 & 0xFF) / 255f).endVertex();
				vertexConsumer.pos(e.x() + e.width(), e.y() + e.height(), 0)
					.color((color >> 16 & 0xFF) / 255f, (color >> 8 & 0xFF) / 255f,
						(color & 0xFF) / 255f, (color >> 24 & 0xFF) / 255f).endVertex();
				vertexConsumer.pos(e.x() + e.width(), e.y(), 0).color((color >> 16 & 0xFF) / 255f,
					(color >> 8 & 0xFF) / 255f, (color & 0xFF) / 255f, (color >> 24 & 0xFF) / 255f).endVertex();
			}
			tess.draw();
		}
		GlStateManager.enableTexture2D();
		GlStateManager.disableBlend();
	}

	public float width(String text) {
		return activeSubFont.splitter.stringWidth(text);
	}

	public float width(ITextComponent text) {
		return activeSubFont.splitter.stringWidth(text);
	}

	public int lineHeight() {
		return activeSubFont.lineHeight / window.getScaleFactor();
	}

	private float getWidth(int codePt, Style style) {
		updateVariant(style);
		Page page = getPage(codePt, false);
		CharInfo c = page.charInfo[codePt & 0xFF];
		return c.advanceX / window.getScaleFactor();
	}

	public List<ITextComponent> split(String text, int maxWidth) {
		return this.splitter.splitLines(text, maxWidth, new Style());
	}

	public List<ITextComponent> split(ITextComponent text, int maxWidth) {
		return this.splitter.splitLines(text, maxWidth, new Style());
	}

	private BuiltText buildText(int color, boolean shadow, Consumer<FormattedCharSink> sink) {
		final float[] penX = {0};
		final float[] penY = {0};
		Object2ObjectOpenHashMap<ResourceLocation, List<CoordData>> screenAndTexCoords = new Object2ObjectOpenHashMap<>();
		ObjectArrayList<EffectData> effects = new ObjectArrayList<>();
		float[] fontBox = new float[]{Float.MAX_VALUE, Float.MAX_VALUE, Float.MIN_VALUE, Float.MIN_VALUE};

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
			TextColor styleColor = (style instanceof StyleExtension) ? ((StyleExtension) style).axolotlclient_rendering$getColor() : null;
			int textColor;
			if (styleColor != null) {
				textColor = ARGB.color(ARGB.alpha(color), styleColor.getValue());
			} else if (style.getColor() != null && style.getColor().isColor()) {
				textColor = ARGB.color(ARGB.alpha(color), TextRenderUtils.getColor(style.getColor()));
			} else textColor = color;

			int shadowColor = shadow ? ARGB.scaleRGB(textColor, 0.25f) : 0;

			Page page = getPage(codePt, true);

			CharInfo c = page.charInfo[codePt & 0xFF];

			float charAdvanceX = c.advanceX;
			float charAdvanceY = c.advanceY;
			int charX0 = c.x0;
			int charY0 = c.y0;
			int charX1 = c.x1;
			int charY1 = c.y1;
			if (style.getObfuscated() && codePt != ' ') {
				IntList codePts = activeSubFont.glyphsByWidth.get(size).get(charAdvanceX);
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

			List<CoordData> list = screenAndTexCoords.computeIfAbsent(page.tex, unused -> new ObjectArrayList<>());

			boolean hasShadow = ARGB.alpha(shadowColor) != 0;
			float shadowOffset = 1f * window.getScaleFactor();
			if (hasShadow) {
				list.add(new CoordData(penX[0] + charX0 + shadowOffset, penY[0] - charY0 + shadowOffset,
					c.u0, c.v0, c.u1, c.v1,
					c.width(), c.height(), shadowColor));
			}

			list.add(new CoordData(penX[0] + charX0, penY[0] - charY0,
				c.u0, c.v0, c.u1, c.v1,
				c.width(), c.height(), textColor));

			if (style.getStrikethrough() || style.getUnderlined()) {
				float effectX = penX[0];
				if (style.getStrikethrough()) {
					long table = FT_Get_Sfnt_Table(activeSubFont.face, FT_SFNT_OS2);
					float strikeoutpos = ascender() / 2f - shadowOffset;
					short strikeoutThicknessFT = activeSubFont.face.underline_thickness();
					if (table != 0L) {
						TT_OS2 t = TT_OS2.create(table);
						strikeoutThicknessFT = t.yStrikeoutSize();
						strikeoutpos = FT_MulFix(t.yStrikeoutPosition(), activeSubFont.face.size().metrics().y_scale()) / 64f;
					}
					float strikeoutThickness = FT_MulFix(strikeoutThicknessFT, activeSubFont.face.size().metrics().y_scale()) / 64f;
					if (hasShadow) {
						effects.add(new EffectData(effectX + shadowOffset, penY[0] - strikeoutpos + shadowOffset,
							charAdvanceX, strikeoutThickness, shadowColor));
					}
					effects.add(new EffectData(effectX, penY[0] - strikeoutpos,
						charAdvanceX, strikeoutThickness, textColor));
				}
				if (style.getUnderlined()) {
					float underlinePos = FT_MulFix(activeSubFont.face.underline_position(), activeSubFont.face.size().metrics().y_scale()) / 64f;
					float underlineThickness = FT_MulFix(activeSubFont.face.underline_thickness(), activeSubFont.face.size().metrics().y_scale()) / 64f;
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
			fontBox[3] = Math.max(fontBox[3], penY[0] + charY1 - (charY0 / 2f));

			penX[0] += charAdvanceX;
			penY[0] += charAdvanceY;

			prevGlyphI[0] = c.glyphI;
			return true;
		});


		return new BuiltText(screenAndTexCoords, effects, fontBox);
	}

	private @NotNull Page getPage(int codePt, boolean forRender) {
		long pgIndex = activeSubFont.getPageIndex(codePt >> 8);
		Page page = activeSubFont.pageMap.computeIfAbsent(size, u -> new Long2ObjectOpenHashMap<>()).get(pgIndex);
		if (page == null) {
			page = activeSubFont.loadPageGlyphDimensions(pgIndex);
		}
		if (forRender && !page.rendered) {
			activeSubFont.loadPageForRender(pgIndex, page);
		}
		return page;
	}

	private void updateVariant(Style style) {
		boolean bold = style.getBold();
		boolean italic = style.getItalic();
		boolean isBold = getVariant(TAG_WEIGHT) == WEIGHT_BOLD;
		boolean isItalic = getVariant(TAG_ITALIC) == ITALIC_ENABLED;
		Long2FloatMap subProps = getSubFontProperties(activeSubFont);
		if (subProps != null) {
			isBold |= subProps.getOrDefault(TAG_WEIGHT, WEIGHT_REGULAR) == WEIGHT_BOLD;
			isItalic |= subProps.getOrDefault(TAG_ITALIC, ITALIC_DISABLED) == ITALIC_ENABLED;
		}
		boolean variantsChanged = false;
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

	static class BuiltText {
		private final Object2ObjectOpenHashMap<ResourceLocation, List<CoordData>> coordData;
		private final List<EffectData> effects;
		private final float[] textBox;
		public BuiltText(Object2ObjectOpenHashMap<ResourceLocation, List<CoordData>> coordData, List<EffectData> effects, float[] textBox) {
			this.coordData = coordData;
			this.effects = effects;
			this.textBox = textBox;
		}
		public Object2ObjectOpenHashMap<ResourceLocation, List<CoordData>> coordData() { return coordData; }
		public List<EffectData> effects() { return effects; }
		public float[] textBox() { return textBox; }
	}

	private Page loadPageGlyphDimensions(long pageIndex) {
		Page page = new Page();
		pageMap.computeIfAbsent(size, u -> new Long2ObjectOpenHashMap<>()).put(pageIndex, page);
		int tWidth, tHeight;
		int pageNo = (int) pageIndex;
		int rowWidth = 0, maxRowWidth = 0, rowHeight = 0, totalHeight = 0;
		for (int codePt = pageNo << 8; codePt < (pageNo + 1) << 8; codePt++) {
			int tblRow = (codePt >> 4) & 0xF;
			int glyphI = FT_Get_Char_Index(face, codePt);
			if (FT_Load_Glyph(face, glyphI, FT_LOAD_NO_BITMAP | FT_LOAD_BITMAP_METRICS_ONLY) != FT_Err_Ok) {
				log.error("Error loading glyph for while getting dimensions: {} ({})", Integer.toHexString(codePt), new String(Character.toChars(codePt)));
				continue;
			}

			org.lwjgl.util.freetype.FT_GlyphSlot slot = face.glyph();
			org.lwjgl.util.freetype.FT_Bitmap bmp = slot.bitmap();

			CharInfo c = page.charInfo[codePt & 0xFF] = new CharInfo();
			c.x0 = slot.bitmap_left();
			c.y0 = slot.bitmap_top();
			c.x1 = bmp.width() + slot.bitmap_left();
			c.y1 = slot.bitmap_top() + bmp.rows();
			c.advanceX = slot.advance().x() / 64f;
			c.advanceY = slot.advance().y() / 64f;
			c.glyphI = glyphI;
			glyphsByWidth.computeIfAbsent(size, u -> new Float2ObjectOpenHashMap<>()).computeIfAbsent(c.advanceX, unused -> new IntArrayList()).add(codePt);

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
		int pageNo = (int) pageIndex;
		page.rendered = true;

		ByteBuffer texData = MemoryUtil.memAlloc(tWidth * tHeight);
		long pointer = MemoryUtil.memAddress(texData);
		MemoryUtil.memSet(pointer, 0, (long) tWidth * tHeight);
		org.lwjgl.util.freetype.FT_GlyphSlot slot = face.glyph();

		int curX = 2;
		int curY = 2;
		int rowHeight = 0;
		for (int codePt = pageNo << 8; codePt < (pageNo + 1) << 8; codePt++) {
			int tblRow = (codePt >> 4) & 0xF;

			CharInfo c = page.charInfo[codePt & 0xFF];
			if (FT_Load_Glyph(face, c.glyphI, FT_LOAD_RENDER) != FT_Err_Ok) {
				log.error("Error loading glyph for render: {} ({})", Integer.toHexString(codePt), new String(Character.toChars(codePt)));
				curX += 2;
			} else {
				org.lwjgl.util.freetype.FT_Bitmap bmp = slot.bitmap();
				final int width = bmp.width();
				final int height = bmp.rows();

				c.u0 = (float) (curX) / tWidth;
				c.v0 = (float) (curY) / tHeight;
				c.u1 = (float) (curX + bmp.width()) / tWidth;
				c.v1 = (float) (curY + bmp.rows()) / tHeight;

				ByteBuffer buffer = bmp.buffer(bmp.width() * bmp.rows());
				if (buffer != null) {
					long src = MemoryUtil.memAddress(buffer);
					for (int y = 0; y < height; y++) {
						MemoryUtil.memCopy(src + ((long) y * width), pointer + (curX + (long) (curY + y) * tWidth), width);
					}
				}
				curX += width + 2;
				rowHeight = Math.max(rowHeight, height);
			}

			if (((codePt + 1 >> 4) & 0xF) != tblRow) {
				curX = 2;
				curY += rowHeight + 2;
				rowHeight = 0;
			}
		}

		String family = face.family_nameString();
		String style = face.style_nameString();
		page.tex = new ResourceLocation(RenderingCommon.MODID,
			String.format("font_%s/%s/page_%d/%d",
				family.replace(" ", "_").toLowerCase(Locale.ROOT),
				style.replace(" ", "_").toLowerCase(Locale.ROOT),
				pageIndex, size
			));
		AbstractTexture tex = new AbstractTexture() {
			@Override
			public void loadTexture(IResourceManager resourceManager) {

			}
		};
		int texI = tex.getGlTextureId();
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
		Minecraft.getMinecraft().getTextureManager().loadTexture(page.tex, tex);
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
		ResourceLocation tex;
		CharInfo[] charInfo = new CharInfo[256];
		int texWidth, texHeight;
		boolean rendered;
	}


	public static class CoordData {
		private final float x, y, u, v, u1, v1;
		private final int width, height, color;
		public CoordData(float x, float y, float u, float v, float u1, float v1, int width, int height, int color) {
			this.x = x; this.y = y; this.u = u; this.v = v; this.u1 = u1; this.v1 = v1;
			this.width = width; this.height = height; this.color = color;
		}
		public float x() { return x; }
		public float y() { return y; }
		public float u() { return u; }
		public float v() { return v; }
		public float u1() { return u1; }
		public float v1() { return v1; }
		public int width() { return width; }
		public int height() { return height; }
		public int color() { return color; }
	}

	public static class EffectData {
		private final float x, y, width, height;
		private final int color;
		public EffectData(float x, float y, float width, float height, int color) {
			this.x = x; this.y = y; this.width = width; this.height = height; this.color = color;
		}
		public float x() { return x; }
		public float y() { return y; }
		public float width() { return width; }
		public float height() { return height; }
		public int color() { return color; }
	}

	private static class SubFont {
		private final Long2FloatMap variant;
		private final Font font;
		public SubFont(Long2FloatMap variant, Font font) {
			this.variant = variant; this.font = font;
		}
		public Long2FloatMap variant() { return variant; }
		public Font font() { return font; }
	}

	private static class VariationAxis {
		private final boolean supported;
		private final float min, def, max;
		private final long tag;
		private final int strid;
		public VariationAxis(boolean supported, float min, float def, float max, long tag, int strid) {
			this.supported = supported; this.min = min; this.def = def; this.max = max; this.tag = tag; this.strid = strid;
		}
		public boolean supported() { return supported; }
		public float min() { return min; }
		public float def() { return def; }
		public float max() { return max; }
		public long tag() { return tag; }
		public int strid() { return strid; }
	}
}
