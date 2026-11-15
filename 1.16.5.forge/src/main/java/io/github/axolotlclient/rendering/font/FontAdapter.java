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
import java.util.Map;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.axolotlclient.rendering.ExRenderPipelines;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.CharacterManager;
import net.minecraft.util.text.ITextProperties;
import net.minecraft.util.text.LanguageMap;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextProcessing;

@SuppressWarnings("unused")
public class FontAdapter extends FontRenderer {
	private static final Minecraft mc = Minecraft.getInstance();
	private final Font font;

	public FontAdapter(Font font) {
		super(r -> null);
		this.font = font;
	}

	static int adjustColor(int color) {
		return (color & 0xFC000000) == 0 ? color | 0xFF000000 : color;
	}

	@Override
	public int drawInBatch(String string, float f, float g, int i, boolean bl, Matrix4f matrix4f, IRenderTypeBuffer multiBufferSource, boolean bl2, int j, int k, boolean bl3) {
		if (bl2) {
			return super.drawInBatch(string, f, g, i, bl, matrix4f, multiBufferSource, true, j, k, bl3);
		}
		return drawInternal(string, f, g, i, bl, matrix4f, multiBufferSource, j, k, bl3);
	}

	@Override
	public int drawInBatch(IReorderingProcessor processor, float f, float g, int i, boolean bl, Matrix4f matrix4f, IRenderTypeBuffer multiBufferSource, boolean bl2, int j, int k) {
		if (bl2) {
			return super.drawInBatch(processor, f, g, i, bl, matrix4f, multiBufferSource, true, j, k);
		}
		return drawInternal(processor, f, g, i, bl, matrix4f, multiBufferSource, j, k);
	}

	private int drawInternal(String text, float x, float y, int color, boolean dropShadow, Matrix4f matrix, IRenderTypeBuffer buffer, int backgroundColor, int packedLightCoords, boolean bidirectional) {
		String t = isBidirectional() ? bidirectionalShaping(text) : text;
		renderText(x, y, matrix, buffer, font.buildText(adjustColor(color), dropShadow, s -> TextProcessing.iterateFormatted(t, Style.EMPTY, s)), backgroundColor);
		return (int) (x + width(text));
	}

	private void renderText(float x, float y, Matrix4f matrix, IRenderTypeBuffer buffer, Font.BuiltText built, int backgroundColor) {
		float s = (float) (1f / mc.getWindow().getGuiScale());
		float[] startOffset = Font.getOffset(0, built);
		Matrix4f pose = new Matrix4f(matrix);
		pose.translate(new Vector3f(x, y - 1f, 0));
		pose.multiply(Matrix4f.createScaleMatrix(s, s, 1));
		pose.multiply(Matrix4f.createTranslateMatrix(0, font.ascender(), 0));
		pose.multiply(Matrix4f.createTranslateMatrix(-startOffset[0], -startOffset[1], 0));

		Tessellator tess = Tessellator.getInstance();
		RenderSystem.enableBlend();
		if (backgroundColor != 0) {
			BufferBuilder vertexConsumer = tess.getBuilder();
			vertexConsumer.begin(7, DefaultVertexFormats.POSITION_COLOR);
			vertexConsumer.vertex(pose, x - 1f, y - 1f, 0.01f).color((backgroundColor >> 16 & 0xFF) / 255f, (backgroundColor >> 8 & 0xFF) / 255f,
				(backgroundColor & 0xFF) / 255f, (backgroundColor >> 24 & 0xFF) / 255f).endVertex();
			vertexConsumer.vertex(pose, x - 1f, y - 1f + built.textBox()[3] + 1f, 0.01f).color((backgroundColor >> 16 & 0xFF) / 255f, (backgroundColor >> 8 & 0xFF) / 255f,
				(backgroundColor & 0xFF) / 255f, (backgroundColor >> 24 & 0xFF) / 255f).endVertex();
			vertexConsumer.vertex(pose, x + built.textBox()[2] + 1f, y - 1f + built.textBox()[3] + 1f, 0.01f).color((backgroundColor >> 16 & 0xFF) / 255f, (backgroundColor >> 8 & 0xFF) / 255f,
				(backgroundColor & 0xFF) / 255f, (backgroundColor >> 24 & 0xFF) / 255f).endVertex();
			vertexConsumer.vertex(pose, x + built.textBox()[2] + 1f, y - 1f, 0.01f).color((backgroundColor >> 16 & 0xFF) / 255f, (backgroundColor >> 8 & 0xFF) / 255f,
				(backgroundColor & 0xFF) / 255f, (backgroundColor >> 24 & 0xFF) / 255f).endVertex();
			tess.end();
		}
		ExRenderPipelines.FONT_PROGRAM.bind();
		for (Map.Entry<ResourceLocation, List<Font.CoordData>> pageGlyphs : built.coordData().object2ObjectEntrySet()) {
			BufferBuilder vertexConsumer = tess.getBuilder();
			vertexConsumer.begin(7, DefaultVertexFormats.POSITION_COLOR_TEX);
			Minecraft.getInstance().getTextureManager().bind(pageGlyphs.getKey());
			for (Font.CoordData g : pageGlyphs.getValue()) {
				int color = g.color();
				vertexConsumer.vertex(pose, g.x(), g.y(), 0).color((color >> 16 & 0xFF) / 255f, (color >> 8 & 0xFF) / 255f,
					(color & 0xFF) / 255f, (color >> 24 & 0xFF) / 255f).uv(g.u(), g.v()).endVertex();
				vertexConsumer.vertex(pose, g.x(), g.y() + g.height(), 0).color((color >> 16 & 0xFF) / 255f, (color >> 8 & 0xFF) / 255f,
					(color & 0xFF) / 255f, (color >> 24 & 0xFF) / 255f).uv(g.u(), g.v1()).endVertex();
				vertexConsumer.vertex(pose, g.x() + g.width(), g.y() + g.height(), 0).color((color >> 16 & 0xFF) / 255f, (color >> 8 & 0xFF) / 255f,
					(color & 0xFF) / 255f, (color >> 24 & 0xFF) / 255f).uv(g.u1(), g.v1()).endVertex();
				vertexConsumer.vertex(pose, g.x() + g.width(), g.y(), 0).color((color >> 16 & 0xFF) / 255f, (color >> 8 & 0xFF) / 255f,
					(color & 0xFF) / 255f, (color >> 24 & 0xFF) / 255f).uv(g.u1(), g.v()).endVertex();
			}
			tess.end();
		}
		ExRenderPipelines.FONT_PROGRAM.unbind();
		RenderSystem.color3f(1, 1, 1);
		RenderSystem.disableTexture();
		if (!built.effects().isEmpty()) {
			BufferBuilder vertexConsumer = tess.getBuilder();
			vertexConsumer.begin(7, DefaultVertexFormats.POSITION_COLOR);
			for (Font.EffectData e : built.effects()) {
				int color = e.color();
				vertexConsumer.vertex(pose, e.x(), e.y(), 0).color((color >> 16 & 0xFF) / 255f, (color >> 8 & 0xFF) / 255f,
					(color & 0xFF) / 255f, (color >> 24 & 0xFF) / 255f).endVertex();
				vertexConsumer.vertex(pose, e.x(), e.y() + e.height(), 0).color((color >> 16 & 0xFF) / 255f, (color >> 8 & 0xFF) / 255f,
					(color & 0xFF) / 255f, (color >> 24 & 0xFF) / 255f).endVertex();
				vertexConsumer.vertex(pose, e.x() + e.width(), e.y() + e.height(), 0).color((color >> 16 & 0xFF) / 255f, (color >> 8 & 0xFF) / 255f,
					(color & 0xFF) / 255f, (color >> 24 & 0xFF) / 255f).endVertex();
				vertexConsumer.vertex(pose, e.x() + e.width(), e.y(), 0).color((color >> 16 & 0xFF) / 255f, (color >> 8 & 0xFF) / 255f,
					(color & 0xFF) / 255f, (color >> 24 & 0xFF) / 255f).endVertex();
			}
			tess.end();
		}
		RenderSystem.enableTexture();
		RenderSystem.enableBlend();
	}

	private int drawInternal(IReorderingProcessor text, float x, float y, int color, boolean dropShadow, Matrix4f matrix, IRenderTypeBuffer buffer, int backgroundColor, int packedLightCoords) {
		Font.BuiltText built = font.buildText(adjustColor(color), dropShadow, text::accept);
		renderText(x, y, matrix, buffer, built, backgroundColor);
		return (int) (x + width(text));
	}

	@Override
	public int width(String text) {
		return MathHelper.ceil(font.width(text));
	}

	@Override
	public int width(ITextProperties text) {
		return MathHelper.ceil(getSplitter().stringWidth(text));
	}

	@Override
	public int width(IReorderingProcessor text) {
		return MathHelper.ceil(font.width(text));
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
	public ITextProperties substrByWidth(ITextProperties text, int maxWidth) {
		return getSplitter().headByWidth(text, maxWidth, Style.EMPTY);
	}

	@Override
	public int wordWrapHeight(String text, int maxWidth) {
		return font.lineHeight() * getSplitter().splitLines(text, maxWidth, Style.EMPTY).size();
	}

	@Override
	public List<IReorderingProcessor> split(ITextProperties text, int maxWidth) {
		return LanguageMap.getInstance().getVisualOrder(getSplitter().splitLines(text, maxWidth, Style.EMPTY));
	}

	@Override
	public CharacterManager getSplitter() {
		return font.splitter;
	}
}
