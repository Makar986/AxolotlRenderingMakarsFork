package io.github.axolotlclient.rendering;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.resources.ResourceLocation;

public class RenderingTestMod implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		HudElementRegistry.addLast(ResourceLocation.fromNamespaceAndPath(RenderingCommon.MODID, "test_hud"),
			(guiGraphics, deltaTracker) -> {
				guiGraphics.axolotlclient_rendering$roundedRect(10, 10, 60, 60, 0xFF777777, 5);
				guiGraphics.axolotlclient_rendering$roundedRect(80, 100, 180, 160, 0xFF777777, 9);
				guiGraphics.axolotlclient_rendering$roundedRect(80, 10, 180, 30, 0x77777777, 9);
				guiGraphics.axolotlclient_rendering$roundedRect(80, 50, 180, 70, 0xAA777777, 15);
				guiGraphics.axolotlclient_rendering$roundedBlit(ResourceLocation.fromNamespaceAndPath("minecraft", "textures/gui/title/background/panorama_0.png"),
					200, 20, 0.f,0, 200, 200, 200, 200, 200);
			});
	}
}
