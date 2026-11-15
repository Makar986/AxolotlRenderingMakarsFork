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
import net.minecraft.util.IChatComponent;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;

/**
 * Convenience class to adapt {@link io.github.axolotlclient.rendering.font.Font} to
 * Minecraft's {@link FontRenderer} so it can be used in methods designed for the latter.
 */
public class FontAdapter extends FontRenderer {
	private static final Minecraft mc = Minecraft.getMinecraft();
	private final io.github.axolotlclient.rendering.font.Font font;

	public FontAdapter(io.github.axolotlclient.rendering.font.Font font) {
		super(mc.gameSettings, new ResourceLocation("textures/font/ascii.png"), mc.getTextureManager(), false);
		this.font = font;
	}

	@Override
	public int drawString(String string, float f, float g, int i, boolean bl) {
		DrawUtil.get().axolotlclient_rendering$drawString(font, string, f, g, adjustColor(i), bl);
		return (int) (f + font.width(string));
	}

	@Override
	public int getStringWidth(String string) {
		return MathHelper.ceiling_float_int(font.width(string));
	}

	@Override
	public int getCharWidth(char c) {
		return MathHelper.ceiling_float_int(font.width(String.valueOf(c)));
	}

	@Override
	public void drawSplitString(String string, int i, int j, int k, int l) {
		DrawUtil.get().axolotlclient_rendering$drawWordWrap(font, string, i, j, k, adjustColor(l));
	}

	@Override
	public int splitStringWidth(String string, int i) {
		return font.lineHeight() * font.split(string, i).size();
	}

	static int adjustColor(int color) {
		return (color & 0xFC000000) == 0 ? color | 0xFF000000 : color;
	}

	@Override
	public List<String> listFormattedStringToWidth(String string, int i) {
		return font.split(string, i).stream().map(IChatComponent::getFormattedText).collect(Collectors.toList());
	}
}
