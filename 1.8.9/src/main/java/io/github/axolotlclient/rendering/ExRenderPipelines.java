package io.github.axolotlclient.rendering;

import java.io.IOException;
import java.io.UncheckedIOException;

import net.minecraft.resource.Identifier;
import net.ornithemc.osl.lifecycle.api.client.MinecraftClientEvents;
import net.ornithemc.osl.resource.loader.api.ResourceLoaderEvents;

public class ExRenderPipelines {

	public static ShaderManager.Shader ROUNDED_RECT_PROGRAM,
		ROUNDED_RECT_TEX_PROGRAM,
		ROUNDED_RECT_OUTLINE_PROGRAM,
		ROUNDED_RECT_TEX_OUTLINE_PROGRAM,
		FONT_PROGRAM;

	public static void init() {
		ResourceLoaderEvents.END_RESOURCE_RELOAD.register(() -> {
			ShaderManager.safeClose(ROUNDED_RECT_PROGRAM, ROUNDED_RECT_TEX_PROGRAM,
				ROUNDED_RECT_OUTLINE_PROGRAM, ROUNDED_RECT_TEX_OUTLINE_PROGRAM,
				FONT_PROGRAM);
			load();
		});
		MinecraftClientEvents.STOP.register(mc ->
			ShaderManager.safeClose(ROUNDED_RECT_PROGRAM, ROUNDED_RECT_TEX_PROGRAM,
				ROUNDED_RECT_OUTLINE_PROGRAM, ROUNDED_RECT_TEX_OUTLINE_PROGRAM,
				FONT_PROGRAM));
	}

	private static void load() {
		try {
			ROUNDED_RECT_PROGRAM = ShaderManager.createProgram(new Identifier("axolotlclient_rendering", "core/rounded_rect"));
			ROUNDED_RECT_TEX_PROGRAM = ShaderManager.createProgram(new Identifier("axolotlclient_rendering", "core/rounded_rect_tex"));
			ROUNDED_RECT_OUTLINE_PROGRAM = ShaderManager.createProgram(new Identifier("axolotlclient_rendering", "core/rounded_rect_outline"));
			ROUNDED_RECT_TEX_OUTLINE_PROGRAM = ShaderManager.createProgram(new Identifier("axolotlclient_rendering", "core/rounded_rect_tex_outline"));
			FONT_PROGRAM = ShaderManager.createProgram(new Identifier("axolotlclient_rendering", "core/font"));
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
}
