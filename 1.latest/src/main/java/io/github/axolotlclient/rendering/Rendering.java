package io.github.axolotlclient.rendering;

import io.github.axolotlclient.rendering.font.Font;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;

public class Rendering implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ResourceLoader.get(PackType.CLIENT_RESOURCES)
			.registerReloader(ResourceLocation.fromNamespaceAndPath(RenderingCommon.MODID,
					"font_reloader"),
				(ResourceManagerReloadListener) resourceManager -> Font.reload());
		ClientLifecycleEvents.CLIENT_STOPPING.register(mc -> Font.closeAll());

	}
}
