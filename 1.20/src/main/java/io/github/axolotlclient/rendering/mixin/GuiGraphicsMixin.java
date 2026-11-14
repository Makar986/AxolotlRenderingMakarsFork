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
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import io.github.axolotlclient.rendering.ExRenderPipelines;
import io.github.axolotlclient.rendering.GuiGraphicsExtension;
import io.github.axolotlclient.rendering.font.Font;
import io.github.axolotlclient.rendering.svg.SVGCache;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(GuiGraphics.class)
public abstract class GuiGraphicsMixin implements GuiGraphicsExtension {

	@Shadow
	@Final
	private PoseStack pose;

	@Shadow
	@Final
	private MultiBufferSource.BufferSource bufferSource;

	@SuppressWarnings("DeprecatedIsStillUsed")
	@Shadow
	@Deprecated
	protected abstract void flushIfUnmanaged();

	@Override
	public void axolotlclient_rendering$segment(int x0, int y0, int x1, int y1, int colorX0Y0, int colorX0Y1, int colorX1Y1, int colorX1Y0, float radius) {
		var matrix = pose.last().pose();
		var consumer = bufferSource.getBuffer(ExRenderPipelines.SEGMENT);
		var x0f = x0 <= x1 ? x0 - radius : x0 + radius;
		var y0f = y0 <= y1 ? y0 - radius : y0 + radius;
		var x1f = x0 <= x1 ? x1 + radius : x1 - radius;
		var y1f = y0 <= y1 ? y1 + radius : y1 - radius;
		var width = x1f - x0f;
		var height = y1f - y0f;
		int z = 0;
		Runnable addX0Y0 = () -> {
			consumer.vertex(matrix, x0f, y0f, z).color(colorX0Y0);
			ExRenderPipelines.addRounding(consumer, radius, 0, 0, 0);
			ExRenderPipelines.addDimensions(consumer, 0, 0, width, height);
			consumer.endVertex();
		}, addX0Y1 = () -> {
			consumer.vertex(matrix, x0f, y1f, z).color(colorX0Y1);
			ExRenderPipelines.addRounding(consumer, radius, 0, 0, 0);
			ExRenderPipelines.addDimensions(consumer, 0, height, width, height);
			consumer.endVertex();
		}, addX1Y1 = () -> {
			consumer.vertex(matrix, x1f, y1f, z).color(colorX1Y1);
			ExRenderPipelines.addRounding(consumer, radius, 0, 0, 0);
			ExRenderPipelines.addDimensions(consumer, width, height, width, height);
			consumer.endVertex();
		}, addX1Y0 = () -> {
			consumer.vertex(matrix, x1f, y0f, z).color(colorX1Y0);
			ExRenderPipelines.addRounding(consumer, radius, 0, 0, 0);
			ExRenderPipelines.addDimensions(consumer, width, 0, width, height);
			consumer.endVertex();
		};
		if (x0 <= x1 && y0 <= y1) {
			addX0Y0.run();
			addX0Y1.run();
			addX1Y1.run();
			addX1Y0.run();
		} else if (x0 > x1 && y0 <= y1) {
			addX0Y0.run();
			addX1Y0.run();
			addX1Y1.run();
			addX0Y1.run();
		} else if (x0 <= x1) {
			addX0Y1.run();
			addX0Y0.run();
			addX1Y0.run();
			addX1Y1.run();
		} else {
			addX1Y0.run();
			addX0Y0.run();
			addX0Y1.run();
			addX1Y1.run();
		}

		this.flushIfUnmanaged();
	}

	@Override
	public void axolotlclient_rendering$roundedRectGradientVarying(int x0, int y0, int x1, int y1, int colorTopLeft, int colorBottomLeft, int colorBottomRight, int colorTopRight, float roundingPxTopLeft, float roundingPxBottomLeft, float roundingPxBottomRight, float roundingPxTopRight) {
		var matrix = pose.last().pose();
		var consumer = bufferSource.getBuffer(ExRenderPipelines.ROUNDED_RECT);
		int width = x1 - x0;
		int height = y1 - y0;
		int z = 0;
		consumer.vertex(matrix, x0, y0, z).color(colorTopLeft);
		ExRenderPipelines.addRounding(consumer, roundingPxTopLeft, roundingPxBottomLeft, roundingPxBottomRight, roundingPxTopRight);
		ExRenderPipelines.addDimensions(consumer, 0, 0, width, height);
		consumer.endVertex();

		consumer.vertex(matrix, x0, y1, z).color(colorBottomLeft);
		ExRenderPipelines.addRounding(consumer, roundingPxTopLeft, roundingPxBottomLeft, roundingPxBottomRight, roundingPxTopRight);
		ExRenderPipelines.addDimensions(consumer, 0, height, width, height);
		consumer.endVertex();

		consumer.vertex(matrix, x1, y1, z).color(colorBottomRight);
		ExRenderPipelines.addRounding(consumer, roundingPxTopLeft, roundingPxBottomLeft, roundingPxBottomRight, roundingPxTopRight);
		ExRenderPipelines.addDimensions(consumer, width, height, width, height);
		consumer.endVertex();

		consumer.vertex(matrix, x1, y0, z).color(colorTopRight);
		ExRenderPipelines.addRounding(consumer, roundingPxTopLeft, roundingPxBottomLeft, roundingPxBottomRight, roundingPxTopRight);
		ExRenderPipelines.addDimensions(consumer, width, 0, width, height);
		consumer.endVertex();
		this.flushIfUnmanaged();
	}

	@Override
	public void axolotlclient_rendering$outlineRoundedRectGradientVarying(int x0, int y0, int x1, int y1, int colorTopLeft, int colorBottomLeft, int colorBottomRight, int colorTopRight, float roundingPxTopLeft, float roundingPxBottomLeft, float roundingPxBottomRight, float roundingPxTopRight, float outlineWidth) {
		var matrix = pose.last().pose();
		var consumer = bufferSource.getBuffer(ExRenderPipelines.ROUNDED_RECT_OUTLINE);
		int width = x1 - x0;
		int height = y1 - y0;
		int z = 0;
		consumer.vertex(matrix, x0, y0, z).color(colorTopLeft);
		ExRenderPipelines.addRounding(consumer, roundingPxTopLeft, roundingPxBottomLeft, roundingPxBottomRight, roundingPxTopRight);
		ExRenderPipelines.addDimensions(consumer, 0, 0, width, height);
		ExRenderPipelines.addOutlineWidth(consumer, outlineWidth);
		consumer.endVertex();

		consumer.vertex(matrix, x0, y1, z).color(colorBottomLeft);
		ExRenderPipelines.addRounding(consumer, roundingPxTopLeft, roundingPxBottomLeft, roundingPxBottomRight, roundingPxTopRight);
		ExRenderPipelines.addDimensions(consumer, 0, height, width, height);
		ExRenderPipelines.addOutlineWidth(consumer, outlineWidth);
		consumer.endVertex();

		consumer.vertex(matrix, x1, y1, z).color(colorBottomRight);
		ExRenderPipelines.addRounding(consumer, roundingPxTopLeft, roundingPxBottomLeft, roundingPxBottomRight, roundingPxTopRight);
		ExRenderPipelines.addDimensions(consumer, width, height, width, height);
		ExRenderPipelines.addOutlineWidth(consumer, outlineWidth);
		consumer.endVertex();

		consumer.vertex(matrix, x1, y0, z).color(colorTopRight);
		ExRenderPipelines.addRounding(consumer, roundingPxTopLeft, roundingPxBottomLeft, roundingPxBottomRight, roundingPxTopRight);
		ExRenderPipelines.addDimensions(consumer, width, 0, width, height);
		ExRenderPipelines.addOutlineWidth(consumer, outlineWidth);
		consumer.endVertex();
		this.flushIfUnmanaged();
	}

	@Override
	public void axolotlclient_rendering$roundedBlitVarying(ResourceLocation location, int x, int y, float u, float v, int width, int height, int uWidth, int vHeight, int textureWidth, int textureHeight, int color, float roundingPxTopLeft, float roundingPxBottomLeft, float roundingPxBottomRight, float roundingPxTopRight) {
		this.innerBlit(location, x, x + width, y, y + height, (u + 0.0F) / textureWidth, (u + uWidth) / textureWidth, (v + 0.0F) / textureHeight, (v + vHeight) / textureHeight, color, roundingPxTopLeft, roundingPxBottomLeft, roundingPxBottomRight, roundingPxTopRight);
	}

	@Override
	public void axolotlclient_rendering$roundedBlitVarying(ResourceLocation atlas, int x0, int y0, int x1, int y1, float u0, float u1, float v0, float v1, int color, float roundingPxTopLeft, float roundingPxBottomLeft, float roundingPxBottomRight, float roundingPxTopRight) {
		this.innerBlit(atlas, x0, x1, y0, y1, u0, u1, v0, v1, color, roundingPxTopLeft, roundingPxBottomLeft, roundingPxBottomRight, roundingPxTopRight);
	}

	@Unique
	private void innerBlit(ResourceLocation atlas, int x0, int x1, int y0, int y1, float u0, float u1, float v0, float v1, int color, float roundingPxTopLeft, float roundingPxBottomLeft, float roundingPxBottomRight, float roundingPxTopRight) {
		RenderSystem.setShaderTexture(0, atlas);
		RenderSystem.enableBlend();
		RenderSystem.blendFuncSeparate(
			GlStateManager.SourceFactor.SRC_ALPHA,
			GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
			GlStateManager.SourceFactor.ONE,
			GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA
		);
		RenderSystem.setShader(() -> ExRenderPipelines.ROUNDED_RECT_TEX_SHADER);
		Matrix4f matrix = this.pose.last().pose();
		int z = 0;
		BufferBuilder consumer = Tesselator.getInstance().getBuilder();
		consumer.begin(VertexFormat.Mode.QUADS, ExRenderPipelines.ROUNDED_RECT_TEX_FORMAT);
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
		BufferUploader.drawWithShader(consumer.end());
	}

	@Override
	public void axolotlclient_rendering$outlineRoundedBlitVarying(ResourceLocation location, int x, int y, float u, float v, int width, int height, int uWidth, int vHeight, int textureWidth, int textureHeight, int color, float roundingPxTopLeft, float roundingPxBottomLeft, float roundingPxBottomRight, float roundingPxTopRight, float outlineWidth) {
		this.innerBlit(location, x, x + width, y, y + height, u / textureWidth, (u + uWidth) / textureWidth, v / textureHeight, (v + vHeight) / textureHeight, color, roundingPxTopLeft, roundingPxBottomLeft, roundingPxBottomRight, roundingPxTopRight, outlineWidth);
	}

	@Override
	public void axolotlclient_rendering$outlineRoundedBlitVarying(ResourceLocation atlas, int x0, int y0, int x1, int y1, float u0, float u1, float v0, float v1, int color, float roundingPxTopLeft, float roundingPxBottomLeft, float roundingPxBottomRight, float roundingPxTopRight, float outlineWidth) {
		this.innerBlit(atlas, x0, x1, y0, y1, u0, u1, v0, v1, color, roundingPxTopLeft, roundingPxBottomLeft, roundingPxBottomRight, roundingPxTopRight, outlineWidth);
	}

	@Unique
	private void innerBlit(ResourceLocation atlas, int x0, int x1, int y0, int y1, float u0, float u1, float v0, float v1, int color, float roundingPxTopLeft, float roundingPxBottomLeft, float roundingPxBottomRight, float roundingPxTopRight, float outlineWidth) {
		RenderSystem.setShaderTexture(0, atlas);
		RenderSystem.enableBlend();
		RenderSystem.blendFuncSeparate(
			GlStateManager.SourceFactor.SRC_ALPHA,
			GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
			GlStateManager.SourceFactor.ONE,
			GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA
		);
		RenderSystem.setShader(() -> ExRenderPipelines.ROUNDED_RECT_TEX_OUTLINE_SHADER);
		Matrix4f matrix = this.pose.last().pose();
		int z = 0;
		BufferBuilder consumer = Tesselator.getInstance().getBuilder();
		consumer.begin(VertexFormat.Mode.QUADS, ExRenderPipelines.ROUNDED_RECT_TEX_OUTLINE_FORMAT);
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
		BufferUploader.drawWithShader(consumer.end());
	}

	@Override
	public void axolotlclient_rendering$drawString(Font font, String text, float x, float y, int color, boolean shadow, int origin) {
		font.renderText((GuiGraphics) (Object) this, text, x, y, color, shadow, origin);
	}

	@Override
	public void axolotlclient_rendering$drawString(Font font, FormattedCharSequence text, float x, float y, int color, boolean shadow, int origin) {
		font.renderText((GuiGraphics) (Object) this, text, x, y, color, shadow, origin);
	}

	@Override
	public void axolotlclient_rendering$drawString(Font font, Component text, float x, float y, int color, boolean shadow, int origin) {
		font.renderText((GuiGraphics) (Object) this, text, x, y, color, shadow, origin);
	}

	@Override
	public void axolotlclient_rendering$blitSVG(ResourceLocation location, int x, int y, int width, int height, int color) {
		var scale = Minecraft.getInstance().getWindow().getGuiScale();
		var key = SVGCache.getInstance().load(location, (float) (width * scale), (float) (height * scale));
		RenderSystem.setShaderTexture(0, key);
		RenderSystem.enableBlend();
		RenderSystem.blendFuncSeparate(
			GlStateManager.SourceFactor.SRC_ALPHA,
			GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
			GlStateManager.SourceFactor.ONE,
			GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA
		);
		RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
		Matrix4f matrix4f = this.pose.last().pose();
		BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
		bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
		bufferBuilder.vertex(matrix4f, x, y, 0).uv((float) 0, 0f).color(color).endVertex();
		bufferBuilder.vertex(matrix4f, x, y + height, 0).uv((float) 0, (float) 1).color(color).endVertex();
		bufferBuilder.vertex(matrix4f, x + width, y + height, 0).uv(1f, (float) 1).color(color).endVertex();
		bufferBuilder.vertex(matrix4f, x + width, y, 0).uv(1f, 0f).color(color).endVertex();
		BufferUploader.drawWithShader(bufferBuilder.end());
	}

	@Override
	public void axolotlclient_rendering$roundedGaussianBlur(float x, float y, float width, float height, float radius, float blurRadius, float sigma, int tintColor) {
		if (ExRenderPipelines.GAUSSIAN_BLUR_SHADER == null) return;
		var mc = Minecraft.getInstance();
		var renderTarget = mc.getMainRenderTarget();
		if (renderTarget == null) return;

		RenderSystem.setShaderTexture(0, renderTarget.getColorTextureId());
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.setShader(() -> ExRenderPipelines.GAUSSIAN_BLUR_SHADER);

		Matrix4f matrix4f = this.pose.last().pose();
		BufferBuilder consumer = Tesselator.getInstance().getBuilder();
		consumer.begin(VertexFormat.Mode.QUADS, ExRenderPipelines.ROUNDED_RECT_TEX_FORMAT);

		consumer.vertex(matrix4f, x, y, 0).uv(0f, 1f).color(tintColor);
		ExRenderPipelines.addRounding(consumer, radius, radius, radius, radius);
		ExRenderPipelines.addDimensions(consumer, 0, 0, width, height);
		consumer.endVertex();

		consumer.vertex(matrix4f, x, y + height, 0).uv(0f, 0f).color(tintColor);
		ExRenderPipelines.addRounding(consumer, radius, radius, radius, radius);
		ExRenderPipelines.addDimensions(consumer, 0, height, width, height);
		consumer.endVertex();

		consumer.vertex(matrix4f, x + width, y + height, 0).uv(1f, 0f).color(tintColor);
		ExRenderPipelines.addRounding(consumer, radius, radius, radius, radius);
		ExRenderPipelines.addDimensions(consumer, width, height, width, height);
		consumer.endVertex();

		consumer.vertex(matrix4f, x + width, y, 0).uv(1f, 1f).color(tintColor);
		ExRenderPipelines.addRounding(consumer, radius, radius, radius, radius);
		ExRenderPipelines.addDimensions(consumer, width, 0, width, height);
		consumer.endVertex();

		BufferUploader.drawWithShader(consumer.end());
	}

	@Override
	public void axolotlclient_rendering$liquidGlass(float x, float y, float width, float height, float rounding, int tintColor, int borderColor, float borderWidth, float blurRadius, float distortion) {
		if (ExRenderPipelines.LIQUID_GLASS_SHADER == null) return;
		var mc = Minecraft.getInstance();
		var renderTarget = mc.getMainRenderTarget();
		if (renderTarget == null) return;

		RenderSystem.setShaderTexture(0, renderTarget.getColorTextureId());
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.setShader(() -> ExRenderPipelines.LIQUID_GLASS_SHADER);

		Matrix4f matrix4f = this.pose.last().pose();
		BufferBuilder consumer = Tesselator.getInstance().getBuilder();
		consumer.begin(VertexFormat.Mode.QUADS, ExRenderPipelines.ROUNDED_RECT_TEX_FORMAT);

		consumer.vertex(matrix4f, x, y, 0).uv(0f, 1f).color(tintColor);
		ExRenderPipelines.addRounding(consumer, rounding, rounding, rounding, rounding);
		ExRenderPipelines.addDimensions(consumer, 0, 0, width, height);
		consumer.endVertex();

		consumer.vertex(matrix4f, x, y + height, 0).uv(0f, 0f).color(tintColor);
		ExRenderPipelines.addRounding(consumer, rounding, rounding, rounding, rounding);
		ExRenderPipelines.addDimensions(consumer, 0, height, width, height);
		consumer.endVertex();

		consumer.vertex(matrix4f, x + width, y + height, 0).uv(1f, 0f).color(tintColor);
		ExRenderPipelines.addRounding(consumer, rounding, rounding, rounding, rounding);
		ExRenderPipelines.addDimensions(consumer, width, height, width, height);
		consumer.endVertex();

		consumer.vertex(matrix4f, x + width, y, 0).uv(1f, 1f).color(tintColor);
		ExRenderPipelines.addRounding(consumer, rounding, rounding, rounding, rounding);
		ExRenderPipelines.addDimensions(consumer, width, 0, width, height);
		consumer.endVertex();

		BufferUploader.drawWithShader(consumer.end());
	}
}
