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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.Getter;
import net.minecraft.util.text.TextFormatting;
import org.jetbrains.annotations.Nullable;

public class TextColor {
	private static final Map<TextFormatting, TextColor> LEGACY_FORMAT_TO_COLOR;
	private static final Map<String, TextColor> NAMED_COLORS;

	static {
		Map<TextFormatting, TextColor> formatMap = new HashMap<>();
		for (TextFormatting f : TextFormatting.values()) {
			if (f.isColor()) {
				formatMap.put(f, new TextColor(TextRenderUtils.getColor(f), f.getFriendlyName()));
			}
		}
		LEGACY_FORMAT_TO_COLOR = Collections.unmodifiableMap(formatMap);

		Map<String, TextColor> nameMap = new HashMap<>();
		for (TextColor tc : LEGACY_FORMAT_TO_COLOR.values()) {
			if (tc.name != null) {
				nameMap.put(tc.name, tc);
			}
		}
		NAMED_COLORS = Collections.unmodifiableMap(nameMap);
	}

	@Getter
	private final int value;
	private final String name;

	private TextColor(int value, String name) {
		this.value = value;
		this.name = name;
	}

	private TextColor(int value) {
		this.value = value;
		this.name = null;
	}

	@Nullable
	public static TextColor fromLegacyFormat(TextFormatting formatting) {
		return LEGACY_FORMAT_TO_COLOR.get(formatting);
	}

	public static TextColor fromRgb(int color) {
		return new TextColor(color);
	}

	@Nullable
	public static TextColor parseColor(String hexString) {
		if (hexString.startsWith("#")) {
			try {
				int i = Integer.parseInt(hexString.substring(1), 16);
				return fromRgb(i);
			} catch (NumberFormatException var2) {
				return null;
			}
		} else {
			return NAMED_COLORS.get(hexString);
		}
	}
}
