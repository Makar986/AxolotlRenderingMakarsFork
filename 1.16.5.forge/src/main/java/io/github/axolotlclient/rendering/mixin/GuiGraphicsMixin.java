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

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.MemoryTracker;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import io.github.axolotlclient.rendering.ExRenderPipelines;
import io.github.axolotlclient.rendering.GuiGraphicsExtension;
import io.github.axolotlclient.rendering.LiquidGlassStyle;
import io.github.axolotlclient.rendering.ShaderManager;
import io.github.axolotlclient.rendering.Rendering;
import io.github.axolotlclient.rendering.font.Font;
import io.github.axolotlclient.rendering.svg.SVGCache;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(GuiComponent.class)
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
		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder bufferBuilder = tesselator.getBuilder();
		RenderSystem.enableBlend();
		RenderSystem.disableTexture();
		RenderSystem.blendFuncSeparate(770, 771, 1, 0);
		RenderSystem.color4f(1, 1, 1, 1);
		colorTopLeft = Rendering.scaleARGB(colorTopLeft);
		colorBottomLeft = Rendering.scaleARGB(colorBottomLeft);
		colorBottomRight = Rendering.scaleARGB(colorBottomRight);
		colorTopRight = Rendering.scaleARGB(colorTopRight);
		bufferBuilder.begin(7, DefaultVertexFormat.POSITION_COLOR);
		bufferBuilder.vertex(matrix, x1, y0, 0.0f).color((colorTopRight >> 16 & 0xFF) / 255f, (colorTopRight >> 8 & 0xFF) / 255f, (colorTopRight & 0xFF) / 255f, (colorTopRight >> 24 & 0xFF) / 255f).endVertex();
		bufferBuilder.vertex(matrix, x0, y0, 0.0f).color((colorTopLeft >> 16 & 0xFF) / 255f, (colorTopLeft >> 8 & 0xFF) / 255f, (colorTopLeft & 0xFF) / 255f, (colorTopLeft >> 24 & 0xFF) / 255f).endVertex();
		bufferBuilder.vertex(matrix, x0, y1, 0.0f).color((colorBottomLeft >> 16 & 0xFF) / 255f, (colorBottomLeft >> 8 & 0xFF) / 255f, (colorBottomLeft & 0xFF) / 255f, (colorBottomLeft >> 24 & 0xFF) / 255f).endVertex();
		bufferBuilder.vertex(matrix, x1, y1, 0.0f).color((colorBottomRight >> 16 & 0xFF) / 255f, (colorBottomRight >> 8 & 0xFF) / 255f, (colorBottomRight & 0xFF) / 255f, (colorBottomRight >> 24 & 0xFF) / 255f).endVertex();
		tesselator.end();
		RenderSystem.enableTexture();
		RenderSystem.disableBlend();
	}

	@Override
	public void axolotlclient_rendering$segment(PoseStack stack, int x0, int y0, int x1, int y1, int colorX0Y0, int colorX0Y1, int colorX1Y1, int colorX1Y0, float radius) {
		var matrix = stack.last().pose();

		var x0f = x0 <= x1 ? x0 - radius : x0 + radius;
		var y0f = y0 <= y1 ? y0 - radius : y0 + radius;
		var x1f = x0 <= x1 ? x1 + radius : x1 - radius;
		var y1f = y0 <= y1 ? y1 + radius : y1 - radius;
		var width = x1f - x0f;
		var height = y1f - y0f;
		var shader = ExRenderPipelines.SEGMENT_PROGRAM;
		shader.bind();
		shader.setUniform("Rounding",
			/*radiusBottomRight*/ 0,
			/*radiusTopRight*/ 0,
			/*radiusBottomLeft*/ 0,
			/*radiusTopLeft*/ radius);
		var buf = MemoryTracker.createFloatBuffer(16);
		/*buf.put(new float[]{
			width, 0, width, height,
			0, 0, width, height,
			0, height, width, height,
			width, height, width, height
		});*/
		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder bufferBuilder = tesselator.getBuilder();
		RenderSystem.enableBlend();
		RenderSystem.disableTexture();
		RenderSystem.blendFuncSeparate(770, 771, 1, 0);
		RenderSystem.color4f(1, 1, 1, 1);
		var colorTL = Rendering.scaleARGB(colorX0Y0);
		var colorBL = Rendering.scaleARGB(colorX0Y1);
		var colorBR = Rendering.scaleARGB(colorX1Y1);
		var colorTR = Rendering.scaleARGB(colorX1Y0);
		bufferBuilder.begin(7, DefaultVertexFormat.POSITION_COLOR);
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
		Tesselator.getInstance().end();
		RenderSystem.enableTexture();
		RenderSystem.disableBlend();
		shader.unbind();
	}

	@Override
	public void axolotlclient_rendering$roundedRectGradientVarying(PoseStack stack, int x0, int y0, int x1, int y1, int colorTopLeft, int colorBottomLeft, int colorBottomRight, int colorTopRight, float roundingPxTopLeft, float roundingPxBottomLeft, float roundingPxBottomRight, float roundingPxTopRight) {
		var matrix = stack.last().pose();

		int width = x1 - x0;
		int height = y1 - y0;
		var shader = ExRenderPipelines.ROUNDED_RECT_PROGRAM;
		shader.bind();
		shader.setUniform("Rounding",
			/*radiusBottomRight*/ roundingPxBottomRight,
			/*radiusTopRight*/ roundingPxTopRight,
			/*radiusBottomLeft*/ roundingPxBottomLeft,
			/*radiusTopLeft*/ roundingPxTopLeft);
		var buf = MemoryTracker.createFloatBuffer(16);
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
	public void axolotlclient_rendering$outlineRoundedRectGradientVarying(PoseStack stack, int x0, int y0, int x1, int y1, int colorTopLeft, int colorBottomLeft, int colorBottomRight, int colorTopRight, float roundingPxTopLeft, float roundingPxBottomLeft, float roundingPxBottomRight, float roundingPxTopRight, float outlineWidth) {
		var matrix = stack.last().pose();

		int width = Math.abs(x1 - x0);
		int height = Math.abs(y1 - y0);
		var shader = ExRenderPipelines.ROUNDED_RECT_OUTLINE_PROGRAM;
		shader.bind();
		shader.setUniform("Rounding",
			/*radiusBottomRight*/ roundingPxBottomRight,
			/*radiusTopRight*/ roundingPxTopRight,
			/*radiusBottomLeft*/ roundingPxBottomLeft,
			/*radiusTopLeft*/ roundingPxTopLeft);
		var buf = MemoryTracker.createFloatBuffer(16);
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
	public void axolotlclient_rendering$roundedBlitVarying(PoseStack stack, ResourceLocation location, int x, int y, float u, float v, int width, int height, int uWidth, int vHeight, int textureWidth, int textureHeight, int color, float roundingPxTopLeft, float roundingPxBottomLeft, float roundingPxBottomRight, float roundingPxTopRight) {
		this.innerBlit(stack, location, x, x + width, y, y + height, (u + 0.0F) / textureWidth, (u + uWidth) / textureWidth, (v + 0.0F) / textureHeight, (v + vHeight) / textureHeight, color, roundingPxTopLeft, roundingPxBottomLeft, roundingPxBottomRight, roundingPxTopRight);
	}

	@Override
	public void axolotlclient_rendering$roundedBlitVarying(PoseStack stack, ResourceLocation atlas, int x0, int y0, int x1, int y1, float u0, float u1, float v0, float v1, int color, float roundingPxTopLeft, float roundingPxBottomLeft, float roundingPxBottomRight, float roundingPxTopRight) {
		this.innerBlit(stack, atlas, x0, x1, y0, y1, u0, u1, v0, v1, color, roundingPxTopLeft, roundingPxBottomLeft, roundingPxBottomRight, roundingPxTopRight);
	}

	@Unique
	private void innerBlit(PoseStack stack, ResourceLocation atlas, int x0, int x1, int y0, int y1, float u0, float u1, float v0, float v1, int color, float roundingPxTopLeft, float roundingPxBottomLeft, float roundingPxBottomRight, float roundingPxTopRight) {
		/*RenderSystem.setShaderTexture(0, atlas);
		RenderSystem.enableBlend();
		RenderSystem.blendFuncSeparate(
			GlStateManager.SourceFactor.SRC_ALPHA,
			GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
			GlStateManager.SourceFactor.ONE,
			GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA
		);
		RenderSystem.setShader(() -> ExRenderPipelines.ROUNDED_RECT_TEX_SHADER);
		Matrix4f matrix = stack.last().pose();
		int z = 0;
		BufferBuilder consumer = Tesselator.getInstance().getBuilder();
		consumer.begin(7, ExRenderPipelines.ROUNDED_RECT_TEX_FORMAT);
		int width = x1 - x0;
		int height = y1 - y0;
		consumer.vertex(matrix, x0, y0, z).uv(u0, v0).color(color);
		ExRenderPipelines.addRounding(consumer, roundingPxTopLeft, roundingPxBottomLeft, roundingPxBottomRight, roundingPxTopRight);
		ExRenderPipelines.addDimensions(consumer, 0, 0, width, height);
		consumer.endVertex();

		consumer.vertex(matrix, x0, y1, z);
		consumer.uv(u0, v1).color(color);
		ExRenderPipelines.addRounding(consumer, roundingPxTopLeft, roundingPxBottomLeft, roundingPxBottomRight, roundingPxTopRight);
		ExRenderPipelines.addDimensions(consumer, 0, height, width, height);
		consumer.endVertex();

		consumer.vertex(matrix, x1, y1, z);
		consumer.uv(u1, v1).color(color);
		ExRenderPipelines.addRounding(consumer, roundingPxTopLeft, roundingPxBottomLeft, roundingPxBottomRight, roundingPxTopRight);
		ExRenderPipelines.addDimensions(consumer, width, height, width, height);
		consumer.endVertex();

		consumer.vertex(matrix, x1, y0, z);
		consumer.uv(u1, v0).color(color);
		ExRenderPipelines.addRounding(consumer, roundingPxTopLeft, roundingPxBottomLeft, roundingPxBottomRight, roundingPxTopRight);
		ExRenderPipelines.addDimensions(consumer, width, 0, width, height);
		consumer.endVertex();
		BufferUploader.end(consumer);*/
		Matrix4f matrix = stack.last().pose();
		var tex = Minecraft.getInstance().getTextureManager().getTexture(atlas);
		if (tex == null) return;
		int width = Math.abs(x1 - x0);
		int height = Math.abs(y1 - y0);
		var shader = ExRenderPipelines.ROUNDED_RECT_TEX_PROGRAM;
		RenderSystem.enableBlend();
		Minecraft.getInstance().getTextureManager().bind(atlas);
		shader.bind();
		shader.setUniform("Rounding",
			/*radiusBottomRight*/ roundingPxBottomRight,
			/*radiusTopRight*/ roundingPxTopRight,
			/*radiusBottomLeft*/ roundingPxBottomLeft,
			/*radiusTopLeft*/ roundingPxTopLeft);
		var buf = MemoryTracker.createFloatBuffer(16);
		buf.put(new float[]{
			width, 0, width, height,
			0, 0, width, height,
			0, height, width, height,
			width, height, width, height
		});
		buf.flip();
		shader.setUniform("Dimensions", buf);
		RenderSystem.color4f((color >> 16 & 0xFF) / 255f, (color >> 8 & 0xFF) / 255f, (color & 0xFF) / 255f, (color >> 24 & 0xFF) / 255f);
		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder bufferBuilder = tesselator.getBuilder();
		bufferBuilder.begin(7, DefaultVertexFormat.POSITION_TEX);
		bufferBuilder.vertex(matrix, x0, y1, 0.0f).uv(u0, v1).endVertex();
		bufferBuilder.vertex(matrix, x1, y1, 0.0f).uv(u1, v1).endVertex();
		bufferBuilder.vertex(matrix, x1, y0, 0.0f).uv(u1, v0).endVertex();
		bufferBuilder.vertex(matrix, x0, y0, 0.0f).uv(u0, v0).endVertex();
		Tesselator.getInstance().end();
		shader.unbind();
	}

	@Override
	public void axolotlclient_rendering$outlineRoundedBlitVarying(PoseStack stack, ResourceLocation location, int x, int y, float u, float v, int width, int height, int uWidth, int vHeight, int textureWidth, int textureHeight, int color, float roundingPxTopLeft, float roundingPxBottomLeft, float roundingPxBottomRight, float roundingPxTopRight, float outlineWidth) {
		this.innerBlit(stack, location, x, x + width, y, y + height, u / textureWidth, (u + uWidth) / textureWidth, v / textureHeight, (v + vHeight) / textureHeight, color, roundingPxTopLeft, roundingPxBottomLeft, roundingPxBottomRight, roundingPxTopRight, outlineWidth);
	}

	@Override
	public void axolotlclient_rendering$outlineRoundedBlitVarying(PoseStack stack, ResourceLocation atlas, int x0, int y0, int x1, int y1, float u0, float u1, float v0, float v1, int color, float roundingPxTopLeft, float roundingPxBottomLeft, float roundingPxBottomRight, float roundingPxTopRight, float outlineWidth) {
		this.innerBlit(stack, atlas, x0, x1, y0, y1, u0, u1, v0, v1, color, roundingPxTopLeft, roundingPxBottomLeft, roundingPxBottomRight, roundingPxTopRight, outlineWidth);
	}

	@Unique
	private void innerBlit(PoseStack stack, ResourceLocation atlas, int x0, int x1, int y0, int y1, float u0, float u1, float v0, float v1, int color, float roundingPxTopLeft, float roundingPxBottomLeft, float roundingPxBottomRight, float roundingPxTopRight, float outlineWidth) {
		/*RenderSystem.setShaderTexture(0, atlas);
		RenderSystem.enableBlend();
		RenderSystem.blendFuncSeparate(
			GlStateManager.SourceFactor.SRC_ALPHA,
			GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
			GlStateManager.SourceFactor.ONE,
			GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA
		);
		RenderSystem.setShader(() -> ExRenderPipelines.ROUNDED_RECT_TEX_OUTLINE_SHADER);
		Matrix4f matrix = stack.last().pose();
		int z = 0;
		BufferBuilder consumer = Tesselator.getInstance().getBuilder();
		consumer.begin(7, ExRenderPipelines.ROUNDED_RECT_TEX_OUTLINE_FORMAT);
		int width = x1 - x0;
		int height = y1 - y0;
		consumer.vertex(matrix, x0, y0, z).uv(u0, v0).color(color);
		ExRenderPipelines.addRounding(consumer, roundingPxTopLeft, roundingPxBottomLeft, roundingPxBottomRight, roundingPxTopRight);
		ExRenderPipelines.addDimensions(consumer, 0, 0, width, height);
		ExRenderPipelines.addOutlineWidth(consumer, outlineWidth);
		consumer.endVertex();

		consumer.vertex(matrix, x0, y1, z);
		consumer.uv(u0, v1).color(color);
		ExRenderPipelines.addRounding(consumer, roundingPxTopLeft, roundingPxBottomLeft, roundingPxBottomRight, roundingPxTopRight);
		ExRenderPipelines.addDimensions(consumer, 0, height, width, height);
		ExRenderPipelines.addOutlineWidth(consumer, outlineWidth);
		consumer.endVertex();

		consumer.vertex(matrix, x1, y1, z);
		consumer.uv(u1, v1).color(color);
		ExRenderPipelines.addRounding(consumer, roundingPxTopLeft, roundingPxBottomLeft, roundingPxBottomRight, roundingPxTopRight);
		ExRenderPipelines.addDimensions(consumer, width, height, width, height);
		ExRenderPipelines.addOutlineWidth(consumer, outlineWidth);
		consumer.endVertex();

		consumer.vertex(matrix, x1, y0, z);
		consumer.uv(u1, v0).color(color);
		ExRenderPipelines.addRounding(consumer, roundingPxTopLeft, roundingPxBottomLeft, roundingPxBottomRight, roundingPxTopRight);
		ExRenderPipelines.addDimensions(consumer, width, 0, width, height);
		ExRenderPipelines.addOutlineWidth(consumer, outlineWidth);
		consumer.endVertex();
		BufferUploader.end(consumer);*/
		Matrix4f matrix = stack.last().pose();
		var tex = Minecraft.getInstance().getTextureManager().getTexture(atlas);
		if (tex == null) return;
		int width = Math.abs(x1 - x0);
		int height = Math.abs(y1 - y0);
		var shader = ExRenderPipelines.ROUNDED_RECT_TEX_OUTLINE_PROGRAM;
		RenderSystem.enableBlend();
		Minecraft.getInstance().getTextureManager().bind(atlas);
		shader.bind();
		shader.setUniform("Rounding",
			/*radiusBottomRight*/ roundingPxBottomRight,
			/*radiusTopRight*/ roundingPxTopRight,
			/*radiusBottomLeft*/ roundingPxBottomLeft,
			/*radiusTopLeft*/ roundingPxTopLeft);
		var buf = MemoryTracker.createFloatBuffer(16);
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
		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder bufferBuilder = tesselator.getBuilder();
		bufferBuilder.begin(7, DefaultVertexFormat.POSITION_TEX);
		bufferBuilder.vertex(matrix, x0, y1, 0.0f).uv(u0, v1).endVertex();
		bufferBuilder.vertex(matrix, x1, y1, 0.0f).uv(u1, v1).endVertex();
		bufferBuilder.vertex(matrix, x1, y0, 0.0f).uv(u1, v0).endVertex();
		bufferBuilder.vertex(matrix, x0, y0, 0.0f).uv(u0, v0).endVertex();
		Tesselator.getInstance().end();
		shader.unbind();
	}

	@Override
	public void axolotlclient_rendering$drawString(PoseStack stack, Font font, String text, float x, float y, int color, boolean shadow, int origin) {
		font.renderText(stack, text, x, y, color, shadow, origin);
	}

	@Override
	public void axolotlclient_rendering$drawString(PoseStack stack, Font font, FormattedCharSequence text, float x, float y, int color, boolean shadow, int origin) {
		font.renderText(stack, text, x, y, color, shadow, origin);
	}

	@Override
	public void axolotlclient_rendering$drawString(PoseStack stack, Font font, Component text, float x, float y, int color, boolean shadow, int origin) {
		font.renderText(stack, text, x, y, color, shadow, origin);
	}

	@Override
	public void axolotlclient_rendering$blitSVG(PoseStack stack, ResourceLocation location, int x, int y, int width, int height, int color) {
		var scale = Minecraft.getInstance().getWindow().getGuiScale();
		var key = SVGCache.getInstance().load(location, (float) (width * scale), (float) (height * scale));
		RenderSystem.enableBlend();
		RenderSystem.blendFuncSeparate(
			GlStateManager.SourceFactor.SRC_ALPHA,
			GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
			GlStateManager.SourceFactor.ONE,
			GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA
		);
		Minecraft.getInstance().getTextureManager().bind(key);
		Matrix4f matrix4f = stack.last().pose();
		BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
		bufferBuilder.begin(7, DefaultVertexFormat.POSITION_COLOR_TEX);
		bufferBuilder.vertex(matrix4f, x, y, 0).color((color >> 16 & 0xFF) / 255f, (color >> 8 & 0xFF) / 255f, (color & 0xFF) / 255f, (color >> 24 & 0xFF) / 255f).uv((float) 0, 0f).endVertex();
		bufferBuilder.vertex(matrix4f, x, y + height, 0).color((color >> 16 & 0xFF) / 255f, (color >> 8 & 0xFF) / 255f, (color & 0xFF) / 255f, (color >> 24 & 0xFF) / 255f).uv((float) 0, (float) 1).endVertex();
		bufferBuilder.vertex(matrix4f, x + width, y + height, 0).color((color >> 16 & 0xFF) / 255f, (color >> 8 & 0xFF) / 255f, (color & 0xFF) / 255f, (color >> 24 & 0xFF) / 255f).uv(1f, (float) 1).endVertex();
		bufferBuilder.vertex(matrix4f, x + width, y, 0).color((color >> 16 & 0xFF) / 255f, (color >> 8 & 0xFF) / 255f, (color & 0xFF) / 255f, (color >> 24 & 0xFF) / 255f).uv(1f, 0f).endVertex();
		Tesselator.getInstance().end();
	}

	@Override
	public void axolotlclient_rendering$roundedGaussianBlur(PoseStack stack, float x, float y, float width, float height, float radius, float blurRadius, float sigma, int tintColor) {
		ShaderManager.Shader shader = ExRenderPipelines.GAUSSIAN_BLUR_PROGRAM;
		if (shader == null) return;
		Minecraft mc = Minecraft.getInstance();
		RenderSystem.bindTexture(mc.getMainRenderTarget().getColorTextureId());
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		shader.bind();
		shader.setUniform("Dimensions", x, y, width, height);
		shader.setUniform("Rounding", radius, radius, radius, radius);
		shader.setUniform("ScreenSize", (float) mc.getWindow().getWidth(), (float) mc.getWindow().getHeight());
		shader.setUniform("BlurRadius", blurRadius);
		shader.setUniform("Sigma", sigma);
		int c = Rendering.scaleARGB(tintColor);
		RenderSystem.color4f((c >> 16 & 0xFF) / 255f, (c >> 8 & 0xFF) / 255f, (c & 0xFF) / 255f, (c >> 24 & 0xFF) / 255f);
		axolotlclient_rendering$screenQuad(stack, x, y, width, height);
		shader.unbind();
		RenderSystem.color4f(1f, 1f, 1f, 1f);
	}

	@Override
	public void axolotlclient_rendering$liquidGlass(PoseStack stack, float x, float y, float width, float height, float rounding, int tintColor, int borderColor, float borderWidth, float blurRadius, float distortion) {
		LiquidGlassStyle style = LiquidGlassStyle.create()
			.tint(tintColor, ((tintColor >> 24) & 0xFF) / 255.0f)
			.border(borderColor, borderWidth)
			.blurRadius(blurRadius)
			.refractionDispersion(distortion > 0 ? distortion * 1.5f : 7.0f);
		this.axolotlclient_rendering$liquidGlass(stack, x, y, width, height, rounding, style);
	}

	@Override
	public void axolotlclient_rendering$liquidGlass(PoseStack stack, float x, float y, float width, float height, float rounding, LiquidGlassStyle style) {
		ShaderManager.Shader shader = ExRenderPipelines.LIQUID_GLASS_PROGRAM;
		if (shader == null || style == null) return;
		Minecraft mc = Minecraft.getInstance();
		RenderSystem.bindTexture(mc.getMainRenderTarget().getColorTextureId());
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		shader.bind();
		shader.setUniform("Dimensions", x, y, width, height);
		shader.setUniform("Rounding", rounding, rounding, rounding, rounding);
		shader.setUniform("ScreenSize", (float) mc.getWindow().getWidth(), (float) mc.getWindow().getHeight());
		shader.setUniform("BlurRadius", style.getBlurRadius());
		shader.setUniform("Optics0", style.getRefThickness(), style.getRefFactor(), style.getRefDispersion(), style.getRefFresnelRange());
		shader.setUniform("Optics1", style.getRefFresnelHardness(), style.getRefFresnelFactor(), style.getGlareRange(), style.getGlareHardness());

		int tc = Rendering.scaleARGB(style.getTintColor());
		float ta = style.getTintAlpha() > 0 ? style.getTintAlpha() : ((tc >> 24 & 0xFF) / 255f);
		shader.setUniform("GlassTint", (tc >> 16 & 0xFF) / 255f, (tc >> 8 & 0xFF) / 255f, (tc & 0xFF) / 255f, ta);

		int bc = Rendering.scaleARGB(style.getBorderColor());
		shader.setUniform("BorderColor", (bc >> 16 & 0xFF) / 255f, (bc >> 8 & 0xFF) / 255f, (bc & 0xFF) / 255f, (bc >> 24 & 0xFF) / 255f);
		shader.setUniform("BorderWidth", style.getBorderWidth());
		RenderSystem.color4f(1f, 1f, 1f, 1f);
		axolotlclient_rendering$screenQuad(stack, x, y, width, height);
		shader.unbind();
	}

	@Unique
	private void axolotlclient_rendering$screenQuad(PoseStack stack, float x, float y, float width, float height) {
		Matrix4f matrix = stack.last().pose();
		BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
		bufferBuilder.begin(7, DefaultVertexFormat.POSITION_TEX);
		bufferBuilder.vertex(matrix, x, y + height, 0).uv(0f, 0f).endVertex();
		bufferBuilder.vertex(matrix, x + width, y + height, 0).uv(1f, 0f).endVertex();
		bufferBuilder.vertex(matrix, x + width, y, 0).uv(1f, 1f).endVertex();
		bufferBuilder.vertex(matrix, x, y, 0).uv(0f, 1f).endVertex();
		Tesselator.getInstance().end();
	}

	@Override
	public void axolotlclient_rendering$drawMsdfString(PoseStack stack, io.github.axolotlclient.rendering.font.msdf.MsdfFont font, String text, float x, float y, float size, int color, boolean dropShadow) {
		if (font == null) return;
		if (dropShadow) {
			font.drawWithShadow(stack.last().pose(), text, x, y, size, color);
		} else {
			font.draw(stack.last().pose(), text, x, y, size, color);
		}
	}
}
