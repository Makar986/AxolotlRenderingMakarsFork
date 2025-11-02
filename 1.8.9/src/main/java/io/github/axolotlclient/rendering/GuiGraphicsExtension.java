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
import net.minecraft.resource.Identifier;
import net.minecraft.text.Text;

@SuppressWarnings("unused")
public interface GuiGraphicsExtension {
	/*	General */
	int guiWidth();
	int guiHeight();

	/* outlineRoundedRect */
	default void axolotlclient_rendering$outlineCircle(int cX, int cY, int color, int radius, float outlineWidth) {
		this.axolotlclient_rendering$outlineRoundedRect(cX - radius, cY - radius, cX + radius, cY + radius, color, radius, outlineWidth);
	}

	default void axolotlclient_rendering$outlineRoundedRect(int x0, int y0, int x1, int y1, int color, int roundingPx, float outlineWidth) {
		this.axolotlclient_rendering$outlineRoundedRectVarying(x0, y0, x1, y1, color, roundingPx, roundingPx, roundingPx, roundingPx, outlineWidth);
	}

	default void axolotlclient_rendering$outlineRoundedRectVarying(int x0, int y0, int x1, int y1, int color, int roundingPxTopLeft, int roundingPxBottomLeft, int roundingPxBottomRight, int roundingPxTopRight, float outlineWidth) {

	}

	/* outlineRoundedBlit */
	default void axolotlclient_rendering$outlineRoundedBlit(Identifier atlas, int x, int y, int width, int height, int rounding, float outlineWidth) {
		this.axolotlclient_rendering$outlineRoundedBlit(atlas, x, y, x + width, y + height, 0, 1f, 0, 1, rounding, outlineWidth);
	}

	default void axolotlclient_rendering$outlineRoundedBlit(Identifier atlas, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight, int color, int rounding, float outlineWidth) {
		this.axolotlclient_rendering$outlineRoundedBlit(atlas, x, y, u, v, width, height, width, height, textureWidth, textureHeight, color, rounding, outlineWidth);
	}

	default void axolotlclient_rendering$outlineRoundedBlit(Identifier atlas, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight, int rounding, float outlineWidth) {
		this.axolotlclient_rendering$outlineRoundedBlit(atlas, x, y, u, v, width, height, width, height, textureWidth, textureHeight, rounding, outlineWidth);
	}

	default void axolotlclient_rendering$outlineRoundedBlit(Identifier atlas, int x, int y, float u, float v, int width, int height, int uWidth, int vHeight, int textureWidth, int textureHeight, int rounding, float outlineWidth) {
		this.axolotlclient_rendering$outlineRoundedBlit(atlas, x, y, u, v, width, height, uWidth, vHeight, textureWidth, textureHeight, -1, rounding, outlineWidth);
	}

	default void axolotlclient_rendering$outlineRoundedBlit(Identifier location, int x, int y, float u, float v, int width, int height, int uWidth, int vHeight, int textureWidth, int textureHeight, int color, int roundingPx, float outlineWidth) {
		this.axolotlclient_rendering$outlineRoundedBlitVarying(location, x, y, u, v, width, height, uWidth, vHeight, textureWidth, textureHeight, color, roundingPx, roundingPx, roundingPx, roundingPx, outlineWidth);
	}

	default void axolotlclient_rendering$outlineRoundedBlitVarying(Identifier atlas, int x, int y, int width, int height, int roundingPxTopLeft, int roundingPxBottomLeft, int roundingPxBottomRight, int roundingPxTopRight, float outlineWidth) {
		this.axolotlclient_rendering$outlineRoundedBlitVarying(atlas, x, y, x + width, y + height, 0, 1f, 0, 1, roundingPxTopLeft, roundingPxBottomLeft, roundingPxBottomRight, roundingPxTopRight, outlineWidth);
	}

	default void axolotlclient_rendering$outlineRoundedBlitVarying(Identifier atlas, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight, int color, int roundingPxTopLeft, int roundingPxBottomLeft, int roundingPxBottomRight, int roundingPxTopRight, float outlineWidth) {
		this.axolotlclient_rendering$outlineRoundedBlitVarying(atlas, x, y, u, v, width, height, width, height, textureWidth, textureHeight, color, roundingPxTopLeft, roundingPxBottomLeft, roundingPxBottomRight, roundingPxTopRight, outlineWidth);
	}

	default void axolotlclient_rendering$outlineRoundedBlitVarying(Identifier atlas, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight, int roundingPxTopLeft, int roundingPxBottomLeft, int roundingPxBottomRight, int roundingPxTopRight, float outlineWidth) {
		this.axolotlclient_rendering$outlineRoundedBlitVarying(atlas, x, y, u, v, width, height, width, height, textureWidth, textureHeight, roundingPxTopLeft, roundingPxBottomLeft, roundingPxBottomRight, roundingPxTopRight, outlineWidth);
	}

	default void axolotlclient_rendering$outlineRoundedBlitVarying(Identifier atlas, int x, int y, float u, float v, int width, int height, int uWidth, int vHeight, int textureWidth, int textureHeight, int roundingPxTopLeft, int roundingPxBottomLeft, int roundingPxBottomRight, int roundingPxTopRight, float outlineWidth) {
		this.axolotlclient_rendering$outlineRoundedBlitVarying(atlas, x, y, u, v, width, height, uWidth, vHeight, textureWidth, textureHeight, -1, roundingPxTopLeft, roundingPxBottomLeft, roundingPxBottomRight, roundingPxTopRight, outlineWidth);
	}

	default void axolotlclient_rendering$outlineRoundedBlitVarying(Identifier location, int x, int y, float u, float v, int width, int height, int uWidth, int vHeight, int textureWidth, int textureHeight, int color, int roundingPxTopLeft, int roundingPxBottomLeft, int roundingPxBottomRight, int roundingPxTopRight, float outlineWidth) {
	}

	default void axolotlclient_rendering$outlineRoundedBlit(Identifier atlas, int x0, int y0, int x1, int y1, float u0, float u1, float v0, float v1, int rounding, float outlineWidth) {
		this.axolotlclient_rendering$outlineRoundedBlit(atlas, x0, y0, x1, y1, u0, u1, v0, v1, -1, rounding, outlineWidth);
	}

	default void axolotlclient_rendering$outlineRoundedBlit(Identifier atlas, int x0, int y0, int x1, int y1, float u0, float u1, float v0, float v1, int color, int rounding, float outlineWidth) {
		this.axolotlclient_rendering$outlineRoundedBlitVarying(atlas, x0, y0, x1, y1, u0, u1, v0, v1, color, rounding, rounding, rounding, rounding, outlineWidth);
	}

	default void axolotlclient_rendering$outlineRoundedBlitVarying(Identifier atlas, int x0, int y0, int x1, int y1, float u0, float u1, float v0, float v1, int roundingPxTopLeft, int roundingPxBottomLeft, int roundingPxBottomRight, int roundingPxTopRight, float outlineWidth) {
		this.axolotlclient_rendering$outlineRoundedBlitVarying(atlas, x0, y0, x1, y1, u0, u1, v0, v1, -1, roundingPxTopLeft, roundingPxBottomLeft, roundingPxBottomRight, roundingPxTopRight, outlineWidth);
	}

	default void axolotlclient_rendering$outlineRoundedBlitVarying(Identifier atlas, int x0, int y0, int x1, int y1, float u0, float u1, float v0, float v1, int color, int roundingPxTopLeft, int roundingPxBottomLeft, int roundingPxBottomRight, int roundingPxTopRight, float outlineWidth) {
	}

	default void axolotlclient_rendering$outlineCircleBlit(Identifier atlas, int cX, int cY, float u, float v, int radius, int textureWidth, int textureHeight, int color, int rounding, float outlineWidth) {
		this.axolotlclient_rendering$outlineRoundedBlit(atlas, cX - radius, cY - radius, u, v, radius * 2, radius * 2, radius * 2, radius * 2, textureWidth, textureHeight, color, rounding, outlineWidth);
	}

	default void axolotlclient_rendering$outlineCircleBlit(Identifier atlas, int cX, int cY, float u, float v, int radius, int textureWidth, int textureHeight, int rounding, float outlineWidth) {
		this.axolotlclient_rendering$outlineRoundedBlit(atlas, cX - radius, cY - radius, u, v, radius * 2, radius * 2, textureWidth, textureHeight, rounding, outlineWidth);
	}

	default void axolotlclient_rendering$outlineCircleBlit(Identifier atlas, int cX, int cY, float u, float v, int radius, int uWidth, int vHeight, int textureWidth, int textureHeight, int rounding, float outlineWidth) {
		this.axolotlclient_rendering$outlineRoundedBlit(atlas, cX - radius, cY - radius, u, v, radius * 2, radius * 2, uWidth, vHeight, textureWidth, textureHeight, -1, rounding, outlineWidth);
	}

	default void axolotlclient_rendering$outlineCircleBlit(Identifier location, int cX, int cY, float u, float v, int radius, int uWidth, int vHeight, int textureWidth, int textureHeight, int color, int roundingPx, float outlineWidth) {
		this.axolotlclient_rendering$outlineRoundedBlit(location, cX - radius, cY - radius, u, v, radius * 2, radius * 2, uWidth, vHeight, textureWidth, textureHeight, color, roundingPx, outlineWidth);
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

	default void axolotlclient_rendering$roundedRect(int x0, int y0, int x1, int y1, int color, int roundingPx) {
		this.axolotlclient_rendering$roundedRectVarying(x0, y0, x1, y1, color, roundingPx, roundingPx, roundingPx, roundingPx);
	}

	default void axolotlclient_rendering$roundedRectVarying(int x0, int y0, int x1, int y1, int color, int roundingPxTopLeft, int roundingPxBottomLeft, int roundingPxBottomRight, int roundingPxTopRight) {

	}

	/* roundedBlit */
	default void axolotlclient_rendering$roundedBlit(Identifier atlas, int x, int y, int width, int height, int rounding) {
		this.axolotlclient_rendering$roundedBlit(atlas, x, y, x + width, y + height, 0, 1f, 0, 1, rounding);
	}

	default void axolotlclient_rendering$roundedBlit(Identifier atlas, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight, int color, int rounding) {
		this.axolotlclient_rendering$roundedBlit(atlas, x, y, u, v, width, height, width, height, textureWidth, textureHeight, color, rounding);
	}

	default void axolotlclient_rendering$roundedBlit(Identifier atlas, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight, int rounding) {
		this.axolotlclient_rendering$roundedBlit(atlas, x, y, u, v, width, height, width, height, textureWidth, textureHeight, rounding);
	}

	default void axolotlclient_rendering$roundedBlit(Identifier atlas, int x, int y, float u, float v, int width, int height, int uWidth, int vHeight, int textureWidth, int textureHeight, int rounding) {
		this.axolotlclient_rendering$roundedBlit(atlas, x, y, u, v, width, height, uWidth, vHeight, textureWidth, textureHeight, -1, rounding);
	}

	default void axolotlclient_rendering$roundedBlit(Identifier location, int x, int y, float u, float v, int width, int height, int uWidth, int vHeight, int textureWidth, int textureHeight, int color, int roundingPx) {
		this.axolotlclient_rendering$roundedBlitVarying(location, x, y, u, v, width, height, uWidth, vHeight, textureWidth, textureHeight, color, roundingPx, roundingPx, roundingPx, roundingPx);
	}

	default void axolotlclient_rendering$roundedBlitVarying(Identifier atlas, int x, int y, int width, int height, int roundingPxTopLeft, int roundingPxBottomLeft, int roundingPxBottomRight, int roundingPxTopRight) {
		this.axolotlclient_rendering$roundedBlitVarying(atlas, x, y, x + width, y + height, 0, 1f, 0, 1, roundingPxTopLeft, roundingPxBottomLeft, roundingPxBottomRight, roundingPxTopRight);
	}

	default void axolotlclient_rendering$roundedBlitVarying(Identifier atlas, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight, int color, int roundingPxTopLeft, int roundingPxBottomLeft, int roundingPxBottomRight, int roundingPxTopRight) {
		this.axolotlclient_rendering$roundedBlitVarying(atlas, x, y, u, v, width, height, width, height, textureWidth, textureHeight, color, roundingPxTopLeft, roundingPxBottomLeft, roundingPxBottomRight, roundingPxTopRight);
	}

	default void axolotlclient_rendering$roundedBlitVarying(Identifier atlas, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight, int roundingPxTopLeft, int roundingPxBottomLeft, int roundingPxBottomRight, int roundingPxTopRight) {
		this.axolotlclient_rendering$roundedBlitVarying(atlas, x, y, u, v, width, height, width, height, textureWidth, textureHeight, roundingPxTopLeft, roundingPxBottomLeft, roundingPxBottomRight, roundingPxTopRight);
	}

	default void axolotlclient_rendering$roundedBlitVarying(Identifier atlas, int x, int y, float u, float v, int width, int height, int uWidth, int vHeight, int textureWidth, int textureHeight, int roundingPxTopLeft, int roundingPxBottomLeft, int roundingPxBottomRight, int roundingPxTopRight) {
		this.axolotlclient_rendering$roundedBlitVarying(atlas, x, y, u, v, width, height, uWidth, vHeight, textureWidth, textureHeight, -1, roundingPxTopLeft, roundingPxBottomLeft, roundingPxBottomRight, roundingPxTopRight);
	}

	default void axolotlclient_rendering$roundedBlitVarying(Identifier location, int x, int y, float u, float v, int width, int height, int uWidth, int vHeight, int textureWidth, int textureHeight, int color, int roundingPxTopLeft, int roundingPxBottomLeft, int roundingPxBottomRight, int roundingPxTopRight) {
	}

	default void axolotlclient_rendering$roundedBlit(Identifier atlas, int x0, int y0, int x1, int y1, float u0, float u1, float v0, float v1, int rounding) {
		this.axolotlclient_rendering$roundedBlit(atlas, x0, y0, x1, y1, u0, u1, v0, v1, -1, rounding);
	}

	default void axolotlclient_rendering$roundedBlit(Identifier atlas, int x0, int y0, int x1, int y1, float u0, float u1, float v0, float v1, int color, int rounding) {
		this.axolotlclient_rendering$roundedBlitVarying(atlas, x0, y0, x1, y1, u0, u1, v0, v1, color, rounding, rounding, rounding, rounding);
	}

	default void axolotlclient_rendering$roundedBlitVarying(Identifier atlas, int x0, int y0, int x1, int y1, float u0, float u1, float v0, float v1, int roundingPxTopLeft, int roundingPxBottomLeft, int roundingPxBottomRight, int roundingPxTopRight) {
		this.axolotlclient_rendering$roundedBlitVarying(atlas, x0, y0, x1, y1, u0, u1, v0, v1, -1, roundingPxTopLeft, roundingPxBottomLeft, roundingPxBottomRight, roundingPxTopRight);
	}

	default void axolotlclient_rendering$roundedBlitVarying(Identifier atlas, int x0, int y0, int x1, int y1, float u0, float u1, float v0, float v1, int color, int roundingPxTopLeft, int roundingPxBottomLeft, int roundingPxBottomRight, int roundingPxTopRight) {
	}

	default void axolotlclient_rendering$circleBlit(Identifier atlas, int cX, int cY, float u, float v, int radius, int textureWidth, int textureHeight, int color, int rounding) {
		this.axolotlclient_rendering$roundedBlit(atlas, cX - radius, cY - radius, u, v, radius * 2, radius * 2, radius * 2, radius * 2, textureWidth, textureHeight, color, rounding);
	}

	default void axolotlclient_rendering$circleBlit(Identifier atlas, int cX, int cY, float u, float v, int radius, int textureWidth, int textureHeight, int rounding) {
		this.axolotlclient_rendering$roundedBlit(atlas, cX - radius, cY - radius, u, v, radius * 2, radius * 2, textureWidth, textureHeight, rounding);
	}

	default void axolotlclient_rendering$circleBlit(Identifier atlas, int cX, int cY, float u, float v, int radius, int uWidth, int vHeight, int textureWidth, int textureHeight, int rounding) {
		this.axolotlclient_rendering$roundedBlit(atlas, cX - radius, cY - radius, u, v, radius * 2, radius * 2, uWidth, vHeight, textureWidth, textureHeight, -1, rounding);
	}

	default void axolotlclient_rendering$circleBlit(Identifier location, int cX, int cY, float u, float v, int radius, int uWidth, int vHeight, int textureWidth, int textureHeight, int color, int roundingPx) {
		this.axolotlclient_rendering$roundedBlit(location, cX - radius, cY - radius, u, v, radius * 2, radius * 2, uWidth, vHeight, textureWidth, textureHeight, color, roundingPx);
	}

	default void axolotlclient_rendering$circleBlit(Identifier atlas, int cX, int cY, int radius, int color) {
		this.axolotlclient_rendering$roundedBlit(atlas, cX - radius, cY - radius, cX + radius, cY + radius, 0, 1f, 0, 1, radius, color);
	}

	default void axolotlclient_rendering$circleBlit(Identifier atlas, int cX, int cY, int radius) {
		this.axolotlclient_rendering$roundedBlit(atlas, cX - radius, cY - radius, cX + radius, cY + radius, 0, 1f, 0, 1, radius);
	}

	/*drawString*/
	default void axolotlclient_rendering$drawString(Font font, String text, float x, float y, int color, boolean shadow) {

	}

	default void axolotlclient_rendering$drawString(Font font, Text text, float x, float y, int color, boolean shadow) {

	}

	default void axolotlclient_rendering$drawString(Font font, String text, float x, float y, int color) {
		this.axolotlclient_rendering$drawString(font, text, x, y, color, true);
	}

	default void axolotlclient_rendering$drawString(Font font, Text text, float x, float y, int color) {
		this.axolotlclient_rendering$drawString(font, text, x, y, color, true);
	}

	/*drawWordWrap*/
	default void axolotlclient_rendering$drawWordWrap(Font font, Text text, int x, int y, int lineWidth, int color) {
		this.axolotlclient_rendering$drawWordWrap(font, text, x, y, lineWidth, color, true);
	}

	default void axolotlclient_rendering$drawWordWrap(Font font, Text text, int x, int y, int lineWidth, int color, boolean dropShadow) {
		for (var formattedCharSequence : font.split(text, lineWidth)) {
			this.axolotlclient_rendering$drawString(font, formattedCharSequence, x, y, color, dropShadow);
			y += font.lineHeight();
		}
	}

	/*blitSVG*/
	default void axolotlclient_rendering$blitSVG(Identifier location, int x, int y, int width, int height, int color) {

	}

}
