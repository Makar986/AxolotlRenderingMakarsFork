/*
 * Copyright © 2026 moehreag <moehreag@gmail.com> & Contributors
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

package io.github.axolotlclient.rendering.font.msdf;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.ResourceLocation;

public class MsdfFontAdapter extends FontRenderer {
	private static final Minecraft mc = Minecraft.getMinecraft();
	private final MsdfFont font;
	private final float size;

	public MsdfFontAdapter(MsdfFont font, float size) {
		super(mc.gameSettings, new ResourceLocation("textures/font/ascii.png"), mc.getTextureManager(), false);
		this.font = font;
		this.size = size;
	}

	public MsdfFontAdapter(MsdfFont font) {
		this(font, 9.0f);
	}

	@Override
	public int drawString(String string, float f, float g, int i, boolean bl) {
		if (bl) {
			font.drawWithShadow(string, f, g, size, i);
		} else {
			font.draw(string, f, g, size, i);
		}
		return (int) (f + font.getWidth(string, size));
	}

	@Override
	public int getStringWidth(String string) {
		return MathHelper.ceil(font.getWidth(string, size));
	}

	@Override
	public int getCharWidth(char c) {
		return MathHelper.ceil(font.getWidth(String.valueOf(c), size));
	}

	public MsdfFont getMsdfFont() {
		return font;
	}

	public float getSize() {
		return size;
	}
}
