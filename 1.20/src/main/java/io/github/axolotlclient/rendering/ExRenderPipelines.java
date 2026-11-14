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

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import org.jetbrains.annotations.ApiStatus;

public class ExRenderPipelines {
	public static final VertexFormatElement ROUNDING = new VertexFormatElement(0, VertexFormatElement.Type.FLOAT, VertexFormatElement.Usage.GENERIC, 4);
	public static final VertexFormatElement DIMENSIONS = new VertexFormatElement(0, VertexFormatElement.Type.FLOAT, VertexFormatElement.Usage.GENERIC, 4);
	private static final VertexFormatElement OUTLINE_WIDTH = new VertexFormatElement(0, VertexFormatElement.Type.FLOAT, VertexFormatElement.Usage.GENERIC, 1);

	@ApiStatus.Internal
	public static ShaderInstance SEGMENT_SHADER, ROUNDED_RECT_SHADER, ROUNDED_RECT_TEX_SHADER, ROUNDED_RECT_OUTLINE_SHADER, ROUNDED_RECT_TEX_OUTLINE_SHADER, FONT_SHADER, GAUSSIAN_BLUR_SHADER, LIQUID_GLASS_SHADER, MSDF_FONT_SHADER;

	public static final VertexFormat ROUNDED_RECT_FORMAT = new VertexFormat(ImmutableMap.<String, VertexFormatElement>builder()
		.put("Position", DefaultVertexFormat.ELEMENT_POSITION)
		.put("Color", DefaultVertexFormat.ELEMENT_COLOR)
		.put("Rounding", ROUNDING)
		.put("Dimensions", DIMENSIONS)
		.build());

	public static final VertexFormat ROUNDED_RECT_OUTLINE_FORMAT = new VertexFormat(ImmutableMap.<String, VertexFormatElement>builder()
		.put("Position", DefaultVertexFormat.ELEMENT_POSITION)
		.put("Color", DefaultVertexFormat.ELEMENT_COLOR)
		.put("Rounding", ROUNDING)
		.put("Dimensions", DIMENSIONS)
		.put("OutlineWidth", OUTLINE_WIDTH)
		.build());

	public static RenderType ROUNDED_RECT = RenderType.create(RenderingCommon.MODID + ":rounded_rect",
		ROUNDED_RECT_FORMAT,
		VertexFormat.Mode.QUADS,
		RenderType.SMALL_BUFFER_SIZE,
		RenderType.CompositeState.builder()
			.setShaderState(new RenderStateShard.ShaderStateShard(() -> ExRenderPipelines.ROUNDED_RECT_SHADER))
			.setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
			.setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
			.createCompositeState(false));

	public static RenderType SEGMENT = RenderType.create(RenderingCommon.MODID + ":segment",
		ROUNDED_RECT_FORMAT,
		VertexFormat.Mode.QUADS,
		RenderType.SMALL_BUFFER_SIZE,
		RenderType.CompositeState.builder()
			.setShaderState(new RenderStateShard.ShaderStateShard(() -> ExRenderPipelines.SEGMENT_SHADER))
			.setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
			.setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
			.createCompositeState(false));

	public static RenderType ROUNDED_RECT_OUTLINE = RenderType.create(RenderingCommon.MODID + ":rounded_rect_outline",
		ROUNDED_RECT_OUTLINE_FORMAT,
		VertexFormat.Mode.QUADS,
		RenderType.SMALL_BUFFER_SIZE,
		RenderType.CompositeState.builder()
			.setShaderState(new RenderStateShard.ShaderStateShard(() -> ExRenderPipelines.ROUNDED_RECT_OUTLINE_SHADER))
			.setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
			.setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
			.createCompositeState(false));

	public static RenderType FONT = RenderType.create(RenderingCommon.MODID + ":font",
		DefaultVertexFormat.POSITION_COLOR_TEX,
		VertexFormat.Mode.QUADS,
		RenderType.SMALL_BUFFER_SIZE,
		RenderType.CompositeState.builder()
			.setShaderState(new RenderStateShard.ShaderStateShard(() -> ExRenderPipelines.FONT_SHADER))
			.setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
			.setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
			.createCompositeState(false));

	public static final VertexFormat ROUNDED_RECT_TEX_FORMAT = new VertexFormat(ImmutableMap.<String, VertexFormatElement>builder()
		.put("Position", DefaultVertexFormat.ELEMENT_POSITION)
		.put("UV0", DefaultVertexFormat.ELEMENT_UV0)
		.put("Color", DefaultVertexFormat.ELEMENT_COLOR)
		.put("Rounding", ROUNDING)
		.put("Dimensions", DIMENSIONS)
		.build());

	public static final VertexFormat ROUNDED_RECT_TEX_OUTLINE_FORMAT = new VertexFormat(ImmutableMap.<String, VertexFormatElement>builder()
		.put("Position", DefaultVertexFormat.ELEMENT_POSITION)
		.put("UV0", DefaultVertexFormat.ELEMENT_UV0)
		.put("Color", DefaultVertexFormat.ELEMENT_COLOR)
		.put("Rounding", ROUNDING)
		.put("Dimensions", DIMENSIONS)
		.put("OutlineWidth", OUTLINE_WIDTH)
		.build());

	public static void addRounding(VertexConsumer consumer, float tl, float bl, float br, float tr) {
		if (!(consumer instanceof BufferBuilder builder)) {
			throw new IllegalArgumentException("Not a BufferBuilder!");
		}
		builder.putFloat(0, br);
		builder.putFloat(4, tr);
		builder.putFloat(8, bl);
		builder.putFloat(12, tl);
		builder.nextElement();
	}

	public static void addDimensions(VertexConsumer consumer, float relX, float relY, float width, float height) {
		if (!(consumer instanceof BufferBuilder builder)) {
			throw new IllegalArgumentException("Not a BufferBuilder!");
		}
		builder.putFloat(0, relX);
		builder.putFloat(4, relY);
		builder.putFloat(8, width);
		builder.putFloat(12, height);
		builder.nextElement();
	}

	public static void addOutlineWidth(VertexConsumer consumer, float outlineWidth) {
		if (!(consumer instanceof BufferBuilder builder)) {
			throw new IllegalArgumentException("Not a BufferBuilder!");
		}
		builder.putFloat(0, outlineWidth);
		builder.nextElement();
	}
}
