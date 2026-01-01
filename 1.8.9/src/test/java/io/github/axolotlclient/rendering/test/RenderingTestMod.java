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

package io.github.axolotlclient.rendering.test;

import io.github.axolotlclient.rendering.DrawUtil;
import io.github.axolotlclient.rendering.font.DefaultFont;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiElement;
import net.minecraft.resource.Identifier;


public class RenderingTestMod implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
	}

	public static void renderHud() {
		var font = DefaultFont.inter();
		var guiGraphics = DrawUtil.get();
		var adapter = DefaultFont.interAdapter();

		guiGraphics.axolotlclient_rendering$roundedRectGradientVarying(10, 10, 60, 60, 0xFF777777, 0xFF777777, 0xFFFF7777, 0xFF777777, 5, 0, 0, 5);
		guiGraphics.axolotlclient_rendering$roundedRectVarying(10, 60, 60, 80, 0xFF77CC77, 0, 5, 5, 0);
		guiGraphics.axolotlclient_rendering$outlineRoundedRect(10, 10, 60, 50, 0xFF77AAAA, 5, 3);
		guiGraphics.axolotlclient_rendering$roundedRect(80, 100, 180, 160, 0xFF777777, 9);
		guiGraphics.axolotlclient_rendering$roundedRect(80, 10, 180, 30, 0x77777777, 9);
		guiGraphics.axolotlclient_rendering$outlineRoundedRect(80, 10, 180, 30, 0x99FF3333, 9, 3);
		guiGraphics.axolotlclient_rendering$roundedRect(80, 50, 180, 70, 0xAA777777, 10);
		guiGraphics.axolotlclient_rendering$circleBlit(new Identifier("minecraft", "textures/gui/title/background/panorama_0.png"),
			300, 120, 100);
		guiGraphics.axolotlclient_rendering$outlineCircleBlit(new Identifier("minecraft", "textures/gui/title/background/panorama_1.png"),
			300, 120, -1, 100, 5f);

		var t = "Example §otext§r §nwith§r §kmultiple§r §mwords and §5some §a§lcolors§r.";
		Minecraft.getInstance().textRenderer.draw(t, 20, 100, -1, true);
		guiGraphics.axolotlclient_rendering$drawString(font, t, 20, 110, -1);
		font.withSize(12, () -> guiGraphics.axolotlclient_rendering$drawString(font, t, 20, 125, -1));
		var w = "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, " +
			"sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, " +
			"sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. " +
			"Stet clita kasd gubergren, no sea takimata sanctus est";
		guiGraphics.axolotlclient_rendering$drawWordWrap(font, w, 20, 150, 80, -1);
		guiGraphics.axolotlclient_rendering$blitSVG(new Identifier("axolotlclient-rendering", "textures/svg/23.svg"),
			guiGraphics.guiWidth() - 50, 0, 50, 50, -1);
		var el = new GuiElement();
		el.drawString(adapter, "AAAAaÄǍÄÅ", 20, 140, -1);
		Minecraft.getInstance().textRenderer.drawSplit(w, 180, 150, 80, -1);
		guiGraphics.axolotlclient_rendering$roundedVLine(guiGraphics.guiWidth() - 30, 150 - 1, 200, 0xFF8833FF);
		guiGraphics.axolotlclient_rendering$roundedRect(guiGraphics.guiWidth() - 15, 150, guiGraphics.guiWidth() - 14, 200, 0xFF885533, 1.5f);
		guiGraphics.axolotlclient_rendering$roundedRect(guiGraphics.guiWidth() - 18, 150, guiGraphics.guiWidth() - 16, 200, 0xFF777777, 1.5f);
		guiGraphics.axolotlclient_rendering$roundedRect(guiGraphics.guiWidth() - 25, 150, guiGraphics.guiWidth() - 20, 200, 0xFF885533, 2.75f);
		guiGraphics.axolotlclient_rendering$roundedRect(guiGraphics.guiWidth() - 30, 220, guiGraphics.guiWidth() - 10, 230, 0xFFFFAA33, 5f);
		guiGraphics.axolotlclient_rendering$outlineRoundedRect(guiGraphics.guiWidth() - 30, 220, guiGraphics.guiWidth() - 10, 230, 0xFF222222, 5f, 1f);

		guiGraphics.axolotlclient_rendering$roundedRect(250-5, 60-5, 300+5, 60+5, -1, 0);
		guiGraphics.axolotlclient_rendering$segmentGradientH(250, 60, 300, 60, 0xFFFF0000, 0xFF00FF00, 5);

		var r = 5;
		guiGraphics.axolotlclient_rendering$segmentGradientH(200, 100, 250, 70, 0xFF00FF00, 0xFF0000FF, r);
		guiGraphics.axolotlclient_rendering$segmentGradientH(200, 70, 250, 100, 0xFF00FF00, 0xFF0000FF, r);

		guiGraphics.axolotlclient_rendering$segmentGradientH(200, 100, 250, 130, 0xFF00FF00, 0xFF0000FF, r);
		guiGraphics.axolotlclient_rendering$segmentGradientH(200, 130, 250, 100, 0xFF00FF00, 0xFF0000FF, r);

		guiGraphics.axolotlclient_rendering$segmentGradientH(200, 100, 150, 130, 0xFF00FF00, 0xFF0000FF, r);
		guiGraphics.axolotlclient_rendering$segmentGradientH(200, 130, 150, 100, 0xFF00FF00, 0xFF0000FF, r);

		guiGraphics.axolotlclient_rendering$segmentGradientH(200, 100, 150, 70, 0xFF00FF00, 0xFF0000FF, r);
		guiGraphics.axolotlclient_rendering$segmentGradientH(200, 70, 150, 100, 0xFF00FF00, 0xFF0000FF, r);

		guiGraphics.axolotlclient_rendering$segmentGradientV(150, 50, 250, 70, 0xFFFF0000, 0xFF00FF00, 1f);
	}
}
