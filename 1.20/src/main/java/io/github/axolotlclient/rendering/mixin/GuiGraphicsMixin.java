package io.github.axolotlclient.rendering.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import io.github.axolotlclient.rendering.ExRenderPipelines;
import io.github.axolotlclient.rendering.GuiGraphicsExtension;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
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
	public void axolotlclient_rendering$roundedRect(int x0, int y0, int x1, int y1, int color, int roundingPx) {
		var matrix = pose.last().pose();
		var consumer = bufferSource.getBuffer(ExRenderPipelines.ROUNDED_RECT);
		int width = x1 - x0;
		int height = y1 - y0;
		int col1 = color;
		int col2 = color;
		int col3 = color;
		int col4 = color;
		int z = 0;
		int radiusTopLeft = roundingPx;
		int radiusBottomLeft = roundingPx;
		int radiusBottomRight = roundingPx;
		int radiusTopRight = roundingPx;
		consumer.vertex(matrix, x0, y0, z).color(col1);
		ExRenderPipelines.addRounding(consumer, radiusTopLeft, radiusBottomLeft, radiusBottomRight, radiusTopRight);
		ExRenderPipelines.addDimensions(consumer, 0, 0, width, height);
		consumer.endVertex();

		consumer.vertex(matrix, x0, y1, z).color(col2);
		ExRenderPipelines.addRounding(consumer, radiusTopLeft, radiusBottomLeft, radiusBottomRight, radiusTopRight);
		ExRenderPipelines.addDimensions(consumer, 0, height, width, height);
		consumer.endVertex();

		consumer.vertex(matrix, x1, y1, z).color(col3);
		ExRenderPipelines.addRounding(consumer, radiusTopLeft, radiusBottomLeft, radiusBottomRight, radiusTopRight);
		ExRenderPipelines.addDimensions(consumer, width, height, width, height);
		consumer.endVertex();

		consumer.vertex(matrix, x1, y0, z).color(col4);
		ExRenderPipelines.addRounding(consumer, radiusTopLeft, radiusBottomLeft, radiusBottomRight, radiusTopRight);
		ExRenderPipelines.addDimensions(consumer, width, 0, width, height);
		consumer.endVertex();
		this.flushIfUnmanaged();
	}

	@Override
	public void axolotlclient_rendering$roundedBlit(
		ResourceLocation atlas, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight, int color, int rounding
	) {
		this.axolotlclient_rendering$roundedBlit(atlas, x, y, u, v, width, height, width, height, textureWidth, textureHeight, color, rounding);
	}

	@Override
	public void axolotlclient_rendering$roundedBlit(ResourceLocation atlas, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight, int rounding) {
		this.axolotlclient_rendering$roundedBlit(atlas, x, y, u, v, width, height, width, height, textureWidth, textureHeight, rounding);
	}

	@Override
	public void axolotlclient_rendering$roundedBlit(
		ResourceLocation atlas,
		int x,
		int y,
		float u,
		float v,
		int width,
		int height,
		int uWidth,
		int vHeight,
		int textureWidth,
		int textureHeight,
		int rounding
	) {
		this.axolotlclient_rendering$roundedBlit(atlas, x, y, u, v, width, height, uWidth, vHeight, textureWidth, textureHeight, -1, rounding);
	}

	@Override
	public void axolotlclient_rendering$roundedBlit(ResourceLocation location, int x, int y, float u, float v, int width, int height, int uWidth, int vHeight, int textureWidth, int textureHeight, int color, int roundingPx) {
		this.innerBlit(
			location,
			x,
			x + width,
			y,
			y + height,
			(u + 0.0F) / textureWidth,
			(u + uWidth) / textureWidth,
			(v + 0.0F) / textureHeight,
			(v + vHeight) / textureHeight,
			color,
			roundingPx
		);
	}

	@Override
	public void axolotlclient_rendering$roundedBlit(ResourceLocation atlas, int x0, int y0, int x1, int y1, float u0, float u1, float v0, float v1, int rounding) {
		this.innerBlit(atlas, x0, x1, y0, y1, u0, u1, v0, v1, -1, rounding);
	}

	@Unique
	private void innerBlit(ResourceLocation atlas, int x0, int x1, int y0, int y1, float u0, float u1, float v0, float v1, int color, int roundingPx) {
		RenderSystem.setShaderTexture(0, atlas);
		RenderSystem.setShader(ExRenderPipelines::roundedRectTexShader);
		Matrix4f matrix = this.pose.last().pose();
		int z = 0;
		BufferBuilder consumer = Tesselator.getInstance().getBuilder();
		consumer.begin(VertexFormat.Mode.QUADS, ExRenderPipelines.ROUNDED_RECT_TEX_FORMAT);
		int width = x1 - x0;
		int height = y1 - y0;
		int col1 = color;
		int col2 = color;
		int col3 = color;
		int col4 = color;
		int radiusTopLeft = roundingPx;
		int radiusBottomLeft = roundingPx;
		int radiusBottomRight = roundingPx;
		int radiusTopRight = roundingPx;
		consumer.vertex(matrix, x0, y0, z)
			.uv(u0, v0).color(col1);
		ExRenderPipelines.addRounding(consumer, radiusTopLeft, radiusBottomLeft, radiusBottomRight, radiusTopRight);
		ExRenderPipelines.addDimensions(consumer, 0, 0, width, height);
		consumer.endVertex();

		consumer.vertex(matrix, x0, y1, z);
		consumer.uv(u0, v0).color(col2);
		ExRenderPipelines.addRounding(consumer, radiusTopLeft, radiusBottomLeft, radiusBottomRight, radiusTopRight);
		ExRenderPipelines.addDimensions(consumer, 0, height, width, height);
		consumer.endVertex();

		consumer.vertex(matrix, x1, y1, z);
		consumer.uv(u1, v1).color(col3);
		ExRenderPipelines.addRounding(consumer, radiusTopLeft, radiusBottomLeft, radiusBottomRight, radiusTopRight);
		ExRenderPipelines.addDimensions(consumer, width, height, width, height);
		consumer.endVertex();

		consumer.vertex(matrix, x1, y0, z);
		consumer.uv(u1, v0).color(col4);
		ExRenderPipelines.addRounding(consumer, radiusTopLeft, radiusBottomLeft, radiusBottomRight, radiusTopRight);
		ExRenderPipelines.addDimensions(consumer, width, 0, width, height);
		consumer.endVertex();
		Tesselator.getInstance().end();
	}
}
