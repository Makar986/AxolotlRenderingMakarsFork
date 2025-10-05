package io.github.axolotlclient.rendering;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.resources.ResourceLocation;

public class RenderingTestMod implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		HudRenderCallback.EVENT.register((guiGraphics, deltaTracker) -> {
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
		});
	}
}
