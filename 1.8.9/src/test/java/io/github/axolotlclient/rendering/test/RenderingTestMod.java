package io.github.axolotlclient.rendering.test;

import io.github.axolotlclient.rendering.DrawUtil;
import net.fabricmc.api.ClientModInitializer;


public class RenderingTestMod implements ClientModInitializer {
	@Override
	public void onInitializeClient() {

	}

	public static void renderHud() {
		var guiGraphics = DrawUtil.get();
		guiGraphics.axolotlclient_rendering$roundedRect(10, 10, 60, 60, 0xFF777777, 5);
		guiGraphics.axolotlclient_rendering$roundedRect(80, 100, 180, 160, 0xFF777777, 9);
		guiGraphics.axolotlclient_rendering$roundedRect(80, 10, 180, 30, 0x77777777, 9);
		guiGraphics.axolotlclient_rendering$roundedRect(80, 50, 180, 70, 0xAA777777, 100);
	}
}
