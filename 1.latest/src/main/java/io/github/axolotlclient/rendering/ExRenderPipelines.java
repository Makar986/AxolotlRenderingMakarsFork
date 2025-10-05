package io.github.axolotlclient.rendering;

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
	private static int nextUnusedVFEIndex() {
		for (int i = 0; i < VertexFormatElement.MAX_COUNT; i++) {
			if (VertexFormatElement.byId(i) == null) {
				return i;
			}
		}
		throw new IllegalStateException("Not enough VertexFormatElement IDs available. " +
			"This should not be possible unless other mods are broken.");
	}

	public static final int NUM_CUSTOM_VERTEX_FORMAT_ELEMENTS = 2;
	// rounding is a vec4
	public static final VertexFormatElement ROUNDING = VertexFormatElement.register(nextUnusedVFEIndex(), 0, VertexFormatElement.Type.FLOAT, VertexFormatElement.Usage.GENERIC, 4);
	// dimensions is a vec2
	public static final VertexFormatElement DIMENSIONS = VertexFormatElement.register(nextUnusedVFEIndex(), 0, VertexFormatElement.Type.FLOAT, VertexFormatElement.Usage.GENERIC, 4);
	public static RenderPipeline ROUNDED_RECT = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.GUI_SNIPPET)
		.withLocation(ResourceLocation.fromNamespaceAndPath(RenderingCommon.MODID, "rounded_rect"))
		.withVertexShader(ResourceLocation.fromNamespaceAndPath(RenderingCommon.MODID, "core/rounded_rect"))
		.withFragmentShader(ResourceLocation.fromNamespaceAndPath(RenderingCommon.MODID, "core/rounded_rect"))
		.withVertexFormat(VertexFormat.builder()
			.add("Position", VertexFormatElement.POSITION)
			.add("Color", VertexFormatElement.COLOR)
			.add("Rounding", ROUNDING)
			.add("Dimensions", DIMENSIONS)
			.build(), VertexFormat.Mode.QUADS).build());
	public static RenderPipeline ROUNDED_RECT_TEX = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.GUI_TEXTURED_SNIPPET)
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

	private static long beginElement(VertexConsumer consumer, VertexFormatElement element) {
		if (!(consumer instanceof BufferBuilder builder)) {
			throw new IllegalArgumentException("Not a BufferBuilder!");
		}
		return ((BufferBuilderAccessor) builder).begin(element);
	}
}
