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

package io.github.axolotlclient.rendering.mixin;

import java.nio.FloatBuffer;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.axolotlclient.rendering.ExRenderPipelines;
import io.github.axolotlclient.rendering.GuiGraphicsExtension;
import io.github.axolotlclient.rendering.Rendering;
import io.github.axolotlclient.rendering.ShaderManager;
import io.github.axolotlclient.rendering.font.Font;
import io.github.axolotlclient.rendering.svg.SVGCache;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.Texture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.text.ITextComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(AbstractGui.class)
public abstract class GuiGraphicsMixin implements GuiGraphicsExtension {

	@Override
	public int guiWidth() {
		return Minecraft.getInstance().getWindow().getGuiScaledWidth();
	}

	@Override
	public int guiHeight() {
		return Minecraft.getInstance().getWindow().getGuiScaledHeight();
	}

	@Unique
	private void fill(Matrix4f matrix, float x0, float y0, float x1, float y1, int colorTopLeft, int colorBottomLeft, int colorBottomRight, int colorTopRight) {
		Tessellator tesselator = Tessellator.getInstance();
		BufferBuilder bufferBuilder = tesselator.getBuilder();
		RenderSystem.enableBlend();
		RenderSystem.disableTexture();
		RenderSystem.blendFuncSeparate(770, 771, 1, 0);
		RenderSystem.color4f(1, 1, 1, 1);
		colorTopLeft = Rendering.scaleARGB(colorTopLeft);
		colorBottomLeft = Rendering.scaleARGB(colorBottomLeft);
		colorBottomRight = Rendering.scaleARGB(colorBottomRight);
		colorTopRight = Rendering.scaleARGB(colorTopRight);
		bufferBuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
		bufferBuilder.vertex(matrix, x1, y0, 0.0f).color((colorTopRight >> 16 & 0xFF) / 255f, (colorTopRight >> 8 & 0xFF) / 255f, (colorTopRight & 0xFF) / 255f, (colorTopRight >> 24 & 0xFF) / 255f).endVertex();
		bufferBuilder.vertex(matrix, x0, y0, 0.0f).color((colorTopLeft >> 16 & 0xFF) / 255f, (colorTopLeft >> 8 & 0xFF) / 255f, (colorTopLeft & 0xFF) / 255f, (colorTopLeft >> 24 & 0xFF) / 255f).endVertex();
		bufferBuilder.vertex(matrix, x0, y1, 0.0f).color((colorBottomLeft >> 16 & 0xFF) / 255f, (colorBottomLeft >> 8 & 0xFF) / 255f, (colorBottomLeft & 0xFF) / 255f, (colorBottomLeft >> 24 & 0xFF) / 255f).endVertex();
		bufferBuilder.vertex(matrix, x1, y1, 0.0f).color((colorBottomRight >> 16 & 0xFF) / 255f, (colorBottomRight >> 8 & 0xFF) / 255f, (colorBottomRight & 0xFF) / 255f, (colorBottomRight >> 24 & 0xFF) / 255f).endVertex();
		tesselator.end();
		RenderSystem.enableTexture();
		RenderSystem.disableBlend();
	}

	@Override
	public void axolotlclient_rendering$segment(MatrixStack stack, int x0, int y0, int x1, int y1, int colorX0Y0, int colorX0Y1, int colorX1Y1, int colorX1Y0, float radius) {
		Matrix4f matrix = stack.last().pose();

		float x0f = x0 <= x1 ? x0 - radius : x0 + radius;
		float y0f = y0 <= y1 ? y0 - radius : y0 + radius;
		float x1f = x0 <= x1 ? x1 + radius : x1 - radius;
		float y1f = y0 <= y1 ? y1 + radius : y1 - radius;
		float width = x1f - x0f;
		float height = y1f - y0f;
		ShaderManager.Shader shader = ExRenderPipelines.SEGMENT_PROGRAM;
		shader.bind();
		shader.setUniform("Rounding",
			/*radiusBottomRight*/ 0,
			/*radiusTopRight*/ 0,
			/*radiusBottomLeft*/ 0,
			/*radiusTopLeft*/ radius);
		FloatBuffer buf = GLAllocation.createFloatBuffer(16);
		Tessellator tesselator = Tessellator.getInstance();
		BufferBuilder bufferBuilder = tesselator.getBuilder();
		RenderSystem.enableBlend();
		RenderSystem.disableTexture();
		RenderSystem.blendFuncSeparate(770, 771, 1, 0);
		RenderSystem.color4f(1, 1, 1, 1);
		final int colorTL = Rendering.scaleARGB(colorX0Y0);
		final int colorBL = Rendering.scaleARGB(colorX0Y1);
		final int colorBR = Rendering.scaleARGB(colorX1Y1);
		final int colorTR = Rendering.scaleARGB(colorX1Y0);
		bufferBuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
		Runnable addX1Y0 = () -> bufferBuilder.vertex(matrix, x1f, y0f, 0.0f).color((colorTR >> 16 & 0xFF) / 255f, (colorTR >> 8 & 0xFF) / 255f, (colorTR & 0xFF) / 255f, (colorTR >> 24 & 0xFF) / 255f).endVertex();
		Runnable addX0Y0 = () -> bufferBuilder.vertex(matrix, x0f, y0f, 0.0f).color((colorTL >> 16 & 0xFF) / 255f, (colorTL >> 8 & 0xFF) / 255f, (colorTL & 0xFF) / 255f, (colorTL >> 24 & 0xFF) / 255f).endVertex();
		Runnable addX0Y1 = () -> bufferBuilder.vertex(matrix, x0f, y1f, 0.0f).color((colorBL >> 16 & 0xFF) / 255f, (colorBL >> 8 & 0xFF) / 255f, (colorBL & 0xFF) / 255f, (colorBL >> 24 & 0xFF) / 255f).endVertex();
		Runnable addX1Y1 = () -> bufferBuilder.vertex(matrix, x1f, y1f, 0.0f).color((colorBR >> 16 & 0xFF) / 255f, (colorBR >> 8 & 0xFF) / 255f, (colorBR & 0xFF) / 255f, (colorBR >> 24 & 0xFF) / 255f).endVertex();
		if (x0 <= x1 && y0 <= y1) {
			buf.put(new float[]{
				width, 0, width, height,
				0, 0, width, height,
				0, height, width, height,
				width, height, width, height
			});
			buf.flip();
			shader.setUniform("Dimensions", buf);
			addX1Y0.run();
			addX0Y0.run();
			addX0Y1.run();
			addX1Y1.run();
		} else if (x0 > x1 && y0 <= y1) {
			buf.put(new float[]{
				width, height, width, height,
				0, height, width, height,
				0, 0, width, height,
				width, 0, width, height
			});
			buf.flip();
			shader.setUniform("Dimensions", buf);
			addX1Y1.run();
			addX0Y1.run();
			addX0Y0.run();
			addX1Y0.run();
		} else if (x0 <= x1) {
			buf.put(new float[]{
				0, height, width, height,
				0, 0, width, height,
				width, 0, width, height,
				width, height, width, height
			});
			buf.flip();
			shader.setUniform("Dimensions", buf);
			addX0Y1.run();
			addX0Y0.run();
			addX1Y0.run();
			addX1Y1.run();
		} else {
			buf.put(new float[]{
				0, 0, width, height,
				0, height, width, height,
				width, height, width, height,
				width, 0, width, height,
			});
			buf.flip();
			shader.setUniform("Dimensions", buf);
			addX0Y0.run();
			addX0Y1.run();
			addX1Y1.run();
			addX1Y0.run();
		}
		Tessellator.getInstance().end();
		RenderSystem.enableTexture();
		RenderSystem.disableBlend();
		shader.unbind();
	}

	@Override
	public void axolotlclient_rendering$roundedRectGradientVarying(MatrixStack stack, int x0, int y0, int x1, int y1, int colorTopLeft, int colorBottomLeft, int colorBottomRight, int colorTopRight, float roundingPxTopLeft, float roundingPxBottomLeft, float roundingPxBottomRight, float roundingPxTopRight) {
		Matrix4f matrix = stack.last().pose();

		int width = x1 - x0;
		int height = y1 - y0;
		ShaderManager.Shader shader = ExRenderPipelines.ROUNDED_RECT_PROGRAM;
		shader.bind();
		shader.setUniform("Rounding",
			/*radiusBottomRight*/ roundingPxBottomRight,
			/*radiusTopRight*/ roundingPxTopRight,
			/*radiusBottomLeft*/ roundingPxBottomLeft,
			/*radiusTopLeft*/ roundingPxTopLeft);
		FloatBuffer buf = GLAllocation.createFloatBuffer(16);
		buf.put(new float[]{
			width, 0, width, height,
			0, 0, width, height,
			0, height, width, height,
			width, height, width, height
		});
		buf.flip();
		shader.setUniform("Dimensions", buf);
		RenderSystem.color4f(1, 1, 1, 1);
		fill(matrix, x0, y0, x1, y1, colorTopLeft, colorBottomLeft, colorBottomRight, colorTopRight);
		shader.unbind();
	}

	@Override
	public void axolotlclient_rendering$outlineRoundedRectGradientVarying(MatrixStack stack, int x0, int y0, int x1, int y1, int colorTopLeft, int colorBottomLeft, int colorBottomRight, int colorTopRight, float roundingPxTopLeft, float roundingPxBottomLeft, float roundingPxBottomRight, float roundingPxTopRight, float outlineWidth) {
		Matrix4f matrix = stack.last().pose();

		int width = Math.abs(x1 - x0);
		int height = Math.abs(y1 - y0);
		ShaderManager.Shader shader = ExRenderPipelines.ROUNDED_RECT_OUTLINE_PROGRAM;
		shader.bind();
		shader.setUniform("Rounding",
			/*radiusBottomRight*/ roundingPxBottomRight,
			/*radiusTopRight*/ roundingPxTopRight,
			/*radiusBottomLeft*/ roundingPxBottomLeft,
			/*radiusTopLeft*/ roundingPxTopLeft);
		FloatBuffer buf = GLAllocation.createFloatBuffer(16);
		buf.put(new float[]{
			width, 0, width, height,
			0, 0, width, height,
			0, height, width, height,
			width, height, width, height
		});
		buf.flip();
		shader.setUniform("Dimensions", buf);
		shader.setUniform("OutlineWidth", outlineWidth);
		RenderSystem.color4f(1, 1, 1, 1);
		fill(matrix, x0, y0, x1, y1, colorTopLeft, colorBottomLeft, colorBottomRight, colorTopRight);
		shader.unbind();
	}

	@Override
	public void axolotlclient_rendering$roundedBlitVarying(MatrixStack stack, ResourceLocation location, int x, int y, float u, float v, int width, int height, int uWidth, int vHeight, int textureWidth, int textureHeight, int color, float roundingPxTopLeft, float roundingPxBottomLeft, float roundingPxBottomRight, float roundingPxTopRight) {
		this.innerBlit(stack, location, x, x + width, y, y + height, (u + 0.0F) / textureWidth, (u + uWidth) / textureWidth, (v + 0.0F) / textureHeight, (v + vHeight) / textureHeight, color, roundingPxTopLeft, roundingPxBottomLeft, roundingPxBottomRight, roundingPxTopRight);
	}

	@Override
	public void axolotlclient_rendering$roundedBlitVarying(MatrixStack stack, ResourceLocation atlas, int x0, int y0, int x1, int y1, float u0, float u1, float v0, float v1, int color, float roundingPxTopLeft, float roundingPxBottomLeft, float roundingPxBottomRight, float roundingPxTopRight) {
		this.innerBlit(stack, atlas, x0, x1, y0, y1, u0, u1, v0, v1, color, roundingPxTopLeft, roundingPxBottomLeft, roundingPxBottomRight, roundingPxTopRight);
	}

	@Unique
	private void innerBlit(MatrixStack stack, ResourceLocation atlas, int x0, int x1, int y0, int y1, float u0, float u1, float v0, float v1, int color, float roundingPxTopLeft, float roundingPxBottomLeft, float roundingPxBottomRight, float roundingPxTopRight) {
		Matrix4f matrix = stack.last().pose();
		Texture tex = Minecraft.getInstance().getTextureManager().getTexture(atlas);
		if (tex == null) return;
		int width = Math.abs(x1 - x0);
		int height = Math.abs(y1 - y0);
		ShaderManager.Shader shader = ExRenderPipelines.ROUNDED_RECT_TEX_PROGRAM;
		RenderSystem.enableBlend();
		Minecraft.getInstance().getTextureManager().bind(atlas);
		shader.bind();
		shader.setUniform("Rounding",
			/*radiusBottomRight*/ roundingPxBottomRight,
			/*radiusTopRight*/ roundingPxTopRight,
			/*radiusBottomLeft*/ roundingPxBottomLeft,
			/*radiusTopLeft*/ roundingPxTopLeft);
		FloatBuffer buf = GLAllocation.createFloatBuffer(16);
		buf.put(new float[]{
			width, 0, width, height,
			0, 0, width, height,
			0, height, width, height,
			width, height, width, height
		});
		buf.flip();
		shader.setUniform("Dimensions", buf);
		RenderSystem.color4f((color >> 16 & 0xFF) / 255f, (color >> 8 & 0xFF) / 255f, (color & 0xFF) / 255f, (color >> 24 & 0xFF) / 255f);
		Tessellator tesselator = Tessellator.getInstance();
		BufferBuilder bufferBuilder = tesselator.getBuilder();
		bufferBuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
		bufferBuilder.vertex(matrix, x0, y1, 0.0f).uv(u0, v1).endVertex();
		bufferBuilder.vertex(matrix, x1, y1, 0.0f).uv(u1, v1).endVertex();
		bufferBuilder.vertex(matrix, x1, y0, 0.0f).uv(u1, v0).endVertex();
		bufferBuilder.vertex(matrix, x0, y0, 0.0f).uv(u0, v0).endVertex();
		Tessellator.getInstance().end();
		shader.unbind();
	}

	@Override
	public void axolotlclient_rendering$outlineRoundedBlitVarying(MatrixStack stack, ResourceLocation location, int x, int y, float u, float v, int width, int height, int uWidth, int vHeight, int textureWidth, int textureHeight, int color, float roundingPxTopLeft, float roundingPxBottomLeft, float roundingPxBottomRight, float roundingPxTopRight, float outlineWidth) {
		this.innerBlit(stack, location, x, x + width, y, y + height, u / textureWidth, (u + uWidth) / textureWidth, v / textureHeight, (v + vHeight) / textureHeight, color, roundingPxTopLeft, roundingPxBottomLeft, roundingPxBottomRight, roundingPxTopRight, outlineWidth);
	}

	@Override
	public void axolotlclient_rendering$outlineRoundedBlitVarying(MatrixStack stack, ResourceLocation atlas, int x0, int y0, int x1, int y1, float u0, float u1, float v0, float v1, int color, float roundingPxTopLeft, float roundingPxBottomLeft, float roundingPxBottomRight, float roundingPxTopRight, float outlineWidth) {
		this.innerBlit(stack, atlas, x0, x1, y0, y1, u0, u1, v0, v1, color, roundingPxTopLeft, roundingPxBottomLeft, roundingPxBottomRight, roundingPxTopRight, outlineWidth);
	}

	@Unique
	private void innerBlit(MatrixStack stack, ResourceLocation atlas, int x0, int x1, int y0, int y1, float u0, float u1, float v0, float v1, int color, float roundingPxTopLeft, float roundingPxBottomLeft, float roundingPxBottomRight, float roundingPxTopRight, float outlineWidth) {
		Matrix4f matrix = stack.last().pose();
		Texture tex = Minecraft.getInstance().getTextureManager().getTexture(atlas);
		if (tex == null) return;
		int width = Math.abs(x1 - x0);
		int height = Math.abs(y1 - y0);
		ShaderManager.Shader shader = ExRenderPipelines.ROUNDED_RECT_TEX_OUTLINE_PROGRAM;
		RenderSystem.enableBlend();
		Minecraft.getInstance().getTextureManager().bind(atlas);
		shader.bind();
		shader.setUniform("Rounding",
			/*radiusBottomRight*/ roundingPxBottomRight,
			/*radiusTopRight*/ roundingPxTopRight,
			/*radiusBottomLeft*/ roundingPxBottomLeft,
			/*radiusTopLeft*/ roundingPxTopLeft);
		FloatBuffer buf = GLAllocation.createFloatBuffer(16);
		buf.put(new float[]{
			width, 0, width, height,
			0, 0, width, height,
			0, height, width, height,
			width, height, width, height
		});
		buf.flip();
		shader.setUniform("Dimensions", buf);
		shader.setUniform("OutlineWidth", outlineWidth);
		RenderSystem.color4f((color >> 16 & 0xFF) / 255f, (color >> 8 & 0xFF) / 255f, (color & 0xFF) / 255f, (color >> 24 & 0xFF) / 255f);
		Tessellator tesselator = Tessellator.getInstance();
		BufferBuilder bufferBuilder = tesselator.getBuilder();
		bufferBuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
		bufferBuilder.vertex(matrix, x0, y1, 0.0f).uv(u0, v1).endVertex();
		bufferBuilder.vertex(matrix, x1, y1, 0.0f).uv(u1, v1).endVertex();
		bufferBuilder.vertex(matrix, x1, y0, 0.0f).uv(u1, v0).endVertex();
		bufferBuilder.vertex(matrix, x0, y0, 0.0f).uv(u0, v0).endVertex();
		Tessellator.getInstance().end();
		shader.unbind();
	}

	@Override
	public void axolotlclient_rendering$drawString(MatrixStack stack, Font font, String text, float x, float y, int color, boolean shadow, int origin) {
		font.renderText(stack, text, x, y, color, shadow, origin);
	}

	@Override
	public void axolotlclient_rendering$drawString(MatrixStack stack, Font font, IReorderingProcessor text, float x, float y, int color, boolean shadow, int origin) {
		font.renderText(stack, text, x, y, color, shadow, origin);
	}

	@Override
	public void axolotlclient_rendering$drawString(MatrixStack stack, Font font, ITextComponent text, float x, float y, int color, boolean shadow, int origin) {
		font.renderText(stack, text, x, y, color, shadow, origin);
	}

	@Override
	public void axolotlclient_rendering$blitSVG(MatrixStack stack, ResourceLocation location, int x, int y, int width, int height, int color) {
		double scale = Minecraft.getInstance().getWindow().getGuiScale();
		ResourceLocation key = SVGCache.getInstance().load(location, (float) (width * scale), (float) (height * scale));
		RenderSystem.enableBlend();
		RenderSystem.blendFuncSeparate(
			GlStateManager.SourceFactor.SRC_ALPHA,
			GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
			GlStateManager.SourceFactor.ONE,
			GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA
		);
		Minecraft.getInstance().getTextureManager().bind(key);
		Matrix4f matrix4f = stack.last().pose();
		BufferBuilder bufferBuilder = Tessellator.getInstance().getBuilder();
		bufferBuilder.begin(7, DefaultVertexFormats.POSITION_COLOR_TEX);
		bufferBuilder.vertex(matrix4f, x, y, 0).color((color >> 16 & 0xFF) / 255f, (color >> 8 & 0xFF) / 255f, (color & 0xFF) / 255f, (color >> 24 & 0xFF) / 255f).uv((float) 0, 0f).endVertex();
		bufferBuilder.vertex(matrix4f, x, y + height, 0).color((color >> 16 & 0xFF) / 255f, (color >> 8 & 0xFF) / 255f, (color & 0xFF) / 255f, (color >> 24 & 0xFF) / 255f).uv((float) 0, (float) 1).endVertex();
		bufferBuilder.vertex(matrix4f, x + width, y + height, 0).color((color >> 16 & 0xFF) / 255f, (color >> 8 & 0xFF) / 255f, (color & 0xFF) / 255f, (color >> 24 & 0xFF) / 255f).uv(1f, (float) 1).endVertex();
		bufferBuilder.vertex(matrix4f, x + width, y, 0).color((color >> 16 & 0xFF) / 255f, (color >> 8 & 0xFF) / 255f, (color & 0xFF) / 255f, (color >> 24 & 0xFF) / 255f).uv(1f, 0f).endVertex();
		Tessellator.getInstance().end();
	}
}
