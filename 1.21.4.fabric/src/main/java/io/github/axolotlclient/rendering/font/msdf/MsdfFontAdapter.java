/*
 * Copyright © 2026 moehreag <moehreag@gmail.com> & Contributors
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

package io.github.axolotlclient.rendering.font.msdf;

import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import org.joml.Matrix4f;

public class MsdfFontAdapter extends Font {
	private final MsdfFont font;
	private final float size;

	public MsdfFontAdapter(MsdfFont font, float size) {
		super(null, false);
		this.font = font;
		this.size = size;
	}

	public MsdfFontAdapter(MsdfFont font) {
		this(font, 9.0f);
	}

	@Override
	public int width(String text) {
		return (int) Math.ceil(font.getWidth(text, size));
	}

	@Override
	public int width(FormattedCharSequence text) {
		StringBuilder sb = new StringBuilder();
		text.accept((index, style, codePoint) -> {
			sb.appendCodePoint(codePoint);
			return true;
		});
		return width(sb.toString());
	}

	@Override
	public int width(FormattedText text) {
		return text != null ? width(text.getString()) : 0;
	}

	@Override
	public int drawInBatch(String text, float x, float y, int color, boolean dropShadow, Matrix4f matrix, MultiBufferSource buffer, DisplayMode displayMode, int backgroundColor, int packedLightCoords) {
		var pose = new Matrix4f(matrix).translate(x, y, 0);
		if (dropShadow) {
			font.drawWithShadow(pose, text, 0, 0, size, color);
		} else {
			font.draw(pose, text, 0, 0, size, color);
		}
		return (int) (x + width(text));
	}

	@Override
	public int drawInBatch(FormattedCharSequence text, float x, float y, int color, boolean dropShadow, Matrix4f matrix, MultiBufferSource buffer, DisplayMode displayMode, int backgroundColor, int packedLightCoords) {
		StringBuilder sb = new StringBuilder();
		text.accept((index, style, codePoint) -> {
			sb.appendCodePoint(codePoint);
			return true;
		});
		return drawInBatch(sb.toString(), x, y, color, dropShadow, matrix, buffer, displayMode, backgroundColor, packedLightCoords);
	}

	public MsdfFont getMsdfFont() {
		return font;
	}

	public float getSize() {
		return size;
	}
}
