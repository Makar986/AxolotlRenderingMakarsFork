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
import net.minecraft.util.FormattedCharSequence;

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

	public MsdfFont getMsdfFont() {
		return font;
	}

	public float getSize() {
		return size;
	}
}
