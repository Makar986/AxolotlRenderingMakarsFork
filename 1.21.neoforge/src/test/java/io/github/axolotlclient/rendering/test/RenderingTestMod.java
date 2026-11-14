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

import io.github.axolotlclient.rendering.LiquidGlassStyle;
import io.github.axolotlclient.rendering.font.DefaultFont;
import io.github.axolotlclient.rendering.font.msdf.DefaultMsdfFont;
import io.github.axolotlclient.rendering.font.msdf.MsdfFont;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.common.NeoForge;

import java.io.File;

@Mod("axolotlclient_rendering_test")
public class RenderingTestMod {

	private static MsdfFont rubikFont = null;
	private static boolean rubikAttempted = false;

	public RenderingTestMod() {
		NeoForge.EVENT_BUS.addListener(this::onRenderGui);
	}

	private void onRenderGui(RenderGuiEvent.Post event) {
		var g = event.getGuiGraphics();
		var guiGraphics = (io.github.axolotlclient.rendering.GuiGraphicsExtension) g;
		var font = DefaultFont.inter();
		var adapter = DefaultFont.interAdapter();

		guiGraphics.axolotlclient_rendering$roundedRectGradientVarying(10, 10, 60, 60, 0xFF777777, 0xFF777777, 0xFFFF7777, 0xFF777777, 5, 0, 0, 5);
		guiGraphics.axolotlclient_rendering$roundedRectVarying(10, 60, 60, 80, 0xFF77CC77, 0, 5, 5, 0);
		guiGraphics.axolotlclient_rendering$outlineRoundedRect(10, 10, 60, 50, 0xFF77AAAA, 5, 3);
		guiGraphics.axolotlclient_rendering$roundedRect(80, 100, 180, 160, 0xFF777777, 9);
		guiGraphics.axolotlclient_rendering$roundedRect(80, 10, 180, 30, 0x77777777, 9);
		guiGraphics.axolotlclient_rendering$outlineRoundedRect(80, 10, 180, 30, 0x99FF3333, 9, 3);
		guiGraphics.axolotlclient_rendering$roundedRect(80, 50, 180, 70, 0xAA777777, 10);
		guiGraphics.axolotlclient_rendering$circleBlit(ResourceLocation.fromNamespaceAndPath("minecraft", "textures/gui/title/background/panorama_0.png"),
			300, 120, 100);
		guiGraphics.axolotlclient_rendering$outlineCircleBlit(ResourceLocation.fromNamespaceAndPath("minecraft", "textures/gui/title/background/panorama_1.png"),
			300, 120, -1, 100, 5f);

		var t = "Example §otext§r §nwith§r §kmultiple§r §mwords and §5some §a§lcolors§r.";
		g.drawString(Minecraft.getInstance().font, t, 20, 100, -1);
		guiGraphics.axolotlclient_rendering$drawString(font, t, 20, 110, -1);
		font.withSize(12, () -> guiGraphics.axolotlclient_rendering$drawString(font, t, 20, 125, -1));
		var w = Component.literal("Lorem ipsum dolor sit amet, consetetur sadipscing elitr, " +
			"sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, " +
			"sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. " +
			"Stet clita kasd gubergren, no sea takimata sanctus est");
		guiGraphics.axolotlclient_rendering$drawWordWrap(font, w, 20, 150, 80, -1);
		guiGraphics.axolotlclient_rendering$blitSVG(ResourceLocation.fromNamespaceAndPath("axolotlclient-rendering", "textures/svg/23.svg"),
			g.guiWidth() - 50, 0, 50, 50, -1);
		g.drawString(adapter, Component.literal("AAAAaÄǍÄÅ"), 20, 140, -1);
		g.drawWordWrap(adapter, w, 100, 150, 80, -1);
		guiGraphics.axolotlclient_rendering$roundedVLine(g.guiWidth() - 30, 150 - 1, 200, 0xFF8833FF);
		guiGraphics.axolotlclient_rendering$roundedRect(g.guiWidth() - 15, 150, g.guiWidth() - 14, 200, 0xFF885533, 1.5f);
		guiGraphics.axolotlclient_rendering$roundedRect(g.guiWidth() - 18, 150, g.guiWidth() - 16, 200, 0xFF777777, 1.5f);
		guiGraphics.axolotlclient_rendering$roundedRect(g.guiWidth() - 25, 150, g.guiWidth() - 20, 200, 0xFF885533, 2.75f);
		guiGraphics.axolotlclient_rendering$roundedRect(g.guiWidth() - 30, 220, g.guiWidth() - 10, 230, 0xFFFFAA33, 5f);
		guiGraphics.axolotlclient_rendering$outlineRoundedRect(g.guiWidth() - 30, 220, g.guiWidth() - 10, 230, 0xFF222222, 5f, 1f);

		guiGraphics.axolotlclient_rendering$roundedRect(250 - 5, 60 - 5, 300 + 5, 60 + 5, -1, 0);
		guiGraphics.axolotlclient_rendering$segmentGradientH(250, 60, 300, 60, 0xFFFF0000, 0xFF00FF00, 5);

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

		// GaussianBlur demo
		guiGraphics.axolotlclient_rendering$roundedGaussianBlur(400, 50, 120, 80, 10f, 6f, 3.5f);

		// LiquidGlass demo (ReGlass physics: Snell refraction, chromatic dispersion, Fresnel & glare)
		LiquidGlassStyle glassStyle = LiquidGlassStyle.create()
			.tint(0xFFFFFF, 0.15f)
			.refractionThickness(13.15f)
			.refractionFactor(1.4f)
			.refractionDispersion(7.0f)
			.fresnel(39.71f, 0.2f, 0.2f)
			.glare(30.0f, 0.2f, 0.9f)
			.blurRadius(8.0f)
			.border(0x80FFFFFF, 1.2f);
		guiGraphics.axolotlclient_rendering$liquidGlass(400, 150, 180, 95, 14f, glassStyle);

		// MSDF Font demo (Montserrat default built-in)
		var msdfFont = DefaultMsdfFont.montserrat();
		if (msdfFont != null) {
			guiGraphics.axolotlclient_rendering$drawMsdfString(msdfFont, "MSDF Montserrat Font", 410, 165, 14f, 0xFFFFFFFF, true);
			guiGraphics.axolotlclient_rendering$drawMsdfString(msdfFont, "ReGlass Liquid Style", 410, 185, 11f, 0xFF77DDFF, true);
			guiGraphics.axolotlclient_rendering$drawMsdfString(msdfFont, "Sharp at 22px!", 410, 205, 22f, 0xFFFFCC00, true);
		}

		// Try loading another MSDF font from user's fonts.zip if present (cached)
		if (!rubikAttempted) {
			rubikAttempted = true;
			try {
				File zip = new File("/home/makar/Downloads/fonts.zip");
				if (zip.exists()) {
					rubikFont = MsdfFont.fromZip(zip, "BoldRubik");
				}
			} catch (Exception ignored) {
			}
		}
		if (rubikFont != null) {
			guiGraphics.axolotlclient_rendering$drawMsdfString(rubikFont, "Loaded from fonts.zip: Rubik Bold 16px", 400, 260, 16f, 0xFF55FF55, true);
		}
	}
}
