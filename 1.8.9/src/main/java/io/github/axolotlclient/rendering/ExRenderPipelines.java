/*
 * Copyright © 2025 moehreag <moehreag@gmail.com> & Contributors
 *
 * This file is part of AxolotlClient (Rendering Mod).
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 * For more information, see the LICENSE file.
 */

package io.github.axolotlclient.rendering;

import java.io.IOException;
import java.io.UncheckedIOException;

import net.minecraft.resource.Identifier;
import net.ornithemc.osl.lifecycle.api.client.MinecraftClientEvents;
import net.ornithemc.osl.resource.loader.api.client.ClientResourceLoaderEvents;

public class ExRenderPipelines {

	public static ShaderManager.Shader ROUNDED_RECT_PROGRAM,
		ROUNDED_RECT_TEX_PROGRAM,
		ROUNDED_RECT_OUTLINE_PROGRAM,
		ROUNDED_RECT_TEX_OUTLINE_PROGRAM,
		FONT_PROGRAM,
		SEGMENT_PROGRAM,
		GAUSSIAN_BLUR_PROGRAM,
		LIQUID_GLASS_PROGRAM;

	public static void init() {
		ClientResourceLoaderEvents.END_RESOURCE_RELOAD.register((manager, ctx) -> {
			ShaderManager.safeClose(ROUNDED_RECT_PROGRAM, ROUNDED_RECT_TEX_PROGRAM,
				ROUNDED_RECT_OUTLINE_PROGRAM, ROUNDED_RECT_TEX_OUTLINE_PROGRAM,
				FONT_PROGRAM, SEGMENT_PROGRAM, GAUSSIAN_BLUR_PROGRAM, LIQUID_GLASS_PROGRAM);
			load();
		});
		MinecraftClientEvents.STOP.register(mc ->
			ShaderManager.safeClose(ROUNDED_RECT_PROGRAM, ROUNDED_RECT_TEX_PROGRAM,
				ROUNDED_RECT_OUTLINE_PROGRAM, ROUNDED_RECT_TEX_OUTLINE_PROGRAM,
				FONT_PROGRAM, SEGMENT_PROGRAM, GAUSSIAN_BLUR_PROGRAM, LIQUID_GLASS_PROGRAM));
	}

	private static void load() {
		try {
			ROUNDED_RECT_PROGRAM = ShaderManager.createProgram(new Identifier(RenderingCommon.MODID, "core/rounded_rect"));
			ROUNDED_RECT_TEX_PROGRAM = ShaderManager.createProgram(new Identifier(RenderingCommon.MODID, "core/rounded_rect_tex"));
			ROUNDED_RECT_OUTLINE_PROGRAM = ShaderManager.createProgram(new Identifier(RenderingCommon.MODID, "core/rounded_rect_outline"));
			ROUNDED_RECT_TEX_OUTLINE_PROGRAM = ShaderManager.createProgram(new Identifier(RenderingCommon.MODID, "core/rounded_rect_tex_outline"));
			FONT_PROGRAM = ShaderManager.createProgram(new Identifier(RenderingCommon.MODID, "core/font"));
			SEGMENT_PROGRAM = ShaderManager.createProgram(new Identifier(RenderingCommon.MODID, "core/rounded_rect"), new Identifier(RenderingCommon.MODID, "core/segment"));
			GAUSSIAN_BLUR_PROGRAM = ShaderManager.createProgram(new Identifier(RenderingCommon.MODID, "core/gaussian_blur"));
			LIQUID_GLASS_PROGRAM = ShaderManager.createProgram(new Identifier(RenderingCommon.MODID, "core/liquid_glass"));
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
}
