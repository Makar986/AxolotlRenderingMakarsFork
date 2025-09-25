package io.github.axolotlclient.rendering;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tessellator;
import net.minecraft.client.gui.GuiElement;
import net.minecraft.resource.Identifier;
import org.spongepowered.asm.mixin.Unique;

import static org.lwjgl.opengl.GL20.glUseProgram;

public class DrawUtil implements GuiGraphicsExtension {

	private static final DrawUtil INSTANCE = new DrawUtil();

	public static GuiGraphicsExtension get() {
		return INSTANCE;
	}

	@Override
	public void axolotlclient_rendering$roundedRect(int x0, int y0, int x1, int y1, int color, int roundingPx) {
		int radiusTopLeft = roundingPx;
		int radiusBottomLeft = roundingPx;
		int radiusBottomRight = roundingPx;
		int radiusTopRight = roundingPx;
		var tess = Tessellator.getInstance();
		GuiElement.fill(x0, y0, x1, y1, -1);
		if (x0 < x1) {
			int n = x0;
			x0 = x1;
			x1 = n;
		}

		if (y0 < y1) {
			int n = y0;
			y0 = y1;
			y1 = n;
		}
		int width = Math.abs(x1 - x0);
		int height = Math.abs(y1 - y0);
		var consumer = tess.getBuilder();

		//color = -1;
		int z = 0;

		glUseProgram(ExRenderPipelines.ROUNDED_RECT_PROGRAM);
		//ShaderManager.uploadUniforms(ExRenderPipelines.ROUNDED_RECT_PROGRAM);
		int alpha = (color >> 24 & 0xFF);
		int red = (color >> 16 & 0xFF);
		int green = (color >> 8 & 0xFF);
		int blue = (color & 0xFF);
		consumer.begin(7, ExRenderPipelines.ROUNDED_RECT_FORMAT);

		consumer.vertex(x1, y0, z).color(red, green, blue, alpha);
		ExRenderPipelines.addRounding(consumer, radiusTopLeft, radiusBottomLeft, radiusBottomRight, radiusTopRight);
		ExRenderPipelines.addDimensions(consumer, width, 0, width, height);
		consumer.nextVertex();

		consumer.vertex(x0, y0, z).color(red, green, blue, alpha);
		ExRenderPipelines.addRounding(consumer, radiusTopLeft, radiusBottomLeft, radiusBottomRight, radiusTopRight);
		ExRenderPipelines.addDimensions(consumer, 0, 0, width, height);
		consumer.nextVertex();

		consumer.vertex(x0, y1, z).color(red, green, blue, alpha);
		ExRenderPipelines.addRounding(consumer, radiusTopLeft, radiusBottomLeft, radiusBottomRight, radiusTopRight);
		ExRenderPipelines.addDimensions(consumer, 0, height, width, height);
		consumer.nextVertex();

		consumer.vertex(x1, y1, z).color(red, green, blue, alpha);
		ExRenderPipelines.addRounding(consumer, radiusTopLeft, radiusBottomLeft, radiusBottomRight, radiusTopRight);
		ExRenderPipelines.addDimensions(consumer, width, height, width, height);
		tess.end();
		glUseProgram(0);
		//effect.draw(consumer);
		//effect.clear();
	}

	@Override
	public void axolotlclient_rendering$roundedBlit(
		Identifier atlas, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight, int color, int rounding
	) {
		this.axolotlclient_rendering$roundedBlit(atlas, x, y, u, v, width, height, width, height, textureWidth, textureHeight, color, rounding);
	}

	@Override
	public void axolotlclient_rendering$roundedBlit(Identifier atlas, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight, int rounding) {
		this.axolotlclient_rendering$roundedBlit(atlas, x, y, u, v, width, height, width, height, textureWidth, textureHeight, rounding);
	}

	@Override
	public void axolotlclient_rendering$roundedBlit(
		Identifier atlas,
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
	public void axolotlclient_rendering$roundedBlit(Identifier location, int x, int y, float u, float v, int width, int height, int uWidth, int vHeight, int textureWidth, int textureHeight, int color, int roundingPx) {
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
	public void axolotlclient_rendering$roundedBlit(Identifier atlas, int x0, int y0, int x1, int y1, float u0, float u1, float v0, float v1, int rounding) {
		this.innerBlit(atlas, x0, x1, y0, y1, u0, u1, v0, v1, -1, rounding);
	}

	@Unique
	private void innerBlit(Identifier atlas, int x0, int x1, int y0, int y1, float u0, float u1, float v0, float v1, int color, int roundingPx) {
		/*int z = 0;
		Minecraft.getInstance().getTextureManager().bind(atlas);
		BufferBuilder consumer = Tessellator.getInstance().getBuilder();
		var effect = ExRenderPipelines.ROUNDED_RECT_TEX_SHADER;
		effect.apply();
		consumer.begin(7, ExRenderPipelines.ROUNDED_RECT_TEX_FORMAT);
		int width = x1 - x0;
		int height = y1 - y0;
		int radiusTopLeft = roundingPx;
		int radiusBottomLeft = roundingPx;
		int radiusBottomRight = roundingPx;
		int radiusTopRight = roundingPx;
		consumer.vertex(x0, y0, z)
			.texture(u0, v0).color(color >> 16 & 0xFF, color >> 8 & 0xFF, color & 0xFF, color >> 24 & 0xFF);
		ExRenderPipelines.addRounding(consumer, radiusTopLeft, radiusBottomLeft, radiusBottomRight, radiusTopRight);
		ExRenderPipelines.addDimensions(consumer, 0, 0, width, height);
		consumer.nextVertex();

		consumer.vertex(x0, y1, z);
		consumer.texture(u0, v0).color(color >> 16 & 0xFF, color >> 8 & 0xFF, color & 0xFF, color >> 24 & 0xFF);
		ExRenderPipelines.addRounding(consumer, radiusTopLeft, radiusBottomLeft, radiusBottomRight, radiusTopRight);
		ExRenderPipelines.addDimensions(consumer, 0, height, width, height);
		consumer.nextVertex();

		consumer.vertex(x1, y1, z);
		consumer.texture(u1, v1).color(color >> 16 & 0xFF, color >> 8 & 0xFF, color & 0xFF, color >> 24 & 0xFF);
		ExRenderPipelines.addRounding(consumer, radiusTopLeft, radiusBottomLeft, radiusBottomRight, radiusTopRight);
		ExRenderPipelines.addDimensions(consumer, width, height, width, height);
		consumer.nextVertex();

		consumer.vertex(x1, y0, z);
		consumer.texture(u1, v0).color(color >> 16 & 0xFF, color >> 8 & 0xFF, color & 0xFF, color >> 24 & 0xFF);
		ExRenderPipelines.addRounding(consumer, radiusTopLeft, radiusBottomLeft, radiusBottomRight, radiusTopRight);
		ExRenderPipelines.addDimensions(consumer, width, 0, width, height);
		consumer.nextVertex();
		Tessellator.getInstance().end();
		effect.clear();*/
	}
}
