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

import com.mojang.blaze3d.matrix.MatrixStack;
import io.github.axolotlclient.rendering.GuiGraphicsExtension;
import io.github.axolotlclient.rendering.font.DefaultFont;
import io.github.axolotlclient.rendering.font.Font;
import io.github.axolotlclient.rendering.font.FontAdapter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod("axolotlclient_rendering_test")
public class RenderingTestMod {

	private static final AbstractGui gui = new AbstractGui() {};

	public RenderingTestMod() {
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
		if (event.getType() != RenderGameOverlayEvent.ElementType.ALL) return;

		MatrixStack stack = event.getMatrixStack();
		GuiGraphicsExtension guiGraphics = (GuiGraphicsExtension) gui;
		Font font = DefaultFont.inter();
		FontAdapter adapter = DefaultFont.interAdapter();

		guiGraphics.axolotlclient_rendering$roundedRectGradientVarying(stack, 10, 10, 60, 60, 0xFF777777, 0xFF777777, 0xFFFF7777, 0xFF777777, 5, 0, 0, 5);
		guiGraphics.axolotlclient_rendering$roundedRectVarying(stack, 10, 60, 60, 80, 0xFF77CC77, 0, 5, 5, 0);
		guiGraphics.axolotlclient_rendering$outlineRoundedRect(stack, 10, 10, 60, 50, 0xFF77AAAA, 5, 3);
		guiGraphics.axolotlclient_rendering$roundedRect(stack, 80, 100, 180, 160, 0xFF777777, 9);
		guiGraphics.axolotlclient_rendering$roundedRect(stack, 80, 10, 180, 30, 0x77777777, 9);
		guiGraphics.axolotlclient_rendering$outlineRoundedRect(stack, 80, 10, 180, 30, 0x99FF3333, 9, 3);
		guiGraphics.axolotlclient_rendering$roundedRect(stack, 80, 50, 180, 70, 0xAA777777, 10);
		guiGraphics.axolotlclient_rendering$circleBlit(stack, new ResourceLocation("minecraft", "textures/gui/title/background/panorama_0.png"),
			300, 120, 100);
		guiGraphics.axolotlclient_rendering$outlineCircleBlit(stack, new ResourceLocation("minecraft", "textures/gui/title/background/panorama_1.png"),
			300, 120, -1, 100, 5f);

		String t = "Example §otext§r §nwith§r §kmultiple§r §mwords and §5some §a§lcolors§r.";
		AbstractGui.drawString(stack, Minecraft.getInstance().font, t, 20, 100, -1);
		guiGraphics.axolotlclient_rendering$drawString(stack, font, t, 20, 110, -1);
		font.withSize(12, () -> guiGraphics.axolotlclient_rendering$drawString(stack, font, t, 20, 125, -1));
		StringTextComponent w = new StringTextComponent("Lorem ipsum dolor sit amet, consetetur sadipscing elitr, " +
			"sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, " +
			"sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. " +
			"Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet.");
		guiGraphics.axolotlclient_rendering$drawWordWrap(stack, font, w, 20, 140, 200, -1);
		AbstractGui.drawString(stack, Minecraft.getInstance().font, "Using FontAdapter:", 20, 200, -1);
		AbstractGui.drawString(stack, adapter, t, 20, 210, -1);
		adapter.drawWordWrap(w, 20, 230, 200, -1);

		guiGraphics.axolotlclient_rendering$segment(stack, 20, 320, 100, 360, 0xFF00FF00, 0xFFFF0000, 0xFF0000FF, 0xFFFFFF00, 10);

		guiGraphics.axolotlclient_rendering$blitSVG(stack, new ResourceLocation("axolotlclient-rendering", "textures/svg/rainbow.svg"),
			230, 20, 50, 50);
		guiGraphics.axolotlclient_rendering$blitSVG(stack, new ResourceLocation("axolotlclient-rendering", "textures/svg/23.svg"),
			230, 75, 50, 50, 0xFFFF0000);
	}
}
