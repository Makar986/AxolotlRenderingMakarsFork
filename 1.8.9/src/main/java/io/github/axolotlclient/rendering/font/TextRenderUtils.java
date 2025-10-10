package io.github.axolotlclient.rendering.font;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import io.github.axolotlclient.rendering.StyleExtension;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.text.Formatting;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

@Slf4j
public final class TextRenderUtils {

	private static final Map<Character, Formatting> formattingCodes;
	private static final Object2IntMap<Formatting> formattingColors;

	static {
		var map = new Object2ObjectOpenHashMap<Character, Formatting>();
		map.put('0', Formatting.BLACK);
		map.put('1', Formatting.DARK_BLUE);
		map.put('2', Formatting.DARK_GREEN);
		map.put('3', Formatting.DARK_AQUA);
		map.put('4', Formatting.DARK_RED);
		map.put('5', Formatting.DARK_PURPLE);
		map.put('6', Formatting.GOLD);
		map.put('7', Formatting.GRAY);
		map.put('8', Formatting.DARK_GRAY);
		map.put('9', Formatting.BLUE);
		map.put('a', Formatting.GREEN);
		map.put('b', Formatting.AQUA);
		map.put('c', Formatting.RED);
		map.put('d', Formatting.LIGHT_PURPLE);
		map.put('e', Formatting.YELLOW);
		map.put('f', Formatting.WHITE);
		map.put('k', Formatting.OBFUSCATED);
		map.put('l', Formatting.BOLD);
		map.put('m', Formatting.STRIKETHROUGH);
		map.put('n', Formatting.UNDERLINE);
		map.put('o', Formatting.ITALIC);
		map.put('r', Formatting.RESET);
		formattingCodes = map;
		formattingColors = new Object2IntOpenHashMap<>();
		formattingColors.put(Formatting.BLACK, 0);
		formattingColors.put(Formatting.DARK_BLUE, 0xaa);
		formattingColors.put(Formatting.DARK_GREEN, 0xaa00);
		formattingColors.put(Formatting.DARK_AQUA, 0xaaaa);
		formattingColors.put(Formatting.DARK_RED, 0xaa0000);
		formattingColors.put(Formatting.DARK_PURPLE, 0xaa00aa);
		formattingColors.put(Formatting.GOLD, 0xffaa00);
		formattingColors.put(Formatting.GRAY, 0xaaaaaa);
		formattingColors.put(Formatting.DARK_GRAY, 0x555555);
		formattingColors.put(Formatting.BLUE, 0x5555ff);
		formattingColors.put(Formatting.GREEN, 0x55ff55);
		formattingColors.put(Formatting.AQUA, 5636095);
		formattingColors.put(Formatting.RED, 16733525);
		formattingColors.put(Formatting.LIGHT_PURPLE, 16733695);
		formattingColors.put(Formatting.YELLOW, 16777045);
		formattingColors.put(Formatting.WHITE, 16777215);
	}

	public static Formatting getByCode(char c) {
		return formattingCodes.get(c);
	}

	private static Formatting byCodeOfFirstChar(String code) {
		char c = code.toLowerCase(Locale.ROOT).charAt(0);

		return formattingCodes.get(c);
	}

	private static final Pattern CODE_PATTERN = Pattern.compile("§");

	public static Text format(Text text) {
		Text n = null;
		for (var t : text) {
			if (!t.getContent().contains("§")) {
				var r = new LiteralText(t.getContent());
				r.setStyle(t.getStyle());
				if (n == null) {
					n = r;
				} else {
					n.append(r);
					t.getStyle().setParent(n.getStyle());
				}
			} else {
				var formatted = formatFromCodes(t.getContent());
				formatted.setStyle(t.getStyle());
				if (n == null) {
					n = formatted;
				} else {
					n.append(formatted);
					formatted.getStyle().setParent(n.getStyle());
				}
			}
		}
		return n;
	}

	private static Text formatFromCodes(String formattedString) {
		Text text = new LiteralText("");
		String[] arr = CODE_PATTERN.split(formattedString);

		List<Formatting> modifiers = new ArrayList<>();
		Formatting color = null;
		TextColor br$color = null;
		for (int i = 0, length = arr.length; i < length; i++) {
			String s = arr[i];
			if (s.isEmpty()) {
				continue;
			} else if (i == 0) {
				text.append(s);
				continue;
			}
			Formatting formatting = byCodeOfFirstChar(s);
			Text part;
			int pL = s.length();
			int formatLength = 1;
			if (formatting == null) {
				if (s.toLowerCase(Locale.ROOT).charAt(0) == '#') {
					br$color = TextColor.parseColor(s.substring(0, 7));
					formatLength = 7;
				} else {
					text.append(s);
					continue;
				}
			} else {
				if (formatting.equals(Formatting.RESET)) {
					modifiers.clear();
					color = null;
				} else if (formatting.isModifier()) {
					modifiers.add(formatting);
				} else {
					color = formatting;
				}
			}
			if (pL == 1) {
				continue;
			}
			part = new LiteralText(s.substring(formatLength));

			if (color != null) {
				part.setStyle(part.getStyle().setColor(color));
			}
			if (br$color != null) {
				((StyleExtension) part.getStyle()).axolotlclient_rendering$setColor(br$color);
			}

			if (!modifiers.isEmpty()) {
				for (Formatting mod : modifiers) {
					switch (mod) {
						case OBFUSCATED -> part.getStyle().setObfuscated(true);
						case BOLD -> part.getStyle().setBold(true);
						case ITALIC -> part.getStyle().setItalic(true);
						case UNDERLINE -> part.getStyle().setUnderlined(true);
						case STRIKETHROUGH -> part.getStyle().setStrikethrough(true);
						default -> log.warn("Unexpected modifier: {}", mod);
					}
				}
			}
			text.append(part);
		}
		return text;
	}

	public static int getColor(Formatting formatting) {
		return formattingColors.getInt(formatting);
	}

	public static Style applyLegacyFormat(Style in, Formatting formatting) {
		TextColor textColor = ((StyleExtension) in).axolotlclient_rendering$getColor();
		Boolean bold = in.isBold();
		Boolean italic = in.isItalic();
		Boolean strikethrough = in.isStrikethrough();
		Boolean underline = in.isUnderlined();
		Boolean obfuscated = in.isObfuscated();
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

		var n = new Style();
		n.setColor(formatting);
		((StyleExtension) n).axolotlclient_rendering$setColor(textColor);
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
