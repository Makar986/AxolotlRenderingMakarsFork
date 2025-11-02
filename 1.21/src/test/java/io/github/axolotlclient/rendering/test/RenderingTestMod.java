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

import java.io.IOException;

import io.github.axolotlclient.rendering.font.Font;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

public class RenderingTestMod implements ClientModInitializer {
	private Font font;

	@Override
	public void onInitializeClient() {
		HudRenderCallback.EVENT.register((guiGraphics, deltaTracker) -> {
			if (font == null) {
				try {
					font = Font.read(this.getClass().getResourceAsStream("/assets/axolotlclient_rendering/fonts/InterVariable.ttf"), 9)
						.addSubFont(Font.TAG_ITALIC, Font.ITALIC_ENABLED,
							Font.read(this.getClass().getResourceAsStream("/assets/axolotlclient_rendering/fonts/InterVariable-Italic.ttf"), 9));
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
			guiGraphics.axolotlclient_rendering$roundedRectVarying(10, 10, 60, 60, 0xFF777777, 5, 0, 0, 5);
			guiGraphics.axolotlclient_rendering$roundedRectVarying(10, 60, 60, 80, 0xFF77CC77, 0, 5, 5, 0);
			guiGraphics.axolotlclient_rendering$outlineRoundedRect(10, 10, 60, 50, 0xFF77AAAA, 5, 3);
			guiGraphics.axolotlclient_rendering$roundedRect(80, 100, 180, 160, 0xFF777777, 9);
			guiGraphics.axolotlclient_rendering$roundedRect(80, 10, 180, 30, 0x77777777, 9);
			guiGraphics.axolotlclient_rendering$outlineRoundedRect(80, 10, 180, 30, 0x99FF3333, 9, 3);
			guiGraphics.axolotlclient_rendering$roundedRect(80, 50, 180, 70, 0xAA777777, 15);
			guiGraphics.axolotlclient_rendering$circleBlit(ResourceLocation.fromNamespaceAndPath("minecraft", "textures/gui/title/background/panorama_0.png"),
				300, 120, 100);
			guiGraphics.axolotlclient_rendering$outlineCircleBlit(ResourceLocation.fromNamespaceAndPath("minecraft", "textures/gui/title/background/panorama_1.png"),
				300, 120, -1, 100, 5f);
			var t = "Example §otext§r §nwith§r §kmultiple§r §mwords and §5some §a§lcolors§r.";
			guiGraphics.drawString(Minecraft.getInstance().font, t, 20, 100, -1);
			guiGraphics.axolotlclient_rendering$drawString(font, t, 20, 110, -1);
			guiGraphics.axolotlclient_rendering$blitSVG(ResourceLocation.fromNamespaceAndPath("axolotlclient_rendering", "textures/svg/23.svg"),
				guiGraphics.guiWidth()-50, 0, 50, 50, -1);
		});
	}
}
