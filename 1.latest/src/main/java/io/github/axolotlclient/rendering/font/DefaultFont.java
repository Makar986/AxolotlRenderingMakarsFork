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

package io.github.axolotlclient.rendering.font;

public final class DefaultFont {
	private static final Font INTER;

	static {
		try (var regular = DefaultFont.class.getResourceAsStream("/assets/axolotlclient_rendering/fonts/InterVariable.ttf");
			 var italic = DefaultFont.class.getResourceAsStream("/assets/axolotlclient_rendering/fonts/InterVariable-Italic.ttf")) {
			if (regular == null || italic == null) {
				throw new IllegalStateException();
			}
			INTER = Font.read(regular, 9)
				.addSubFont(Font.TAG_ITALIC, Font.ITALIC_ENABLED, Font.read(italic, 9));
		} catch (Exception e) {
			throw new RuntimeException("The Inter font is packaged with this library!", e);
		}
	}

	private DefaultFont() {
	}

	public static Font inter() {
		return INTER;
	}
}
