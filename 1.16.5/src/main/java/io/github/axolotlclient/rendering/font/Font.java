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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.function.Consumer;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import io.github.axolotlclient.rendering.ExRenderPipelines;
import io.github.axolotlclient.rendering.Rendering;
import io.github.axolotlclient.rendering.RenderingCommon;
import io.github.axolotlclient.rendering.mixin.NativeImageAccessor;
import io.github.axolotlclient.rendering.util.ARGB;
import it.unimi.dsi.fastutil.floats.Float2ObjectMap;
import it.unimi.dsi.fastutil.floats.Float2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.longs.*;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.minecraft.client.Minecraft;
import net.minecraft.client.StringSplitter;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.FormattedCharSink;
import net.minecraft.util.StringDecomposer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.stb.STBTruetype;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

@SuppressWarnings({"DataFlowIssue", "unused"})
@Log4j2
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
		Font.fonts.removeIf(w -> w.refersTo(null));
		Font.fonts.forEach(w -> {
			Font f = w.get();
			f.clearPageMap();
			f.loadGlyphDimensions();
		});
	}

	public static void onScaleChange() {
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

	private static final Random RANDOM = new Random();

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
	STBTTFontinfo face;
	private int mcSize, size;
	private int lineHeight;
	private float stbScale, ascender;
	private final String family, style;
	final StringSplitter splitter = new StringSplitter(this::getWidth);
	private final ObjectList<SubFont> subFonts = new ObjectArrayList<>();
	private final Int2ObjectMap<Float2ObjectMap<IntList>> glyphsByWidth = new Int2ObjectOpenHashMap<>();
	private Font activeSubFont = this;

	private final Int2ObjectMap<Long2ObjectMap<Page>> pageMap = new Int2ObjectOpenHashMap<>();

	Font(ByteBuffer font, boolean ownsFontMem, int size, long faceIndex) throws IOException {
		fonts.add(new WeakReference<>(this));
		this.fontMem = ownsFontMem ? font : null;
		try (var stack = MemoryStack.stackPush()) {
			var fontInfo = STBTTFontinfo.malloc();
			if (!STBTruetype.stbtt_InitFont(fontInfo, font)) {
				fontInfo.free();
				throw new IOException("Unsupported font format");
			}
			face = fontInfo;

			/*
			 * MISSING: Font variations. STBTrueType does not support them. See https://github.com/nothings/stb/issues/509.
			 */

			var familyBuf = STBTruetype.stbtt_GetFontNameString(face,
				STBTruetype.STBTT_PLATFORM_ID_MICROSOFT,
				STBTruetype.STBTT_MS_EID_UNICODE_FULL,
				STBTruetype.STBTT_MS_LANG_ENGLISH,
				1);
			var styleBuf = STBTruetype.stbtt_GetFontNameString(face,
				STBTruetype.STBTT_PLATFORM_ID_MICROSOFT,
				STBTruetype.STBTT_MS_EID_UNICODE_BMP,
				STBTruetype.STBTT_MS_LANG_ENGLISH,
				2);
			if (familyBuf != null) {
				family = StandardCharsets.UTF_16.decode(familyBuf).toString();
			} else family = "unknown";
			if (styleBuf != null) {
				style = StandardCharsets.UTF_16.decode(styleBuf).toString();
			} else style = "unknown";

			try {
				resize(size);
			} catch (RuntimeException e) {
				face.free();
				throw e;
			}
		}
	}

	public static int makeVariantTag(String tag) {
		if (tag.length() != 4) {
			throw new IllegalArgumentException("Tags must have 4 characters");
		}
		return makeVariantTag(tag.charAt(0), tag.charAt(1), tag.charAt(2), tag.charAt(3));
	}

	public static int makeVariantTag(char x1, char x2, char x3, char x4) {
		return (((int) x1 & 0xFF) << 24) | (((int) x2 & 0xFF) << 16) | (((int) x3 & 0xFF) << 8) | ((int) x4 & 0xFF);
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

	/*
	 * STB_TrueType does not support variant fonts.
	 * See <a href="https://github.com/nothings/stb/issues/509">the github issue</a>.
	 */
	private void setVariants() {
		// no-op
	}

	/*
	 * STB_TrueType does not support variant fonts.
	 * See <a href="https://github.com/nothings/stb/issues/509">the github issue</a>.
	 */
	public void setVariant(long tag, float value) {
		// no-op
	}

	/*
	 * STB_TrueType does not support variant fonts.
	 * See <a href="https://github.com/nothings/stb/issues/509">the github issue</a>.
	 */
	public boolean hasVariant(long tag) {
		return false;
	}

	/*
	 * STB_TrueType does not support variant fonts.
	 * See <a href="https://github.com/nothings/stb/issues/509">the github issue</a>.
	 */
	public float getVariant(long tag) {
		return 0;
	}

	/*
	 * STB_TrueType does not support variant fonts.
	 * See <a href="https://github.com/nothings/stb/issues/509">the github issue</a>.
	 */
	public void resetVariants() {
		// no-op
	}

	private long getPageIndex(int no) {
		return no;
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
		var origSize = this.mcSize;
		resize(mcSize);
		drawer.run();
		resize(origSize);
	}

	private void resizeInternal(int mcSize) {
		this.mcSize = mcSize;
		size = (int) ((mcSize + 2) * Minecraft.getInstance().getWindow().getGuiScale());
		stbScale = STBTruetype.stbtt_ScaleForPixelHeight(face, size);
		try (var stack = MemoryStack.stackPush()) {
			var asc = stack.mallocInt(1);
			var desc = stack.mallocInt(1);
			var space = stack.mallocInt(1);
			STBTruetype.stbtt_GetFontVMetrics(face, asc, desc, space);
			ascender = asc.get(0) * stbScale;
			lineHeight = (int) ((asc.get(0) - desc.get(0) + space.get(0)) * stbScale);
		}

		loadGlyphDimensions();
	}

	private void loadGlyphDimensions() {
		for (var i = 0; i < 0xFFF; i += 256) {
			getPage(i, false);
		}
	}

	private void clearCurrentSizePages() {
		if (pageMap.containsKey(size)) {
			for (var page : pageMap.get(size).values()) {
				Minecraft.getInstance().getTextureManager().release(page.tex);
			}
			pageMap.clear();
		}
		if (glyphsByWidth.containsKey(size)) {
			glyphsByWidth.get(size).clear();
		}
	}

	private void clearPageMap() {
		for (var pages : pageMap.int2ObjectEntrySet()) {
			for (var page : pages.getValue().values()) {
				Minecraft.getInstance().getTextureManager().release(page.tex);
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

		if (face != null) {
			face.free();
			face = null;
		}

		if (fontMem != null) {
			MemoryUtil.memFree(fontMem);
			fontMem = null;
		}
	}

	public void renderText(PoseStack graphics, FormattedCharSequence text, float x, float y, int color, boolean shadow, int alignFlags) {
		var built = buildText(color, shadow, text::accept);
		renderTextCommon(graphics, x, y, alignFlags, built);
	}

	public void renderText(PoseStack graphics, String text, float x, float y, int color, boolean shadow, int alignFlags) {
		var built = buildText(color, shadow, s -> StringDecomposer.iterateFormatted(text, Style.EMPTY, s));
		renderTextCommon(graphics, x, y, alignFlags, built);
	}

	public void renderText(PoseStack graphics, Component text, float x, float y, int color, boolean shadow, int alignFlags) {
		var built = buildText(color, shadow, s -> StringDecomposer.iterateFormatted(text, Style.EMPTY, s));
		renderTextCommon(graphics, x, y, alignFlags, built);
	}

	private void renderTextCommon(PoseStack graphics, float x, float y, int alignFlags, BuiltText builtText) {
		float[] startOffset = getOffset(alignFlags, builtText);
		var scale = (float) Minecraft.getInstance().getWindow().getGuiScale();

		graphics.pushPose();
		graphics.translate(x, y - 1f, 0);
		graphics.scale(1f / scale, 1f / scale, 1);
		graphics.translate(0, ascender(), 0);
		graphics.translate(-startOffset[0], -startOffset[1], 0);

		renderTextCommon(graphics, builtText);
		graphics.popPose();
	}

	float ascender() {
		return ascender;
	}

	static float @NotNull [] getOffset(int alignFlags, BuiltText builtText) {
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
		return startOffset;
	}

	private void renderTextCommon(PoseStack graphics, BuiltText builtText) {
		var pose = graphics.last().pose();
		Tesselator tess = Tesselator.getInstance();
		RenderSystem.enableBlend();
		ExRenderPipelines.FONT_PROGRAM.bind();
		RenderSystem.color3f(1, 1, 1);
		var vertexConsumer = tess.getBuilder();
		for (var pageGlyphs : builtText.coordData().object2ObjectEntrySet().stream().skip(0).toList()) {
			Minecraft.getInstance().getTextureManager().bind(pageGlyphs.getKey());
			vertexConsumer.begin(GL11.GL_QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
			for (var g : pageGlyphs.getValue()) {
				var color = Rendering.scaleARGB(g.color());
				vertexConsumer.vertex(pose, g.x(), g.y(), 0).uv(g.u(), g.v())
					.color((color >> 16 & 0xFF) / 255f, (color >> 8 & 0xFF) / 255f,
						(color & 0xFF) / 255f, (color >> 24 & 0xFF) / 255f).endVertex();
				vertexConsumer.vertex(pose, g.x(), g.y() + g.height(), 0).uv(g.u(), g.v1())
					.color((color >> 16 & 0xFF) / 255f, (color >> 8 & 0xFF) / 255f,
						(color & 0xFF) / 255f, (color >> 24 & 0xFF) / 255f).endVertex();
				vertexConsumer.vertex(pose, g.x() + g.width(), g.y() + g.height(), 0).uv(g.u1(), g.v1())
					.color((color >> 16 & 0xFF) / 255f, (color >> 8 & 0xFF) / 255f,
						(color & 0xFF) / 255f, (color >> 24 & 0xFF) / 255f).endVertex();
				vertexConsumer.vertex(pose, g.x() + g.width(), g.y(), 0).uv(g.u1(), g.v())
					.color((color >> 16 & 0xFF) / 255f, (color >> 8 & 0xFF) / 255f,
						(color & 0xFF) / 255f, (color >> 24 & 0xFF) / 255f).endVertex();
			}
			tess.end();
		}
		ExRenderPipelines.FONT_PROGRAM.unbind();
		RenderSystem.color3f(1, 1, 1);
		RenderSystem.disableTexture();
		if (!builtText.effects().isEmpty()) {
			vertexConsumer.begin(GL11.GL_QUADS, DefaultVertexFormat.POSITION_COLOR);
			for (var e : builtText.effects()) {
				var color = Rendering.scaleARGB(e.color());
				vertexConsumer.vertex(pose, e.x(), e.y(), 0).color((color >> 16 & 0xFF) / 255f,
					(color >> 8 & 0xFF) / 255f, (color & 0xFF) / 255f, (color >> 24 & 0xFF) / 255f).endVertex();
				vertexConsumer.vertex(pose, e.x(), e.y() + e.height(), 0).color((color >> 16 & 0xFF) / 255f,
					(color >> 8 & 0xFF) / 255f, (color & 0xFF) / 255f, (color >> 24 & 0xFF) / 255f).endVertex();
				vertexConsumer.vertex(pose, e.x() + e.width(), e.y() + e.height(), 0)
					.color((color >> 16 & 0xFF) / 255f, (color >> 8 & 0xFF) / 255f,
						(color & 0xFF) / 255f, (color >> 24 & 0xFF) / 255f).endVertex();
				vertexConsumer.vertex(pose, e.x() + e.width(), e.y(), 0).color((color >> 16 & 0xFF) / 255f,
					(color >> 8 & 0xFF) / 255f, (color & 0xFF) / 255f, (color >> 24 & 0xFF) / 255f).endVertex();
			}
			tess.end();
		}
		RenderSystem.enableTexture();
		RenderSystem.disableBlend();
	}

	public float width(String text) {
		return activeSubFont.splitter.stringWidth(text);
	}

	public float width(FormattedCharSequence text) {
		return activeSubFont.splitter.stringWidth(text);
	}

	public float width(Component text) {
		return activeSubFont.splitter.stringWidth(text);
	}

	public int lineHeight() {
		return (int) (activeSubFont.lineHeight / Minecraft.getInstance().getWindow().getGuiScale());
	}

	private float getWidth(int codePt, Style style) {
		updateVariant(style);
		var page = getPage(codePt, false);
		var c = page.charInfo[codePt & 0xFF];
		return (float) (c.advanceX / Minecraft.getInstance().getWindow().getGuiScale());
	}

	public List<FormattedCharSequence> split(String text, int maxWidth) {
		return Language.getInstance().getVisualOrder(this.splitter.splitLines(text, maxWidth, Style.EMPTY));
	}

	public List<FormattedCharSequence> split(FormattedText text, int maxWidth) {
		return Language.getInstance().getVisualOrder(this.splitter.splitLines(text, maxWidth, Style.EMPTY));
	}

	BuiltText buildText(int color, boolean shadow, Consumer<FormattedCharSink> sink) {
		final float[] penX = {0};
		final float[] penY = {0};
		var screenAndTexCoords = new Object2ObjectOpenHashMap<ResourceLocation, List<CoordData>>();
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
			var styleColor = style.getColor();
			int textColor = styleColor != null ? ARGB.color(ARGB.alpha(color), styleColor.getValue()) : color;

			int shadowColor = shadow ? ARGB.scaleRGB(textColor, 0.25f) : 0;

			var page = getPage(codePt, true);

			var c = page.charInfo[codePt & 0xFF];

			float charAdvanceX = c.advanceX;
			int charX0 = c.x0;
			int charY0 = c.y0;
			int charX1 = c.x1;
			int charY1 = c.y1;
			if (style.isObfuscated() && codePt != ' ') {
				var codePts = activeSubFont.glyphsByWidth.get(size).get(charAdvanceX);
				codePt = codePts.getInt(RANDOM.nextInt(codePts.size()));
				page = getPage(codePt, true);

				c = page.charInfo[codePt & 0xFF];
			}

			if (prevGlyphI[0] > 0 && c.glyphI > 0) {
				penX[0] += (STBTruetype.stbtt_GetGlyphKernAdvance(activeSubFont.face, prevGlyphI[0], c.glyphI) * stbScale);
			}

			var list = screenAndTexCoords.computeIfAbsent(page.tex, unused -> new ObjectArrayList<>());

			var hasShadow = ARGB.alpha(shadowColor) != 0;
			var shadowOffset = (float) (1f * Minecraft.getInstance().getWindow().getGuiScale());
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
					var strikeoutpos = ascender / 2 - (int) Minecraft.getInstance().getWindow().getGuiScale();

					var strikeoutThickness = (int) Minecraft.getInstance().getWindow().getGuiScale();
					if (hasShadow) {
						effects.add(new EffectData(effectX + shadowOffset, penY[0] - strikeoutpos + shadowOffset,
							charAdvanceX, strikeoutThickness, shadowColor));
					}
					effects.add(new EffectData(effectX, penY[0] - strikeoutpos,
						charAdvanceX, strikeoutThickness, textColor));
				}
				if (style.isUnderlined()) {
					var underlinePos = -(int) Minecraft.getInstance().getWindow().getGuiScale() * 2;
					var underlineThickness = (int) Minecraft.getInstance().getWindow().getGuiScale();
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

			prevGlyphI[0] = c.glyphI;
			return true;
		});


		return new BuiltText(screenAndTexCoords, effects, fontBox);
	}

	private @NotNull Page getPage(int codePt, boolean forRender) {
		long pgIndex = activeSubFont.getPageIndex(codePt >> 8);
		var page = activeSubFont.pageMap.computeIfAbsent(size, u -> new Long2ObjectOpenHashMap<>()).get(pgIndex);
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
		var isBold = false;
		var isItalic = false;
		var subProps = getSubFontProperties(activeSubFont);
		if (subProps != null) {
			isBold = subProps.getOrDefault(TAG_WEIGHT, WEIGHT_REGULAR) == WEIGHT_BOLD;
			isItalic = subProps.getOrDefault(TAG_ITALIC, ITALIC_DISABLED) == ITALIC_ENABLED;
		}
		if (bold && !isBold) {
			activeSubFont = getSubFont(TAG_WEIGHT, WEIGHT_BOLD);
		} else if (!bold && isBold) {
			activeSubFont = getSubFont(TAG_WEIGHT, WEIGHT_REGULAR);
		}
		if (italic && !isItalic) {
			activeSubFont = getSubFont(TAG_ITALIC, ITALIC_ENABLED);
		} else if (!italic && isItalic) {
			activeSubFont = getSubFont(TAG_ITALIC, ITALIC_DISABLED);
		}
	}

	record BuiltText(Object2ObjectOpenHashMap<ResourceLocation, List<CoordData>> coordData,
	                 List<EffectData> effects, float[] textBox) {

	}

	private Page loadPageGlyphDimensions(long pageIndex) {
		var page = new Page();
		pageMap.computeIfAbsent(size, u -> new Long2ObjectOpenHashMap<>()).put(pageIndex, page);
		int tWidth, tHeight;
		var pageNo = (int) pageIndex;
		int rowWidth = 0, maxRowWidth = 0, rowHeight = 0, totalHeight = 0;
		for (int codePt = pageNo << 8; codePt < (pageNo + 1) << 8; codePt++) {
			int tblRow = (codePt >> 4) & 0xF;
			var glyphI = STBTruetype.stbtt_FindGlyphIndex(face, codePt);
			try (var stack = MemoryStack.stackPush()) {
				var x0B = stack.mallocInt(1);
				var y0B = stack.mallocInt(1);
				var x1B = stack.mallocInt(1);
				var y1B = stack.mallocInt(1);
				STBTruetype.stbtt_GetGlyphBitmapBox(face, glyphI, stbScale, stbScale, x0B, y0B, x1B, y1B);
				var advanceXB = stack.mallocInt(1);
				var bitmap_leftB = stack.mallocInt(1);
				STBTruetype.stbtt_GetGlyphHMetrics(face, glyphI, advanceXB, bitmap_leftB);

				var c = page.charInfo[codePt & 0xFF] = new CharInfo();
				var bearingX = bitmap_leftB.get(0) * stbScale;
				int x0 = x0B.get(0), y0 = y0B.get(0), x1 = x1B.get(0), y1 = y1B.get(0);
				var bearingY = ascender - y0;
				var width = x1 - x0;
				var height = y1 - y0;
				c.x0 = (int) (bearingX + x0B.get(0));
				c.y0 = -y0;
				c.x1 = (int) (bearingX + x0B.get(0) + width);
				c.y1 = c.y0 + height; //bearingY + (y1B.get(0) - y0B.get(0));
				c.advanceX = advanceXB.get(0) * stbScale;
				c.glyphI = glyphI;
				glyphsByWidth.computeIfAbsent(size, u -> new Float2ObjectOpenHashMap<>()).computeIfAbsent(c.advanceX, unused -> new IntArrayList()).add(codePt);

				rowWidth += c.x1 - c.x0 + 2;
				rowHeight = Math.max(c.y1 - c.y0, rowHeight);
			}
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

		var texData = new NativeImage(NativeImage.Format.LUMINANCE, tWidth, tHeight, false);
		MemoryUtil.memSet(((NativeImageAccessor) (Object) texData).getPointer(), 0, (long) tWidth * tHeight);

		int curX = 2;
		int curY = 2;
		int rowHeight = 0;
		for (int codePt = pageNo << 8; codePt < (pageNo + 1) << 8; codePt++) {
			int tblRow = (codePt >> 4) & 0xF;

			var c = page.charInfo[codePt & 0xFF];
			try (var stack = MemoryStack.stackPush()) {
				var wB = stack.mallocInt(1);
				var hB = stack.mallocInt(1);
				var offXB = stack.mallocInt(1);
				var offYB = stack.mallocInt(1);
				var buffer = STBTruetype.stbtt_GetGlyphBitmap(face, stbScale, stbScale, c.glyphI, wB, hB, offXB, offYB);
				final int width = wB.get(0);
				final int height = hB.get(0);

				c.u0 = (float) (curX) / texData.getWidth();
				c.v0 = (float) (curY) / texData.getHeight();
				c.u1 = (float) (curX + width) / texData.getWidth();
				c.v1 = (float) (curY + height) / texData.getHeight();

				if (buffer != null) {
					long src = MemoryUtil.memAddress(buffer);
					for (int y = 0; y < height; y++) {
						MemoryUtil.memCopy(src + ((long) y * width), ((NativeImageAccessor) (Object) texData).getPointer() + (curX + (long) (curY + y) * tWidth), width);
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

		page.tex = new ResourceLocation(RenderingCommon.MODID,
			"font_%s/%s/page_%d/%d".formatted(
				family.replace(" ", "_").toLowerCase(Locale.ROOT),
				style.replace(" ", "_").toLowerCase(Locale.ROOT),
				pageIndex, size
			));
		var tex = new DynamicTexture(texData);
		Minecraft.getInstance().getTextureManager().register(page.tex, tex);
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
		float advanceX;
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


	public record CoordData(float x, float y, float u, float v, float u1, float v1, int width, int height, int color) {
	}

	public record EffectData(float x, float y, float width, float height, int color) {
	}

	private record SubFont(Long2FloatMap variant, Font font) {
	}

	private record VariationAxis(boolean supported, float min, float def, float max, float tag, int strid) {
	}
}
