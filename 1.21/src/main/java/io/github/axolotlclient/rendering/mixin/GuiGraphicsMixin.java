package io.github.axolotlclient.rendering.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import io.github.axolotlclient.rendering.ExRenderPipelines;
import io.github.axolotlclient.rendering.GuiGraphicsExtension;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
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
		int z = 0;
		consumer.addVertex(matrix, x0, y0, z).setColor(color);
		ExRenderPipelines.addRounding(consumer, roundingPx, roundingPx, roundingPx, roundingPx);
		ExRenderPipelines.addDimensions(consumer, 0, 0, width, height);

		consumer.addVertex(matrix, x0, y1, z).setColor(color);
		ExRenderPipelines.addRounding(consumer, roundingPx, roundingPx, roundingPx, roundingPx);
		ExRenderPipelines.addDimensions(consumer, 0, height, width, height);

		consumer.addVertex(matrix, x1, y1, z).setColor(color);
		ExRenderPipelines.addRounding(consumer, roundingPx, roundingPx, roundingPx, roundingPx);
		ExRenderPipelines.addDimensions(consumer, width, height, width, height);

		consumer.addVertex(matrix, x1, y0, z).setColor(color);
		ExRenderPipelines.addRounding(consumer, roundingPx, roundingPx, roundingPx, roundingPx);
		ExRenderPipelines.addDimensions(consumer, width, 0, width, height);
		this.flushIfUnmanaged();
	}

	@Override
	public void axolotlclient_rendering$outlineRoundedRect(int x0, int y0, int x1, int y1, int color, int roundingPx, float outlineWidth) {
		var matrix = pose.last().pose();
		var consumer = bufferSource.getBuffer(ExRenderPipelines.ROUNDED_RECT_OUTLINE);
		int width = x1 - x0;
		int height = y1 - y0;
		int z = 0;
		consumer.addVertex(matrix, x0, y0, z).setColor(color);
		ExRenderPipelines.addRounding(consumer, roundingPx, roundingPx, roundingPx, roundingPx);
		ExRenderPipelines.addDimensions(consumer, 0, 0, width, height);
		ExRenderPipelines.addOutlineWidth(consumer, outlineWidth);

		consumer.addVertex(matrix, x0, y1, z).setColor(color);
		ExRenderPipelines.addRounding(consumer, roundingPx, roundingPx, roundingPx, roundingPx);
		ExRenderPipelines.addDimensions(consumer, 0, height, width, height);
		ExRenderPipelines.addOutlineWidth(consumer, outlineWidth);

		consumer.addVertex(matrix, x1, y1, z).setColor(color);
		ExRenderPipelines.addRounding(consumer, roundingPx, roundingPx, roundingPx, roundingPx);
		ExRenderPipelines.addDimensions(consumer, width, height, width, height);
		ExRenderPipelines.addOutlineWidth(consumer, outlineWidth);

		consumer.addVertex(matrix, x1, y0, z).setColor(color);
		ExRenderPipelines.addRounding(consumer, roundingPx, roundingPx, roundingPx, roundingPx);
		ExRenderPipelines.addDimensions(consumer, width, 0, width, height);
		ExRenderPipelines.addOutlineWidth(consumer, outlineWidth);
		this.flushIfUnmanaged();
	}

	@Override
	public void axolotlclient_rendering$roundedBlit(ResourceLocation location, int x, int y, float u, float v, int width, int height, int uWidth, int vHeight, int textureWidth, int textureHeight, int color, int roundingPx) {
		this.innerBlit(location, x, x + width, y, y + height, (u + 0.0F) / textureWidth, (u + uWidth) / textureWidth, (v + 0.0F) / textureHeight, (v + vHeight) / textureHeight, color, roundingPx);
	}

	@Override
	public void axolotlclient_rendering$roundedBlit(ResourceLocation atlas, int x0, int y0, int x1, int y1, float u0, float u1, float v0, float v1, int rounding) {
		this.innerBlit(atlas, x0, x1, y0, y1, u0, u1, v0, v1, -1, rounding);
	}

	@Unique
	private void innerBlit(ResourceLocation atlas, int x0, int x1, int y0, int y1, float u0, float u1, float v0, float v1, int color, int roundingPx) {
		RenderSystem.setShaderTexture(0, atlas);
		RenderSystem.setShader(() -> ExRenderPipelines.ROUNDED_RECT_TEX_SHADER);
		Matrix4f matrix = this.pose.last().pose();
		int z = 0;
		BufferBuilder consumer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, ExRenderPipelines.ROUNDED_RECT_TEX_FORMAT);
		int width = x1 - x0;
		int height = y1 - y0;
		consumer.addVertex(matrix, x0, y0, z)
			.setUv(u0, v0).setColor(color);
		ExRenderPipelines.addRounding(consumer, roundingPx, roundingPx, roundingPx, roundingPx);
		ExRenderPipelines.addDimensions(consumer, 0, 0, width, height);

		consumer.addVertex(matrix, x0, y1, z);
		consumer.setUv(u0, v0).setColor(color);
		ExRenderPipelines.addRounding(consumer, roundingPx, roundingPx, roundingPx, roundingPx);
		ExRenderPipelines.addDimensions(consumer, 0, height, width, height);


		consumer.addVertex(matrix, x1, y1, z);
		consumer.setUv(u1, v1).setColor(color);
		ExRenderPipelines.addRounding(consumer, roundingPx, roundingPx, roundingPx, roundingPx);
		ExRenderPipelines.addDimensions(consumer, width, height, width, height);

		consumer.addVertex(matrix, x1, y0, z);
		consumer.setUv(u1, v0).setColor(color);
		ExRenderPipelines.addRounding(consumer, roundingPx, roundingPx, roundingPx, roundingPx);
		ExRenderPipelines.addDimensions(consumer, width, 0, width, height);
		BufferUploader.drawWithShader(consumer.buildOrThrow());
	}

	@Override
	public void axolotlclient_rendering$outlineRoundedBlit(ResourceLocation location, int x, int y, float u, float v, int width, int height, int uWidth, int vHeight, int textureWidth, int textureHeight, int color, int roundingPx, float outlineWidth) {
		this.innerBlit(location, x, x + width, y, y + height, u / textureWidth, (u + uWidth) / textureWidth, v / textureHeight, (v + vHeight) / textureHeight, color, roundingPx, outlineWidth);
	}

	@Override
	public void axolotlclient_rendering$outlineRoundedBlit(ResourceLocation atlas, int x0, int y0, int x1, int y1, float u0, float u1, float v0, float v1, int rounding, float outlineWidth) {
		this.innerBlit(atlas, x0, x1, y0, y1, u0, u1, v0, v1, -1, rounding, outlineWidth);
	}

	@Unique
	private void innerBlit(ResourceLocation atlas, int x0, int x1, int y0, int y1, float u0, float u1, float v0, float v1, int color, int roundingPx, float outlineWidth) {
		RenderSystem.setShaderTexture(0, atlas);
		RenderSystem.setShader(() -> ExRenderPipelines.ROUNDED_RECT_TEX_OUTLINE_SHADER);
		Matrix4f matrix = this.pose.last().pose();
		int z = 0;
		BufferBuilder consumer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, ExRenderPipelines.ROUNDED_RECT_TEX_OUTLINE_FORMAT);
		int width = x1 - x0;
		int height = y1 - y0;
		consumer.addVertex(matrix, x0, y0, z)
			.setUv(u0, v0).setColor(color);
		ExRenderPipelines.addRounding(consumer, roundingPx, roundingPx, roundingPx, roundingPx);
		ExRenderPipelines.addDimensions(consumer, 0, 0, width, height);
		ExRenderPipelines.addOutlineWidth(consumer, outlineWidth);

		consumer.addVertex(matrix, x0, y1, z);
		consumer.setUv(u0, v0).setColor(color);
		ExRenderPipelines.addRounding(consumer, roundingPx, roundingPx, roundingPx, roundingPx);
		ExRenderPipelines.addDimensions(consumer, 0, height, width, height);
		ExRenderPipelines.addOutlineWidth(consumer, outlineWidth);

		consumer.addVertex(matrix, x1, y1, z);
		consumer.setUv(u1, v1).setColor(color);
		ExRenderPipelines.addRounding(consumer, roundingPx, roundingPx, roundingPx, roundingPx);
		ExRenderPipelines.addDimensions(consumer, width, height, width, height);
		ExRenderPipelines.addOutlineWidth(consumer, outlineWidth);

		consumer.addVertex(matrix, x1, y0, z);
		consumer.setUv(u1, v0).setColor(color);
		ExRenderPipelines.addRounding(consumer, roundingPx, roundingPx, roundingPx, roundingPx);
		ExRenderPipelines.addDimensions(consumer, width, 0, width, height);
		ExRenderPipelines.addOutlineWidth(consumer, outlineWidth);
		BufferUploader.drawWithShader(consumer.buildOrThrow());
	}
}
