package io.github.axolotlclient.rendering;

import net.minecraft.resources.ResourceLocation;

@SuppressWarnings("unused")
public interface GuiGraphicsExtension {
	/* outlineRoundedRect */
	default void axolotlclient_rendering$outlineCircle(int cX, int cY, int color, int radius, float outlineWidth) {
		this.axolotlclient_rendering$outlineRoundedRect(cX-radius, cY-radius, cX+radius, cY+radius, color, radius, outlineWidth);
	}
	default void axolotlclient_rendering$outlineRoundedRect(int x0, int y0, int x1, int y1, int color, int roundingPx, float outlineWidth) {

	}

	/* outlineRoundedBlit */
	default void axolotlclient_rendering$outlineRoundedBlit(ResourceLocation atlas, int x, int y, int width, int height, int rounding, float outlineWidth) {
		this.axolotlclient_rendering$outlineRoundedBlit(atlas, x, y, x+width, y+height, 0, 1f, 0, 1, rounding, outlineWidth);
	}

	default void axolotlclient_rendering$outlineRoundedBlit(ResourceLocation atlas, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight, int color, int rounding, float outlineWidth) {
		this.axolotlclient_rendering$outlineRoundedBlit(atlas, x, y, u, v, width, height, width, height, textureWidth, textureHeight, color, rounding, outlineWidth);
	}

	default void axolotlclient_rendering$outlineRoundedBlit(ResourceLocation atlas, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight, int rounding, float outlineWidth) {
		this.axolotlclient_rendering$outlineRoundedBlit(atlas, x, y, u, v, width, height, width, height, textureWidth, textureHeight, rounding, outlineWidth);
	}

	default void axolotlclient_rendering$outlineRoundedBlit(ResourceLocation atlas, int x, int y, float u, float v, int width, int height, int uWidth, int vHeight, int textureWidth, int textureHeight, int rounding, float outlineWidth) {
		this.axolotlclient_rendering$outlineRoundedBlit(atlas, x, y, u, v, width, height, uWidth, vHeight, textureWidth, textureHeight, -1, rounding, outlineWidth);
	}

	default void axolotlclient_rendering$outlineRoundedBlit(ResourceLocation location, int x, int y, float u, float v, int width, int height, int uWidth, int vHeight, int textureWidth, int textureHeight, int color, int roundingPx, float outlineWidth) {
	}

	default void axolotlclient_rendering$outlineRoundedBlit(ResourceLocation atlas, int x0, int y0, int x1, int y1, float u0, float u1, float v0, float v1, int rounding, float outlineWidth) {
	}

	default void axolotlclient_rendering$outlineCircleBlit(ResourceLocation atlas, int cX, int cY, float u, float v, int radius, int textureWidth, int textureHeight, int color, int rounding, float outlineWidth) {
		this.axolotlclient_rendering$outlineRoundedBlit(atlas, cX-radius, cY-radius, u, v, radius*2, radius*2, radius*2, radius*2, textureWidth, textureHeight, color, rounding, outlineWidth);
	}

	default void axolotlclient_rendering$outlineCircleBlit(ResourceLocation atlas, int cX, int cY, float u, float v, int radius, int textureWidth, int textureHeight, int rounding, float outlineWidth) {
		this.axolotlclient_rendering$outlineRoundedBlit(atlas, cX-radius, cY-radius, u, v, radius*2, radius*2, textureWidth, textureHeight, rounding, outlineWidth);
	}

	default void axolotlclient_rendering$outlineCircleBlit(ResourceLocation atlas, int cX, int cY, float u, float v, int radius, int uWidth, int vHeight, int textureWidth, int textureHeight, int rounding, float outlineWidth) {
		this.axolotlclient_rendering$outlineRoundedBlit(atlas, cX-radius, cY-radius, u, v, radius*2, radius*2, uWidth, vHeight, textureWidth, textureHeight, -1, rounding, outlineWidth);
	}
	default void axolotlclient_rendering$outlineCircleBlit(ResourceLocation location, int cX, int cY, float u, float v, int radius, int uWidth, int vHeight, int textureWidth, int textureHeight, int color, int roundingPx, float outlineWidth) {
		this.axolotlclient_rendering$outlineRoundedBlit(location, cX-radius, cY-radius, u, v, radius*2, radius*2, uWidth, vHeight, textureWidth, textureHeight, color, roundingPx, outlineWidth);
	}

	default void axolotlclient_rendering$outlineCircleBlit(ResourceLocation atlas, int cX, int cY, int radius, float outlineWidth) {
		this.axolotlclient_rendering$outlineRoundedBlit(atlas, cX-radius, cY-radius, cX+radius, cY+radius, 0, 1f, 0, 1, radius, outlineWidth);
	}

	/* roundedRect */
	default void axolotlclient_rendering$circle(int cX, int cY, int color, int radius) {
		this.axolotlclient_rendering$roundedRect(cX-radius, cY-radius, cX+radius, cY+radius, color, radius);
	}
	default void axolotlclient_rendering$roundedRect(int x0, int y0, int x1, int y1, int color, int roundingPx) {

	}

	/* roundedBlit */
	default void axolotlclient_rendering$roundedBlit(ResourceLocation atlas, int x, int y, int width, int height, int rounding) {
		this.axolotlclient_rendering$roundedBlit(atlas, x, y, x+width, y+height, 0, 1f, 0, 1, rounding);
	}

	default void axolotlclient_rendering$roundedBlit(ResourceLocation atlas, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight, int color, int rounding) {
		this.axolotlclient_rendering$roundedBlit(atlas, x, y, u, v, width, height, width, height, textureWidth, textureHeight, color, rounding);
	}

	default void axolotlclient_rendering$roundedBlit(ResourceLocation atlas, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight, int rounding) {
		this.axolotlclient_rendering$roundedBlit(atlas, x, y, u, v, width, height, width, height, textureWidth, textureHeight, rounding);
	}

	default void axolotlclient_rendering$roundedBlit(ResourceLocation atlas, int x, int y, float u, float v, int width, int height, int uWidth, int vHeight, int textureWidth, int textureHeight, int rounding) {
		this.axolotlclient_rendering$roundedBlit(atlas, x, y, u, v, width, height, uWidth, vHeight, textureWidth, textureHeight, -1, rounding);
	}

	default void axolotlclient_rendering$roundedBlit(ResourceLocation location, int x, int y, float u, float v, int width, int height, int uWidth, int vHeight, int textureWidth, int textureHeight, int color, int roundingPx) {
	}

	default void axolotlclient_rendering$roundedBlit(ResourceLocation atlas, int x0, int y0, int x1, int y1, float u0, float u1, float v0, float v1, int rounding) {
	}

	default void axolotlclient_rendering$circleBlit(ResourceLocation atlas, int cX, int cY, float u, float v, int radius, int textureWidth, int textureHeight, int color, int rounding) {
		this.axolotlclient_rendering$roundedBlit(atlas, cX-radius, cY-radius, u, v, radius*2, radius*2, radius*2, radius*2, textureWidth, textureHeight, color, rounding);
	}

	default void axolotlclient_rendering$circleBlit(ResourceLocation atlas, int cX, int cY, float u, float v, int radius, int textureWidth, int textureHeight, int rounding) {
		this.axolotlclient_rendering$roundedBlit(atlas, cX-radius, cY-radius, u, v, radius*2, radius*2, textureWidth, textureHeight, rounding);
	}

	default void axolotlclient_rendering$circleBlit(ResourceLocation atlas, int cX, int cY, float u, float v, int radius, int uWidth, int vHeight, int textureWidth, int textureHeight, int rounding) {
		this.axolotlclient_rendering$roundedBlit(atlas, cX-radius, cY-radius, u, v, radius*2, radius*2, uWidth, vHeight, textureWidth, textureHeight, -1, rounding);
	}
	default void axolotlclient_rendering$circleBlit(ResourceLocation location, int cX, int cY, float u, float v, int radius, int uWidth, int vHeight, int textureWidth, int textureHeight, int color, int roundingPx) {
		this.axolotlclient_rendering$roundedBlit(location, cX-radius, cY-radius, u, v, radius*2, radius*2, uWidth, vHeight, textureWidth, textureHeight, color, roundingPx);
	}

	default void axolotlclient_rendering$circleBlit(ResourceLocation atlas, int cX, int cY, int radius) {
		this.axolotlclient_rendering$roundedBlit(atlas, cX-radius, cY-radius, cX+radius, cY+radius, 0, 1f, 0, 1, radius);
	}
}
