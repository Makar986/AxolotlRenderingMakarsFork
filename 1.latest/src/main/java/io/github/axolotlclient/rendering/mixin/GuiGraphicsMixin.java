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

package io.github.axolotlclient.rendering.mixin;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.textures.GpuTextureView;
import io.github.axolotlclient.rendering.*;
import io.github.axolotlclient.rendering.font.Font;
import io.github.axolotlclient.rendering.svg.SVGCache;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.render.state.GuiRenderState;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
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

	@Shadow
	protected abstract void innerBlit(RenderPipeline pipeline, ResourceLocation atlas, int x0, int x1, int y0, int y1, float u0, float u1, float v0, float v1, int color);

	@Override
	public void axolotlclient_rendering$roundedRectGradientVarying(int x0, int y0, int x1, int y1, int colorTopLeft, int colorBottomLeft, int colorBottomRight, int colorTopRight, int roundingPxTopLeft, int roundingPxBottomLeft, int roundingPxBottomRight, int roundingPxTopRight) {
		guiRenderState.submitGuiElement(new ColoredRoundedRectangleRenderState(ExRenderPipelines.ROUNDED_RECT,
			TextureSetup.noTexture(),
			new Matrix3x2f(pose()),
			x0, y0, x1, y1,
			colorTopLeft, colorBottomLeft, colorBottomRight, colorTopRight,
			roundingPxTopLeft, roundingPxBottomLeft, roundingPxBottomRight, roundingPxTopRight,
			scissorStack.peek()));
	}

	@Override
	public void axolotlclient_rendering$outlineRoundedRectGradientVarying(int x0, int y0, int x1, int y1, int colorTopLeft, int colorBottomLeft, int colorBottomRight, int colorTopRight, int roundingPxTopLeft, int roundingPxBottomLeft, int roundingPxBottomRight, int roundingPxTopRight, float outlineWidth) {
		guiRenderState.submitGuiElement(new ColoredRoundedRectangleOutlineRenderState(ExRenderPipelines.ROUNDED_RECT_OUTLINE,
			TextureSetup.noTexture(),
			new Matrix3x2f(pose()),
			x0, y0, x1, y1,
			colorTopLeft, colorBottomLeft, colorBottomRight, colorTopRight,
			roundingPxTopLeft, roundingPxBottomLeft, roundingPxBottomRight, roundingPxTopRight,
			outlineWidth, scissorStack.peek()));
	}

	@Override
	public void axolotlclient_rendering$roundedBlitVarying(ResourceLocation location, int x, int y, float u, float v, int width, int height, int uWidth, int vHeight, int textureWidth, int textureHeight, int color, int roundingPxTopLeft, int roundingPxBottomLeft, int roundingPxBottomRight, int roundingPxTopRight) {
		this.innerBlit(ExRenderPipelines.ROUNDED_RECT_TEX, location, x, x + width, y, y + height, u / textureWidth, (u + uWidth) / textureWidth, v / textureHeight, (v + vHeight) / textureHeight, color, roundingPxTopLeft, roundingPxBottomLeft, roundingPxBottomRight, roundingPxTopRight);
	}

	@Override
	public void axolotlclient_rendering$roundedBlitVarying(ResourceLocation atlas, int x0, int y0, int x1, int y1, float u0, float u1, float v0, float v1, int color, int roundingPxTopLeft, int roundingPxBottomLeft, int roundingPxBottomRight, int roundingPxTopRight) {
		this.innerBlit(ExRenderPipelines.ROUNDED_RECT_TEX, atlas, x0, x1, y0, y1, u0, u1, v0, v1, color, roundingPxTopLeft, roundingPxBottomLeft, roundingPxBottomRight, roundingPxTopRight);
	}

	@Unique
	private void innerBlit(RenderPipeline pipeline, ResourceLocation atlas, int x0, int x1, int y0, int y1, float u0, float u1, float v0, float v1, int color, int roundingPxTopLeft, int roundingPxBottomLeft, int roundingPxBottomRight, int roundingPxTopRight) {
		GpuTextureView gpuTextureView = this.minecraft.getTextureManager().getTexture(atlas).getTextureView();
		this.guiRenderState.submitGuiElement(
			new ColoredRoundedRectangleBlitRenderState(
				pipeline,
				TextureSetup.singleTexture(gpuTextureView),
				new Matrix3x2f(this.pose()),
				x0, y0, x1, y1,
				u0, u1, v0, v1,
				color, color, color, color,
				roundingPxTopLeft, roundingPxBottomLeft, roundingPxBottomRight, roundingPxTopRight,
				this.scissorStack.peek()
			)
		);
	}

	@Override
	public void axolotlclient_rendering$outlineRoundedBlitVarying(ResourceLocation location, int x, int y, float u, float v, int width, int height, int uWidth, int vHeight, int textureWidth, int textureHeight, int color, int roundingPxTopLeft, int roundingPxBottomLeft, int roundingPxBottomRight, int roundingPxTopRight, float outlineWidth) {
		this.innerBlit(ExRenderPipelines.ROUNDED_RECT_TEX_OUTLINE, location, x, x + width, y, y + height,
			u / textureWidth, (u + uWidth) / textureWidth, v / textureHeight, (v + vHeight) / textureHeight, color, roundingPxTopLeft, roundingPxBottomLeft, roundingPxBottomRight, roundingPxTopRight, outlineWidth);
	}

	@Override
	public void axolotlclient_rendering$outlineRoundedBlitVarying(ResourceLocation atlas, int x0, int y0, int x1, int y1, float u0, float u1, float v0, float v1, int color, int roundingPxTopLeft, int roundingPxBottomLeft, int roundingPxBottomRight, int roundingPxTopRight, float outlineWidth) {
		this.innerBlit(ExRenderPipelines.ROUNDED_RECT_TEX_OUTLINE, atlas, x0, x1, y0, y1, u0, u1, v0, v1, color, roundingPxTopLeft, roundingPxBottomLeft, roundingPxBottomRight, roundingPxTopRight, outlineWidth);
	}

	@Unique
	private void innerBlit(RenderPipeline pipeline, ResourceLocation atlas, int x0, int x1, int y0, int y1, float u0, float u1, float v0, float v1, int color, int roundingPxTopLeft, int roundingPxBottomLeft, int roundingPxBottomRight, int roundingPxTopRight, float outlineWidth) {
		GpuTextureView gpuTextureView = this.minecraft.getTextureManager().getTexture(atlas).getTextureView();
		this.guiRenderState.submitGuiElement(
			new ColoredRoundedRectangleBlitOutlineRenderState(
				pipeline,
				TextureSetup.singleTexture(gpuTextureView),
				new Matrix3x2f(this.pose()),
				x0, y0, x1, y1,
				u0, u1, v0, v1,
				color, color, color, color,
				roundingPxTopLeft, roundingPxBottomLeft, roundingPxBottomRight, roundingPxTopRight,
				outlineWidth, this.scissorStack.peek()
			)
		);
	}

	@Override
	public void axolotlclient_rendering$drawString(Font font, String text, float x, float y, int color, boolean shadow, int origin) {
		font.renderText((GuiGraphics) (Object) this, text, x, y, color, shadow, origin);
	}

	@Override
	public void axolotlclient_rendering$drawString(Font font, FormattedCharSequence text, float x, float y, int color, boolean shadow, int origin) {
		font.renderText((GuiGraphics) (Object) this, text, x, y, color, shadow, origin);
	}

	@Override
	public void axolotlclient_rendering$drawString(Font font, Component text, float x, float y, int color, boolean shadow, int origin) {
		font.renderText((GuiGraphics) (Object) this, text, x, y, color, shadow, origin);
	}

	@Override
	public void axolotlclient_rendering$blitSVG(ResourceLocation location, int x, int y, int width, int height, int color) {
		var scale = Minecraft.getInstance().getWindow().getGuiScale();
		var key = SVGCache.getInstance().load(location, width * scale, height * scale);
		innerBlit(RenderPipelines.GUI_TEXTURED, key, x, x + width, y, y + height, 0, 1f, 0f, 1, color);
	}

}
