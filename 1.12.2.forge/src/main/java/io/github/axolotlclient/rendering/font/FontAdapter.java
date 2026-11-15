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

import java.util.List;
import java.util.stream.Collectors;

import io.github.axolotlclient.rendering.DrawUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;

/**
 * Convenience class to adapt {@link io.github.axolotlclient.rendering.font.Font} to
 * Minecraft's {@link FontRenderer} so it can be used in methods designed for the latter.
 * Only supports GUI rendering, everything else is silently delegated to Minecraft's {@link Font}.
 *
 * @implNote Assumes a font size of <b>9 pixels</b>. Spacing can be janky as a result of this assumption
 * as barely any font will have an integer font size. In order to get correct results, use methods
 * in {@link io.github.axolotlclient.rendering.GuiGraphicsExtension} that directly accept a {@link io.github.axolotlclient.rendering.font.Font}.
 */

public class FontAdapter extends FontRenderer {
	private static final Minecraft mc = Minecraft.getMinecraft();
	private final io.github.axolotlclient.rendering.font.Font font;

	public FontAdapter(io.github.axolotlclient.rendering.font.Font font) {
		super(mc.gameSettings, new ResourceLocation("textures/font/ascii.png"), mc.getTextureManager(), false);
		this.font = font;
		this.FONT_HEIGHT = font.lineHeight();
	}

	@Override
	public int drawString(String text, float x, float y, int color, boolean dropShadow) {
		DrawUtil.get().axolotlclient_rendering$drawString(font, text, x, y, adjustColor(color), dropShadow);
		return (int) (x + font.width(text));
	}

	@Override
	public int getStringWidth(String text) {
		return MathHelper.ceil(font.width(text));
	}

	@Override
	public int getCharWidth(char character) {
		return MathHelper.ceil(font.width(String.valueOf(character)));
	}

	@Override
	public void drawSplitString(String str, int x, int y, int wrapWidth, int textColor) {
		DrawUtil.get().axolotlclient_rendering$drawWordWrap(font, str, x, y, wrapWidth, adjustColor(textColor));
	}

	public int splitAndGetHeight(String string, int wrapWidth) {
		return font.lineHeight() * font.split(string, wrapWidth).size();
	}

	static int adjustColor(int color) {
		return (color & 0xFC000000) == 0 ? color | 0xFF000000 : color;
	}

	@Override
	public List<String> listFormattedStringToWidth(String str, int wrapWidth) {
		return font.split(str, wrapWidth).stream().map(ITextComponent::getFormattedText).collect(Collectors.toList());
	}
}
