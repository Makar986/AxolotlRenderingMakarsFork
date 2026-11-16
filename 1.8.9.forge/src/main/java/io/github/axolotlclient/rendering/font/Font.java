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
import io.github.axolotlclient.rendering.ExRenderPipelines;
import io.github.axolotlclient.rendering.Rendering;
import io.github.axolotlclient.rendering.RenderingCommon;
import io.github.axolotlclient.rendering.StyleExtension;
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
import lombok.extern.log4j.Log4j2;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.font.LineMetrics;
import java.awt.font.TextAttribute;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;

@SuppressWarnings({"resource", "DataFlowIssue", "unused"})
@Log4j2
public class Font implements Closeable {

	/*
	 * 1.8.9 Forge port: Minecraft 1.8.9 ships LWJGL 2, so FreeType (LWJGL 3) is not available at runtime.
	 * Glyphs are rasterized with java.awt instead. Variable font axes cannot be driven through AWT,
	 * so the weight axis is emulated (synthetic bold at >= 600) and italics come from the italic sub font.
	 */
	private static final FontRenderContext FRC = new FontRenderContext(null, true, true);
	private static final float SYNTHETIC_BOLD_THRESHOLD = 600;

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
			if (f != null) {
				f.clearPageMap();
				f.loadGlyphDimensions();
			}
		});
	}

	public static void onScaleChange() {
		window = new ScaledResolution(Minecraft.getMinecraft());
		Font.fonts.removeIf(w -> w.get() == null);
		Font.fonts.forEach(w -> {
			Font font = w.get();
			if (font != null) {
				font.resizeInternal(font.mcSize);
			}
		});
	}

	public static void closeAll() {
		Font.fonts.removeIf(w -> w.get() == null);
		Font.fonts.forEach(w -> {
			Font f = w.get();
			if (f != null) {
				f.close();
			}
		});
	}

	private static ScaledResolution window = new ScaledResolution(Minecraft.getMinecraft());

	public static ScaledResolution getWindow() {
		return window != null ? window : new ScaledResolution(Minecraft.getMinecraft());
	}

	private static final Random RANDOM = new Random();

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
		return new Font(font, size);
	}

	/**
	 * Create a font from this file.
	 *
	 * @param font the font file
	 * @param size the font size in pixels
	 * @throws IOException if an I/O exception occurs
	 */
	public static Font read(ByteBuffer font, int size) throws IOException {
		byte[] bytes = new byte[font.remaining()];
		font.duplicate().get(bytes);
		return read(bytes, size);
	}

	private java.awt.Font baseFont;
	private java.awt.Font sizedFont;
	private java.awt.Font kerningFont;
	private LineMetrics lineMetrics;
	private final Long2FloatMap kerningCache = new Long2FloatOpenHashMap();
	private boolean hasKerningInfo;
	private int mcSize, size;
	private int lineHeight;
	private final Long2ObjectMap<ObjectFloatPair<VariationAxis>> variations = new Long2ObjectLinkedOpenHashMap<>();
	private final StringSplitter splitter = new StringSplitter(this::getWidth);
	private final ObjectList<SubFont> subFonts = new ObjectArrayList<>();
	private final Int2ObjectMap<Float2ObjectMap<IntList>> glyphsByWidth = new Int2ObjectOpenHashMap<>();
	private Font activeSubFont = this;

	private final Int2ObjectMap<Long2ObjectMap<Page>> pageMap = new Int2ObjectOpenHashMap<>();

	Font(byte[] font, int size) throws IOException {
		try {
			baseFont = java.awt.Font.createFont(java.awt.Font.TRUETYPE_FONT, new ByteArrayInputStream(font));
		} catch (java.awt.FontFormatException e) {
			throw new IOException("Unsupported font format", e);
		}
		fonts.add(new WeakReference<>(this));
		variations.put(TAG_WEIGHT, new ObjectFloatMutablePair<>(new VariationAxis(true, 100, WEIGHT_REGULAR, 900, TAG_WEIGHT, 0), WEIGHT_REGULAR));
		resize(size);
	}

	public static int makeVariantTag(String tag) {
		if (tag.length() != 4) {
			throw new IllegalArgumentException("Tags must have 4 characters");
		}
		return makeVariantTag(tag.charAt(0), tag.charAt(1), tag.charAt(2), tag.charAt(3));
	}

	public static int makeVariantTag(char x1, char x2, char x3, char x4) {
		return (x1 & 0xFF) << 24 | (x2 & 0xFF) << 16 | (x3 & 0xFF) << 8 | (x4 & 0xFF);
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
		int style = getVariant(TAG_WEIGHT) >= SYNTHETIC_BOLD_THRESHOLD ? java.awt.Font.BOLD : java.awt.Font.PLAIN;
		sizedFont = baseFont.deriveFont(style, (float) size);
		Map<TextAttribute, Object> attrs = new HashMap<>();
		attrs.put(TextAttribute.KERNING, TextAttribute.KERNING_ON);
		kerningFont = sizedFont.deriveFont(attrs);
		kerningCache.clear();
		lineMetrics = sizedFont.getLineMetrics("Ag", FRC);
		lineHeight = (int) Math.ceil(lineMetrics.getHeight());
	}

	private float kerning(int prevCodePt, int codePt) {
		long key = (long) prevCodePt << 32 | (codePt & 0xFFFFFFFFL);
		if (kerningCache.containsKey(key)) {
			return kerningCache.get(key);
		}
		char[] chars = (new String(Character.toChars(prevCodePt)) + new String(Character.toChars(codePt))).toCharArray();
		GlyphVector plain = sizedFont.createGlyphVector(FRC, chars);
		GlyphVector kerned = kerningFont.layoutGlyphVector(FRC, chars, 0, chars.length, java.awt.Font.LAYOUT_LEFT_TO_RIGHT);
		float k = 0;
		if (plain.getNumGlyphs() == kerned.getNumGlyphs() && kerned.getNumGlyphs() > 1) {
			int last = kerned.getNumGlyphs() - 1;
			k = (float) (kerned.getGlyphPosition(last).getX() - plain.getGlyphPosition(last).getX());
		}
		kerningCache.put(key, k);
		return k;
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
				if (e instanceof ObjectFloatMutablePair) {
					((ObjectFloatMutablePair<VariationAxis>) e).value(value);
				}
				break;
			}
		}
	}

	public void resetVariants() {
		if (!variations.isEmpty()) {
			for (ObjectFloatPair<VariationAxis> e : variations.values()) {
				if (e instanceof ObjectFloatMutablePair) {
					((ObjectFloatMutablePair<VariationAxis>) e).value(e.key().def());
				}
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
		size = mcSize * getWindow().getScaleFactor();
		setVariants();

		hasKerningInfo = true;

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
	}

	public void renderText(String text, float x, float y, int color, boolean shadow, int alignFlags) {
		BuiltText built = buildText(color, shadow, s -> StringDecomposer.iterateFormatted(text, new ChatStyle(), s));
		renderTextCommon(x, y, alignFlags, built);
	}

	public void renderText(IChatComponent text, float x, float y, int color, boolean shadow, int alignFlags) {
		BuiltText built = buildText(color, shadow, s -> StringDecomposer.iterateFormatted(text, new ChatStyle(), s));
		renderTextCommon(x, y, alignFlags, built);
	}

	private void renderTextCommon(float x, float y, int alignFlags, BuiltText builtText) {
		float[] startOffset = getOffset(alignFlags, builtText);
		float scale = (float) getWindow().getScaleFactor();

		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y - 1f, 0);
		GlStateManager.scale(1f / scale, 1f / scale, 1);
		GlStateManager.translate(0, ascender(), 0);
		GlStateManager.translate(-startOffset[0], -startOffset[1], 0);

		renderTextCommon(builtText);
		GlStateManager.popMatrix();
	}

	float ascender() {
		return MathHelper.floor_float(activeSubFont.lineMetrics.getAscent());
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
		GlStateManager.color(1, 1, 1, 1);
		WorldRenderer vertexConsumer = tess.getWorldRenderer();
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
		GlStateManager.color(1, 1, 1, 1);
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

	public float width(IChatComponent text) {
		return activeSubFont.splitter.stringWidth(text);
	}

	public int lineHeight() {
		return activeSubFont.lineHeight / getWindow().getScaleFactor();
	}

	private float getWidth(int codePt, ChatStyle style) {
		updateVariant(style);
		Page page = getPage(codePt, false);
		CharInfo c = page.charInfo[codePt & 0xFF];
		return c.advanceX / getWindow().getScaleFactor();
	}

	public List<IChatComponent> split(String text, int maxWidth) {
		return this.splitter.splitLines(text, maxWidth, new ChatStyle());
	}

	public List<IChatComponent> split(IChatComponent text, int maxWidth) {
		return this.splitter.splitLines(text, maxWidth, new ChatStyle());
	}

	private BuiltText buildText(int color, boolean shadow, Consumer<FormattedCharSink> sink) {
		final float[] penX = {0};
		final float[] penY = {0};
		Object2ObjectOpenHashMap<ResourceLocation, List<CoordData>> screenAndTexCoords = new Object2ObjectOpenHashMap<>();
		ObjectArrayList<EffectData> effects = new ObjectArrayList<>();
		float[] fontBox = new float[]{Float.MAX_VALUE, Float.MAX_VALUE, Float.MIN_VALUE, Float.MIN_VALUE};

		final int[] prevCodePt = {-1};

		sink.accept((index, style, codePt) -> {
			if (codePt == '\n') {
				penX[0] = 0;
				penY[0] += lineHeight();
				prevCodePt[0] = -1;
				return true;
			} else if (codePt == '\r') {
				penX[0] = 0;
				prevCodePt[0] = -1;
				return true;
			}

			updateVariant(style);
			TextColor styleColor = (style instanceof StyleExtension) ? ((StyleExtension) style).axolotlclient_rendering$getColor() : null;
			int textColor;
			if (styleColor != null) {
				textColor = ARGB.color(ARGB.alpha(color), styleColor.getValue());
			} else if (style.getColor() != null && style.getColor().isColor()) {
				textColor = ARGB.color(ARGB.alpha(color), TextRenderUtils.getColor(style.getColor()));
			} else {
				textColor = color;
			}

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
				Float2ObjectMap<IntList> bySize = activeSubFont.glyphsByWidth.get(size);
				IntList codePts = bySize != null ? bySize.get(charAdvanceX) : null;
				if (codePts != null && !codePts.isEmpty()) {
					codePt = codePts.getInt(RANDOM.nextInt(codePts.size()));
					page = getPage(codePt, true);

					c = page.charInfo[codePt & 0xFF];
				}
			}

			if (hasKerningInfo && prevCodePt[0] > 0) {
				penX[0] += activeSubFont.kerning(prevCodePt[0], codePt);
			}

			List<CoordData> list = screenAndTexCoords.computeIfAbsent(page.tex, unused -> new ObjectArrayList<>());

			boolean hasShadow = ARGB.alpha(shadowColor) != 0;
			float shadowOffset = 1f * getWindow().getScaleFactor();
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
					LineMetrics lm = activeSubFont.lineMetrics;
					float strikeoutpos = -lm.getStrikethroughOffset();
					float strikeoutThickness = Math.max(1, lm.getStrikethroughThickness());
					if (hasShadow) {
						effects.add(new EffectData(effectX + shadowOffset, penY[0] - strikeoutpos + shadowOffset,
							charAdvanceX, strikeoutThickness, shadowColor));
					}
					effects.add(new EffectData(effectX, penY[0] - strikeoutpos,
						charAdvanceX, strikeoutThickness, textColor));
				}
				if (style.getUnderlined()) {
					float underlinePos = -activeSubFont.lineMetrics.getUnderlineOffset();
					float underlineThickness = Math.max(1, activeSubFont.lineMetrics.getUnderlineThickness());
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

			prevCodePt[0] = codePt;
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

	private void updateVariant(ChatStyle style) {
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

		public Object2ObjectOpenHashMap<ResourceLocation, List<CoordData>> coordData() {
			return coordData;
		}

		public List<EffectData> effects() {
			return effects;
		}

		public float[] textBox() {
			return textBox;
		}
	}

	private Page loadPageGlyphDimensions(long pageIndex) {
		Page page = new Page();
		pageMap.computeIfAbsent(size, u -> new Long2ObjectOpenHashMap<>()).put(pageIndex, page);
		int tWidth, tHeight;
		int pageNo = (int) pageIndex;
		int rowWidth = 0, maxRowWidth = 0, rowHeight = 0, totalHeight = 0;
		for (int codePt = pageNo << 8; codePt < (pageNo + 1) << 8; codePt++) {
			int tblRow = (codePt >> 4) & 0xF;
			CharInfo c = page.charInfo[codePt & 0xFF] = new CharInfo();
			int bmpWidth = 0, bmpRows = 0;
			try {
				GlyphVector gv = glyphVector(codePt);
				Rectangle bounds = gv.getGlyphPixelBounds(0, FRC, 0, 0);
				bmpWidth = Math.max(0, bounds.width);
				bmpRows = Math.max(0, bounds.height);
				c.x0 = bounds.x;
				c.y0 = -bounds.y;
				c.x1 = bmpWidth + bounds.x;
				c.y1 = -bounds.y + bmpRows;
				c.advanceX = gv.getGlyphMetrics(0).getAdvanceX();
				c.advanceY = 0;
				c.glyphI = gv.getGlyphCode(0);
				if (sizedFont.canDisplay(codePt)) {
					glyphsByWidth.computeIfAbsent(size, u -> new Float2ObjectOpenHashMap<>()).computeIfAbsent(c.advanceX, unused -> new IntArrayList()).add(codePt);
				}
			} catch (RuntimeException e) {
				log.error("Error loading glyph while getting dimensions: {} ({})", Integer.toHexString(codePt), Character.toString((char) codePt), e);
			}

			rowWidth += bmpWidth + 2;
			rowHeight = Math.max(bmpRows, rowHeight);
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

		BufferedImage image = new BufferedImage(tWidth, tHeight, BufferedImage.TYPE_BYTE_GRAY);
		Graphics2D g = image.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(java.awt.Color.WHITE);

		int curX = 2;
		int curY = 2;
		int rowHeight = 0;
		for (int codePt = pageNo << 8; codePt < (pageNo + 1) << 8; codePt++) {
			int tblRow = (codePt >> 4) & 0xF;

			CharInfo c = page.charInfo[codePt & 0xFF];
			if (c == null) {
				curX += 2;
			} else {
				final int width = c.width();
				final int height = c.height();

				c.u0 = (float) (curX) / tWidth;
				c.v0 = (float) (curY) / tHeight;
				c.u1 = (float) (curX + width) / tWidth;
				c.v1 = (float) (curY + height) / tHeight;

				if (width > 0 && height > 0) {
					try {
						g.drawGlyphVector(glyphVector(codePt), curX - c.x0, curY + c.y0);
					} catch (RuntimeException e) {
						log.error("Error loading glyph for render: {} ({})", Integer.toHexString(codePt), Character.toString((char) codePt), e);
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

		g.dispose();
		byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
		ByteBuffer texData = BufferUtils.createByteBuffer(pixels.length);
		texData.put(pixels).flip();

		String family = baseFont.getFamily(Locale.ROOT);
		String style = baseFont.getFontName(Locale.ROOT) + (getVariant(TAG_WEIGHT) >= SYNTHETIC_BOLD_THRESHOLD ? "_bold" : "");
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
		GL11.glTexSubImage2D(0xde1, 0, 0, 0, tWidth, tHeight, 0x1903, 5121, texData);
		Minecraft.getMinecraft().getTextureManager().loadTexture(page.tex, tex);
	}

	private GlyphVector glyphVector(int codePt) {
		return sizedFont.createGlyphVector(FRC, Character.toChars(codePt));
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
			this.x = x;
			this.y = y;
			this.u = u;
			this.v = v;
			this.u1 = u1;
			this.v1 = v1;
			this.width = width;
			this.height = height;
			this.color = color;
		}

		public float x() {
			return x;
		}

		public float y() {
			return y;
		}

		public float u() {
			return u;
		}

		public float v() {
			return v;
		}

		public float u1() {
			return u1;
		}

		public float v1() {
			return v1;
		}

		public int width() {
			return width;
		}

		public int height() {
			return height;
		}

		public int color() {
			return color;
		}
	}

	public static class EffectData {
		private final float x, y, width, height;
		private final int color;

		public EffectData(float x, float y, float width, float height, int color) {
			this.x = x;
			this.y = y;
			this.width = width;
			this.height = height;
			this.color = color;
		}

		public float x() {
			return x;
		}

		public float y() {
			return y;
		}

		public float width() {
			return width;
		}

		public float height() {
			return height;
		}

		public int color() {
			return color;
		}
	}

	private static class SubFont {
		private final Long2FloatMap variant;
		private final Font font;

		public SubFont(Long2FloatMap variant, Font font) {
			this.variant = variant;
			this.font = font;
		}

		public Long2FloatMap variant() {
			return variant;
		}

		public Font font() {
			return font;
		}
	}

	private static class VariationAxis {
		private final boolean supported;
		private final float min, def, max, tag;
		private final int strid;

		public VariationAxis(boolean supported, float min, float def, float max, float tag, int strid) {
			this.supported = supported;
			this.min = min;
			this.def = def;
			this.max = max;
			this.tag = tag;
			this.strid = strid;
		}

		public boolean supported() {
			return supported;
		}

		public float min() {
			return min;
		}

		public float def() {
			return def;
		}

		public float max() {
			return max;
		}

		public float tag() {
			return tag;
		}

		public int strid() {
			return strid;
		}
	}
}
