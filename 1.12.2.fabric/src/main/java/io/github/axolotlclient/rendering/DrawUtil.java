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

import net.minecraft.client.render.platform.GlStateManager;
import net.minecraft.client.render.platform.MemoryTracker;
import net.minecraft.client.render.vertex.BufferBuilder;
import net.minecraft.client.render.vertex.DefaultVertexFormat;
import net.minecraft.client.render.vertex.Tesselator;
import io.github.axolotlclient.rendering.font.Font;
import io.github.axolotlclient.rendering.svg.SVGCache;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.Window;
import net.minecraft.resource.Identifier;
import net.minecraft.text.Text;
import org.jetbrains.annotations.ApiStatus;
import org.spongepowered.asm.mixin.Unique;

public class DrawUtil implements GuiGraphicsExtension {

	private static final DrawUtil INSTANCE = new DrawUtil();

	public static GuiGraphicsExtension get() {
		return INSTANCE;
	}

	private Window window;

	@ApiStatus.Internal
	void resetWindow() {
		window = null;
	}

	public Window getWindow() {
		if (window == null) {
			window = new Window(Minecraft.getInstance());
		}
		return window;
	}

	@Override
	public int guiWidth() {
		return getWindow().getWidth();
	}

	@Override
	public int guiHeight() {
		return getWindow().getHeight();
	}

	@Unique
	private void fill(float x0, float y0, float x1, float y1, int colorTopLeft, int colorBottomLeft, int colorBottomRight, int colorTopRight) {
		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder bufferBuilder = tesselator.getBuffer();
		GlStateManager.enableBlend();
		GlStateManager.disableTexture();
		GlStateManager.blendFuncSeparate(770, 771, 1, 0);
		GlStateManager.color4f(1, 1, 1, 1);
		colorTopLeft = Rendering.scaleARGB(colorTopLeft);
		colorBottomLeft = Rendering.scaleARGB(colorBottomLeft);
		colorBottomRight = Rendering.scaleARGB(colorBottomRight);
		colorTopRight = Rendering.scaleARGB(colorTopRight);
		bufferBuilder.begin(7, DefaultVertexFormat.POSITION_COLOR);
		bufferBuilder.vertex(x1, y0, 0.0).color((colorTopRight >> 16 & 0xFF) / 255f, (colorTopRight >> 8 & 0xFF) / 255f, (colorTopRight & 0xFF) / 255f, (colorTopRight >> 24 & 0xFF) / 255f).nextVertex();
		bufferBuilder.vertex(x0, y0, 0.0).color((colorTopLeft >> 16 & 0xFF) / 255f, (colorTopLeft >> 8 & 0xFF) / 255f, (colorTopLeft & 0xFF) / 255f, (colorTopLeft >> 24 & 0xFF) / 255f).nextVertex();
		bufferBuilder.vertex(x0, y1, 0.0).color((colorBottomLeft >> 16 & 0xFF) / 255f, (colorBottomLeft >> 8 & 0xFF) / 255f, (colorBottomLeft & 0xFF) / 255f, (colorBottomLeft >> 24 & 0xFF) / 255f).nextVertex();
		bufferBuilder.vertex(x1, y1, 0.0).color((colorBottomRight >> 16 & 0xFF) / 255f, (colorBottomRight >> 8 & 0xFF) / 255f, (colorBottomRight & 0xFF) / 255f, (colorBottomRight >> 24 & 0xFF) / 255f).nextVertex();
		tesselator.end();
		GlStateManager.enableTexture();
		GlStateManager.disableBlend();
	}

	@Override
	public void axolotlclient_rendering$segment(int x0, int y0, int x1, int y1, int colorX0Y0, int colorX0Y1, int colorX1Y1, int colorX1Y0, float radius) {
		var x0f = x0 <= x1 ? x0 - radius : x0 + radius;
		var y0f = y0 <= y1 ? y0 - radius : y0 + radius;
		var x1f = x0 <= x1 ? x1 + radius : x1 - radius;
		var y1f = y0 <= y1 ? y1 + radius : y1 - radius;
		var width = (x1f - x0f);
		var height = (y1f - y0f);
		var shader = ExRenderPipelines.SEGMENT_PROGRAM;
		shader.bind();
		shader.setUniform("Rounding",
			/*radiusBottomRight*/ 0,
			/*radiusTopRight*/ 0,
			/*radiusBottomLeft*/ 0,
			/*radiusTopLeft*/ radius);
		var buf = MemoryTracker.createFloatBuffer(16);
		/*buf.put(new float[]{
			width, 0, width, height,
			0, 0, width, height,
			0, height, width, height,
			width, height, width, height
		});*/
		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder bufferBuilder = tesselator.getBuffer();
		GlStateManager.enableBlend();
		GlStateManager.disableTexture();
		GlStateManager.blendFuncSeparate(770, 771, 1, 0);
		GlStateManager.color4f(1, 1, 1, 1);
		var colorTL = Rendering.scaleARGB(colorX0Y0);
		var colorBL = Rendering.scaleARGB(colorX0Y1);
		var colorBR = Rendering.scaleARGB(colorX1Y1);
		var colorTR = Rendering.scaleARGB(colorX1Y0);
		bufferBuilder.begin(7, DefaultVertexFormat.POSITION_COLOR);
		Runnable addX1Y0 = () -> bufferBuilder.vertex(x1f, y0f, 0.0).color((colorTR >> 16 & 0xFF) / 255f, (colorTR >> 8 & 0xFF) / 255f, (colorTR & 0xFF) / 255f, (colorTR >> 24 & 0xFF) / 255f).nextVertex();
		Runnable addX0Y0 = () -> bufferBuilder.vertex(x0f, y0f, 0.0).color((colorTL >> 16 & 0xFF) / 255f, (colorTL >> 8 & 0xFF) / 255f, (colorTL & 0xFF) / 255f, (colorTL >> 24 & 0xFF) / 255f).nextVertex();
		Runnable addX0Y1 = () -> bufferBuilder.vertex(x0f, y1f, 0.0).color((colorBL >> 16 & 0xFF) / 255f, (colorBL >> 8 & 0xFF) / 255f, (colorBL & 0xFF) / 255f, (colorBL >> 24 & 0xFF) / 255f).nextVertex();
		Runnable addX1Y1 = () -> bufferBuilder.vertex(x1f, y1f, 0.0).color((colorBR >> 16 & 0xFF) / 255f, (colorBR >> 8 & 0xFF) / 255f, (colorBR & 0xFF) / 255f, (colorBR >> 24 & 0xFF) / 255f).nextVertex();
		if (x0 <= x1 && y0 <= y1) {
			buf.put(new float[]{
				width, 0, width, height,
				0, 0, width, height,
				0, height, width, height,
				width, height, width, height
			});
			buf.flip();
			shader.setUniform("Dimensions", buf);
			addX1Y0.run();
			addX0Y0.run();
			addX0Y1.run();
			addX1Y1.run();
		} else if (x0 > x1 && y0 <= y1) {
			buf.put(new float[]{
				width, height, width, height,
				0, height, width, height,
				0, 0, width, height,
				width, 0, width, height
			});
			buf.flip();
			shader.setUniform("Dimensions", buf);
			addX1Y1.run();
			addX0Y1.run();
			addX0Y0.run();
			addX1Y0.run();
		} else if (x0 <= x1) {
			buf.put(new float[]{
				0, height, width, height,
				0, 0, width, height,
				width, 0, width, height,
				width, height, width, height
			});
			buf.flip();
			shader.setUniform("Dimensions", buf);
			addX0Y1.run();
			addX0Y0.run();
			addX1Y0.run();
			addX1Y1.run();
		} else {
			buf.put(new float[]{
				0, 0, width, height,
				0, height, width, height,
				width, height, width, height,
				width, 0, width, height,
			});
			buf.flip();
			shader.setUniform("Dimensions", buf);
			addX0Y0.run();
			addX0Y1.run();
			addX1Y1.run();
			addX1Y0.run();
		}
		tesselator.end();
		GlStateManager.enableTexture();
		GlStateManager.disableBlend();
		shader.unbind();
	}

	@Override
	public void axolotlclient_rendering$roundedRectGradientVarying(int x0, int y0, int x1, int y1, int colorTopLeft, int colorBottomLeft, int colorBottomRight, int colorTopRight, float roundingPxTopLeft, float roundingPxBottomLeft, float roundingPxBottomRight, float roundingPxTopRight) {
		int width = Math.abs(x1 - x0);
		int height = Math.abs(y1 - y0);
		var shader = ExRenderPipelines.ROUNDED_RECT_PROGRAM;
		shader.bind();
		shader.setUniform("Rounding",
			/*radiusBottomRight*/ roundingPxBottomRight,
			/*radiusTopRight*/ roundingPxTopRight,
			/*radiusBottomLeft*/ roundingPxBottomLeft,
			/*radiusTopLeft*/ roundingPxTopLeft);
		var buf = MemoryTracker.createFloatBuffer(16);
		buf.put(new float[]{
			width, 0, width, height,
			0, 0, width, height,
			0, height, width, height,
			width, height, width, height
		});
		buf.flip();
		shader.setUniform("Dimensions", buf);
		GlStateManager.color4f(1, 1, 1, 1);
		fill(x0, y0, x1, y1, colorTopLeft, colorBottomLeft, colorBottomRight, colorTopRight);
		shader.unbind();
	}

	@Override
	public void axolotlclient_rendering$outlineRoundedRectGradientVarying(int x0, int y0, int x1, int y1, int colorTopLeft, int colorBottomLeft, int colorBottomRight, int colorTopRight, float roundingPxTopLeft, float roundingPxBottomLeft, float roundingPxBottomRight, float roundingPxTopRight, float outlineWidth) {
		int width = Math.abs(x1 - x0);
		int height = Math.abs(y1 - y0);
		var shader = ExRenderPipelines.ROUNDED_RECT_OUTLINE_PROGRAM;
		shader.bind();
		shader.setUniform("Rounding",
			/*radiusBottomRight*/ roundingPxBottomRight,
			/*radiusTopRight*/ roundingPxTopRight,
			/*radiusBottomLeft*/ roundingPxBottomLeft,
			/*radiusTopLeft*/ roundingPxTopLeft);
		var buf = MemoryTracker.createFloatBuffer(16);
		buf.put(new float[]{
			width, 0, width, height,
			0, 0, width, height,
			0, height, width, height,
			width, height, width, height
		});
		buf.flip();
		shader.setUniform("Dimensions", buf);
		shader.setUniform("OutlineWidth", outlineWidth);
		GlStateManager.color4f(1, 1, 1, 1);
		fill(x0, y0, x1, y1, colorTopLeft, colorBottomLeft, colorBottomRight, colorTopRight);
		shader.unbind();
	}

	@Override
	public void axolotlclient_rendering$roundedBlitVarying(Identifier location, int x, int y, float u, float v, int width, int height, int uWidth, int vHeight, int textureWidth, int textureHeight, int color, float roundingPxTopLeft, float roundingPxBottomLeft, float roundingPxBottomRight, float roundingPxTopRight) {
		this.innerBlit(location, x, x + width, y, y + height, (u + 0.0F) / textureWidth, (u + uWidth) / textureWidth, (v + 0.0F) / textureHeight, (v + vHeight) / textureHeight, color, roundingPxTopLeft, roundingPxBottomLeft, roundingPxBottomRight, roundingPxTopRight);
	}

	@Override
	public void axolotlclient_rendering$roundedBlitVarying(Identifier atlas, int x0, int y0, int x1, int y1, float u0, float u1, float v0, float v1, int color, float roundingPxTopLeft, float roundingPxBottomLeft, float roundingPxBottomRight, float roundingPxTopRight) {
		this.innerBlit(atlas, x0, x1, y0, y1, u0, u1, v0, v1, color, roundingPxTopLeft, roundingPxBottomLeft, roundingPxBottomRight, roundingPxTopRight);
	}

	@Unique
	private void innerBlit(Identifier atlas, int x0, int x1, int y0, int y1, float u0, float u1, float v0, float v1, int color, float roundingPxTopLeft, float roundingPxBottomLeft, float roundingPxBottomRight, float roundingPxTopRight) {
		var tex = Minecraft.getInstance().getTextureManager().get(atlas);
		if (tex == null) return;
		int width = Math.abs(x1 - x0);
		int height = Math.abs(y1 - y0);
		var shader = ExRenderPipelines.ROUNDED_RECT_TEX_PROGRAM;
		GlStateManager.enableBlend();
		Minecraft.getInstance().getTextureManager().bind(atlas);
		shader.bind();
		shader.setUniform("Rounding",
			/*radiusBottomRight*/ roundingPxBottomRight,
			/*radiusTopRight*/ roundingPxTopRight,
			/*radiusBottomLeft*/ roundingPxBottomLeft,
			/*radiusTopLeft*/ roundingPxTopLeft);
		var buf = MemoryTracker.createFloatBuffer(16);
		buf.put(new float[]{
			width, 0, width, height,
			0, 0, width, height,
			0, height, width, height,
			width, height, width, height
		});
		buf.flip();
		shader.setUniform("Dimensions", buf);
		GlStateManager.color4f((color >> 16 & 0xFF) / 255f, (color >> 8 & 0xFF) / 255f, (color & 0xFF) / 255f, (color >> 24 & 0xFF) / 255f);
		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder bufferBuilder = tesselator.getBuffer();
		bufferBuilder.begin(7, DefaultVertexFormat.POSITION_TEX);
		bufferBuilder.vertex(x0, y1, 0.0).texture(u0, v1).nextVertex();
		bufferBuilder.vertex(x1, y1, 0.0).texture(u1, v1).nextVertex();
		bufferBuilder.vertex(x1, y0, 0.0).texture(u1, v0).nextVertex();
		bufferBuilder.vertex(x0, y0, 0.0).texture(u0, v0).nextVertex();
		tesselator.end();
		shader.unbind();
	}

	@Override
	public void axolotlclient_rendering$outlineRoundedBlitVarying(Identifier location, int x, int y, float u, float v, int width, int height, int uWidth, int vHeight, int textureWidth, int textureHeight, int color, float roundingPxTopLeft, float roundingPxBottomLeft, float roundingPxBottomRight, float roundingPxTopRight, float outlineWidth) {
		this.innerBlit(location, x, x + width, y, y + height, u / textureWidth, (u + uWidth) / textureWidth, v / textureHeight, (v + vHeight) / textureHeight, color, roundingPxTopLeft, roundingPxBottomLeft, roundingPxBottomRight, roundingPxTopRight, outlineWidth);
	}

	@Override
	public void axolotlclient_rendering$outlineRoundedBlitVarying(Identifier atlas, int x0, int y0, int x1, int y1, float u0, float u1, float v0, float v1, int color, float roundingPxTopLeft, float roundingPxBottomLeft, float roundingPxBottomRight, float roundingPxTopRight, float outlineWidth) {
		this.innerBlit(atlas, x0, x1, y0, y1, u0, u1, v0, v1, color, roundingPxTopLeft, roundingPxBottomLeft, roundingPxBottomRight, roundingPxTopRight, outlineWidth);
	}

	@Unique
	private void innerBlit(Identifier atlas, int x0, int x1, int y0, int y1, float u0, float u1, float v0, float v1, int color, float roundingPxTopLeft, float roundingPxBottomLeft, float roundingPxBottomRight, float roundingPxTopRight, float outlineWidth) {
		var tex = Minecraft.getInstance().getTextureManager().get(atlas);
		if (tex == null) return;
		int width = Math.abs(x1 - x0);
		int height = Math.abs(y1 - y0);
		var shader = ExRenderPipelines.ROUNDED_RECT_TEX_OUTLINE_PROGRAM;
		GlStateManager.enableBlend();
		Minecraft.getInstance().getTextureManager().bind(atlas);
		shader.bind();
		shader.setUniform("Rounding",
			/*radiusBottomRight*/ roundingPxBottomRight,
			/*radiusTopRight*/ roundingPxTopRight,
			/*radiusBottomLeft*/ roundingPxBottomLeft,
			/*radiusTopLeft*/ roundingPxTopLeft);
		var buf = MemoryTracker.createFloatBuffer(16);
		buf.put(new float[]{
			width, 0, width, height,
			0, 0, width, height,
			0, height, width, height,
			width, height, width, height
		});
		buf.flip();
		shader.setUniform("Dimensions", buf);
		shader.setUniform("OutlineWidth", outlineWidth);
		GlStateManager.color4f((color >> 16 & 0xFF) / 255f, (color >> 8 & 0xFF) / 255f, (color & 0xFF) / 255f, (color >> 24 & 0xFF) / 255f);
		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder bufferBuilder = tesselator.getBuffer();
		bufferBuilder.begin(7, DefaultVertexFormat.POSITION_TEX);
		bufferBuilder.vertex(x0, y1, 0.0).texture(u0, v1).nextVertex();
		bufferBuilder.vertex(x1, y1, 0.0).texture(u1, v1).nextVertex();
		bufferBuilder.vertex(x1, y0, 0.0).texture(u1, v0).nextVertex();
		bufferBuilder.vertex(x0, y0, 0.0).texture(u0, v0).nextVertex();
		tesselator.end();
		shader.unbind();
	}

	@Override
	public void axolotlclient_rendering$drawString(Font font, String text, float x, float y, int color, boolean shadow, int origin) {
		font.renderText(text, x, y, color, shadow, origin);
	}

	@Override
	public void axolotlclient_rendering$drawString(Font font, Text text, float x, float y, int color, boolean shadow, int origin) {
		font.renderText(text, x, y, color, shadow, origin);
	}

	@Override
	public void axolotlclient_rendering$blitSVG(Identifier location, int x, int y, int width, int height, int color) {
		var scale = new Window(Minecraft.getInstance()).getScale();
		var key = SVGCache.getInstance().load(location, (float) (width * scale), (float) (height * scale));
		GlStateManager.enableBlend();
		Minecraft.getInstance().getTextureManager().bind(key);
		GlStateManager.color4f((color >> 16 & 0xFF) / 255f, (color >> 8 & 0xFF) / 255f, (color & 0xFF) / 255f, (color >> 24 & 0xFF) / 255f);
		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder bufferBuilder = tesselator.getBuffer();
		bufferBuilder.begin(7, DefaultVertexFormat.POSITION_TEX);
		bufferBuilder.vertex(x, y+height, 0.0).texture(0, 1f).nextVertex();
		bufferBuilder.vertex(x+width, y+height, 0.0).texture(1f, 1f).nextVertex();
		bufferBuilder.vertex(x+width, y, 0.0).texture(1f, 0).nextVertex();
		bufferBuilder.vertex(x, y, 0.0).texture(0, 0).nextVertex();
		tesselator.end();
	}

	@Override
	public void axolotlclient_rendering$roundedGaussianBlur(float x, float y, float width, float height, float radius, float blurRadius, float sigma, int tintColor) {
		var shader = ExRenderPipelines.GAUSSIAN_BLUR_PROGRAM;
		if (shader == null) return;
		var mc = Minecraft.getInstance();
		var target = mc.getRenderTarget();
		if (target != null) {
			GlStateManager.bindTexture(target.colorTextureId);
		}
		GlStateManager.enableBlend();
		GlStateManager.blendFuncSeparate(770, 771, 1, 0);
		shader.bind();
		shader.setUniform("Dimensions", x, y, width, height);
		shader.setUniform("Rounding", radius, radius, radius, radius);
		var win = getWindow();
		shader.setUniform("ScreenSize", (float) win.getWidth(), (float) win.getHeight());
		shader.setUniform("BlurRadius", blurRadius);
		shader.setUniform("Sigma", sigma);
		int c = Rendering.scaleARGB(tintColor);
		GlStateManager.color4f((c >> 16 & 0xFF) / 255f, (c >> 8 & 0xFF) / 255f, (c & 0xFF) / 255f, (c >> 24 & 0xFF) / 255f);

		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder bufferBuilder = tesselator.getBuffer();
		bufferBuilder.begin(7, DefaultVertexFormat.POSITION_TEX);
		bufferBuilder.vertex(x, y + height, 0.0).texture(0.0, 0.0).nextVertex();
		bufferBuilder.vertex(x + width, y + height, 0.0).texture(1.0, 0.0).nextVertex();
		bufferBuilder.vertex(x + width, y, 0.0).texture(1.0, 1.0).nextVertex();
		bufferBuilder.vertex(x, y, 0.0).texture(0.0, 1.0).nextVertex();
		tesselator.end();
		shader.unbind();
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
		var shader = ExRenderPipelines.LIQUID_GLASS_PROGRAM;
		if (shader == null || style == null) return;
		var mc = Minecraft.getInstance();
		var target = mc.getRenderTarget();
		if (target != null) {
			GlStateManager.bindTexture(target.colorTextureId);
		}
		GlStateManager.enableBlend();
		GlStateManager.blendFuncSeparate(770, 771, 1, 0);
		shader.bind();
		shader.setUniform("Dimensions", x, y, width, height);
		shader.setUniform("Rounding", rounding, rounding, rounding, rounding);
		var win = getWindow();
		shader.setUniform("ScreenSize", (float) win.getWidth(), (float) win.getHeight());
		shader.setUniform("BlurRadius", style.getBlurRadius());
		shader.setUniform("Optics0", style.getRefThickness(), style.getRefFactor(), style.getRefDispersion(), style.getRefFresnelRange());
		shader.setUniform("Optics1", style.getRefFresnelHardness(), style.getRefFresnelFactor(), style.getGlareRange(), style.getGlareHardness());

		int tc = Rendering.scaleARGB(style.getTintColor());
		float ta = style.getTintAlpha() > 0 ? style.getTintAlpha() : ((tc >> 24 & 0xFF) / 255f);
		shader.setUniform("GlassTint", (tc >> 16 & 0xFF) / 255f, (tc >> 8 & 0xFF) / 255f, (tc & 0xFF) / 255f, ta);

		int bc = Rendering.scaleARGB(style.getBorderColor());
		shader.setUniform("BorderColor", (bc >> 16 & 0xFF) / 255f, (bc >> 8 & 0xFF) / 255f, (bc & 0xFF) / 255f, (bc >> 24 & 0xFF) / 255f);
		shader.setUniform("BorderWidth", style.getBorderWidth());
		GlStateManager.color4f(1f, 1f, 1f, 1f);

		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder bufferBuilder = tesselator.getBuffer();
		bufferBuilder.begin(7, DefaultVertexFormat.POSITION_TEX);
		bufferBuilder.vertex(x, y + height, 0.0).texture(0.0, 0.0).nextVertex();
		bufferBuilder.vertex(x + width, y + height, 0.0).texture(1.0, 0.0).nextVertex();
		bufferBuilder.vertex(x + width, y, 0.0).texture(1.0, 1.0).nextVertex();
		bufferBuilder.vertex(x, y, 0.0).texture(0.0, 1.0).nextVertex();
		tesselator.end();
		shader.unbind();
	}

	@Override
	public void axolotlclient_rendering$drawMsdfString(io.github.axolotlclient.rendering.font.msdf.MsdfFont font, String text, float x, float y, float size, int color, boolean dropShadow) {
		if (font == null || text == null || text.isEmpty()) return;
		if (dropShadow) {
			font.drawWithShadow(text, x, y, size, color);
		} else {
			font.draw(text, x, y, size, color);
		}
	}
}
