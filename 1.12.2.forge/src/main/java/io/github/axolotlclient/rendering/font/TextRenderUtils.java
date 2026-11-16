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

import java.util.Map;

import io.github.axolotlclient.rendering.StyleExtension;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.extern.log4j.Log4j2;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.Style;

@Log4j2
public final class TextRenderUtils {

	private static final Map<Character, TextFormatting> formattingCodes;
	private static final Object2IntMap<TextFormatting> formattingColors;

	static {
		Object2ObjectOpenHashMap<Character, TextFormatting> map = new Object2ObjectOpenHashMap<>();
		map.put('0', TextFormatting.BLACK);
		map.put('1', TextFormatting.DARK_BLUE);
		map.put('2', TextFormatting.DARK_GREEN);
		map.put('3', TextFormatting.DARK_AQUA);
		map.put('4', TextFormatting.DARK_RED);
		map.put('5', TextFormatting.DARK_PURPLE);
		map.put('6', TextFormatting.GOLD);
		map.put('7', TextFormatting.GRAY);
		map.put('8', TextFormatting.DARK_GRAY);
		map.put('9', TextFormatting.BLUE);
		map.put('a', TextFormatting.GREEN);
		map.put('b', TextFormatting.AQUA);
		map.put('c', TextFormatting.RED);
		map.put('d', TextFormatting.LIGHT_PURPLE);
		map.put('e', TextFormatting.YELLOW);
		map.put('f', TextFormatting.WHITE);
		map.put('k', TextFormatting.OBFUSCATED);
		map.put('l', TextFormatting.BOLD);
		map.put('m', TextFormatting.STRIKETHROUGH);
		map.put('n', TextFormatting.UNDERLINE);
		map.put('o', TextFormatting.ITALIC);
		map.put('r', TextFormatting.RESET);
		formattingCodes = map;
		formattingColors = new Object2IntOpenHashMap<>();
		formattingColors.put(TextFormatting.BLACK, 0);
		formattingColors.put(TextFormatting.DARK_BLUE, 0xaa);
		formattingColors.put(TextFormatting.DARK_GREEN, 0xaa00);
		formattingColors.put(TextFormatting.DARK_AQUA, 0xaaaa);
		formattingColors.put(TextFormatting.DARK_RED, 0xaa0000);
		formattingColors.put(TextFormatting.DARK_PURPLE, 0xaa00aa);
		formattingColors.put(TextFormatting.GOLD, 0xffaa00);
		formattingColors.put(TextFormatting.GRAY, 0xaaaaaa);
		formattingColors.put(TextFormatting.DARK_GRAY, 0x555555);
		formattingColors.put(TextFormatting.BLUE, 0x5555ff);
		formattingColors.put(TextFormatting.GREEN, 0x55ff55);
		formattingColors.put(TextFormatting.AQUA, 5636095);
		formattingColors.put(TextFormatting.RED, 16733525);
		formattingColors.put(TextFormatting.LIGHT_PURPLE, 16733695);
		formattingColors.put(TextFormatting.YELLOW, 16777045);
		formattingColors.put(TextFormatting.WHITE, 16777215);
	}

	public static TextFormatting getByCode(char c) {
		return formattingCodes.get(c);
	}

	public static int getColor(TextFormatting formatting) {
		return formattingColors.getInt(formatting);
	}

	@SuppressWarnings("WrapperTypeMayBePrimitive")
	public static Style applyLegacyFormat(Style in, TextFormatting formatting) {
		TextColor textColor = in instanceof StyleExtension ? ((StyleExtension) in).axolotlclient_rendering$getColor() : null;
		Boolean bold = in.getBold();
		Boolean italic = in.getItalic();
		Boolean strikethrough = in.getStrikethrough();
		Boolean underline = in.getUnderlined();
		Boolean obfuscated = in.getObfuscated();
		switch (formatting) {
			case OBFUSCATED:
				obfuscated = true;
				break;
			case BOLD:
				bold = true;
				break;
			case STRIKETHROUGH:
				strikethrough = true;
				break;
			case UNDERLINE:
				underline = true;
				break;
			case ITALIC:
				italic = true;
				break;
			case RESET:
				return new Style();
			default:
				obfuscated = false;
				bold = false;
				strikethrough = false;
				underline = false;
				italic = false;
				textColor = TextColor.fromLegacyFormat(formatting);
		}

		Style n = new Style();
		n.setColor(formatting);
		if (n instanceof StyleExtension) {
			((StyleExtension) n).axolotlclient_rendering$setColor(textColor);
		}
		n.setBold(bold);
		n.setItalic(italic);
		n.setUnderlined(underline);
		n.setStrikethrough(strikethrough);
		n.setObfuscated(obfuscated);
		n.setClickEvent(in.getClickEvent());
		n.setHoverEvent(in.getHoverEvent());
		n.setInsertion(in.getInsertion());
		return n;
	}
}
