package io.github.axolotlclient.rendering;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.*;
import io.github.axolotlclient.rendering.mixin.BufferBuilderAccessor;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import org.jetbrains.annotations.ApiStatus;

public class ExRenderPipelines {

	public static final int NUM_CUSTOM_VERTEX_FORMAT_ELEMENTS = 2;
	// rounding is a vec4
	public static final VertexFormatElement ROUNDING = new VertexFormatElement(0, VertexFormatElement.Type.FLOAT, VertexFormatElement.Usage.GENERIC, 4);
	// dimensions is a vec2
	public static final VertexFormatElement DIMENSIONS = new VertexFormatElement(0, VertexFormatElement.Type.FLOAT, VertexFormatElement.Usage.GENERIC, 4);

	@ApiStatus.Internal
	public static ShaderInstance ROUNDED_RECT_SHADER;

	private static ShaderInstance roundedRectShader() {
		return ROUNDED_RECT_SHADER;
	}

	public static final VertexFormat ROUNDED_RECT_FORMAT = new VertexFormat(ImmutableMap.<String, VertexFormatElement>builder()
		.put("Position", DefaultVertexFormat.ELEMENT_POSITION)
		.put("Color", DefaultVertexFormat.ELEMENT_COLOR)
		.put("Rounding", ROUNDING)
		.put("Dimensions", DIMENSIONS)
		.build());

	public static RenderType ROUNDED_RECT = RenderType.create(RenderingCommon.MODID + ":rounded_rect",
		ROUNDED_RECT_FORMAT,
		VertexFormat.Mode.QUADS,
		RenderType.SMALL_BUFFER_SIZE,
		RenderType.CompositeState.builder()
			.setShaderState(new RenderStateShard.ShaderStateShard(ExRenderPipelines::roundedRectShader))
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

	public static void addRounding(VertexConsumer consumer, float tl, float bl, float br, float tr) {
		if (!(consumer instanceof BufferBuilder builder)) {
			throw new IllegalArgumentException("Not a BufferBuilder!");
		}
		var accessor = ((BufferBuilderAccessor) builder);
		builder.putFloat(0, br);
		builder.putFloat(4, tr);
		builder.putFloat(8, bl);
		builder.putFloat(12, tl);
		accessor.invokeNextElement();
	}

	public static void addDimensions(VertexConsumer consumer, float relX, float relY, float width, float height) {
		if (!(consumer instanceof BufferBuilder builder)) {
			throw new IllegalArgumentException("Not a BufferBuilder!");
		}
		var accessor = ((BufferBuilderAccessor) builder);
		builder.putFloat(0, relX);
		builder.putFloat(4, relY);
		builder.putFloat(8, width);
		builder.putFloat(12, height);
		accessor.invokeNextElement();
	}

}
