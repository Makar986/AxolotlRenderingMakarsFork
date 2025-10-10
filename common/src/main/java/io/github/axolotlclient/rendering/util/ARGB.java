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

package io.github.axolotlclient.rendering.util;

public class ARGB {
	public static int alpha(int color) {
		return color >>> 24;
	}

	public static int red(int color) {
		return color >> 16 & 0xFF;
	}

	public static int green(int color) {
		return color >> 8 & 0xFF;
	}

	public static int blue(int color) {
		return color & 0xFF;
	}

	public static int color(int alpha, int red, int green, int blue) {
		return (alpha & 0xFF) << 24 | (red & 0xFF) << 16 | (green & 0xFF) << 8 | blue & 0xFF;
	}

	public static int color(int red, int green, int blue) {
		return color(255, red, green, blue);
	}

	public static int scaleRGB(int color, float scale) {
		return scaleRGB(color, scale, scale, scale);
	}

	public static int scaleRGB(int color, float redScale, float greenScale, float blueScale) {
		return color(
			alpha(color),
			clamp((int)(red(color) * redScale), 0, 255),
			clamp((int)(green(color) * greenScale), 0, 255),
			clamp((int)(blue(color) * blueScale), 0, 255)
		);
	}

	public static int opaque(int color) {
		return color | 0xFF000000;
	}

	public static int transparent(int color) {
		return color & 0xFFFFFF;
	}

	public static int color(int alpha, int color) {
		return alpha << 24 | color & 0xFFFFFF;
	}

	public static int color(float alpha, int color) {
		return as8BitChannel(alpha) << 24 | color & 0xFFFFFF;
	}

	public static int as8BitChannel(float value) {
		return floor(value * 255.0F);
	}

	public static float alphaFloat(int color) {
		return from8BitChannel(alpha(color));
	}

	public static float redFloat(int color) {
		return from8BitChannel(red(color));
	}

	public static float greenFloat(int color) {
		return from8BitChannel(green(color));
	}

	public static float blueFloat(int color) {
		return from8BitChannel(blue(color));
	}

	private static float from8BitChannel(int value) {
		return value / 255.0F;
	}

	public static int toABGR(int color) {
		return color & -16711936 | (color & 0xFF0000) >> 16 | (color & 0xFF) << 16;
	}

	private static int lerpInt(float delta, int start, int end) {
		return start + floor(delta * (end - start));
	}

	private static int floor(float value) {
		int i = (int)value;
		return value < i ? i - 1 : i;
	}

	private static int clamp(long value, int min, int max) {
		if (min > max) {
			int i = max;
			max = min;
			min = i;
		}
		return (int) Math.min(max, Math.max(value, min));
	}
}
