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

import java.util.Optional;

import io.github.axolotlclient.rendering.StyleExtension;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

public class StringDecomposer {
	private static final char REPLACEMENT_CHAR = '\uFFFD';
	@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
	private static final Optional<Object> STOP_ITERATION = Optional.of(new Object());

	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	private static boolean feedChar(ChatStyle style, FormattedCharSink sink, int position, char character) {
		return Character.isSurrogate(character) ? sink.accept(position, style, REPLACEMENT_CHAR) : sink.accept(position, style, character);
	}

	public static boolean iterate(String text, ChatStyle style, FormattedCharSink sink) {
		int i = text.length();

		for (int j = 0; j < i; j++) {
			char c = text.charAt(j);
			if (Character.isHighSurrogate(c)) {
				if (j + 1 >= i) {
					if (!sink.accept(j, style, REPLACEMENT_CHAR)) {
						return false;
					}
					break;
				}

				char d = text.charAt(j + 1);
				if (Character.isLowSurrogate(d)) {
					if (!sink.accept(j, style, Character.toCodePoint(c, d))) {
						return false;
					}

					j++;
				} else if (!sink.accept(j, style, REPLACEMENT_CHAR)) {
					return false;
				}
			} else if (!feedChar(style, sink, j, c)) {
				return false;
			}
		}

		return true;
	}

	public static boolean iterateBackwards(String text, ChatStyle style, FormattedCharSink sink) {
		int i = text.length();

		for (int j = i - 1; j >= 0; j--) {
			char c = text.charAt(j);
			if (Character.isLowSurrogate(c)) {
				if (j - 1 < 0) {
					if (!sink.accept(0, style, REPLACEMENT_CHAR)) {
						return false;
					}
					break;
				}

				char d = text.charAt(j - 1);
				if (Character.isHighSurrogate(d)) {
					j--;
					if (!sink.accept(j, style, Character.toCodePoint(d, c))) {
						return false;
					}
				} else if (!sink.accept(j, style, REPLACEMENT_CHAR)) {
					return false;
				}
			} else if (!feedChar(style, sink, j, c)) {
				return false;
			}
		}

		return true;
	}

	public static boolean iterateFormatted(String text, ChatStyle style, FormattedCharSink sink) {
		return iterateFormatted(text, 0, style, sink);
	}

	public static boolean iterateFormatted(String text, int skip, ChatStyle style, FormattedCharSink sink) {
		return iterateFormatted(text, skip, style, style, sink);
	}

	public static boolean iterateFormatted(String text, int skip, ChatStyle currentStyle, ChatStyle defaultStyle, FormattedCharSink sink) {
		int length = text.length();
		ChatStyle style = currentStyle;

		for (int index = skip; index < length; index++) {
			char c = text.charAt(index);
			if (c == 167) {
				if (index + 1 >= length) {
					break;
				}

				char code = text.charAt(index + 1);
				EnumChatFormatting chatFormatting = TextRenderUtils.getByCode(code);
				if (chatFormatting != null) {
					style = chatFormatting == EnumChatFormatting.RESET ? defaultStyle : TextRenderUtils.applyLegacyFormat(style, chatFormatting);
				} else if (code == '#' && index + 8 < length) {
					String color = text.substring(index + 1, index + 8);
					if (style instanceof StyleExtension) {
						style = ((StyleExtension) style).axolotlclient_rendering$setColor(TextColor.parseColor(color));
					}
					index += 6;
				}

				index++;
			} else if (Character.isHighSurrogate(c)) {
				if (index + 1 >= length) {
					if (!sink.accept(index, style, REPLACEMENT_CHAR)) {
						return false;
					}
					break;
				}

				char d = text.charAt(index + 1);
				if (Character.isLowSurrogate(d)) {
					if (!sink.accept(index, style, Character.toCodePoint(c, d))) {
						return false;
					}

					index++;
				} else if (!sink.accept(index, style, REPLACEMENT_CHAR)) {
					return false;
				}
			} else if (!feedChar(style, sink, index, c)) {
				return false;
			}
		}

		return true;
	}

	public static boolean iterateFormatted(IChatComponent text, ChatStyle style, FormattedCharSink sink) {
		Optional<Object> opt = Optional.empty();
		for (IChatComponent t : text) {
			ChatStyle tStyle = t.getChatStyle();
			ChatStyle applied = (tStyle instanceof StyleExtension) ? ((StyleExtension) tStyle).axolotlclient_rendering$applyTo(style) : tStyle;
			if (applied == null) {
				applied = style;
			}
			if (!iterateFormatted(t.getUnformattedTextForChat(), 0, applied, sink)) {
				opt = STOP_ITERATION;
			}
			if (opt.isPresent()) {
				break;
			}
		}
		return !opt.isPresent();
	}
}
