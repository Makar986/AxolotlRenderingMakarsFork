package io.github.axolotlclient.rendering;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.renderer.RenderType;
import org.joml.Matrix4f;

public class RenderingTestMod implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		HudRenderCallback.EVENT.register((guiGraphics, deltaTracker) -> {
			guiGraphics.axolotlclient_rendering$roundedRect(10, 10, 60, 60, 0xFF777777, 5);
			guiGraphics.axolotlclient_rendering$roundedRect(80, 100, 180, 160, 0xFF777777, 9);
			guiGraphics.axolotlclient_rendering$roundedRect(80, 10, 180, 30, 0x77777777, 9);
			guiGraphics.axolotlclient_rendering$roundedRect(80, 50, 180, 70, 0xAA777777, 100);
			Matrix4f matrix4f = guiGraphics.pose().last().pose();

			// Why. I would like to know which gl states this resets.
			VertexConsumer vertexConsumer = guiGraphics.bufferSource().getBuffer(RenderType.gui());
			vertexConsumer.vertex(matrix4f, 0, 0, 0).color(0, 0, 0, 0).endVertex();
			guiGraphics.bufferSource().endBatch(RenderType.gui());
		});
	}
}
