package io.github.axolotlclient.rendering;

import io.github.axolotlclient.rendering.font.Font;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;

public class Rendering implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ResourceManagerHelper.get(PackType.CLIENT_RESOURCES)
			.registerReloadListener(new SimpleSynchronousResourceReloadListener() {
				@Override
				public ResourceLocation getFabricId() {
					return new ResourceLocation(RenderingCommon.MODID,
						"font_reloader");
				}

				@Override
				public void onResourceManagerReload(ResourceManager resourceManager) {
					Font.reload();
				}
			});
		ClientLifecycleEvents.CLIENT_STOPPING.register(mc -> Font.closeAll());
	}
}
