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

import io.github.axolotlclient.rendering.GuiGraphicsExtractorExtension;
import io.github.axolotlclient.rendering.RenderingCommon;
import io.github.axolotlclient.rendering.font.DefaultFont;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = "axolotlclient_rendering_test", dist = Dist.CLIENT)
public class RenderingTestMod {

	public RenderingTestMod() {
		NeoForge.EVENT_BUS.addListener(this::render);
	}

	private void render(RenderGuiEvent.Post event) {
		var g = event.getGuiGraphics();
		var guiGraphics = (GuiGraphicsExtractorExtension) (Object) g;
		var font = DefaultFont.inter();
		var adapter = DefaultFont.interAdapter();

		guiGraphics.axolotlclient_rendering$roundedRectGradientVarying(10, 10, 60, 60, 0xFF777777, 0xFF777777, 0xFFFF7777, 0xFF777777, 5, 0, 0, 5);
		guiGraphics.axolotlclient_rendering$roundedRectVarying(10, 60, 60, 80, 0xFF77CC77, 0, 5, 5, 0);
		guiGraphics.axolotlclient_rendering$outlineRoundedRect(10, 10, 60, 50, 0xFF77AAAA, 5, 3);
		guiGraphics.axolotlclient_rendering$roundedRect(80, 100, 180, 160, 0xFF777777, 9);
		guiGraphics.axolotlclient_rendering$roundedRect(80, 10, 180, 30, 0x77777777, 9);
		guiGraphics.axolotlclient_rendering$outlineRoundedRect(80, 10, 180, 30, 0x99FF3333, 9, 3);
		guiGraphics.axolotlclient_rendering$roundedRect(80, 50, 180, 70, 0xAA777777, 10);
		guiGraphics.axolotlclient_rendering$circleBlit(Identifier.fromNamespaceAndPath("minecraft", "textures/gui/title/background/panorama_0.png"),
			300, 120, 100);
		guiGraphics.axolotlclient_rendering$outlineCircleBlit(Identifier.fromNamespaceAndPath("minecraft", "textures/gui/title/background/panorama_1.png"),
			300, 120, -1, 100, 5f);

		var t = "Example §otext§r §nwith§r §kmultiple§r §mwords and §5some §a§lcolors§r.";
		g.text(Minecraft.getInstance().font, t, 20, 100, -1);
		guiGraphics.axolotlclient_rendering$text(font, t, 20, 110, -1);
		font.withSize(12, () -> guiGraphics.axolotlclient_rendering$text(font, t, 20, 125, -1));
		var w = Component.literal("Lorem ipsum dolor sit amet, consetetur sadipscing elitr, " +
			"sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, " +
			"sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. " +
			"Stet clita kasd gubergren, no sea takimata sanctus est");
		guiGraphics.axolotlclient_rendering$textWithWordWrap(font, w, 20, 150, 80, -1);
		guiGraphics.axolotlclient_rendering$blitSVG(Identifier.fromNamespaceAndPath("axolotlclient-rendering", "textures/svg/23.svg"),
			g.guiWidth() - 50, 0, 50, 50, -1);
		g.text(adapter, Component.literal("AAAAaÄǍÄÅ"), 20, 140, -1);
		g.textWithWordWrap(adapter, w, 100, 150, 80, -1);
		g.textWithWordWrap(Minecraft.getInstance().font, w, 180, 150, 80, -1);
		guiGraphics.axolotlclient_rendering$roundedVLine(g.guiWidth() - 30, 150 - 1, 200, 0xFF8833FF);
		guiGraphics.axolotlclient_rendering$roundedRect(g.guiWidth() - 15, 150, g.guiWidth() - 14, 200, 0xFF885533, 1.5f);
		guiGraphics.axolotlclient_rendering$roundedRect(g.guiWidth() - 18, 150, g.guiWidth() - 16, 200, 0xFF777777, 1.5f);
		guiGraphics.axolotlclient_rendering$roundedRect(g.guiWidth() - 25, 150, g.guiWidth() - 20, 200, 0xFF885533, 2.75f);
		guiGraphics.axolotlclient_rendering$roundedRect(g.guiWidth() - 30, 220, g.guiWidth() - 10, 230, 0xFFFFAA33, 5f);
		guiGraphics.axolotlclient_rendering$outlineRoundedRect(g.guiWidth() - 30, 220, g.guiWidth() - 10, 230, 0xFF222222, 5f, 1f);

		var x = 280;
		var y = 160;
		var r = 5;
		guiGraphics.axolotlclient_rendering$roundedRect(x + 70 - r, y - 20 - r, x + 120 + r, y - 20 + r, -1, 0);
		guiGraphics.axolotlclient_rendering$segmentGradientH(x + 70, y - 20, x + 120, y - 20, 0xFFFF0000, 0xFF00FF00, r);

		guiGraphics.axolotlclient_rendering$segmentGradientH(x + 50, y + 30, x + 100, y, 0xFF00FF00, 0xFF0000FF, r);
		guiGraphics.axolotlclient_rendering$segmentGradientH(x + 50, y, x + 100, y + 30, 0xFF00FF00, 0xFF0000FF, r);

		guiGraphics.axolotlclient_rendering$segmentGradientH(x + 50, y + 30, x + 100, y + 60, 0xFF00FF00, 0xFF0000FF, r);
		guiGraphics.axolotlclient_rendering$segmentGradientH(x + 50, y + 60, x + 100, y + 30, 0xFF00FF00, 0xFF0000FF, r);

		guiGraphics.axolotlclient_rendering$segmentGradientH(x + 50, y + 30, x, y + 60, 0xFF00FF00, 0xFF0000FF, r);
		guiGraphics.axolotlclient_rendering$segmentGradientH(x + 50, y + 60, x, y + 30, 0xFF00FF00, 0xFF0000FF, r);

		guiGraphics.axolotlclient_rendering$segmentGradientH(x + 50, y + 30, x, y, 0xFF00FF00, 0xFF0000FF, r);
		guiGraphics.axolotlclient_rendering$segmentGradientH(x + 50, y, x, y + 30, 0xFF00FF00, 0xFF0000FF, r);

		guiGraphics.axolotlclient_rendering$segmentGradientV(x, y - 20, x + 100, y, 0xFFFF0000, 0xFF00FF00, 1f);
	}
}
