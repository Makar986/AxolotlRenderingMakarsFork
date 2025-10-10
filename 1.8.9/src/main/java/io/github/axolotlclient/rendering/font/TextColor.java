package io.github.axolotlclient.rendering.font;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.Getter;
import net.minecraft.text.Formatting;
import org.jetbrains.annotations.Nullable;

public class TextColor {
	private static final Map<Formatting, TextColor> LEGACY_FORMAT_TO_COLOR = Stream.of(Formatting.values())
		.filter(Formatting::isColor)
		.collect(Collectors.toUnmodifiableMap(Function.identity(), chatFormatting -> new TextColor(TextRenderUtils.getColor(chatFormatting), chatFormatting.getName())));
	private static final Map<String, TextColor> NAMED_COLORS = LEGACY_FORMAT_TO_COLOR.values()
		.stream()
		.collect(Collectors.toUnmodifiableMap(textColor -> textColor.name, Function.identity()));
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
	public static TextColor fromLegacyFormat(Formatting formatting) {
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
