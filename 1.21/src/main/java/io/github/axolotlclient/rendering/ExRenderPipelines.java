package io.github.axolotlclient.rendering;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import io.github.axolotlclient.rendering.mixin.BufferBuilderAccessor;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
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

	public static final int NUM_CUSTOM_VERTEX_FORMAT_ELEMENTS = 2;
	// rounding is a vec4
	public static final VertexFormatElement ROUNDING = VertexFormatElement.register(nextUnusedVFEIndex(), 0, VertexFormatElement.Type.FLOAT, VertexFormatElement.Usage.GENERIC, 4);
	// dimensions is a vec2
	public static final VertexFormatElement DIMENSIONS = VertexFormatElement.register(nextUnusedVFEIndex(), 0, VertexFormatElement.Type.FLOAT, VertexFormatElement.Usage.GENERIC, 4);

	@ApiStatus.Internal
	public static ShaderInstance ROUNDED_RECT_SHADER;

	private static ShaderInstance roundedRectShader() {
		return ROUNDED_RECT_SHADER;
	}

	public static final VertexFormat ROUNDED_RECT_FORMAT = VertexFormat.builder().add("Position", VertexFormatElement.POSITION)
		.add("Color", VertexFormatElement.COLOR)
		.add("Rounding", ROUNDING)
		.add("Dimensions", DIMENSIONS)
		.build();

	public static RenderType ROUNDED_RECT = RenderType.create(RenderingCommon.MODID + ":rounded_rect",
		ROUNDED_RECT_FORMAT,
		VertexFormat.Mode.QUADS,
		RenderType.SMALL_BUFFER_SIZE,
		RenderType.CompositeState.builder()
			.setShaderState(new RenderStateShard.ShaderStateShard(ExRenderPipelines::roundedRectShader))
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
		var accessor = ((BufferBuilderAccessor) builder);
		return accessor.begin(element);
	}
}
