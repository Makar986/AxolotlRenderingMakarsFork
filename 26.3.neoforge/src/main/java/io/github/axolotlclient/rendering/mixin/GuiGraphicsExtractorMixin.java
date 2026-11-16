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
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import io.github.axolotlclient.rendering.*;
import io.github.axolotlclient.rendering.font.Font;
import io.github.axolotlclient.rendering.svg.SVGCache;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
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

@Mixin(GuiGraphicsExtractor.class)
public abstract class GuiGraphicsExtractorMixin implements GuiGraphicsExtractorExtension {

	@Shadow
	public abstract Matrix3x2fStack pose();

	@Shadow
	@Final
	public GuiRenderState guiRenderState;

	@Shadow
	@Final
	private Minecraft minecraft;

	@Shadow
	protected abstract void innerBlit(RenderPipeline pipeline, Identifier atlas, int x0, int x1, int y0, int y1, float u0, float u1, float v0, float v1, int color);

	@Override
	public void axolotlclient_rendering$segment(int x0, int y0, int x1, int y1, int colorX0Y0, int colorX0Y1, int colorX1Y1, int colorX1Y0, float radius) {
		Matrix3x2f pose = new Matrix3x2f(pose());
		var x0f = x0 <= x1 ? x0 - radius : x0 + radius;
		var y0f = y0 <= y1 ? y0 - radius : y0 + radius;
		var x1f = x0 <= x1 ? x1 + radius : x1 - radius;
		var y1f = y0 <= y1 ? y1 + radius : y1 - radius;
		guiRenderState.addGuiElement(new SegmentRenderState(ExRenderPipelines.SEGMENT,
			TextureSetup.noTexture(), pose,
			x0f, y0f, x1f, y1f,
			colorX0Y0, colorX0Y1, colorX1Y1, colorX1Y0,
			radius, ((GuiGraphicsExtractor) (Object) this).peekScissorStack()));
	}

	@Override
	public void axolotlclient_rendering$roundedRectGradientVarying(int x0, int y0, int x1, int y1, int colorTopLeft, int colorBottomLeft, int colorBottomRight, int colorTopRight, float roundingPxTopLeft, float roundingPxBottomLeft, float roundingPxBottomRight, float roundingPxTopRight) {
		guiRenderState.addGuiElement(new ColoredRoundedRectangleRenderState(ExRenderPipelines.ROUNDED_RECT,
			TextureSetup.noTexture(),
			new Matrix3x2f(pose()),
			x0, y0, x1, y1,
			colorTopLeft, colorBottomLeft, colorBottomRight, colorTopRight,
			roundingPxTopLeft, roundingPxBottomLeft, roundingPxBottomRight, roundingPxTopRight,
			((GuiGraphicsExtractor) (Object) this).peekScissorStack()));
	}

	@Override
	public void axolotlclient_rendering$outlineRoundedRectGradientVarying(int x0, int y0, int x1, int y1, int colorTopLeft, int colorBottomLeft, int colorBottomRight, int colorTopRight, float roundingPxTopLeft, float roundingPxBottomLeft, float roundingPxBottomRight, float roundingPxTopRight, float outlineWidth) {
		guiRenderState.addGuiElement(new ColoredRoundedRectangleOutlineRenderState(ExRenderPipelines.ROUNDED_RECT_OUTLINE,
			TextureSetup.noTexture(),
			new Matrix3x2f(pose()),
			x0, y0, x1, y1,
			colorTopLeft, colorBottomLeft, colorBottomRight, colorTopRight,
			roundingPxTopLeft, roundingPxBottomLeft, roundingPxBottomRight, roundingPxTopRight,
			outlineWidth, ((GuiGraphicsExtractor) (Object) this).peekScissorStack()));
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
		this.guiRenderState.addGuiElement(
			new ColoredRoundedRectangleBlitRenderState(
				pipeline,
				TextureSetup.singleTexture(tex.getTextureView(), tex.getSampler()),
				new Matrix3x2f(this.pose()),
				x0, y0, x1, y1,
				u0, u1, v0, v1,
				color, color, color, color,
				roundingPxTopLeft, roundingPxBottomLeft, roundingPxBottomRight, roundingPxTopRight,
				((GuiGraphicsExtractor) (Object) this).peekScissorStack()
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
		this.guiRenderState.addGuiElement(
			new ColoredRoundedRectangleBlitOutlineRenderState(
				pipeline,
				TextureSetup.singleTexture(tex.getTextureView(), tex.getSampler()),
				new Matrix3x2f(this.pose()),
				x0, y0, x1, y1,
				u0, u1, v0, v1,
				color, color, color, color,
				roundingPxTopLeft, roundingPxBottomLeft, roundingPxBottomRight, roundingPxTopRight,
				outlineWidth, ((GuiGraphicsExtractor) (Object) this).peekScissorStack()
			)
		);
	}

	@Override
	public void axolotlclient_rendering$text(Font font, String text, float x, float y, int color, boolean shadow, int origin) {
		font.renderText((GuiGraphicsExtractor) (Object) this, text, x, y, color, shadow, origin);
	}

	@Override
	public void axolotlclient_rendering$text(Font font, FormattedCharSequence text, float x, float y, int color, boolean shadow, int origin) {
		font.renderText((GuiGraphicsExtractor) (Object) this, text, x, y, color, shadow, origin);
	}

	@Override
	public void axolotlclient_rendering$text(Font font, Component text, float x, float y, int color, boolean shadow, int origin) {
		font.renderText((GuiGraphicsExtractor) (Object) this, text, x, y, color, shadow, origin);
	}

	@Override
	public void axolotlclient_rendering$blitSVG(Identifier location, int x, int y, int width, int height, int color) {
		var scale = Minecraft.getInstance().getWindow().getGuiScale();
		var key = SVGCache.getInstance().load(location, width * scale, height * scale);
		innerBlit(RenderPipelines.GUI_TEXTURED, key, x, x + width, y, y + height, 0, 1f, 0f, 1, color);
	}

	@WrapOperation(method = "textRendererForWidget", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;createDefaultTextParameters(F)Lnet/minecraft/client/gui/ActiveTextCollector$Parameters;"))
	private ActiveTextCollector.Parameters allowCustomFontRenderingForWidgets(GuiGraphicsExtractor instance, float v, Operation<ActiveTextCollector.Parameters> original, AbstractWidget widget) {
		var font = ((AbstractWidgetExtension) widget).axolotlclient_rendering$getFont();
		var o = original.call(instance, v);
		if (font != null) return ((ActiveTextCollectorParametersExtension) (Object) o).axolotlclient_rendering$withFont(font);
		return o;
	}

	@Override
	public void axolotlclient_rendering$roundedGaussianBlur(float x, float y, float width, float height, float radius, float blurRadius, float sigma, int tintColor) {
		this.guiRenderState.addGuiElement(new EffectRenderStates.GaussianBlur(BackdropTexture.request(),
			new Matrix3x2f(this.pose()), x, y, width, height, radius, blurRadius, sigma, tintColor, ((GuiGraphicsExtractor) (Object) this).peekScissorStack()));
	}

	@Override
	public void axolotlclient_rendering$liquidGlass(float x, float y, float width, float height, float rounding, int tintColor, int borderColor, float borderWidth, float blurRadius, float distortion) {
		LiquidGlassStyle style = LiquidGlassStyle.create()
			.tint(tintColor, ((tintColor >> 24) & 0xFF) / 255.0f)
			.border(borderColor, borderWidth)
			.blurRadius(blurRadius)
			.refractionDispersion(distortion > 0 ? distortion * 1.5f : 7.0f);
		this.axolotlclient_rendering$liquidGlass(x, y, width, height, rounding, style);
	}

	@Override
	public void axolotlclient_rendering$liquidGlass(float x, float y, float width, float height, float rounding, LiquidGlassStyle style) {
		if (style == null) return;
		this.guiRenderState.addGuiElement(new EffectRenderStates.LiquidGlass(BackdropTexture.request(),
			new Matrix3x2f(this.pose()), x, y, width, height, rounding, style, ((GuiGraphicsExtractor) (Object) this).peekScissorStack()));
	}

	@Override
	public void axolotlclient_rendering$drawMsdfString(io.github.axolotlclient.rendering.font.msdf.MsdfFont font, String text, float x, float y, float size, int color, boolean dropShadow) {
		if (font == null || text == null || text.isEmpty()) return;
		if ((color & 0xFF000000) == 0) color |= 0xFF000000;
		if (dropShadow) {
			int shadowColor = ((color & 0xFCFCFC) >> 2) | (color & 0xFF000000);
			float offset = Math.max(1.0f, size * 0.05f);
			axolotlclient_rendering$submitMsdf(font, text, x + offset, y + offset, size, shadowColor);
		}
		axolotlclient_rendering$submitMsdf(font, text, x, y, size, color);
	}

	@Unique
	private void axolotlclient_rendering$submitMsdf(io.github.axolotlclient.rendering.font.msdf.MsdfFont font, String text, float x, float y, float size, int color) {
		var quads = font.layout(text, x, y, size);
		if (quads.isEmpty()) return;
		this.guiRenderState.addGuiElement(EffectRenderStates.MsdfText.of(font.textureSetup(),
			new Matrix3x2f(this.pose()), quads, color, ((GuiGraphicsExtractor) (Object) this).peekScissorStack()));
	}
}
