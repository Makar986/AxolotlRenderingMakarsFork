package io.github.axolotlclient.rendering.mixin;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.textures.GpuTextureView;
import io.github.axolotlclient.rendering.ColoredRoundedRectangleBlitRenderState;
import io.github.axolotlclient.rendering.ColoredRoundedRectangleRenderState;
import io.github.axolotlclient.rendering.ExRenderPipelines;
import io.github.axolotlclient.rendering.GuiGraphicsExtension;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.render.state.GuiRenderState;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(GuiGraphics.class)
public abstract class GuiGraphicsMixin implements GuiGraphicsExtension {

	@Shadow
	@Final
	public GuiGraphics.ScissorStack scissorStack;

	@Shadow
	public abstract Matrix3x2fStack pose();

	@Shadow
	@Final
	public GuiRenderState guiRenderState;

	@Shadow
	@Final
	private Minecraft minecraft;

	@Override
	public void axolotlclient_rendering$roundedRect(int x0, int y0, int x1, int y1, int color, int roundingPx) {
		guiRenderState.submitGuiElement(new ColoredRoundedRectangleRenderState(ExRenderPipelines.ROUNDED_RECT,
			TextureSetup.noTexture(),
			new Matrix3x2f(pose()),
			x0,
			y0,
			x1,
			y1,
			color,
			color,
			color,
			color,
			roundingPx,
			roundingPx,
			roundingPx,
			roundingPx,
			scissorStack.peek()));
	}

	@Override
	public void axolotlclient_rendering$roundedBlit(
		ResourceLocation atlas, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight, int color, int rounding
	) {
		this.axolotlclient_rendering$roundedBlit(atlas, x, y, u, v, width, height, width, height, textureWidth, textureHeight, color, rounding);
	}

	@Override
	public void axolotlclient_rendering$roundedBlit(ResourceLocation atlas, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight, int rounding) {
		this.axolotlclient_rendering$roundedBlit(atlas, x, y, u, v, width, height, width, height, textureWidth, textureHeight, rounding);
	}

	@Override
	public void axolotlclient_rendering$roundedBlit(
		ResourceLocation atlas,
		int x,
		int y,
		float u,
		float v,
		int width,
		int height,
		int uWidth,
		int vHeight,
		int textureWidth,
		int textureHeight,
		int rounding
	) {
		this.axolotlclient_rendering$roundedBlit(atlas, x, y, u, v, width, height, uWidth, vHeight, textureWidth, textureHeight, -1, rounding);
	}

	@Override
	public void axolotlclient_rendering$roundedBlit(ResourceLocation location, int x, int y, float u, float v, int width, int height, int uWidth, int vHeight, int textureWidth, int textureHeight, int color, int roundingPx) {
		this.innerBlit(
			ExRenderPipelines.ROUNDED_RECT_TEX,
			location,
			x,
			x + width,
			y,
			y + height,
			(u + 0.0F) / textureWidth,
			(u + uWidth) / textureWidth,
			(v + 0.0F) / textureHeight,
			(v + vHeight) / textureHeight,
			color,
			roundingPx
		);
	}

	@Override
	public void axolotlclient_rendering$roundedBlit(ResourceLocation atlas, int x0, int y0, int x1, int y1, float u0, float u1, float v0, float v1, int rounding) {
		this.innerBlit(ExRenderPipelines.ROUNDED_RECT_TEX, atlas, x0, x1, y0, y1, u0, u1, v0, v1, -1, rounding);
	}

	@Unique
	private void innerBlit(RenderPipeline pipeline, ResourceLocation atlas, int x0, int x1, int y0, int y1, float u0, float u1, float v0, float v1, int color, int rounding) {
		GpuTextureView gpuTextureView = this.minecraft.getTextureManager().getTexture(atlas).getTextureView();
		this.submitBlit(pipeline, gpuTextureView, x0, y0, x1, y1, u0, u1, v0, v1, color, rounding);
	}

	@Unique
	private void submitBlit(
		RenderPipeline pipeline, GpuTextureView atlasTexture, int x0, int y0, int x1, int y1, float u0, float u1, float v0, float v1, int color, int rounding
	) {
		this.guiRenderState
			.submitGuiElement(
				new ColoredRoundedRectangleBlitRenderState(
					pipeline,
					TextureSetup.singleTexture(atlasTexture),
					new Matrix3x2f(this.pose()),
					x0,
					y0,
					x1,
					y1,
					u0,
					u1,
					v0,
					v1,
					color,
					color,
					color,
					color,
					rounding,
					rounding,
					rounding,
					rounding,
					this.scissorStack.peek()
				)
			);
	}
}
