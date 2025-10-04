package io.github.axolotlclient.rendering;

import java.io.IOException;
import java.io.UncheckedIOException;

import net.minecraft.resource.Identifier;
import net.ornithemc.osl.resource.loader.api.ResourceLoaderEvents;

public class ExRenderPipelines {

	public static ShaderManager.Shader ROUNDED_RECT_PROGRAM, ROUNDED_RECT_TEX_PROGRAM;

	public static void init() {
		ResourceLoaderEvents.END_RESOURCE_RELOAD.register(() -> {
			ShaderManager.safeClose(ROUNDED_RECT_PROGRAM);
			ShaderManager.safeClose(ROUNDED_RECT_TEX_PROGRAM);
			load();
		});
	}

	private static void load() {
		try {
			ROUNDED_RECT_PROGRAM = ShaderManager.createProgram(new Identifier("program/rounded_rect"));
			ROUNDED_RECT_TEX_PROGRAM = ShaderManager.createProgram(new Identifier("program/rounded_rect_tex"));
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
}
