package io.github.axolotlclient.rendering;


import lombok.extern.slf4j.Slf4j;
import net.fabricmc.api.ClientModInitializer;

@Slf4j
public class Rendering implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		ExRenderPipelines.init();
	}
}
