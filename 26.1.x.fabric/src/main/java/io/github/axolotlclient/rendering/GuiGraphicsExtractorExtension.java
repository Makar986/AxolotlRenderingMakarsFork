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
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;

@SuppressWarnings("unused")
public interface GuiGraphicsExtractorExtension {
	/* roundedLine */
	default void axolotlclient_rendering$roundedHLine(int minX, int maxX, int y, int color) {
		this.axolotlclient_rendering$roundedRect(minX, y, maxX + 1, y + 1, color, 1.25f);
	}

	default void axolotlclient_rendering$roundedVLine(int x, int minY, int maxY, int color) {
		this.axolotlclient_rendering$roundedRect(x, minY + 1, x + 1, maxY, color, 1.25f);
	}

	/* segment */
	/// @see #axolotlclient_rendering$segment(int, int, int, int, int, int, int, int, float)
	default void axolotlclient_rendering$segmentGradientH(int x0, int y0, int x1, int y1, int colorX0, int colorX1, float radius) {
		axolotlclient_rendering$segment(x0, y0, x1, y1, colorX0, colorX0, colorX1, colorX1, radius);
	}

	/// @see #axolotlclient_rendering$segment(int, int, int, int, int, int, int, int, float)
	default void axolotlclient_rendering$segmentGradientV(int x0, int y0, int x1, int y1, int colorY0, int colorY1, float radius) {
		axolotlclient_rendering$segment(x0, y0, x1, y1, colorY0, colorY1, colorY1, colorY0, radius);
	}

	/// Draw a line segment.
	/// @param x0 starting point, x coordinate
	/// @param y0 starting point, y coordinate
	/// @param x1 end point, x coordinate
	/// @param y1 end point, y coordinate
	/// @param colorX0Y0 color (start, start)
	/// @param colorX0Y1 color (start, end)
	/// @param colorX1Y1 color (end, end)
	/// @param colorX1Y0 color (end, start)
	/// @param radius line radius, the width will be 2*radius.
	/// @implNote The line will overdraw by `radius` on each end. This is intentional,
	/// it means that the drawn line is filled exactly `radius` distance from the vector `[start, end]`.
	default void axolotlclient_rendering$segment(int x0, int y0, int x1, int y1, int colorX0Y0, int colorX0Y1, int colorX1Y1, int colorX1Y0, float radius) {

	}

	/* outlineRoundedRect */
	default void axolotlclient_rendering$outlineCircle(int cX, int cY, int color, int radius, float outlineWidth) {
		this.axolotlclient_rendering$outlineRoundedRect(cX - radius, cY - radius, cX + radius, cY + radius, color, radius, outlineWidth);
	}

	default void axolotlclient_rendering$outlineRoundedRect(int x0, int y0, int x1, int y1, int color, float roundingPx, float outlineWidth) {
		this.axolotlclient_rendering$outlineRoundedRectVarying(x0, y0, x1, y1, color, roundingPx, roundingPx, roundingPx, roundingPx, outlineWidth);
	}

	default void axolotlclient_rendering$outlineRoundedRectVarying(int x0, int y0, int x1, int y1, int color, float roundingPxTopLeft, float roundingPxBottomLeft, float roundingPxBottomRight, float roundingPxTopRight, float outlineWidth) {
		this.axolotlclient_rendering$outlineRoundedRectGradientVarying(x0, y0, x1, y1, color, color, color, color, roundingPxTopLeft, roundingPxBottomLeft, roundingPxBottomRight, roundingPxTopRight, outlineWidth);
	}

	default void axolotlclient_rendering$outlineRoundedRectGradient(int x0, int y0, int x1, int y1, int colorTopLeft, int colorBottomLeft, int colorBottomRight, int colorTopRight, float roundingPx, float outlineWidth) {
		this.axolotlclient_rendering$outlineRoundedRectGradientVarying(x0, y0, x1, y1, colorTopLeft, colorBottomLeft, colorBottomRight, colorTopRight, roundingPx, roundingPx, roundingPx, roundingPx, outlineWidth);
	}

	default void axolotlclient_rendering$outlineRoundedRectGradientVarying(int x0, int y0, int x1, int y1, int colorTopLeft, int colorBottomLeft, int colorBottomRight, int colorTopRight, float roundingPxTopLeft, float roundingPxBottomLeft, float roundingPxBottomRight, float roundingPxTopRight, float outlineWidth) {

	}

	/* outlineRoundedBlit */
	default void axolotlclient_rendering$outlineRoundedBlit(Identifier atlas, int x, int y, int width, int height, float rounding, float outlineWidth) {
		this.axolotlclient_rendering$outlineRoundedBlit(atlas, x, y, x + width, y + height, 0, 1f, 0, 1, rounding, outlineWidth);
	}

	default void axolotlclient_rendering$outlineRoundedBlit(Identifier atlas, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight, int color, float rounding, float outlineWidth) {
		this.axolotlclient_rendering$outlineRoundedBlit(atlas, x, y, u, v, width, height, width, height, textureWidth, textureHeight, color, rounding, outlineWidth);
	}

	default void axolotlclient_rendering$outlineRoundedBlit(Identifier atlas, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight, float rounding, float outlineWidth) {
		this.axolotlclient_rendering$outlineRoundedBlit(atlas, x, y, u, v, width, height, width, height, textureWidth, textureHeight, rounding, outlineWidth);
	}

	default void axolotlclient_rendering$outlineRoundedBlit(Identifier atlas, int x, int y, float u, float v, int width, int height, int uWidth, int vHeight, int textureWidth, int textureHeight, float rounding, float outlineWidth) {
		this.axolotlclient_rendering$outlineRoundedBlit(atlas, x, y, u, v, width, height, uWidth, vHeight, textureWidth, textureHeight, -1, rounding, outlineWidth);
	}

	default void axolotlclient_rendering$outlineRoundedBlit(Identifier location, int x, int y, float u, float v, int width, int height, int uWidth, int vHeight, int textureWidth, int textureHeight, int color, float roundingPx, float outlineWidth) {
		this.axolotlclient_rendering$outlineRoundedBlitVarying(location, x, y, u, v, width, height, uWidth, vHeight, textureWidth, textureHeight, color, roundingPx, roundingPx, roundingPx, roundingPx, outlineWidth);
	}

	default void axolotlclient_rendering$outlineRoundedBlitVarying(Identifier atlas, int x, int y, int width, int height, float roundingPxTopLeft, float roundingPxBottomLeft, float roundingPxBottomRight, float roundingPxTopRight, float outlineWidth) {
		this.axolotlclient_rendering$outlineRoundedBlitVarying(atlas, x, y, x + width, y + height, 0, 1f, 0, 1, roundingPxTopLeft, roundingPxBottomLeft, roundingPxBottomRight, roundingPxTopRight, outlineWidth);
	}

	default void axolotlclient_rendering$outlineRoundedBlitVarying(Identifier atlas, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight, int color, float roundingPxTopLeft, float roundingPxBottomLeft, float roundingPxBottomRight, float roundingPxTopRight, float outlineWidth) {
		this.axolotlclient_rendering$outlineRoundedBlitVarying(atlas, x, y, u, v, width, height, width, height, textureWidth, textureHeight, color, roundingPxTopLeft, roundingPxBottomLeft, roundingPxBottomRight, roundingPxTopRight, outlineWidth);
	}

	default void axolotlclient_rendering$outlineRoundedBlitVarying(Identifier atlas, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight, float roundingPxTopLeft, float roundingPxBottomLeft, float roundingPxBottomRight, float roundingPxTopRight, float outlineWidth) {
		this.axolotlclient_rendering$outlineRoundedBlitVarying(atlas, x, y, u, v, width, height, width, height, textureWidth, textureHeight, roundingPxTopLeft, roundingPxBottomLeft, roundingPxBottomRight, roundingPxTopRight, outlineWidth);
	}

	default void axolotlclient_rendering$outlineRoundedBlitVarying(Identifier atlas, int x, int y, float u, float v, int width, int height, int uWidth, int vHeight, int textureWidth, int textureHeight, float roundingPxTopLeft, float roundingPxBottomLeft, float roundingPxBottomRight, float roundingPxTopRight, float outlineWidth) {
		this.axolotlclient_rendering$outlineRoundedBlitVarying(atlas, x, y, u, v, width, height, uWidth, vHeight, textureWidth, textureHeight, -1, roundingPxTopLeft, roundingPxBottomLeft, roundingPxBottomRight, roundingPxTopRight, outlineWidth);
	}

	default void axolotlclient_rendering$outlineRoundedBlitVarying(Identifier location, int x, int y, float u, float v, int width, int height, int uWidth, int vHeight, int textureWidth, int textureHeight, int color, float roundingPxTopLeft, float roundingPxBottomLeft, float roundingPxBottomRight, float roundingPxTopRight, float outlineWidth) {
	}

	default void axolotlclient_rendering$outlineRoundedBlit(Identifier atlas, int x0, int y0, int x1, int y1, float u0, float u1, float v0, float v1, float rounding, float outlineWidth) {
		this.axolotlclient_rendering$outlineRoundedBlit(atlas, x0, y0, x1, y1, u0, u1, v0, v1, -1, rounding, outlineWidth);
	}

	default void axolotlclient_rendering$outlineRoundedBlit(Identifier atlas, int x0, int y0, int x1, int y1, float u0, float u1, float v0, float v1, int color, float rounding, float outlineWidth) {
		this.axolotlclient_rendering$outlineRoundedBlitVarying(atlas, x0, y0, x1, y1, u0, u1, v0, v1, color, rounding, rounding, rounding, rounding, outlineWidth);
	}

	default void axolotlclient_rendering$outlineRoundedBlitVarying(Identifier atlas, int x0, int y0, int x1, int y1, float u0, float u1, float v0, float v1, float roundingPxTopLeft, float roundingPxBottomLeft, float roundingPxBottomRight, float roundingPxTopRight, float outlineWidth) {
		this.axolotlclient_rendering$outlineRoundedBlitVarying(atlas, x0, y0, x1, y1, u0, u1, v0, v1, -1, roundingPxTopLeft, roundingPxBottomLeft, roundingPxBottomRight, roundingPxTopRight, outlineWidth);
	}

	default void axolotlclient_rendering$outlineRoundedBlitVarying(Identifier atlas, int x0, int y0, int x1, int y1, float u0, float u1, float v0, float v1, int color, float roundingPxTopLeft, float roundingPxBottomLeft, float roundingPxBottomRight, float roundingPxTopRight, float outlineWidth) {
	}

	default void axolotlclient_rendering$outlineCircleBlit(Identifier atlas, int cX, int cY, float u, float v, int radius, int textureWidth, int textureHeight, int color, float outlineWidth) {
		this.axolotlclient_rendering$outlineRoundedBlit(atlas, cX - radius, cY - radius, u, v, radius * 2, radius * 2, radius * 2, radius * 2, textureWidth, textureHeight, color, radius, outlineWidth);
	}

	default void axolotlclient_rendering$outlineCircleBlit(Identifier atlas, int cX, int cY, float u, float v, int radius, int textureWidth, int textureHeight, float outlineWidth) {
		this.axolotlclient_rendering$outlineRoundedBlit(atlas, cX - radius, cY - radius, u, v, radius * 2, radius * 2, textureWidth, textureHeight, radius, outlineWidth);
	}

	default void axolotlclient_rendering$outlineCircleBlit(Identifier atlas, int cX, int cY, float u, float v, int radius, int uWidth, int vHeight, int textureWidth, int textureHeight, float outlineWidth) {
		this.axolotlclient_rendering$outlineRoundedBlit(atlas, cX - radius, cY - radius, u, v, radius * 2, radius * 2, uWidth, vHeight, textureWidth, textureHeight, -1, radius, outlineWidth);
	}

	default void axolotlclient_rendering$outlineCircleBlit(Identifier location, int cX, int cY, float u, float v, int radius, int uWidth, int vHeight, int textureWidth, int textureHeight, int color, float outlineWidth) {
		this.axolotlclient_rendering$outlineRoundedBlit(location, cX - radius, cY - radius, u, v, radius * 2, radius * 2, uWidth, vHeight, textureWidth, textureHeight, color, radius, outlineWidth);
	}

	default void axolotlclient_rendering$outlineCircleBlit(Identifier atlas, int cX, int cY, int color, int radius, float outlineWidth) {
		this.axolotlclient_rendering$outlineRoundedBlit(atlas, cX - radius, cY - radius, cX + radius, cY + radius, 0, 1f, 0, 1, color, radius, outlineWidth);
	}

	default void axolotlclient_rendering$outlineCircleBlit(Identifier atlas, int cX, int cY, int radius, float outlineWidth) {
		this.axolotlclient_rendering$outlineRoundedBlit(atlas, cX - radius, cY - radius, cX + radius, cY + radius, 0, 1f, 0, 1, radius, outlineWidth);
	}

	/* roundedRect */
	default void axolotlclient_rendering$circle(int cX, int cY, int color, int radius) {
		this.axolotlclient_rendering$roundedRect(cX - radius, cY - radius, cX + radius, cY + radius, color, radius);
	}

	default void axolotlclient_rendering$roundedRect(int x0, int y0, int x1, int y1, int color, float roundingPx) {
		this.axolotlclient_rendering$roundedRectVarying(x0, y0, x1, y1, color, roundingPx, roundingPx, roundingPx, roundingPx);
	}

	default void axolotlclient_rendering$roundedRectVarying(int x0, int y0, int x1, int y1, int color, float roundingPxTopLeft, float roundingPxBottomLeft, float roundingPxBottomRight, float roundingPxTopRight) {
		this.axolotlclient_rendering$roundedRectGradientVarying(x0, y0, x1, y1, color, color, color, color, roundingPxTopLeft, roundingPxBottomLeft, roundingPxBottomRight, roundingPxTopRight);
	}

	default void axolotlclient_rendering$circleGradient(int cX, int cY, int colorTopLeft, int colorBottomLeft, int colorBottomRight, int colorTopRight, int radius) {
		this.axolotlclient_rendering$roundedRectGradient(cX - radius, cY - radius, cX + radius, cY + radius, colorTopLeft, colorBottomLeft, colorBottomRight, colorTopRight, radius);
	}

	default void axolotlclient_rendering$roundedRectGradient(int x0, int y0, int x1, int y1, int colorTopLeft, int colorBottomLeft, int colorBottomRight, int colorTopRight, float roundingPx) {
		this.axolotlclient_rendering$roundedRectGradientVarying(x0, y0, x1, y1, colorTopLeft, colorBottomLeft, colorBottomRight, colorTopRight, roundingPx, roundingPx, roundingPx, roundingPx);
	}

	default void axolotlclient_rendering$roundedRectGradientVarying(int x0, int y0, int x1, int y1, int colorTopLeft, int colorBottomLeft, int colorBottomRight, int colorTopRight, float roundingPxTopLeft, float roundingPxBottomLeft, float roundingPxBottomRight, float roundingPxTopRight) {

	}

	/* roundedBlit */
	default void axolotlclient_rendering$roundedBlit(Identifier atlas, int x, int y, int width, int height, float rounding) {
		this.axolotlclient_rendering$roundedBlit(atlas, x, y, x + width, y + height, 0, 1f, 0, 1, rounding);
	}

	default void axolotlclient_rendering$roundedBlit(Identifier atlas, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight, int color, float rounding) {
		this.axolotlclient_rendering$roundedBlit(atlas, x, y, u, v, width, height, width, height, textureWidth, textureHeight, color, rounding);
	}

	default void axolotlclient_rendering$roundedBlit(Identifier atlas, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight, float rounding) {
		this.axolotlclient_rendering$roundedBlit(atlas, x, y, u, v, width, height, width, height, textureWidth, textureHeight, rounding);
	}

	default void axolotlclient_rendering$roundedBlit(Identifier atlas, int x, int y, float u, float v, int width, int height, int uWidth, int vHeight, int textureWidth, int textureHeight, float rounding) {
		this.axolotlclient_rendering$roundedBlit(atlas, x, y, u, v, width, height, uWidth, vHeight, textureWidth, textureHeight, -1, rounding);
	}

	default void axolotlclient_rendering$roundedBlit(Identifier location, int x, int y, float u, float v, int width, int height, int uWidth, int vHeight, int textureWidth, int textureHeight, int color, float roundingPx) {
		axolotlclient_rendering$roundedBlitVarying(location, x, y, u, v, width, height, uWidth, vHeight, textureWidth, textureHeight, color, roundingPx, roundingPx, roundingPx, roundingPx);
	}

	default void axolotlclient_rendering$roundedBlitVarying(Identifier atlas, int x, int y, int width, int height, float roundingPxTopLeft, float roundingPxBottomLeft, float roundingPxBottomRight, float roundingPxTopRight) {
		this.axolotlclient_rendering$roundedBlitVarying(atlas, x, y, x + width, y + height, 0, 1f, 0, 1, roundingPxTopLeft, roundingPxBottomLeft, roundingPxBottomRight, roundingPxTopRight);
	}

	default void axolotlclient_rendering$roundedBlitVarying(Identifier atlas, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight, int color, float roundingPxTopLeft, float roundingPxBottomLeft, float roundingPxBottomRight, float roundingPxTopRight) {
		this.axolotlclient_rendering$roundedBlitVarying(atlas, x, y, u, v, width, height, width, height, textureWidth, textureHeight, color, roundingPxTopLeft, roundingPxBottomLeft, roundingPxBottomRight, roundingPxTopRight);
	}

	default void axolotlclient_rendering$roundedBlitVarying(Identifier atlas, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight, float roundingPxTopLeft, float roundingPxBottomLeft, float roundingPxBottomRight, float roundingPxTopRight) {
		this.axolotlclient_rendering$roundedBlitVarying(atlas, x, y, u, v, width, height, width, height, textureWidth, textureHeight, roundingPxTopLeft, roundingPxBottomLeft, roundingPxBottomRight, roundingPxTopRight);
	}

	default void axolotlclient_rendering$roundedBlitVarying(Identifier atlas, int x, int y, float u, float v, int width, int height, int uWidth, int vHeight, int textureWidth, int textureHeight, float roundingPxTopLeft, float roundingPxBottomLeft, float roundingPxBottomRight, float roundingPxTopRight) {
		this.axolotlclient_rendering$roundedBlitVarying(atlas, x, y, u, v, width, height, uWidth, vHeight, textureWidth, textureHeight, -1, roundingPxTopLeft, roundingPxBottomLeft, roundingPxBottomRight, roundingPxTopRight);
	}

	default void axolotlclient_rendering$roundedBlitVarying(Identifier location, int x, int y, float u, float v, int width, int height, int uWidth, int vHeight, int textureWidth, int textureHeight, int color, float roundingPxTopLeft, float roundingPxBottomLeft, float roundingPxBottomRight, float roundingPxTopRight) {
	}

	default void axolotlclient_rendering$roundedBlit(Identifier atlas, int x0, int y0, int x1, int y1, float u0, float u1, float v0, float v1, float rounding) {
		this.axolotlclient_rendering$roundedBlit(atlas, x0, y0, x1, y1, u0, u1, v0, v1, -1, rounding);
	}

	default void axolotlclient_rendering$roundedBlit(Identifier atlas, int x0, int y0, int x1, int y1, float u0, float u1, float v0, float v1, int color, float rounding) {
		this.axolotlclient_rendering$roundedBlitVarying(atlas, x0, y0, x1, y1, u0, u1, v0, v1, color, rounding, rounding, rounding, rounding);
	}

	default void axolotlclient_rendering$roundedBlitVarying(Identifier atlas, int x0, int y0, int x1, int y1, float u0, float u1, float v0, float v1, float roundingPxTopLeft, float roundingPxBottomLeft, float roundingPxBottomRight, float roundingPxTopRight) {
		this.axolotlclient_rendering$roundedBlitVarying(atlas, x0, y0, x1, y1, u0, u1, v0, v1, -1, roundingPxTopLeft, roundingPxBottomLeft, roundingPxBottomRight, roundingPxTopRight);
	}

	default void axolotlclient_rendering$roundedBlitVarying(Identifier atlas, int x0, int y0, int x1, int y1, float u0, float u1, float v0, float v1, int color, float roundingPxTopLeft, float roundingPxBottomLeft, float roundingPxBottomRight, float roundingPxTopRight) {
	}

	default void axolotlclient_rendering$circleBlit(Identifier atlas, int cX, int cY, float u, float v, int radius, int textureWidth, int textureHeight, int color) {
		this.axolotlclient_rendering$roundedBlit(atlas, cX - radius, cY - radius, u, v, radius * 2, radius * 2, radius * 2, radius * 2, textureWidth, textureHeight, color, radius);
	}

	default void axolotlclient_rendering$circleBlit(Identifier atlas, int cX, int cY, float u, float v, int radius, int textureWidth, int textureHeight) {
		this.axolotlclient_rendering$roundedBlit(atlas, cX - radius, cY - radius, u, v, radius * 2, radius * 2, textureWidth, textureHeight, radius);
	}

	default void axolotlclient_rendering$circleBlit(Identifier atlas, int cX, int cY, float u, float v, int radius, int uWidth, int vHeight, int textureWidth, int textureHeight) {
		this.axolotlclient_rendering$roundedBlit(atlas, cX - radius, cY - radius, u, v, radius * 2, radius * 2, uWidth, vHeight, textureWidth, textureHeight, -1, radius);
	}

	default void axolotlclient_rendering$circleBlit(Identifier location, int cX, int cY, float u, float v, int radius, int uWidth, int vHeight, int textureWidth, int textureHeight, int color) {
		this.axolotlclient_rendering$roundedBlit(location, cX - radius, cY - radius, u, v, radius * 2, radius * 2, uWidth, vHeight, textureWidth, textureHeight, color, radius);
	}

	default void axolotlclient_rendering$circleBlit(Identifier atlas, int cX, int cY, int radius, int color) {
		this.axolotlclient_rendering$roundedBlit(atlas, cX - radius, cY - radius, cX + radius, cY + radius, 0, 1f, 0, 1, radius, color);
	}

	default void axolotlclient_rendering$circleBlit(Identifier atlas, int cX, int cY, int radius) {
		this.axolotlclient_rendering$roundedBlit(atlas, cX - radius, cY - radius, cX + radius, cY + radius, 0, 1f, 0, 1, radius);
	}

	/*drawString*/
	default void axolotlclient_rendering$text(Font font, String text, float x, float y, int color, boolean shadow, int origin) {

	}

	default void axolotlclient_rendering$text(Font font, FormattedCharSequence text, float x, float y, int color, boolean shadow, int origin) {

	}

	default void axolotlclient_rendering$text(Font font, Component text, float x, float y, int color, boolean shadow, int origin) {

	}

	default void axolotlclient_rendering$text(Font font, String text, float x, float y, int color, boolean shadow) {
		this.axolotlclient_rendering$text(font, text, x, y, color, shadow, 0);
	}

	default void axolotlclient_rendering$text(Font font, FormattedCharSequence text, float x, float y, int color, boolean shadow) {
		this.axolotlclient_rendering$text(font, text, x, y, color, shadow, 0);
	}

	default void axolotlclient_rendering$text(Font font, Component text, float x, float y, int color, boolean shadow) {
		this.axolotlclient_rendering$text(font, text, x, y, color, shadow, 0);
	}

	default void axolotlclient_rendering$text(Font font, String text, float x, float y, int color) {
		this.axolotlclient_rendering$text(font, text, x, y, color, true);
	}

	default void axolotlclient_rendering$text(Font font, FormattedCharSequence text, float x, float y, int color) {
		this.axolotlclient_rendering$text(font, text, x, y, color, true);
	}

	default void axolotlclient_rendering$text(Font font, Component text, float x, float y, int color) {
		this.axolotlclient_rendering$text(font, text, x, y, color, true);
	}

	/*drawCenteredString*/
	default void axolotlclient_rendering$centeredText(Font font, String text, float x, float y, int color, boolean shadow) {
		this.axolotlclient_rendering$text(font, text, x, y, color, shadow, Font.ORIGIN_HORIZ_CENTER);
	}

	default void axolotlclient_rendering$centeredText(Font font, FormattedCharSequence text, float x, float y, int color, boolean shadow) {
		this.axolotlclient_rendering$text(font, text, x, y, color, shadow, Font.ORIGIN_HORIZ_CENTER);
	}

	default void axolotlclient_rendering$centeredText(Font font, Component text, float x, float y, int color, boolean shadow) {
		this.axolotlclient_rendering$text(font, text, x, y, color, shadow, Font.ORIGIN_HORIZ_CENTER);
	}

	default void axolotlclient_rendering$centeredText(Font font, String text, float x, float y, int color) {
		this.axolotlclient_rendering$centeredText(font, text, x, y, color, true);
	}

	default void axolotlclient_rendering$centeredText(Font font, FormattedCharSequence text, float x, float y, int color) {
		this.axolotlclient_rendering$centeredText(font, text, x, y, color, true);
	}

	default void axolotlclient_rendering$centeredText(Font font, Component text, float x, float y, int color) {
		this.axolotlclient_rendering$centeredText(font, text, x, y, color, true);
	}

	/*drawWordWrap*/
	default void axolotlclient_rendering$textWithWordWrap(Font font, FormattedText text, int x, int y, int lineWidth, int color) {
		this.axolotlclient_rendering$textWithWordWrap(font, text, x, y, lineWidth, color, true);
	}

	default void axolotlclient_rendering$textWithWordWrap(Font font, FormattedText text, int x, int y, int lineWidth, int color, boolean dropShadow) {
		for (FormattedCharSequence formattedCharSequence : font.split(text, lineWidth)) {
			this.axolotlclient_rendering$text(font, formattedCharSequence, x, y, color, dropShadow, Font.ORIGIN_VERT_BASELINE);
			y += font.lineHeight();
		}
	}

	default void axolotlclient_rendering$textWithWordWrap(Font font, String text, int x, int y, int lineWidth, int color) {
		this.axolotlclient_rendering$textWithWordWrap(font, text, x, y, lineWidth, color, true);
	}

	default void axolotlclient_rendering$textWithWordWrap(Font font, String text, int x, int y, int lineWidth, int color, boolean dropShadow) {
		for (FormattedCharSequence formattedCharSequence : font.split(text, lineWidth)) {
			this.axolotlclient_rendering$text(font, formattedCharSequence, x, y, color, dropShadow, Font.ORIGIN_VERT_BASELINE);
			y += font.lineHeight();
		}
	}

	/*blitSVG*/
	default void axolotlclient_rendering$blitSVG(Identifier location, int x, int y, int width, int height) {
		axolotlclient_rendering$blitSVG(location, x, y, width, height, -1);
	}

	default void axolotlclient_rendering$blitSVG(Identifier location, int x, int y, int width, int height, int color) {

	}

	/* Gaussian Blur */
	default void axolotlclient_rendering$gaussianBlur(float x, float y, float width, float height, float blurRadius, float sigma) {
		this.axolotlclient_rendering$roundedGaussianBlur(x, y, width, height, 0.0f, blurRadius, sigma, 0);
	}

	default void axolotlclient_rendering$roundedGaussianBlur(float x, float y, float width, float height, float radius, float blurRadius, float sigma) {
		this.axolotlclient_rendering$roundedGaussianBlur(x, y, width, height, radius, blurRadius, sigma, 0);
	}

	default void axolotlclient_rendering$roundedGaussianBlur(float x, float y, float width, float height, float radius, float blurRadius, float sigma, int tintColor) {
	}

	/* Liquid Glass */
	default void axolotlclient_rendering$liquidGlass(float x, float y, float width, float height, float rounding, int tintColor) {
		this.axolotlclient_rendering$liquidGlass(x, y, width, height, rounding, tintColor, 0x40FFFFFF, 1.0f, 6.0f, 4.0f);
	}

	default void axolotlclient_rendering$liquidGlass(float x, float y, float width, float height, float rounding, int tintColor, int borderColor, float borderWidth, float blurRadius) {
		this.axolotlclient_rendering$liquidGlass(x, y, width, height, rounding, tintColor, borderColor, borderWidth, blurRadius, 4.0f);
	}

	default void axolotlclient_rendering$liquidGlass(float x, float y, float width, float height, float rounding, int tintColor, int borderColor, float borderWidth, float blurRadius, float distortion) {
	}

	default void axolotlclient_rendering$liquidGlass(float x, float y, float width, float height, float rounding, LiquidGlassStyle style) {
	}

	/* MSDF Font */
	default void axolotlclient_rendering$drawMsdfString(io.github.axolotlclient.rendering.font.msdf.MsdfFont font, String text, float x, float y, float size, int color) {
		this.axolotlclient_rendering$drawMsdfString(font, text, x, y, size, color, false);
	}

	default void axolotlclient_rendering$drawMsdfString(io.github.axolotlclient.rendering.font.msdf.MsdfFont font, String text, float x, float y, float size, int color, boolean dropShadow) {
	}
}
