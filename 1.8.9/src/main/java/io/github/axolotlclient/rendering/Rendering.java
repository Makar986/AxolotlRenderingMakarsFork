package io.github.axolotlclient.rendering;


import io.github.axolotlclient.rendering.font.Font;
import lombok.extern.slf4j.Slf4j;
import net.fabricmc.api.ClientModInitializer;
import net.ornithemc.osl.lifecycle.api.client.MinecraftClientEvents;
import net.ornithemc.osl.resource.loader.api.ResourceLoaderEvents;

@Slf4j
public class Rendering implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		ExRenderPipelines.init();
		ResourceLoaderEvents.END_RESOURCE_RELOAD.register(Font::reload);
		MinecraftClientEvents.STOP.register(mc -> Font.closeAll());
	}
}
