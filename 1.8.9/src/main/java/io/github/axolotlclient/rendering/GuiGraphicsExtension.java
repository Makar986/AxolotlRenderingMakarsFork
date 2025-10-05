package io.github.axolotlclient.rendering;

import net.minecraft.resource.Identifier;

public interface GuiGraphicsExtension {
	default void axolotlclient_rendering$circle(int cX, int cY, int color, int radius) {
		axolotlclient_rendering$roundedRect(cX-radius, cY-radius, cX+radius, cY+radius, color, radius);
	}
	default void axolotlclient_rendering$circleBlit(Identifier atlas, int cX, int cY, int radius) {
		axolotlclient_rendering$roundedBlit(atlas, cX-radius, cY-radius, cX+radius, cY+radius,0, 0, 1, 1f, radius);
	}
	default void axolotlclient_rendering$roundedRect(int x0, int y0, int x1, int y1, int color, int roundingPx) {
	}
	default void axolotlclient_rendering$roundedBlit(Identifier atlas, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight, int color, int rounding) {
	}

	default void axolotlclient_rendering$roundedBlit(Identifier atlas, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight, int rounding) {
	}

	default void axolotlclient_rendering$roundedBlit(Identifier atlas, int x, int y, float u, float v, int width, int height, int uWidth, int vHeight, int textureWidth, int textureHeight, int rounding) {
	}

	default void axolotlclient_rendering$roundedBlit(Identifier location, int x, int y, float u, float v, int width, int height, int uWidth, int vHeight, int textureWidth, int textureHeight, int color, int roundingPx) {
	}

	default void axolotlclient_rendering$roundedBlit(Identifier atlas, int x0, int y0, int x1, int y1, float u0, float u1, float v0, float v1, int rounding) {
	}
}
