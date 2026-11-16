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

import com.mojang.blaze3d.GpuFormat;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import io.github.axolotlclient.rendering.mixin.BufferBuilderAccessor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import org.lwjgl.system.MemoryUtil;

public class ExRenderPipelines {

	public static final String ROUNDING_SEMANTIC_NAME = "Rounding";
	public static final String DIMENSIONS_SEMANTIC_NAME = "Dimensions";
	// Per-element effect parameters for blur / liquid glass (the GUI is drawn in batches, so no uniforms).
	public static final String EFFECT0_SEMANTIC_NAME = "Effect0";
	public static final String EFFECT1_SEMANTIC_NAME = "Effect1";
	public static final String EFFECT2_SEMANTIC_NAME = "Effect2";
	public static final String EFFECT3_SEMANTIC_NAME = "Effect3";
	public static final RenderPipeline ROUNDED_RECT = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.GUI_SNIPPET)
		.withLocation(Identifier.fromNamespaceAndPath(RenderingCommon.MODID, "rounded_rect"))
		.withVertexShader(Identifier.fromNamespaceAndPath(RenderingCommon.MODID, "core/rounded_rect"))
		.withFragmentShader(Identifier.fromNamespaceAndPath(RenderingCommon.MODID, "core/rounded_rect"))
		.withVertexBinding(0, VertexFormat.builder(0)
			.addAttribute(DefaultVertexFormat.POSITION_SEMANTIC_NAME, GpuFormat.RGB32_FLOAT)
			.addAttribute(DefaultVertexFormat.COLOR_SEMANTIC_NAME, GpuFormat.RGBA8_UNORM)
			.addAttribute(ROUNDING_SEMANTIC_NAME, GpuFormat.RGBA32_FLOAT)
			.addAttribute(DIMENSIONS_SEMANTIC_NAME, GpuFormat.RGBA32_FLOAT)
			.build()).build());

	public static final RenderPipeline SEGMENT = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.GUI_SNIPPET)
		.withLocation(Identifier.fromNamespaceAndPath(RenderingCommon.MODID, "segment"))
		.withVertexShader(Identifier.fromNamespaceAndPath(RenderingCommon.MODID, "core/rounded_rect"))
		.withFragmentShader(Identifier.fromNamespaceAndPath(RenderingCommon.MODID, "core/segment"))
		.withVertexBinding(0, VertexFormat.builder(0)
			.addAttribute(DefaultVertexFormat.POSITION_SEMANTIC_NAME, GpuFormat.RGB32_FLOAT)
			.addAttribute(DefaultVertexFormat.COLOR_SEMANTIC_NAME, GpuFormat.RGBA8_UNORM)
			.addAttribute(ROUNDING_SEMANTIC_NAME, GpuFormat.RGBA32_FLOAT)
			.addAttribute(DIMENSIONS_SEMANTIC_NAME, GpuFormat.RGBA32_FLOAT)
			.build()).build());

	public static final RenderPipeline ROUNDED_RECT_TEX = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.GUI_TEXTURED_SNIPPET)
		.withLocation(Identifier.fromNamespaceAndPath(RenderingCommon.MODID, "rounded_rect_tex"))
		.withVertexShader(Identifier.fromNamespaceAndPath(RenderingCommon.MODID, "core/rounded_rect_tex"))
		.withFragmentShader(Identifier.fromNamespaceAndPath(RenderingCommon.MODID, "core/rounded_rect_tex"))
		.withVertexBinding(0, VertexFormat.builder(0)
			.addAttribute(DefaultVertexFormat.POSITION_SEMANTIC_NAME, GpuFormat.RGB32_FLOAT)
			.addAttribute(DefaultVertexFormat.COLOR_SEMANTIC_NAME, GpuFormat.RGBA8_UNORM)
			.addAttribute(DefaultVertexFormat.UV0_SEMANTIC_NAME, GpuFormat.RG32_FLOAT)
			.addAttribute(ROUNDING_SEMANTIC_NAME, GpuFormat.RGBA32_FLOAT)
			.addAttribute(DIMENSIONS_SEMANTIC_NAME, GpuFormat.RGBA32_FLOAT)
			.build()).build());

	public static final String OUTLINE_WIDTH_SEMANTIC_NAME = "OutlineWidth";
	public static final RenderPipeline ROUNDED_RECT_OUTLINE = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.GUI_SNIPPET)
		.withLocation(Identifier.fromNamespaceAndPath(RenderingCommon.MODID, "rounded_rect_outline"))
		.withVertexShader(Identifier.fromNamespaceAndPath(RenderingCommon.MODID, "core/rounded_rect_outline"))
		.withFragmentShader(Identifier.fromNamespaceAndPath(RenderingCommon.MODID, "core/rounded_rect_outline"))
		.withVertexBinding(0, VertexFormat.builder(0)
			.addAttribute(DefaultVertexFormat.POSITION_SEMANTIC_NAME, GpuFormat.RGB32_FLOAT)
			.addAttribute(DefaultVertexFormat.COLOR_SEMANTIC_NAME, GpuFormat.RGBA8_UNORM)
			.addAttribute(ROUNDING_SEMANTIC_NAME, GpuFormat.RGBA32_FLOAT)
			.addAttribute(DIMENSIONS_SEMANTIC_NAME, GpuFormat.RGBA32_FLOAT)
			.addAttribute(OUTLINE_WIDTH_SEMANTIC_NAME, GpuFormat.R32_FLOAT)
			.build()).build());

	public static final RenderPipeline ROUNDED_RECT_TEX_OUTLINE = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.GUI_TEXTURED_SNIPPET)
		.withLocation(Identifier.fromNamespaceAndPath(RenderingCommon.MODID, "rounded_rect_tex_outline"))
		.withVertexShader(Identifier.fromNamespaceAndPath(RenderingCommon.MODID, "core/rounded_rect_tex_outline"))
		.withFragmentShader(Identifier.fromNamespaceAndPath(RenderingCommon.MODID, "core/rounded_rect_tex_outline"))
		.withVertexBinding(0, VertexFormat.builder(0)
			.addAttribute(DefaultVertexFormat.POSITION_SEMANTIC_NAME, GpuFormat.RGB32_FLOAT)
			.addAttribute(DefaultVertexFormat.COLOR_SEMANTIC_NAME, GpuFormat.RGBA8_UNORM)
			.addAttribute(DefaultVertexFormat.UV0_SEMANTIC_NAME, GpuFormat.RG32_FLOAT)
			.addAttribute(ROUNDING_SEMANTIC_NAME, GpuFormat.RGBA32_FLOAT)
			.addAttribute(DIMENSIONS_SEMANTIC_NAME, GpuFormat.RGBA32_FLOAT)
			.addAttribute(OUTLINE_WIDTH_SEMANTIC_NAME, GpuFormat.R32_FLOAT)
			.build()).build());

	public static final RenderPipeline FONT = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.GUI_TEXTURED_SNIPPET)
		.withLocation(Identifier.fromNamespaceAndPath(RenderingCommon.MODID, "font"))
		.withVertexShader(Identifier.fromNamespaceAndPath(RenderingCommon.MODID, "core/font"))
		.withFragmentShader(Identifier.fromNamespaceAndPath(RenderingCommon.MODID, "core/font"))
		.build());

	/**
	 * Samples {@link BackdropTexture} (a copy of the frame taken right before the GUI is drawn).
	 * Effect0 = (blurRadius, sigma, -, -).
	 */
	public static final RenderPipeline GAUSSIAN_BLUR = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.GUI_TEXTURED_SNIPPET)
		.withLocation(Identifier.fromNamespaceAndPath(RenderingCommon.MODID, "gaussian_blur"))
		.withVertexShader(Identifier.fromNamespaceAndPath(RenderingCommon.MODID, "core/gaussian_blur"))
		.withFragmentShader(Identifier.fromNamespaceAndPath(RenderingCommon.MODID, "core/gaussian_blur"))
		.withVertexBinding(0, VertexFormat.builder(0)
			.addAttribute(DefaultVertexFormat.POSITION_SEMANTIC_NAME, GpuFormat.RGB32_FLOAT)
			.addAttribute(DefaultVertexFormat.COLOR_SEMANTIC_NAME, GpuFormat.RGBA8_UNORM)
			.addAttribute(DefaultVertexFormat.UV0_SEMANTIC_NAME, GpuFormat.RG32_FLOAT)
			.addAttribute(ROUNDING_SEMANTIC_NAME, GpuFormat.RGBA32_FLOAT)
			.addAttribute(DIMENSIONS_SEMANTIC_NAME, GpuFormat.RGBA32_FLOAT)
			.addAttribute(EFFECT0_SEMANTIC_NAME, GpuFormat.RGBA32_FLOAT)
			.build())
		.build());

	/**
	 * Samples {@link BackdropTexture}.
	 * Effect0 = optics (refThickness, refFactor, refDispersion, refFresnelRange),
	 * Effect1 = optics (refFresnelHardness, refFresnelFactor, glareRange, glareHardness),
	 * Effect2 = (blurRadius, borderWidth, -, -), Effect3 = border color (rgba).
	 */
	public static final RenderPipeline LIQUID_GLASS = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.GUI_TEXTURED_SNIPPET)
		.withLocation(Identifier.fromNamespaceAndPath(RenderingCommon.MODID, "liquid_glass"))
		.withVertexShader(Identifier.fromNamespaceAndPath(RenderingCommon.MODID, "core/liquid_glass"))
		.withFragmentShader(Identifier.fromNamespaceAndPath(RenderingCommon.MODID, "core/liquid_glass"))
		.withVertexBinding(0, VertexFormat.builder(0)
			.addAttribute(DefaultVertexFormat.POSITION_SEMANTIC_NAME, GpuFormat.RGB32_FLOAT)
			.addAttribute(DefaultVertexFormat.COLOR_SEMANTIC_NAME, GpuFormat.RGBA8_UNORM)
			.addAttribute(DefaultVertexFormat.UV0_SEMANTIC_NAME, GpuFormat.RG32_FLOAT)
			.addAttribute(ROUNDING_SEMANTIC_NAME, GpuFormat.RGBA32_FLOAT)
			.addAttribute(DIMENSIONS_SEMANTIC_NAME, GpuFormat.RGBA32_FLOAT)
			.addAttribute(EFFECT0_SEMANTIC_NAME, GpuFormat.RGBA32_FLOAT)
			.addAttribute(EFFECT1_SEMANTIC_NAME, GpuFormat.RGBA32_FLOAT)
			.addAttribute(EFFECT2_SEMANTIC_NAME, GpuFormat.RGBA32_FLOAT)
			.addAttribute(EFFECT3_SEMANTIC_NAME, GpuFormat.RGBA32_FLOAT)
			.build())
		.build());

	public static final RenderPipeline MSDF_FONT = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.GUI_TEXTURED_SNIPPET)
		.withLocation(Identifier.fromNamespaceAndPath(RenderingCommon.MODID, "msdf_font"))
		.withVertexShader(Identifier.fromNamespaceAndPath(RenderingCommon.MODID, "core/msdf_font"))
		.withFragmentShader(Identifier.fromNamespaceAndPath(RenderingCommon.MODID, "core/msdf_font"))
		.build());

	public static void addEffect(VertexConsumer consumer, int index, float x, float y, float z, float w) {
		String name = switch (index) {
			case 0 -> EFFECT0_SEMANTIC_NAME;
			case 1 -> EFFECT1_SEMANTIC_NAME;
			case 2 -> EFFECT2_SEMANTIC_NAME;
			case 3 -> EFFECT3_SEMANTIC_NAME;
			default -> throw new IllegalArgumentException("effect index " + index);
		};
		var offset = beginElement(consumer, name);
		if (offset != -1L) {
			MemoryUtil.memPutFloat(offset, x);
			MemoryUtil.memPutFloat(offset + 4L, y);
			MemoryUtil.memPutFloat(offset + 8L, z);
			MemoryUtil.memPutFloat(offset + 12L, w);
		}
	}

	public static void addRounding(VertexConsumer consumer, float tl, float bl, float br, float tr) {
		var offset = beginElement(consumer, ROUNDING_SEMANTIC_NAME);
		if (offset != -1L) {
			MemoryUtil.memPutFloat(offset, br);
			MemoryUtil.memPutFloat(offset + 4L, tr);
			MemoryUtil.memPutFloat(offset + 8L, bl);
			MemoryUtil.memPutFloat(offset + 12L, tl);
		}
	}

	public static void addDimensions(VertexConsumer consumer, float relX, float relY, float width, float height) {
		var offset = beginElement(consumer, DIMENSIONS_SEMANTIC_NAME);
		if (offset != -1L) {
			MemoryUtil.memPutFloat(offset, relX);
			MemoryUtil.memPutFloat(offset + 4L, relY);
			MemoryUtil.memPutFloat(offset + 8L, width);
			MemoryUtil.memPutFloat(offset + 12L, height);
		}
	}

	public static void addOutlineWidth(VertexConsumer consumer, float outlineWidth) {
		var offset = beginElement(consumer, OUTLINE_WIDTH_SEMANTIC_NAME);
		if (offset != -1) {
			MemoryUtil.memPutFloat(offset, outlineWidth);
		}
	}

	private static long beginElement(VertexConsumer consumer, String elementName) {
		if (!(consumer instanceof BufferBuilder builder)) {
			throw new IllegalArgumentException("Not a BufferBuilder!");
		}
		var element = ((BufferBuilderAccessor) builder).getFormat().getElement(elementName);
		if (element == null) {
			return -1;
		}
		var vertexPointer = ((BufferBuilderAccessor) builder).getVertexPointer();
		return vertexPointer > -1 ? vertexPointer+element.offset() : vertexPointer;
	}
}
