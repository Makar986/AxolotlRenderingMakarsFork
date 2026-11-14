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

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.vertex.VertexFormat;
import io.github.axolotlclient.rendering.mixin.BufferBuilderAccessor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import org.lwjgl.system.MemoryUtil;

public class ExRenderPipelines {

	public static final String ROUNDING_SEMANTIC_NAME = "Rounding";
	public static final String DIMENSIONS_SEMANTIC_NAME = "Dimensions";
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
