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

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;

@Slf4j
public final class TextRenderUtils {

	private static final Map<Character, EnumChatFormatting> formattingCodes;
	private static final Object2IntMap<EnumChatFormatting> formattingColors;

	static {
		Object2ObjectOpenHashMap<Character, EnumChatFormatting> map = new Object2ObjectOpenHashMap<Character, EnumChatFormatting>();
		map.put('0', EnumChatFormatting.BLACK);
		map.put('1', EnumChatFormatting.DARK_BLUE);
		map.put('2', EnumChatFormatting.DARK_GREEN);
		map.put('3', EnumChatFormatting.DARK_AQUA);
		map.put('4', EnumChatFormatting.DARK_RED);
		map.put('5', EnumChatFormatting.DARK_PURPLE);
		map.put('6', EnumChatFormatting.GOLD);
		map.put('7', EnumChatFormatting.GRAY);
		map.put('8', EnumChatFormatting.DARK_GRAY);
		map.put('9', EnumChatFormatting.BLUE);
		map.put('a', EnumChatFormatting.GREEN);
		map.put('b', EnumChatFormatting.AQUA);
		map.put('c', EnumChatFormatting.RED);
		map.put('d', EnumChatFormatting.LIGHT_PURPLE);
		map.put('e', EnumChatFormatting.YELLOW);
		map.put('f', EnumChatFormatting.WHITE);
		map.put('k', EnumChatFormatting.OBFUSCATED);
		map.put('l', EnumChatFormatting.BOLD);
		map.put('m', EnumChatFormatting.STRIKETHROUGH);
		map.put('n', EnumChatFormatting.UNDERLINE);
		map.put('o', EnumChatFormatting.ITALIC);
		map.put('r', EnumChatFormatting.RESET);
		formattingCodes = map;
		formattingColors = new Object2IntOpenHashMap<>();
		formattingColors.put(EnumChatFormatting.BLACK, 0);
		formattingColors.put(EnumChatFormatting.DARK_BLUE, 0xaa);
		formattingColors.put(EnumChatFormatting.DARK_GREEN, 0xaa00);
		formattingColors.put(EnumChatFormatting.DARK_AQUA, 0xaaaa);
		formattingColors.put(EnumChatFormatting.DARK_RED, 0xaa0000);
		formattingColors.put(EnumChatFormatting.DARK_PURPLE, 0xaa00aa);
		formattingColors.put(EnumChatFormatting.GOLD, 0xffaa00);
		formattingColors.put(EnumChatFormatting.GRAY, 0xaaaaaa);
		formattingColors.put(EnumChatFormatting.DARK_GRAY, 0x555555);
		formattingColors.put(EnumChatFormatting.BLUE, 0x5555ff);
		formattingColors.put(EnumChatFormatting.GREEN, 0x55ff55);
		formattingColors.put(EnumChatFormatting.AQUA, 5636095);
		formattingColors.put(EnumChatFormatting.RED, 16733525);
		formattingColors.put(EnumChatFormatting.LIGHT_PURPLE, 16733695);
		formattingColors.put(EnumChatFormatting.YELLOW, 16777045);
		formattingColors.put(EnumChatFormatting.WHITE, 16777215);
	}

	public static EnumChatFormatting getByCode(char c) {
		return formattingCodes.get(c);
	}

	public static int getColor(EnumChatFormatting formatting) {
		return formattingColors.getInt(formatting);
	}

	@SuppressWarnings("WrapperTypeMayBePrimitive")
	public static ChatStyle applyLegacyFormat(ChatStyle in, EnumChatFormatting formatting) {
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
				return new ChatStyle();
			default:
				obfuscated = false;
				bold = false;
				strikethrough = false;
				underline = false;
				italic = false;
		}

		ChatStyle n = new ChatStyle();
		n.setColor(formatting);
		n.setBold(bold);
		n.setItalic(italic);
		n.setUnderlined(underline);
		n.setStrikethrough(strikethrough);
		n.setObfuscated(obfuscated);
		n.setChatClickEvent(in.getChatClickEvent());
		n.setChatHoverEvent(in.getChatHoverEvent());
		n.setInsertion(in.getInsertion());
		return n;
	}
}
