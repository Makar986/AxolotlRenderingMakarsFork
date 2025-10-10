package io.github.axolotlclient.rendering.font;

import net.minecraft.text.Style;

public interface FormattedCharSink {
	boolean accept(int i, Style style, int codepoint);
}
