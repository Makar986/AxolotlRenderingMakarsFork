package io.github.axolotlclient.rendering;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.MemoryTracker;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tessellator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiElement;
import net.minecraft.resource.Identifier;
import org.spongepowered.asm.mixin.Unique;

public class DrawUtil implements GuiGraphicsExtension {

	private static final DrawUtil INSTANCE = new DrawUtil();

	public static GuiGraphicsExtension get() {
		return INSTANCE;
	}

	@Override
	public void axolotlclient_rendering$roundedRect(int x0, int y0, int x1, int y1, int color, int roundingPx) {
		int width = Math.abs(x1 - x0);
		int height = Math.abs(y1 - y0);
		var shader = ExRenderPipelines.ROUNDED_RECT_PROGRAM;
		shader.bind();
		shader.setUniform("Rounding",
			/*radiusTopLeft*/ roundingPx,
			/*radiusTopRight*/ roundingPx,
			/*radiusBottomLeft*/ roundingPx,
			/*radiusBottomRight*/ roundingPx);
		var buf = MemoryTracker.createFloatBuffer(16);
		buf.put(new float[]{
			width, 0, width, height,
			0, 0, width, height,
			0, height, width, height,
			width, height, width, height
		});
		buf.flip();
		shader.setUniform("Dimensions", buf);
		GuiElement.fill(x0, y0, x1, y1, color);
		shader.unbind();
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
		var tex = Minecraft.getInstance().getTextureManager().get(atlas);
		if (tex == null) return;
		int width = Math.abs(x1 - x0);
		int height = Math.abs(y1 - y0);
		var shader = ExRenderPipelines.ROUNDED_RECT_TEX_PROGRAM;
		Minecraft.getInstance().getTextureManager().bind(atlas);
		shader.bind();
		shader.setUniform("Rounding",
			/*radiusTopLeft*/ roundingPx,
			/*radiusTopRight*/ roundingPx,
			/*radiusBottomLeft*/ roundingPx,
			/*radiusBottomRight*/ roundingPx);
		var buf = MemoryTracker.createFloatBuffer(16);
		buf.put(new float[]{
			width, 0, width, height,
			0, 0, width, height,
			0, height, width, height,
			width, height, width, height
		});
		buf.flip();
		shader.setUniform("Dimensions", buf);
		GlStateManager.color4f((color >> 16 & 0xFF) / 255f, (color >> 8 & 0xFF) / 255f, (color & 0xFF) / 255f, (color >> 24 & 0xFF) / 255f);
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferBuilder = tessellator.getBuilder();
		bufferBuilder.begin(7, DefaultVertexFormat.POSITION_TEX);
		bufferBuilder.vertex(x0, y1, 0.0).texture(u0, v1).nextVertex();
		bufferBuilder.vertex(x1, y1, 0.0).texture(u1, v1).nextVertex();
		bufferBuilder.vertex(x1, y0, 0.0).texture(u1, v0).nextVertex();
		bufferBuilder.vertex(x0, y0, 0.0).texture(u0, v0).nextVertex();
		tessellator.end();
		shader.unbind();
	}
}
