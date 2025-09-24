package io.github.axolotlclient.rendering;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.ResourceLocation;

public class ExRenderPipelines {
	public static RenderPipeline ROUNDED_RECT = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.GUI_SNIPPET)
		.withLocation(ResourceLocation.fromNamespaceAndPath(RenderingCommon.MODID, "rounded_rect"))
		.withVertexShader(ResourceLocation.fromNamespaceAndPath(RenderingCommon.MODID, "")).build());
}
