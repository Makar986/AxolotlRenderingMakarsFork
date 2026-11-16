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
import net.minecraft.resources.Identifier;
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

	public static final int NUM_CUSTOM_VERTEX_FORMAT_ELEMENTS = 8;
	private static final VertexFormatElement ROUNDING = VertexFormatElement.register(nextUnusedVFEIndex(), 0, VertexFormatElement.Type.FLOAT, false, 4);
	private static final VertexFormatElement DIMENSIONS = VertexFormatElement.register(nextUnusedVFEIndex(), 0, VertexFormatElement.Type.FLOAT, false, 4);
	private static final VertexFormatElement OUTLINE_WIDTH = VertexFormatElement.register(nextUnusedVFEIndex(), 0, VertexFormatElement.Type.FLOAT, false, 1);
	// Per-element effect parameters for blur / liquid glass (the GUI is drawn in batches, so no uniforms).
	private static final VertexFormatElement EFFECT0 = VertexFormatElement.register(nextUnusedVFEIndex(), 0, VertexFormatElement.Type.FLOAT, false, 4);
	private static final VertexFormatElement EFFECT1 = VertexFormatElement.register(nextUnusedVFEIndex(), 0, VertexFormatElement.Type.FLOAT, false, 4);
	private static final VertexFormatElement EFFECT2 = VertexFormatElement.register(nextUnusedVFEIndex(), 0, VertexFormatElement.Type.FLOAT, false, 4);
	private static final VertexFormatElement EFFECT3 = VertexFormatElement.register(nextUnusedVFEIndex(), 0, VertexFormatElement.Type.FLOAT, false, 4);

	public static final RenderPipeline ROUNDED_RECT = RenderPipeline.builder(RenderPipelines.GUI_SNIPPET)
		.withLocation(Identifier.fromNamespaceAndPath(RenderingCommon.MODID, "rounded_rect"))
		.withVertexShader(Identifier.fromNamespaceAndPath(RenderingCommon.MODID, "core/rounded_rect"))
		.withFragmentShader(Identifier.fromNamespaceAndPath(RenderingCommon.MODID, "core/rounded_rect"))
		.withVertexFormat(VertexFormat.builder()
			.add("Position", VertexFormatElement.POSITION)
			.add("Color", VertexFormatElement.COLOR)
			.add("Rounding", ROUNDING)
			.add("Dimensions", DIMENSIONS)
			.build(), VertexFormat.Mode.QUADS).build();

	public static final RenderPipeline SEGMENT = RenderPipeline.builder(RenderPipelines.GUI_SNIPPET)
		.withLocation(Identifier.fromNamespaceAndPath(RenderingCommon.MODID, "segment"))
		.withVertexShader(Identifier.fromNamespaceAndPath(RenderingCommon.MODID, "core/rounded_rect"))
		.withFragmentShader(Identifier.fromNamespaceAndPath(RenderingCommon.MODID, "core/segment"))
		.withVertexFormat(VertexFormat.builder()
			.add("Position", VertexFormatElement.POSITION)
			.add("Color", VertexFormatElement.COLOR)
			.add("Rounding", ROUNDING)
			.add("Dimensions", DIMENSIONS)
			.build(), VertexFormat.Mode.QUADS).build();

	public static final RenderPipeline ROUNDED_RECT_TEX = RenderPipeline.builder(RenderPipelines.GUI_TEXTURED_SNIPPET)
		.withLocation(Identifier.fromNamespaceAndPath(RenderingCommon.MODID, "rounded_rect_tex"))
		.withVertexShader(Identifier.fromNamespaceAndPath(RenderingCommon.MODID, "core/rounded_rect_tex"))
		.withFragmentShader(Identifier.fromNamespaceAndPath(RenderingCommon.MODID, "core/rounded_rect_tex"))
		.withVertexFormat(VertexFormat.builder()
			.add("Position", VertexFormatElement.POSITION)
			.add("Color", VertexFormatElement.COLOR)
			.add("UV0", VertexFormatElement.UV0)
			.add("Rounding", ROUNDING)
			.add("Dimensions", DIMENSIONS)
			.build(), VertexFormat.Mode.QUADS).build();

	public static final RenderPipeline ROUNDED_RECT_OUTLINE = RenderPipeline.builder(RenderPipelines.GUI_SNIPPET)
		.withLocation(Identifier.fromNamespaceAndPath(RenderingCommon.MODID, "rounded_rect_outline"))
		.withVertexShader(Identifier.fromNamespaceAndPath(RenderingCommon.MODID, "core/rounded_rect_outline"))
		.withFragmentShader(Identifier.fromNamespaceAndPath(RenderingCommon.MODID, "core/rounded_rect_outline"))
		.withVertexFormat(VertexFormat.builder()
			.add("Position", VertexFormatElement.POSITION)
			.add("Color", VertexFormatElement.COLOR)
			.add("Rounding", ROUNDING)
			.add("Dimensions", DIMENSIONS)
			.add("OutlineWidth", OUTLINE_WIDTH)
			.build(), VertexFormat.Mode.QUADS).build();

	public static final RenderPipeline ROUNDED_RECT_TEX_OUTLINE = RenderPipeline.builder(RenderPipelines.GUI_TEXTURED_SNIPPET)
		.withLocation(Identifier.fromNamespaceAndPath(RenderingCommon.MODID, "rounded_rect_tex_outline"))
		.withVertexShader(Identifier.fromNamespaceAndPath(RenderingCommon.MODID, "core/rounded_rect_tex_outline"))
		.withFragmentShader(Identifier.fromNamespaceAndPath(RenderingCommon.MODID, "core/rounded_rect_tex_outline"))
		.withVertexFormat(VertexFormat.builder()
			.add("Position", VertexFormatElement.POSITION)
			.add("Color", VertexFormatElement.COLOR)
			.add("UV0", VertexFormatElement.UV0)
			.add("Rounding", ROUNDING)
			.add("Dimensions", DIMENSIONS)
			.add("OutlineWidth", OUTLINE_WIDTH)
			.build(), VertexFormat.Mode.QUADS).build();

	public static final RenderPipeline FONT = RenderPipeline.builder(RenderPipelines.GUI_TEXTURED_SNIPPET)
		.withLocation(Identifier.fromNamespaceAndPath(RenderingCommon.MODID, "font"))
		.withVertexShader(Identifier.fromNamespaceAndPath(RenderingCommon.MODID, "core/font"))
		.withFragmentShader(Identifier.fromNamespaceAndPath(RenderingCommon.MODID, "core/font"))
		.build();

	public static void init(net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent event) {
		event.registerPipeline(ROUNDED_RECT);
		event.registerPipeline(SEGMENT);
		event.registerPipeline(ROUNDED_RECT_TEX);
		event.registerPipeline(ROUNDED_RECT_OUTLINE);
		event.registerPipeline(ROUNDED_RECT_TEX_OUTLINE);
		event.registerPipeline(FONT);
		event.registerPipeline(GAUSSIAN_BLUR);
		event.registerPipeline(LIQUID_GLASS);
		event.registerPipeline(MSDF_FONT);
	}

	/**
	 * Samples {@link BackdropTexture} (a copy of the frame taken right before the GUI is drawn).
	 * Effect0 = (blurRadius, sigma, -, -).
	 */
	public static final RenderPipeline GAUSSIAN_BLUR = RenderPipeline.builder(RenderPipelines.GUI_TEXTURED_SNIPPET)
		.withLocation(Identifier.fromNamespaceAndPath(RenderingCommon.MODID, "gaussian_blur"))
		.withVertexShader(Identifier.fromNamespaceAndPath(RenderingCommon.MODID, "core/gaussian_blur"))
		.withFragmentShader(Identifier.fromNamespaceAndPath(RenderingCommon.MODID, "core/gaussian_blur"))
		.withVertexFormat(VertexFormat.builder()
			.add("Position", VertexFormatElement.POSITION)
			.add("Color", VertexFormatElement.COLOR)
			.add("UV0", VertexFormatElement.UV0)
			.add("Rounding", ROUNDING)
			.add("Dimensions", DIMENSIONS)
			.add("Effect0", EFFECT0)
			.build(), VertexFormat.Mode.QUADS)
		.build();

	/**
	 * Samples {@link BackdropTexture}.
	 * Effect0 = optics (refThickness, refFactor, refDispersion, refFresnelRange),
	 * Effect1 = optics (refFresnelHardness, refFresnelFactor, glareRange, glareHardness),
	 * Effect2 = (blurRadius, borderWidth, -, -), Effect3 = border color (rgba).
	 */
	public static final RenderPipeline LIQUID_GLASS = RenderPipeline.builder(RenderPipelines.GUI_TEXTURED_SNIPPET)
		.withLocation(Identifier.fromNamespaceAndPath(RenderingCommon.MODID, "liquid_glass"))
		.withVertexShader(Identifier.fromNamespaceAndPath(RenderingCommon.MODID, "core/liquid_glass"))
		.withFragmentShader(Identifier.fromNamespaceAndPath(RenderingCommon.MODID, "core/liquid_glass"))
		.withVertexFormat(VertexFormat.builder()
			.add("Position", VertexFormatElement.POSITION)
			.add("Color", VertexFormatElement.COLOR)
			.add("UV0", VertexFormatElement.UV0)
			.add("Rounding", ROUNDING)
			.add("Dimensions", DIMENSIONS)
			.add("Effect0", EFFECT0)
			.add("Effect1", EFFECT1)
			.add("Effect2", EFFECT2)
			.add("Effect3", EFFECT3)
			.build(), VertexFormat.Mode.QUADS)
		.build();

	public static final RenderPipeline MSDF_FONT = RenderPipeline.builder(RenderPipelines.GUI_TEXTURED_SNIPPET)
		.withLocation(Identifier.fromNamespaceAndPath(RenderingCommon.MODID, "msdf_font"))
		.withVertexShader(Identifier.fromNamespaceAndPath(RenderingCommon.MODID, "core/msdf_font"))
		.withFragmentShader(Identifier.fromNamespaceAndPath(RenderingCommon.MODID, "core/msdf_font"))
		.build();

	public static void addEffect(VertexConsumer consumer, int index, float x, float y, float z, float w) {
		VertexFormatElement element = switch (index) {
			case 0 -> EFFECT0;
			case 1 -> EFFECT1;
			case 2 -> EFFECT2;
			case 3 -> EFFECT3;
			default -> throw new IllegalArgumentException("effect index " + index);
		};
		var offset = beginElement(consumer, element);
		if (offset != -1L) {
			MemoryUtil.memPutFloat(offset, x);
			MemoryUtil.memPutFloat(offset + 4L, y);
			MemoryUtil.memPutFloat(offset + 8L, z);
			MemoryUtil.memPutFloat(offset + 12L, w);
		}
	}

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
