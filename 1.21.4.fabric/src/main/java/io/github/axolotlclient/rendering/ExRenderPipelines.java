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

import com.mojang.blaze3d.vertex.*;
import io.github.axolotlclient.rendering.mixin.BufferBuilderAccessor;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderDefines;
import net.minecraft.client.renderer.ShaderProgram;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;
import org.lwjgl.system.MemoryUtil;

public class ExRenderPipelines {
	private static int nextUnusedVFEIndex() {
		for (int i = 0; i < VertexFormatElement.MAX_COUNT; i++) {
			if (VertexFormatElement.byId(i) == null) {
				return i;
			}
		}
		throw new IllegalStateException("Not enough VertexFormatElement IDs available. " +
			"This should not be possible unless other mods are broken.");
	}

	public static final int NUM_CUSTOM_VERTEX_FORMAT_ELEMENTS = 3;
	public static final VertexFormatElement ROUNDING = VertexFormatElement.register(nextUnusedVFEIndex(), 0, VertexFormatElement.Type.FLOAT, VertexFormatElement.Usage.GENERIC, 4);
	public static final VertexFormatElement DIMENSIONS = VertexFormatElement.register(nextUnusedVFEIndex(), 0, VertexFormatElement.Type.FLOAT, VertexFormatElement.Usage.GENERIC, 4);
	private static final VertexFormatElement OUTLINE_WIDTH = VertexFormatElement.register(nextUnusedVFEIndex(), 0, VertexFormatElement.Type.FLOAT, VertexFormatElement.Usage.GENERIC, 1);

	// 1.21.2+: shader programs are declared up front and compiled lazily by Minecraft's ShaderManager
	// from assets/minecraft/shaders/core/<name>.json, so no GameRenderer#reloadShaders hook is needed.
	private static ShaderProgram program(String name, VertexFormat format) {
		return new ShaderProgram(ResourceLocation.withDefaultNamespace("core/axolotlclient-rendering_" + name), format, ShaderDefines.EMPTY);
	}

	public static final VertexFormat ROUNDED_RECT_FORMAT = VertexFormat.builder()
		.add("Position", VertexFormatElement.POSITION)
		.add("Color", VertexFormatElement.COLOR)
		.add("Rounding", ROUNDING)
		.add("Dimensions", DIMENSIONS)
		.build();

	public static final VertexFormat ROUNDED_RECT_OUTLINE_FORMAT = VertexFormat.builder()
		.add("Position", VertexFormatElement.POSITION)
		.add("Color", VertexFormatElement.COLOR)
		.add("Rounding", ROUNDING)
		.add("Dimensions", DIMENSIONS)
		.add("OutlineWidth", OUTLINE_WIDTH)
		.build();

	@ApiStatus.Internal
	public static final ShaderProgram ROUNDED_RECT_SHADER = program("rounded_rect", ROUNDED_RECT_FORMAT),
		SEGMENT_SHADER = program("segment", ROUNDED_RECT_FORMAT),
		ROUNDED_RECT_OUTLINE_SHADER = program("rounded_rect_outline", ROUNDED_RECT_OUTLINE_FORMAT),
		FONT_SHADER = program("font", DefaultVertexFormat.POSITION_TEX_COLOR),
		MSDF_FONT_SHADER = program("msdf_font", DefaultVertexFormat.POSITION_TEX_COLOR);

	public static RenderType ROUNDED_RECT = RenderType.create(RenderingCommon.MODID + ":rounded_rect",
		ROUNDED_RECT_FORMAT,
		VertexFormat.Mode.QUADS,
		RenderType.SMALL_BUFFER_SIZE,
		RenderType.CompositeState.builder()
			.setShaderState(new RenderStateShard.ShaderStateShard(ExRenderPipelines.ROUNDED_RECT_SHADER))
			.setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
			.setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
			.createCompositeState(false));

	public static RenderType SEGMENT = RenderType.create(RenderingCommon.MODID + ":segment",
		ROUNDED_RECT_FORMAT,
		VertexFormat.Mode.QUADS,
		RenderType.SMALL_BUFFER_SIZE,
		RenderType.CompositeState.builder()
			.setShaderState(new RenderStateShard.ShaderStateShard(ExRenderPipelines.SEGMENT_SHADER))
			.setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
			.setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
			.createCompositeState(false));

	public static RenderType ROUNDED_RECT_OUTLINE = RenderType.create(RenderingCommon.MODID + ":rounded_rect_outline",
		ROUNDED_RECT_OUTLINE_FORMAT,
		VertexFormat.Mode.QUADS,
		RenderType.SMALL_BUFFER_SIZE,
		RenderType.CompositeState.builder()
			.setShaderState(new RenderStateShard.ShaderStateShard(ExRenderPipelines.ROUNDED_RECT_OUTLINE_SHADER))
			.setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
			.setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
			.createCompositeState(false));

	public static RenderType FONT = RenderType.create(RenderingCommon.MODID + ":font",
		DefaultVertexFormat.POSITION_TEX_COLOR,
		VertexFormat.Mode.QUADS,
		RenderType.SMALL_BUFFER_SIZE,
		RenderType.CompositeState.builder()
			.setShaderState(new RenderStateShard.ShaderStateShard(ExRenderPipelines.FONT_SHADER))
			.setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
			.setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
			.createCompositeState(false));

	public static final VertexFormat ROUNDED_RECT_TEX_FORMAT = VertexFormat.builder()
		.add("Position", VertexFormatElement.POSITION)
		.add("UV0", VertexFormatElement.UV0)
		.add("Color", VertexFormatElement.COLOR)
		.add("Rounding", ROUNDING)
		.add("Dimensions", DIMENSIONS)
		.build();

	public static final VertexFormat ROUNDED_RECT_TEX_OUTLINE_FORMAT = VertexFormat.builder()
		.add("Position", VertexFormatElement.POSITION)
		.add("UV0", VertexFormatElement.UV0)
		.add("Color", VertexFormatElement.COLOR)
		.add("Rounding", ROUNDING)
		.add("Dimensions", DIMENSIONS)
		.add("OutlineWidth", OUTLINE_WIDTH)
		.build();

	@ApiStatus.Internal
	public static final ShaderProgram ROUNDED_RECT_TEX_SHADER = program("rounded_rect_tex", ROUNDED_RECT_TEX_FORMAT),
		ROUNDED_RECT_TEX_OUTLINE_SHADER = program("rounded_rect_tex_outline", ROUNDED_RECT_TEX_OUTLINE_FORMAT),
		GAUSSIAN_BLUR_SHADER = program("gaussian_blur", ROUNDED_RECT_TEX_FORMAT),
		LIQUID_GLASS_SHADER = program("liquid_glass", ROUNDED_RECT_TEX_FORMAT);

	public static void addRounding(VertexConsumer consumer, float tl, float bl, float br, float tr) {
		var offset = beginElement(consumer, ROUNDING);
		if (offset != -1L) {
			MemoryUtil.memPutFloat(offset, br);
			MemoryUtil.memPutFloat(offset + 4L, tr);
			MemoryUtil.memPutFloat(offset + 8L, bl);
			MemoryUtil.memPutFloat(offset + 12L, tl);
		}
	}

	public static void addDimensions(VertexConsumer consumer, float relX, float relY, float width, float height) {
		var offset = beginElement(consumer, DIMENSIONS);
		if (offset != -1L) {
			MemoryUtil.memPutFloat(offset, relX);
			MemoryUtil.memPutFloat(offset + 4L, relY);
			MemoryUtil.memPutFloat(offset + 8L, width);
			MemoryUtil.memPutFloat(offset + 12L, height);
		}
	}

	public static void addOutlineWidth(VertexConsumer consumer, float outlineWidth) {
		var offset = beginElement(consumer, OUTLINE_WIDTH);
		if (offset != -1) {
			MemoryUtil.memPutFloat(offset, outlineWidth);
		}
	}

	private static long beginElement(VertexConsumer consumer, VertexFormatElement element) {
		if (!(consumer instanceof BufferBuilder builder)) {
			throw new IllegalArgumentException("Not a BufferBuilder!");
		}
		var accessor = ((BufferBuilderAccessor) builder);
		return accessor.begin(element);
	}
}
