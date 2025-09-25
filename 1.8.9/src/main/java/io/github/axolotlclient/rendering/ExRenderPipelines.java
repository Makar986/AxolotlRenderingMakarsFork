package io.github.axolotlclient.rendering;

import java.io.IOException;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import io.github.axolotlclient.rendering.mixin.BufferBuilderAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.resource.Identifier;

public class ExRenderPipelines {

	public static final int NUM_CUSTOM_VERTEX_FORMAT_ELEMENTS = 2;
	// rounding is a vec4
	public static final VertexFormatElement ROUNDING = new VertexFormatElement(0, VertexFormatElement.Type.FLOAT, VertexFormatElement.Usage.POSITION, 4);
	// dimensions is a vec2
	public static final VertexFormatElement DIMENSIONS = new VertexFormatElement(0, VertexFormatElement.Type.FLOAT, VertexFormatElement.Usage.POSITION, 4);

	/*@ApiStatus.Internal
	public static ShaderManager.EffectWrapper ROUNDED_RECT_SHADER, ROUNDED_RECT_TEX_SHADER;
	public static PostChain ROUNDED_RECT_CHAIN;*/
	public static int ROUNDED_RECT_PROGRAM;

	public static final VertexFormat ROUNDED_RECT_FORMAT = new VertexFormat();
	public static final VertexFormat ROUNDED_RECT_TEX_FORMAT = new VertexFormat();

	static {
		ROUNDED_RECT_FORMAT.addElement(DefaultVertexFormat.POSITION_ELEMENT)
			.addElement(DefaultVertexFormat.COLOR_ELEMENT)
			.addElement(DefaultVertexFormat.UV0_ELEMENT)
			.addElement(DefaultVertexFormat.UV0_ELEMENT)
			.addElement(DefaultVertexFormat.UV0_ELEMENT)
			.addElement(DefaultVertexFormat.UV0_ELEMENT);
			/*.addElement(ROUNDING)
			.addElement(DIMENSIONS);*/

		ROUNDED_RECT_TEX_FORMAT.addElement(DefaultVertexFormat.POSITION_ELEMENT)
			.addElement(DefaultVertexFormat.UV0_ELEMENT)
			.addElement(DefaultVertexFormat.COLOR_ELEMENT)
			.addElement(ROUNDING)
			.addElement(DIMENSIONS);
		/*ROUNDED_RECT_SHADER = ShaderManager.load("rounded_rect");
		ROUNDED_RECT_TEX_SHADER = ShaderManager.load("rounded_rect_tex");*/
		var mc = Minecraft.getInstance();
		try {
			//ROUNDED_RECT_CHAIN = new PostChain(mc.getTextureManager(), mc.getResourceManager(), mc.getRenderTarget(), new Identifier("shaders/post/rounded_rect.json"));
			ROUNDED_RECT_PROGRAM = ShaderManager.createProgram(new Identifier("program/rounded_rect"));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/*public static PostChain pass;

	static {
		try {
			pass = new PostChain(Minecraft.getInstance().getTextureManager(), Minecraft.getInstance().getResourceManager(), Minecraft.getInstance().getRenderTarget(), new Identifier("shaders/core/rounded_rect.json"));
		} catch (IOException e) {
			//LOGGER.warn("Failed to load shader: " + identifier, (Throwable)var3);
			throw new RuntimeException(e);
		}
	}*/

	/*public static RenderType ROUNDED_RECT = RenderType.create(RenderingCommon.MODID + ":rounded_rect",
		ROUNDED_RECT_FORMAT,
		VertexFormat.Mode.QUADS,
		RenderType.SMALL_BUFFER_SIZE,
		RenderType.CompositeState.builder()
			.setShaderState(new RenderStateShard.ShaderStateShard(ExRenderPipelines::roundedRectShader))
			.setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
			.setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
			.createCompositeState(false));*/

	public static void addRounding(BufferBuilder builder, float tl, float bl, float br, float tr) {
		builder.texture(br, tr);
		builder.texture(bl, tl);
		/*var accessor = ((BufferBuilderAccessor) builder);
		var buf = builder.getBuffer();
		var offset = builder.getVertexCount() * builder.getFormat().getVertexSize() + builder.getFormat().getOffset(accessor.getColor());
		buf.putFloat(offset, br);
		buf.putFloat(offset + 4, tr);
		buf.putFloat(offset + 8, bl);
		buf.putFloat(offset + 12, tl);
		accessor.invokeNextElement();*/
	}

	public static void addDimensions(BufferBuilder builder, float relX, float relY, float width, float height) {
		builder.texture(relX, relY);
		builder.texture(width, height);
		/*var accessor = ((BufferBuilderAccessor) builder);
		var buf = builder.getBuffer();
		var offset = builder.getVertexCount() * builder.getFormat().getVertexSize() + builder.getFormat().getOffset(accessor.getColor());
		buf.putFloat(offset, relX);
		buf.putFloat(offset + 4, relY);
		buf.putFloat(offset + 8, width);
		buf.putFloat(offset + 12, height);
		accessor.invokeNextElement();*/
	}

}
