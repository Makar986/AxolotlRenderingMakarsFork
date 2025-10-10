package io.github.axolotlclient.rendering.test;

import java.io.IOException;

import io.github.axolotlclient.rendering.DrawUtil;
import io.github.axolotlclient.rendering.font.Font;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.Minecraft;
import net.minecraft.resource.Identifier;


public class RenderingTestMod implements ClientModInitializer {
	private static Font font;

	@Override
	public void onInitializeClient() {
	}

	public static void renderHud() {
		if (font == null) {
			try {
				font = Font.read(RenderingTestMod.class.getResourceAsStream("/assets/axolotlclient_rendering/fonts/InterVariable.ttf"), 9)
					.addSubFont(Font.TAG_ITALIC, Font.ITALIC_ENABLED,
						Font.read(RenderingTestMod.class.getResourceAsStream("/assets/axolotlclient_rendering/fonts/InterVariable-Italic.ttf"), 9));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		var guiGraphics = DrawUtil.get();
		guiGraphics.axolotlclient_rendering$roundedRect(10, 10, 60, 60, 0xFF777777, 5);
		guiGraphics.axolotlclient_rendering$outlineRoundedRect(10, 10, 60, 60, 0xFF77AAAA, 5, 3);
		guiGraphics.axolotlclient_rendering$roundedRect(80, 100, 180, 160, 0xFF777777, 9);
		guiGraphics.axolotlclient_rendering$roundedRect(80, 10, 180, 30, 0x77777777, 9);
		guiGraphics.axolotlclient_rendering$outlineRoundedRect(80, 10, 180, 30, 0x99FF3333, 9, 3);
		guiGraphics.axolotlclient_rendering$roundedRect(80, 50, 180, 70, 0xAA777777, 15);
		guiGraphics.axolotlclient_rendering$circleBlit(new Identifier("minecraft", "textures/gui/title/background/panorama_0.png"),
			300, 120, 100);
		guiGraphics.axolotlclient_rendering$outlineCircleBlit(new Identifier("minecraft", "textures/gui/title/background/panorama_1.png"),
			300, 120, -1, 100, 5f);

		var t = "Example §otext§r §nwith§r §kmultiple§r §mwords and §5some §a§lcolors§r.";
		Minecraft.getInstance().textRenderer.draw(t, 20, 100, -1, true);
		guiGraphics.axolotlclient_rendering$drawString(font, t, 20, 110, -1);
	}
}
