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
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.ITextComponent;

public class StringDecomposer {
	private static final char REPLACEMENT_CHAR = '\uFFFD';
	@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
	private static final Optional<Object> STOP_ITERATION = Optional.of(new Object());

	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	private static boolean feedChar(Style style, FormattedCharSink sink, int position, char character) {
		return Character.isSurrogate(character) ? sink.accept(position, style, REPLACEMENT_CHAR) : sink.accept(position, style, character);
	}

	public static boolean iterate(String text, Style style, FormattedCharSink sink) {
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

	public static boolean iterateBackwards(String text, Style style, FormattedCharSink sink) {
		int i = text.length();

		for (int j = i - 1; j >= 0; j--) {
			char c = text.charAt(j);
			if (Character.isLowSurrogate(c)) {
				if (j - 1 < 0) {
					if (!sink.accept(0, style, 65533)) {
						return false;
					}
					break;
				}

				char d = text.charAt(j - 1);
				if (Character.isHighSurrogate(d)) {
					if (!sink.accept(--j, style, Character.toCodePoint(d, c))) {
						return false;
					}
				} else if (!sink.accept(j, style, 65533)) {
					return false;
				}
			} else if (!feedChar(style, sink, j, c)) {
				return false;
			}
		}

		return true;
	}

	public static boolean iterateFormatted(String text, Style style, FormattedCharSink sink) {
		return iterateFormatted(text, 0, style, sink);
	}

	public static boolean iterateFormatted(String text, int skip, Style style, FormattedCharSink sink) {
		return iterateFormatted(text, skip, style, style, sink);
	}

	public static boolean iterateFormatted(String text, int skip, Style currentStyle, Style defaultStyle, FormattedCharSink sink) {
		int length = text.length();
		Style style = currentStyle;

		for (int index = skip; index < length; index++) {
			char c = text.charAt(index);
			if (c == '§') {
				if (index + 1 >= length) {
					break;
				}

				char code = text.charAt(index + 1);
				TextFormatting chatFormatting = TextRenderUtils.getByCode(code);
				if (chatFormatting != null) {
					style = chatFormatting == TextFormatting.RESET ? defaultStyle : TextRenderUtils.applyLegacyFormat(style, chatFormatting);
				} else if (code == '#' && index + 8 < length) {
					String color = text.substring(index+1, index+8);
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

				char next = text.charAt(index + 1);
				if (Character.isLowSurrogate(next)) {
					if (!sink.accept(index, style, Character.toCodePoint(c, next))) {
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

	public static boolean iterateFormatted(ITextComponent text, Style style, FormattedCharSink sink) {
		Optional<Object> opt = Optional.empty();
		for (ITextComponent t : text) {
			Style tStyle = t.getStyle();
			Style applied = (tStyle instanceof StyleExtension) ? ((StyleExtension) tStyle).axolotlclient_rendering$applyTo(style) : tStyle;
			if (applied == null) {
				applied = style;
			}
			if (!iterateFormatted(t.getUnformattedComponentText(), 0, applied, sink)) {
				opt = STOP_ITERATION;
			}
			if (opt.isPresent()) {
				break;
			}
		}
		return !opt.isPresent();
	}
}
