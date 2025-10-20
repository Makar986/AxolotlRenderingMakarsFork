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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import io.github.axolotlclient.rendering.mixin.BufferBuilderAccessor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.system.MemoryUtil;

public class ExRenderPipelines {
	private static final MethodHandle HANDLE;

	static {
		try {
			// We can get away with not remapping the name manually here because blaze3d isn't obfuscated anymore
			HANDLE = MethodHandles.publicLookup().findStaticGetter(VertexFormatElement.class, "MAX_COUNT", int.class);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	private static int nextUnusedVFEIndex() {
		try {
			for (int i = 0; i < (Integer) HANDLE.invoke(); i++) {
				if (VertexFormatElement.byId(i) == null) {
					return i;
				}
			}
		} catch (Throwable ignored) {
		}
		throw new IllegalStateException("Not enough VertexFormatElement IDs available. " +
			"This should not be possible unless other mods are broken.");
	}

	public static final int NUM_CUSTOM_VERTEX_FORMAT_ELEMENTS = 4;
	private static final VertexFormatElement ROUNDING = VertexFormatElement.register(nextUnusedVFEIndex(), 0, VertexFormatElement.Type.FLOAT, VertexFormatElement.Usage.GENERIC, 4);
	private static final VertexFormatElement DIMENSIONS = VertexFormatElement.register(nextUnusedVFEIndex(), 0, VertexFormatElement.Type.FLOAT, VertexFormatElement.Usage.GENERIC, 4);
	private static final VertexFormatElement OUTLINE_WIDTH = VertexFormatElement.register(nextUnusedVFEIndex(), 0, VertexFormatElement.Type.FLOAT, VertexFormatElement.Usage.GENERIC, 1);

	public static final RenderPipeline ROUNDED_RECT = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.GUI_SNIPPET)
		.withLocation(ResourceLocation.fromNamespaceAndPath(RenderingCommon.MODID, "rounded_rect"))
		.withVertexShader(ResourceLocation.fromNamespaceAndPath(RenderingCommon.MODID, "core/rounded_rect"))
		.withFragmentShader(ResourceLocation.fromNamespaceAndPath(RenderingCommon.MODID, "core/rounded_rect"))
		.withVertexFormat(VertexFormat.builder()
			.add("Position", VertexFormatElement.POSITION)
			.add("Color", VertexFormatElement.COLOR)
			.add("Rounding", ROUNDING)
			.add("Dimensions", DIMENSIONS)
			.build(), VertexFormat.Mode.QUADS).build());

	public static final RenderPipeline ROUNDED_RECT_TEX = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.GUI_TEXTURED_SNIPPET)
		.withLocation(ResourceLocation.fromNamespaceAndPath(RenderingCommon.MODID, "rounded_rect_tex"))
		.withVertexShader(ResourceLocation.fromNamespaceAndPath(RenderingCommon.MODID, "core/rounded_rect_tex"))
		.withFragmentShader(ResourceLocation.fromNamespaceAndPath(RenderingCommon.MODID, "core/rounded_rect_tex"))
		.withVertexFormat(VertexFormat.builder()
			.add("Position", VertexFormatElement.POSITION)
			.add("Color", VertexFormatElement.COLOR)
			.add("UV0", VertexFormatElement.UV0)
			.add("Rounding", ROUNDING)
			.add("Dimensions", DIMENSIONS)
			.build(), VertexFormat.Mode.QUADS).build());

	public static final RenderPipeline ROUNDED_RECT_OUTLINE = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.GUI_SNIPPET)
		.withLocation(ResourceLocation.fromNamespaceAndPath(RenderingCommon.MODID, "rounded_rect_outline"))
		.withVertexShader(ResourceLocation.fromNamespaceAndPath(RenderingCommon.MODID, "core/rounded_rect_outline"))
		.withFragmentShader(ResourceLocation.fromNamespaceAndPath(RenderingCommon.MODID, "core/rounded_rect_outline"))
		.withVertexFormat(VertexFormat.builder()
			.add("Position", VertexFormatElement.POSITION)
			.add("Color", VertexFormatElement.COLOR)
			.add("Rounding", ROUNDING)
			.add("Dimensions", DIMENSIONS)
			.add("OutlineWidth", OUTLINE_WIDTH)
			.build(), VertexFormat.Mode.QUADS).build());

	public static final RenderPipeline ROUNDED_RECT_TEX_OUTLINE = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.GUI_TEXTURED_SNIPPET)
		.withLocation(ResourceLocation.fromNamespaceAndPath(RenderingCommon.MODID, "rounded_rect_tex_outline"))
		.withVertexShader(ResourceLocation.fromNamespaceAndPath(RenderingCommon.MODID, "core/rounded_rect_tex_outline"))
		.withFragmentShader(ResourceLocation.fromNamespaceAndPath(RenderingCommon.MODID, "core/rounded_rect_tex_outline"))
		.withVertexFormat(VertexFormat.builder()
			.add("Position", VertexFormatElement.POSITION)
			.add("Color", VertexFormatElement.COLOR)
			.add("UV0", VertexFormatElement.UV0)
			.add("Rounding", ROUNDING)
			.add("Dimensions", DIMENSIONS)
			.add("OutlineWidth", OUTLINE_WIDTH)
			.build(), VertexFormat.Mode.QUADS).build());

	public static final RenderPipeline FONT = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.GUI_TEXTURED_SNIPPET)
		.withLocation(ResourceLocation.fromNamespaceAndPath(RenderingCommon.MODID, "font"))
		.withVertexShader(ResourceLocation.fromNamespaceAndPath(RenderingCommon.MODID, "core/font"))
		.withFragmentShader(ResourceLocation.fromNamespaceAndPath(RenderingCommon.MODID, "core/font"))
		.build());

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
		return ((BufferBuilderAccessor) builder).begin(element);
	}
}
