package io.github.axolotlclient.rendering;

import java.io.IOException;

import io.github.axolotlclient.rendering.font.textogl.Font;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

public class RenderingTestMod implements ClientModInitializer {
	private Font font;

	@Override
	public void onInitializeClient() {
		HudElementRegistry.addLast(ResourceLocation.fromNamespaceAndPath(RenderingCommon.MODID, "test_hud"),
			(guiGraphics, deltaTracker) -> {
				if (font == null) {
					try {
						font = Font.read(this.getClass().getResourceAsStream("/assets/axolotlclient_rendering/Inter-Regular.ttf"),
							9);
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				}
				guiGraphics.axolotlclient_rendering$roundedRect(10, 10, 60, 60, 0xFF777777, 5);
				guiGraphics.axolotlclient_rendering$outlineRoundedRect(10, 10, 60, 60, 0xFF77AAAA, 5, 3);
				guiGraphics.axolotlclient_rendering$roundedRect(80, 100, 180, 160, 0xFF777777, 9);
				guiGraphics.axolotlclient_rendering$roundedRect(80, 10, 180, 30, 0x77777777, 9);
				guiGraphics.axolotlclient_rendering$outlineRoundedRect(80, 10, 180, 30, 0x99FF3333, 9, 3);
				guiGraphics.axolotlclient_rendering$roundedRect(80, 50, 180, 70, 0xAA777777, 15);
				guiGraphics.axolotlclient_rendering$circleBlit(ResourceLocation.fromNamespaceAndPath("minecraft", "textures/gui/title/background/panorama_0.png"),
					300, 120, 100);
				guiGraphics.axolotlclient_rendering$outlineCircleBlit(ResourceLocation.fromNamespaceAndPath("minecraft", "textures/gui/title/background/panorama_1.png"),
					300, 120, -1, 100, 5f);
				var t = "Example text with multiple words";
				var w = Minecraft.getInstance().font.width(t);
				guiGraphics.drawString(Minecraft.getInstance().font, t, 20, 100, -1);
				font.renderText(guiGraphics, t, 20+w+2, 100, -1);
			});
	}
}
