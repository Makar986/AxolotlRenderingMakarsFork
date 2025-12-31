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

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import io.github.axolotlclient.rendering.*;
import io.github.axolotlclient.rendering.font.Font;
import io.github.axolotlclient.rendering.svg.SVGCache;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.render.state.GuiRenderState;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

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
	Minecraft minecraft;

	@Shadow
	protected abstract void innerBlit(RenderPipeline pipeline, Identifier atlas, int x0, int x1, int y0, int y1, float u0, float u1, float v0, float v1, int color);

	@Override
	public void axolotlclient_rendering$segment(int x0, int y0, int x1, int y1, int colorTopLeft, int colorBottomLeft, int colorBottomRight, int colorTopRight, float radius) {
		guiRenderState.submitGuiElement(new ColoredRoundedRectangleRenderState(ExRenderPipelines.SEGMENT,
			TextureSetup.noTexture(),
			new Matrix3x2f(pose()),
			x0 - radius, y0 - radius, x1 + radius, y1 + radius,
			colorTopLeft, colorBottomLeft, colorBottomRight, colorTopRight,
			radius, 0, 0, 0,
			scissorStack.peek()));
	}

	@Override
	public void axolotlclient_rendering$roundedRectGradientVarying(int x0, int y0, int x1, int y1, int colorTopLeft, int colorBottomLeft, int colorBottomRight, int colorTopRight, float roundingPxTopLeft, float roundingPxBottomLeft, float roundingPxBottomRight, float roundingPxTopRight) {
		guiRenderState.submitGuiElement(new ColoredRoundedRectangleRenderState(ExRenderPipelines.ROUNDED_RECT,
			TextureSetup.noTexture(),
			new Matrix3x2f(pose()),
			x0, y0, x1, y1,
			colorTopLeft, colorBottomLeft, colorBottomRight, colorTopRight,
			roundingPxTopLeft, roundingPxBottomLeft, roundingPxBottomRight, roundingPxTopRight,
			scissorStack.peek()));
	}

	@Override
	public void axolotlclient_rendering$outlineRoundedRectGradientVarying(int x0, int y0, int x1, int y1, int colorTopLeft, int colorBottomLeft, int colorBottomRight, int colorTopRight, float roundingPxTopLeft, float roundingPxBottomLeft, float roundingPxBottomRight, float roundingPxTopRight, float outlineWidth) {
		guiRenderState.submitGuiElement(new ColoredRoundedRectangleOutlineRenderState(ExRenderPipelines.ROUNDED_RECT_OUTLINE,
			TextureSetup.noTexture(),
			new Matrix3x2f(pose()),
			x0, y0, x1, y1,
			colorTopLeft, colorBottomLeft, colorBottomRight, colorTopRight,
			roundingPxTopLeft, roundingPxBottomLeft, roundingPxBottomRight, roundingPxTopRight,
			outlineWidth, scissorStack.peek()));
	}

	@Override
	public void axolotlclient_rendering$roundedBlitVarying(Identifier location, int x, int y, float u, float v, int width, int height, int uWidth, int vHeight, int textureWidth, int textureHeight, int color, float roundingPxTopLeft, float roundingPxBottomLeft, float roundingPxBottomRight, float roundingPxTopRight) {
		this.innerBlit(ExRenderPipelines.ROUNDED_RECT_TEX, location, x, x + width, y, y + height, u / textureWidth, (u + uWidth) / textureWidth, v / textureHeight, (v + vHeight) / textureHeight, color, roundingPxTopLeft, roundingPxBottomLeft, roundingPxBottomRight, roundingPxTopRight);
	}

	@Override
	public void axolotlclient_rendering$roundedBlitVarying(Identifier atlas, int x0, int y0, int x1, int y1, float u0, float u1, float v0, float v1, int color, float roundingPxTopLeft, float roundingPxBottomLeft, float roundingPxBottomRight, float roundingPxTopRight) {
		this.innerBlit(ExRenderPipelines.ROUNDED_RECT_TEX, atlas, x0, x1, y0, y1, u0, u1, v0, v1, color, roundingPxTopLeft, roundingPxBottomLeft, roundingPxBottomRight, roundingPxTopRight);
	}

	@Unique
	private void innerBlit(RenderPipeline pipeline, Identifier atlas, int x0, int x1, int y0, int y1, float u0, float u1, float v0, float v1, int color, float roundingPxTopLeft, float roundingPxBottomLeft, float roundingPxBottomRight, float roundingPxTopRight) {
		var tex = this.minecraft.getTextureManager().getTexture(atlas);
		this.guiRenderState.submitGuiElement(
			new ColoredRoundedRectangleBlitRenderState(
				pipeline,
				TextureSetup.singleTexture(tex.getTextureView(), tex.getSampler()),
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
	public void axolotlclient_rendering$outlineRoundedBlitVarying(Identifier location, int x, int y, float u, float v, int width, int height, int uWidth, int vHeight, int textureWidth, int textureHeight, int color, float roundingPxTopLeft, float roundingPxBottomLeft, float roundingPxBottomRight, float roundingPxTopRight, float outlineWidth) {
		this.innerBlit(ExRenderPipelines.ROUNDED_RECT_TEX_OUTLINE, location, x, x + width, y, y + height,
			u / textureWidth, (u + uWidth) / textureWidth, v / textureHeight, (v + vHeight) / textureHeight, color, roundingPxTopLeft, roundingPxBottomLeft, roundingPxBottomRight, roundingPxTopRight, outlineWidth);
	}

	@Override
	public void axolotlclient_rendering$outlineRoundedBlitVarying(Identifier atlas, int x0, int y0, int x1, int y1, float u0, float u1, float v0, float v1, int color, float roundingPxTopLeft, float roundingPxBottomLeft, float roundingPxBottomRight, float roundingPxTopRight, float outlineWidth) {
		this.innerBlit(ExRenderPipelines.ROUNDED_RECT_TEX_OUTLINE, atlas, x0, x1, y0, y1, u0, u1, v0, v1, color, roundingPxTopLeft, roundingPxBottomLeft, roundingPxBottomRight, roundingPxTopRight, outlineWidth);
	}

	@Unique
	private void innerBlit(RenderPipeline pipeline, Identifier atlas, int x0, int x1, int y0, int y1, float u0, float u1, float v0, float v1, int color, float roundingPxTopLeft, float roundingPxBottomLeft, float roundingPxBottomRight, float roundingPxTopRight, float outlineWidth) {
		var tex = this.minecraft.getTextureManager().getTexture(atlas);
		this.guiRenderState.submitGuiElement(
			new ColoredRoundedRectangleBlitOutlineRenderState(
				pipeline,
				TextureSetup.singleTexture(tex.getTextureView(), tex.getSampler()),
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
	public void axolotlclient_rendering$blitSVG(Identifier location, int x, int y, int width, int height, int color) {
		var scale = Minecraft.getInstance().getWindow().getGuiScale();
		var key = SVGCache.getInstance().load(location, width * scale, height * scale);
		innerBlit(RenderPipelines.GUI_TEXTURED, key, x, x + width, y, y + height, 0, 1f, 0f, 1, color);
	}

	@WrapOperation(method = "textRendererForWidget", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;createDefaultTextParameters(F)Lnet/minecraft/client/gui/ActiveTextCollector$Parameters;"))
	private ActiveTextCollector.Parameters allowCustomFontRenderingForWidgets(GuiGraphics instance, float v, Operation<ActiveTextCollector.Parameters> original, AbstractWidget widget) {
		var font = widget.axolotlclient_rendering$getFont();
		var o = original.call(instance, v);
		if (font != null) return o.axolotlclient_rendering$withFont(font);
		return o;
	}
}
