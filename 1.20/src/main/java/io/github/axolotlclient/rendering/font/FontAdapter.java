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

import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import io.github.axolotlclient.rendering.ExRenderPipelines;
import net.minecraft.client.Minecraft;
import net.minecraft.client.StringSplitter;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.util.StringDecomposer;
import org.joml.Matrix4f;

/**
 * Convenience class to adapt {@link io.github.axolotlclient.rendering.font.Font} to
 * Minecraft's {@link Font} so it can be used in methods designed for the latter.
 * Only supports GUI rendering, everything else is silently delegated to Minecraft's {@link Font}.
 *
 * @implNote Assumes a font size of <b>9 pixels</b>. Spacing can be janky as a result of this assumption
 * as barely any font will have an integer font size. In order to get correct results, use methods
 * in {@link io.github.axolotlclient.rendering.GuiGraphicsExtension} that directly accept a {@link io.github.axolotlclient.rendering.font.Font}.
 */
@SuppressWarnings({"NullableProblems", "unused"})
public class FontAdapter extends Font {
	private static final Minecraft mc = Minecraft.getInstance();
	private final io.github.axolotlclient.rendering.font.Font font;

	public FontAdapter(io.github.axolotlclient.rendering.font.Font font) {
		super(null, false);
		this.font = font;
	}

	static int adjustColor(int color) {
		return (color & -67108864) == 0 ? color | 0xFF000000 : color;
	}

	@Override
	public int drawInBatch(String text, float x, float y, int color, boolean dropShadow, Matrix4f matrix, MultiBufferSource buffer, DisplayMode displayMode, int backgroundColor, int packedLightCoords, boolean bidirectional) {
		return this.drawInternal(text, x, y, color, dropShadow, matrix, buffer, displayMode, backgroundColor, packedLightCoords, bidirectional);
	}

	@Override
	public int drawInBatch(FormattedCharSequence text, float x, float y, int color, boolean dropShadow, Matrix4f matrix, MultiBufferSource buffer, DisplayMode displayMode, int backgroundColor, int packedLightCoords) {
		return this.drawInternal(text, x, y, color, dropShadow, matrix, buffer, displayMode, backgroundColor, packedLightCoords);
	}

	@Override
	public void drawInBatch8xOutline(FormattedCharSequence text, float x, float y, int color, int backgroundColor, Matrix4f pose, MultiBufferSource bufferSource, int packedLightCoords) {
		mc.font.drawInBatch8xOutline(text, x, y, color, backgroundColor, pose, bufferSource, packedLightCoords);
	}

	private int drawInternal(String text, float x, float y, int color, boolean dropShadow, Matrix4f matrix, MultiBufferSource buffer, DisplayMode displayMode, int backgroundColor, int packedLightCoords, boolean bidirectional) {
		var t = isBidirectional() ? bidirectionalShaping(text) : text;
		return renderText(x, y, matrix, buffer, font.buildText(adjustColor(color), dropShadow, s -> StringDecomposer.iterateFormatted(t, Style.EMPTY, s)), backgroundColor);
	}

	private int renderText(float x, float y, Matrix4f matrix, MultiBufferSource buffer, io.github.axolotlclient.rendering.font.Font.BuiltText built, int backgroundColor) {
		var pose = new Matrix4f(matrix);
		pose.translate(x, y + font.lineHeight(), 0);
		var s = (float) (1f / mc.getWindow().getGuiScale());
		pose.scale(s, s, 1);
		float[] startOffset = io.github.axolotlclient.rendering.font.Font.getOffset(0, built);
		pose.translate(-startOffset[0], -startOffset[1], 0);

		var tess = Tesselator.getInstance();
		RenderSystem.enableBlend();
		RenderSystem.setShader(GameRenderer::getPositionColorShader);
		if (backgroundColor != 0) {
			var vertexConsumer = tess.getBuilder();
			vertexConsumer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
			vertexConsumer.vertex(pose, x - 1f, y - 1f, 0.01f).color(backgroundColor).endVertex();
			vertexConsumer.vertex(pose, x - 1f, y - 1f + built.textBox()[3] + 1f, 0.01f).color(backgroundColor).endVertex();
			vertexConsumer.vertex(pose, x + built.textBox()[2] + 1f, y - 1f + built.textBox()[3] + 1f, 0.01f).color(backgroundColor).endVertex();
			vertexConsumer.vertex(pose, x + built.textBox()[2] + 1f, y - 1f, 0.01f).color(backgroundColor).endVertex();
			BufferUploader.drawWithShader(vertexConsumer.end());
		}
		RenderSystem.setShader(() -> ExRenderPipelines.FONT_SHADER);
		ExRenderPipelines.FONT_SHADER.apply();
		for (var pageGlyphs : built.coordData().object2ObjectEntrySet()) {
			var vertexConsumer = mc.renderBuffers().bufferSource().getBuffer(ExRenderPipelines.FONT);
			RenderSystem.setShaderTexture(0, pageGlyphs.getKey());
			for (var g : pageGlyphs.getValue()) {
				vertexConsumer.vertex(pose, g.x(), g.y(), 0).color(g.color()).uv(g.u(), g.v()).endVertex();
				vertexConsumer.vertex(pose, g.x(), g.y() + g.height(), 0).color(g.color()).uv(g.u(), g.v1()).endVertex();
				vertexConsumer.vertex(pose, g.x() + g.width(), g.y() + g.height(), 0).color(g.color()).uv(g.u1(), g.v1()).endVertex();
				vertexConsumer.vertex(pose, g.x() + g.width(), g.y(), 0).color(g.color()).uv(g.u1(), g.v()).endVertex();
			}
			mc.renderBuffers().bufferSource().endBatch(ExRenderPipelines.FONT);
		}
		RenderSystem.setShader(GameRenderer::getPositionColorShader);
		if (!built.effects().isEmpty()) {
			var vertexConsumer = tess.getBuilder();
			vertexConsumer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
			for (var e : built.effects()) {
				vertexConsumer.vertex(pose, e.x(), e.y(), 0).color(e.color());
				vertexConsumer.vertex(pose, e.x(), e.y() + e.height(), 0).color(e.color());
				vertexConsumer.vertex(pose, e.x() + e.width(), e.y() + e.height(), 0).color(e.color());
				vertexConsumer.vertex(pose, e.x() + e.width(), e.y(), 0).color(e.color());
			}
			BufferUploader.drawWithShader(vertexConsumer.end());
		}
		RenderSystem.enableBlend();
		return (int) built.textBox()[3];
	}

	private int drawInternal(FormattedCharSequence text, float x, float y, int color, boolean dropShadow, Matrix4f matrix, MultiBufferSource buffer, DisplayMode displayMode, int backgroundColor, int packedLightCoords) {
		var built = font.buildText(adjustColor(color), dropShadow, text::accept);
		return renderText(x, y, matrix, buffer, built, backgroundColor);
	}

	@Override
	public int width(String text) {
		return Mth.ceil(font.width(text));
	}

	@Override
	public int width(FormattedText text) {
		return Mth.ceil(getSplitter().stringWidth(text));
	}

	@Override
	public int width(FormattedCharSequence text) {
		return Mth.ceil(font.width(text));
	}

	@Override
	public String plainSubstrByWidth(String text, int maxWidth, boolean tail) {
		return tail ? getSplitter().plainTailByWidth(text, maxWidth, Style.EMPTY) : getSplitter().plainHeadByWidth(text, maxWidth, Style.EMPTY);
	}

	@Override
	public String plainSubstrByWidth(String text, int maxWidth) {
		return getSplitter().plainHeadByWidth(text, maxWidth, Style.EMPTY);
	}

	@Override
	public FormattedText substrByWidth(FormattedText text, int maxWidth) {
		return getSplitter().headByWidth(text, maxWidth, Style.EMPTY);
	}

	@Override
	public int wordWrapHeight(String text, int maxWidth) {
		return font.lineHeight() * getSplitter().splitLines(text, maxWidth, Style.EMPTY).size();
	}

	@Override
	public int wordWrapHeight(FormattedText text, int maxWidth) {
		return font.lineHeight() * getSplitter().splitLines(text, maxWidth, Style.EMPTY).size();
	}

	@Override
	public List<FormattedCharSequence> split(FormattedText text, int maxWidth) {
		return Language.getInstance().getVisualOrder(getSplitter().splitLines(text, maxWidth, Style.EMPTY));
	}

	@Override
	public StringSplitter getSplitter() {
		return font.splitter;
	}
}
