package io.github.axolotlclient.rendering;

import io.github.axolotlclient.rendering.font.TextColor;
import net.minecraft.text.Style;

public interface StyleExtension {
	TextColor axolotlclient_rendering$getColor();
	void axolotlclient_rendering$setColor(TextColor value);

	Style axolotlclient_rendering$applyTo(Style other);
}
