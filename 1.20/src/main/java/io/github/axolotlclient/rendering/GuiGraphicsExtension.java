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

package io.github.axolotlclient.rendering;

import io.github.axolotlclient.rendering.font.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;

@SuppressWarnings("unused")
public interface GuiGraphicsExtension {
	/* roundedLine */
	default void axolotlclient_rendering$roundedHLine(int minX, int maxX, int y, int color) {
		this.axolotlclient_rendering$roundedRect(minX, y, maxX, y+1, color, 1);
	}

	default void axolotlclient_rendering$roundedVLine(int x, int minY, int maxY, int color) {
		this.axolotlclient_rendering$roundedRect(x, minY, x+1, maxY, color, 1);
	}

	/* outlineRoundedRect */
	default void axolotlclient_rendering$outlineCircle(int cX, int cY, int color, int radius, float outlineWidth) {
		this.axolotlclient_rendering$outlineRoundedRect(cX - radius, cY - radius, cX + radius, cY + radius, color, radius, outlineWidth);
	}

	default void axolotlclient_rendering$outlineRoundedRect(int x0, int y0, int x1, int y1, int color, int roundingPx, float outlineWidth) {
		this.axolotlclient_rendering$outlineRoundedRectVarying(x0, y0, x1, y1, color, roundingPx, roundingPx, roundingPx, roundingPx, outlineWidth);
	}

	default void axolotlclient_rendering$outlineRoundedRectVarying(int x0, int y0, int x1, int y1, int color, int roundingPxTopLeft, int roundingPxBottomLeft, int roundingPxBottomRight, int roundingPxTopRight, float outlineWidth) {
		this.axolotlclient_rendering$outlineRoundedRectGradientVarying(x0, y0, x1, y1, color, color, color, color, roundingPxTopLeft, roundingPxBottomLeft, roundingPxBottomRight, roundingPxTopRight, outlineWidth);
	}

	default void axolotlclient_rendering$outlineRoundedRectGradient(int x0, int y0, int x1, int y1, int colorTopLeft, int colorBottomLeft, int colorBottomRight, int colorTopRight, int roundingPx, float outlineWidth) {
		this.axolotlclient_rendering$outlineRoundedRectGradientVarying(x0, y0, x1, y1, colorTopLeft, colorBottomLeft, colorBottomRight, colorTopRight, roundingPx, roundingPx, roundingPx, roundingPx, outlineWidth);
	}

	default void axolotlclient_rendering$outlineRoundedRectGradientVarying(int x0, int y0, int x1, int y1, int colorTopLeft, int colorBottomLeft, int colorBottomRight, int colorTopRight, int roundingPxTopLeft, int roundingPxBottomLeft, int roundingPxBottomRight, int roundingPxTopRight, float outlineWidth) {

	}

	/* outlineRoundedBlit */
	default void axolotlclient_rendering$outlineRoundedBlit(ResourceLocation atlas, int x, int y, int width, int height, int rounding, float outlineWidth) {
		this.axolotlclient_rendering$outlineRoundedBlit(atlas, x, y, x + width, y + height, 0, 1f, 0, 1, rounding, outlineWidth);
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
		this.axolotlclient_rendering$outlineRoundedBlitVarying(location, x, y, u, v, width, height, uWidth, vHeight, textureWidth, textureHeight, color, roundingPx, roundingPx, roundingPx, roundingPx, outlineWidth);
	}

	default void axolotlclient_rendering$outlineRoundedBlitVarying(ResourceLocation atlas, int x, int y, int width, int height, int roundingPxTopLeft, int roundingPxBottomLeft, int roundingPxBottomRight, int roundingPxTopRight, float outlineWidth) {
		this.axolotlclient_rendering$outlineRoundedBlitVarying(atlas, x, y, x + width, y + height, 0, 1f, 0, 1, roundingPxTopLeft, roundingPxBottomLeft, roundingPxBottomRight, roundingPxTopRight, outlineWidth);
	}

	default void axolotlclient_rendering$outlineRoundedBlitVarying(ResourceLocation atlas, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight, int color, int roundingPxTopLeft, int roundingPxBottomLeft, int roundingPxBottomRight, int roundingPxTopRight, float outlineWidth) {
		this.axolotlclient_rendering$outlineRoundedBlitVarying(atlas, x, y, u, v, width, height, width, height, textureWidth, textureHeight, color, roundingPxTopLeft, roundingPxBottomLeft, roundingPxBottomRight, roundingPxTopRight, outlineWidth);
	}

	default void axolotlclient_rendering$outlineRoundedBlitVarying(ResourceLocation atlas, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight, int roundingPxTopLeft, int roundingPxBottomLeft, int roundingPxBottomRight, int roundingPxTopRight, float outlineWidth) {
		this.axolotlclient_rendering$outlineRoundedBlitVarying(atlas, x, y, u, v, width, height, width, height, textureWidth, textureHeight, roundingPxTopLeft, roundingPxBottomLeft, roundingPxBottomRight, roundingPxTopRight, outlineWidth);
	}

	default void axolotlclient_rendering$outlineRoundedBlitVarying(ResourceLocation atlas, int x, int y, float u, float v, int width, int height, int uWidth, int vHeight, int textureWidth, int textureHeight, int roundingPxTopLeft, int roundingPxBottomLeft, int roundingPxBottomRight, int roundingPxTopRight, float outlineWidth) {
		this.axolotlclient_rendering$outlineRoundedBlitVarying(atlas, x, y, u, v, width, height, uWidth, vHeight, textureWidth, textureHeight, -1, roundingPxTopLeft, roundingPxBottomLeft, roundingPxBottomRight, roundingPxTopRight, outlineWidth);
	}

	default void axolotlclient_rendering$outlineRoundedBlitVarying(ResourceLocation location, int x, int y, float u, float v, int width, int height, int uWidth, int vHeight, int textureWidth, int textureHeight, int color, int roundingPxTopLeft, int roundingPxBottomLeft, int roundingPxBottomRight, int roundingPxTopRight, float outlineWidth) {
	}

	default void axolotlclient_rendering$outlineRoundedBlit(ResourceLocation atlas, int x0, int y0, int x1, int y1, float u0, float u1, float v0, float v1, int rounding, float outlineWidth) {
		this.axolotlclient_rendering$outlineRoundedBlit(atlas, x0, y0, x1, y1, u0, u1, v0, v1, -1, rounding, outlineWidth);
	}

	default void axolotlclient_rendering$outlineRoundedBlit(ResourceLocation atlas, int x0, int y0, int x1, int y1, float u0, float u1, float v0, float v1, int color, int rounding, float outlineWidth) {
		this.axolotlclient_rendering$outlineRoundedBlitVarying(atlas, x0, y0, x1, y1, u0, u1, v0, v1, color, rounding, rounding, rounding, rounding, outlineWidth);
	}

	default void axolotlclient_rendering$outlineRoundedBlitVarying(ResourceLocation atlas, int x0, int y0, int x1, int y1, float u0, float u1, float v0, float v1, int roundingPxTopLeft, int roundingPxBottomLeft, int roundingPxBottomRight, int roundingPxTopRight, float outlineWidth) {
		this.axolotlclient_rendering$outlineRoundedBlitVarying(atlas, x0, y0, x1, y1, u0, u1, v0, v1, -1, roundingPxTopLeft, roundingPxBottomLeft, roundingPxBottomRight, roundingPxTopRight, outlineWidth);
	}

	default void axolotlclient_rendering$outlineRoundedBlitVarying(ResourceLocation atlas, int x0, int y0, int x1, int y1, float u0, float u1, float v0, float v1, int color, int roundingPxTopLeft, int roundingPxBottomLeft, int roundingPxBottomRight, int roundingPxTopRight, float outlineWidth) {
	}

	default void axolotlclient_rendering$outlineCircleBlit(ResourceLocation atlas, int cX, int cY, float u, float v, int radius, int textureWidth, int textureHeight, int color, int rounding, float outlineWidth) {
		this.axolotlclient_rendering$outlineRoundedBlit(atlas, cX - radius, cY - radius, u, v, radius * 2, radius * 2, radius * 2, radius * 2, textureWidth, textureHeight, color, rounding, outlineWidth);
	}

	default void axolotlclient_rendering$outlineCircleBlit(ResourceLocation atlas, int cX, int cY, float u, float v, int radius, int textureWidth, int textureHeight, int rounding, float outlineWidth) {
		this.axolotlclient_rendering$outlineRoundedBlit(atlas, cX - radius, cY - radius, u, v, radius * 2, radius * 2, textureWidth, textureHeight, rounding, outlineWidth);
	}

	default void axolotlclient_rendering$outlineCircleBlit(ResourceLocation atlas, int cX, int cY, float u, float v, int radius, int uWidth, int vHeight, int textureWidth, int textureHeight, int rounding, float outlineWidth) {
		this.axolotlclient_rendering$outlineRoundedBlit(atlas, cX - radius, cY - radius, u, v, radius * 2, radius * 2, uWidth, vHeight, textureWidth, textureHeight, -1, rounding, outlineWidth);
	}

	default void axolotlclient_rendering$outlineCircleBlit(ResourceLocation location, int cX, int cY, float u, float v, int radius, int uWidth, int vHeight, int textureWidth, int textureHeight, int color, int roundingPx, float outlineWidth) {
		this.axolotlclient_rendering$outlineRoundedBlit(location, cX - radius, cY - radius, u, v, radius * 2, radius * 2, uWidth, vHeight, textureWidth, textureHeight, color, roundingPx, outlineWidth);
	}

	default void axolotlclient_rendering$outlineCircleBlit(ResourceLocation atlas, int cX, int cY, int color, int radius, float outlineWidth) {
		this.axolotlclient_rendering$outlineRoundedBlit(atlas, cX - radius, cY - radius, cX + radius, cY + radius, 0, 1f, 0, 1, color, radius, outlineWidth);
	}

	default void axolotlclient_rendering$outlineCircleBlit(ResourceLocation atlas, int cX, int cY, int radius, float outlineWidth) {
		this.axolotlclient_rendering$outlineRoundedBlit(atlas, cX - radius, cY - radius, cX + radius, cY + radius, 0, 1f, 0, 1, radius, outlineWidth);
	}

	/* roundedRect */
	default void axolotlclient_rendering$circle(int cX, int cY, int color, int radius) {
		this.axolotlclient_rendering$roundedRect(cX - radius, cY - radius, cX + radius, cY + radius, color, radius);
	}

	default void axolotlclient_rendering$roundedRect(int x0, int y0, int x1, int y1, int color, int roundingPx) {
		this.axolotlclient_rendering$roundedRectVarying(x0, y0, x1, y1, color, roundingPx, roundingPx, roundingPx, roundingPx);
	}

	default void axolotlclient_rendering$roundedRectVarying(int x0, int y0, int x1, int y1, int color, int roundingPxTopLeft, int roundingPxBottomLeft, int roundingPxBottomRight, int roundingPxTopRight) {
		this.axolotlclient_rendering$roundedRectGradientVarying(x0, y0, x1, y1, color, color, color, color, roundingPxTopLeft, roundingPxBottomLeft, roundingPxBottomRight, roundingPxTopRight);
	}

	default void axolotlclient_rendering$circleGradient(int cX, int cY, int colorTopLeft, int colorBottomLeft, int colorBottomRight, int colorTopRight, int radius) {
		this.axolotlclient_rendering$roundedRectGradient(cX - radius, cY - radius, cX + radius, cY + radius, colorTopLeft, colorBottomLeft, colorBottomRight, colorTopRight, radius);
	}

	default void axolotlclient_rendering$roundedRectGradient(int x0, int y0, int x1, int y1, int colorTopLeft, int colorBottomLeft, int colorBottomRight, int colorTopRight, int roundingPx) {
		this.axolotlclient_rendering$roundedRectGradientVarying(x0, y0, x1, y1, colorTopLeft, colorBottomLeft, colorBottomRight, colorTopRight, roundingPx, roundingPx, roundingPx, roundingPx);
	}

	default void axolotlclient_rendering$roundedRectGradientVarying(int x0, int y0, int x1, int y1, int colorTopLeft, int colorBottomLeft, int colorBottomRight, int colorTopRight, int roundingPxTopLeft, int roundingPxBottomLeft, int roundingPxBottomRight, int roundingPxTopRight) {

	}

	/* roundedBlit */
	default void axolotlclient_rendering$roundedBlit(ResourceLocation atlas, int x, int y, int width, int height, int rounding) {
		this.axolotlclient_rendering$roundedBlit(atlas, x, y, x + width, y + height, 0, 1f, 0, 1, rounding);
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
		this.axolotlclient_rendering$roundedBlitVarying(location, x, y, u, v, width, height, uWidth, vHeight, textureWidth, textureHeight, color, roundingPx, roundingPx, roundingPx, roundingPx);
	}

	default void axolotlclient_rendering$roundedBlitVarying(ResourceLocation atlas, int x, int y, int width, int height, int roundingPxTopLeft, int roundingPxBottomLeft, int roundingPxBottomRight, int roundingPxTopRight) {
		this.axolotlclient_rendering$roundedBlitVarying(atlas, x, y, x + width, y + height, 0, 1f, 0, 1, roundingPxTopLeft, roundingPxBottomLeft, roundingPxBottomRight, roundingPxTopRight);
	}

	default void axolotlclient_rendering$roundedBlitVarying(ResourceLocation atlas, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight, int color, int roundingPxTopLeft, int roundingPxBottomLeft, int roundingPxBottomRight, int roundingPxTopRight) {
		this.axolotlclient_rendering$roundedBlitVarying(atlas, x, y, u, v, width, height, width, height, textureWidth, textureHeight, color, roundingPxTopLeft, roundingPxBottomLeft, roundingPxBottomRight, roundingPxTopRight);
	}

	default void axolotlclient_rendering$roundedBlitVarying(ResourceLocation atlas, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight, int roundingPxTopLeft, int roundingPxBottomLeft, int roundingPxBottomRight, int roundingPxTopRight) {
		this.axolotlclient_rendering$roundedBlitVarying(atlas, x, y, u, v, width, height, width, height, textureWidth, textureHeight, roundingPxTopLeft, roundingPxBottomLeft, roundingPxBottomRight, roundingPxTopRight);
	}

	default void axolotlclient_rendering$roundedBlitVarying(ResourceLocation atlas, int x, int y, float u, float v, int width, int height, int uWidth, int vHeight, int textureWidth, int textureHeight, int roundingPxTopLeft, int roundingPxBottomLeft, int roundingPxBottomRight, int roundingPxTopRight) {
		this.axolotlclient_rendering$roundedBlitVarying(atlas, x, y, u, v, width, height, uWidth, vHeight, textureWidth, textureHeight, -1, roundingPxTopLeft, roundingPxBottomLeft, roundingPxBottomRight, roundingPxTopRight);
	}

	default void axolotlclient_rendering$roundedBlitVarying(ResourceLocation location, int x, int y, float u, float v, int width, int height, int uWidth, int vHeight, int textureWidth, int textureHeight, int color, int roundingPxTopLeft, int roundingPxBottomLeft, int roundingPxBottomRight, int roundingPxTopRight) {
	}

	default void axolotlclient_rendering$roundedBlit(ResourceLocation atlas, int x0, int y0, int x1, int y1, float u0, float u1, float v0, float v1, int rounding) {
		this.axolotlclient_rendering$roundedBlit(atlas, x0, y0, x1, y1, u0, u1, v0, v1, -1, rounding);
	}

	default void axolotlclient_rendering$roundedBlit(ResourceLocation atlas, int x0, int y0, int x1, int y1, float u0, float u1, float v0, float v1, int color, int rounding) {
		this.axolotlclient_rendering$roundedBlitVarying(atlas, x0, y0, x1, y1, u0, u1, v0, v1, color, rounding, rounding, rounding, rounding);
	}

	default void axolotlclient_rendering$roundedBlitVarying(ResourceLocation atlas, int x0, int y0, int x1, int y1, float u0, float u1, float v0, float v1, int roundingPxTopLeft, int roundingPxBottomLeft, int roundingPxBottomRight, int roundingPxTopRight) {
		this.axolotlclient_rendering$roundedBlitVarying(atlas, x0, y0, x1, y1, u0, u1, v0, v1, -1, roundingPxTopLeft, roundingPxBottomLeft, roundingPxBottomRight, roundingPxTopRight);
	}

	default void axolotlclient_rendering$roundedBlitVarying(ResourceLocation atlas, int x0, int y0, int x1, int y1, float u0, float u1, float v0, float v1, int color, int roundingPxTopLeft, int roundingPxBottomLeft, int roundingPxBottomRight, int roundingPxTopRight) {
	}

	default void axolotlclient_rendering$circleBlit(ResourceLocation atlas, int cX, int cY, float u, float v, int radius, int textureWidth, int textureHeight, int color, int rounding) {
		this.axolotlclient_rendering$roundedBlit(atlas, cX - radius, cY - radius, u, v, radius * 2, radius * 2, radius * 2, radius * 2, textureWidth, textureHeight, color, rounding);
	}

	default void axolotlclient_rendering$circleBlit(ResourceLocation atlas, int cX, int cY, float u, float v, int radius, int textureWidth, int textureHeight, int rounding) {
		this.axolotlclient_rendering$roundedBlit(atlas, cX - radius, cY - radius, u, v, radius * 2, radius * 2, textureWidth, textureHeight, rounding);
	}

	default void axolotlclient_rendering$circleBlit(ResourceLocation atlas, int cX, int cY, float u, float v, int radius, int uWidth, int vHeight, int textureWidth, int textureHeight, int rounding) {
		this.axolotlclient_rendering$roundedBlit(atlas, cX - radius, cY - radius, u, v, radius * 2, radius * 2, uWidth, vHeight, textureWidth, textureHeight, -1, rounding);
	}

	default void axolotlclient_rendering$circleBlit(ResourceLocation location, int cX, int cY, float u, float v, int radius, int uWidth, int vHeight, int textureWidth, int textureHeight, int color, int roundingPx) {
		this.axolotlclient_rendering$roundedBlit(location, cX - radius, cY - radius, u, v, radius * 2, radius * 2, uWidth, vHeight, textureWidth, textureHeight, color, roundingPx);
	}

	default void axolotlclient_rendering$circleBlit(ResourceLocation atlas, int cX, int cY, int radius, int color) {
		this.axolotlclient_rendering$roundedBlit(atlas, cX - radius, cY - radius, cX + radius, cY + radius, 0, 1f, 0, 1, radius, color);
	}

	default void axolotlclient_rendering$circleBlit(ResourceLocation atlas, int cX, int cY, int radius) {
		this.axolotlclient_rendering$roundedBlit(atlas, cX - radius, cY - radius, cX + radius, cY + radius, 0, 1f, 0, 1, radius);
	}

	/*drawString*/
	default void axolotlclient_rendering$drawString(Font font, String text, float x, float y, int color, boolean shadow, int origin) {

	}

	default void axolotlclient_rendering$drawString(Font font, FormattedCharSequence text, float x, float y, int color, boolean shadow, int origin) {

	}
	default void axolotlclient_rendering$drawString(Font font, Component text, float x, float y, int color, boolean shadow, int origin) {

	}
	default void axolotlclient_rendering$drawString(Font font, String text, float x, float y, int color, boolean shadow) {
		this.axolotlclient_rendering$drawString(font, text, x, y, color, shadow, Font.ORIGIN_VERT_TOP);
	}

	default void axolotlclient_rendering$drawString(Font font, FormattedCharSequence text, float x, float y, int color, boolean shadow) {
		this.axolotlclient_rendering$drawString(font, text, x, y, color, shadow, Font.ORIGIN_VERT_TOP);
	}

	default void axolotlclient_rendering$drawString(Font font, Component text, float x, float y, int color, boolean shadow) {
		this.axolotlclient_rendering$drawString(font, text, x, y, color, shadow, Font.ORIGIN_VERT_TOP);
	}

	default void axolotlclient_rendering$drawString(Font font, String text, float x, float y, int color) {
		this.axolotlclient_rendering$drawString(font, text, x, y, color, true);
	}

	default void axolotlclient_rendering$drawString(Font font, FormattedCharSequence text, float x, float y, int color) {
		this.axolotlclient_rendering$drawString(font, text, x, y, color, true);
	}

	default void axolotlclient_rendering$drawString(Font font, Component text, float x, float y, int color) {
		this.axolotlclient_rendering$drawString(font, text, x, y, color, true);
	}

	/*drawCenteredString*/
	default void axolotlclient_rendering$drawCenteredString(Font font, String text, float x, float y, int color, boolean shadow) {
		this.axolotlclient_rendering$drawString(font, text, x, y, color, shadow, Font.ORIGIN_VERT_TOP | Font.ORIGIN_HORIZ_CENTER);
	}

	default void axolotlclient_rendering$drawCenteredString(Font font, FormattedCharSequence text, float x, float y, int color, boolean shadow) {
		this.axolotlclient_rendering$drawString(font, text, x, y, color, shadow, Font.ORIGIN_VERT_TOP | Font.ORIGIN_HORIZ_CENTER);
	}

	default void axolotlclient_rendering$drawCenteredString(Font font, Component text, float x, float y, int color, boolean shadow) {
		this.axolotlclient_rendering$drawString(font, text, x, y, color, shadow, Font.ORIGIN_VERT_TOP | Font.ORIGIN_HORIZ_CENTER);
	}

	default void axolotlclient_rendering$drawCenteredString(Font font, String text, float x, float y, int color) {
		this.axolotlclient_rendering$drawString(font, text, x, y, color, true);
	}

	default void axolotlclient_rendering$drawCenteredString(Font font, FormattedCharSequence text, float x, float y, int color) {
		this.axolotlclient_rendering$drawString(font, text, x, y, color, true);
	}

	default void axolotlclient_rendering$drawCenteredString(Font font, Component text, float x, float y, int color) {
		this.axolotlclient_rendering$drawString(font, text, x, y, color, true);
	}

	/*drawWordWrap*/
	default void axolotlclient_rendering$drawWordWrap(Font font, FormattedText text, int x, int y, int lineWidth, int color) {
		this.axolotlclient_rendering$drawWordWrap(font, text, x, y, lineWidth, color, true);
	}

	default void axolotlclient_rendering$drawWordWrap(Font font, FormattedText text, int x, int y, int lineWidth, int color, boolean dropShadow) {
		for (FormattedCharSequence formattedCharSequence : font.split(text, lineWidth)) {
			y += font.lineHeight();
			this.axolotlclient_rendering$drawString(font, formattedCharSequence, x, y, color, dropShadow, Font.ORIGIN_VERT_BASELINE);
		}
	}

	/*blitSVG*/
	default void axolotlclient_rendering$blitSVG(ResourceLocation location, int x, int y, int width, int height, int color) {

	}
}
