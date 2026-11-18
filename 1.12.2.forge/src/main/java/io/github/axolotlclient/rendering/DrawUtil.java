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

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.Tessellator;
import io.github.axolotlclient.rendering.font.Font;
import io.github.axolotlclient.rendering.svg.SVGCache;
import net.minecraft.client.Minecraft;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.opengl.GL11;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import org.jetbrains.annotations.ApiStatus;

public class DrawUtil implements GuiGraphicsExtension {

	private static final DrawUtil INSTANCE = new DrawUtil();

	public static GuiGraphicsExtension get() {
		return INSTANCE;
	}

	private ScaledResolution window;

	@ApiStatus.Internal
	void resetWindow() {
		window = null;
	}

	public ScaledResolution getWindow() {
		if (window == null) {
			window = new ScaledResolution(Minecraft.getMinecraft());
		}
		return window;
	}

	@Override
	public int guiWidth() {
		return getWindow().getScaledWidth();
	}

	@Override
	public int guiHeight() {
		return getWindow().getScaledHeight();
	}

	private void fill(float x0, float y0, float x1, float y1, int colorTopLeft, int colorBottomLeft, int colorBottomRight, int colorTopRight) {
		Tessellator tesselator = Tessellator.getInstance();
		BufferBuilder bufferBuilder = tesselator.getBuffer();
		GlStateManager.enableBlend();
		GlStateManager.disableTexture2D();
		GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
		GlStateManager.color(1, 1, 1, 1);
		colorTopLeft = Rendering.scaleARGB(colorTopLeft);
		colorBottomLeft = Rendering.scaleARGB(colorBottomLeft);
		colorBottomRight = Rendering.scaleARGB(colorBottomRight);
		colorTopRight = Rendering.scaleARGB(colorTopRight);
		bufferBuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
		bufferBuilder.pos(x1, y0, 0.0).color((colorTopRight >> 16 & 0xFF) / 255f, (colorTopRight >> 8 & 0xFF) / 255f, (colorTopRight & 0xFF) / 255f, (colorTopRight >> 24 & 0xFF) / 255f).endVertex();
		bufferBuilder.pos(x0, y0, 0.0).color((colorTopLeft >> 16 & 0xFF) / 255f, (colorTopLeft >> 8 & 0xFF) / 255f, (colorTopLeft & 0xFF) / 255f, (colorTopLeft >> 24 & 0xFF) / 255f).endVertex();
		bufferBuilder.pos(x0, y1, 0.0).color((colorBottomLeft >> 16 & 0xFF) / 255f, (colorBottomLeft >> 8 & 0xFF) / 255f, (colorBottomLeft & 0xFF) / 255f, (colorBottomLeft >> 24 & 0xFF) / 255f).endVertex();
		bufferBuilder.pos(x1, y1, 0.0).color((colorBottomRight >> 16 & 0xFF) / 255f, (colorBottomRight >> 8 & 0xFF) / 255f, (colorBottomRight & 0xFF) / 255f, (colorBottomRight >> 24 & 0xFF) / 255f).endVertex();
		tesselator.draw();
		GlStateManager.enableTexture2D();
		GlStateManager.disableBlend();
	}

	@Override
	public void axolotlclient_rendering$segment(int x0, int y0, int x1, int y1, int colorX0Y0, int colorX0Y1, int colorX1Y1, int colorX1Y0, float radius) {
		float x0f = x0 <= x1 ? x0 - radius : x0 + radius;
		float y0f = y0 <= y1 ? y0 - radius : y0 + radius;
		float x1f = x0 <= x1 ? x1 + radius : x1 - radius;
		float y1f = y0 <= y1 ? y1 + radius : y1 - radius;
		float width = (x1f - x0f);
		float height = (y1f - y0f);
		ShaderManager.Shader shader = ExRenderPipelines.SEGMENT_PROGRAM;
		shader.bind();
		shader.setUniform("Rounding",
			/*radiusBottomRight*/ 0,
			/*radiusTopRight*/ 0,
			/*radiusBottomLeft*/ 0,
			/*radiusTopLeft*/ radius);
		java.nio.FloatBuffer buf = GLAllocation.createDirectFloatBuffer(16);
		/*buf.put(new float[]{
			width, 0, width, height,
			0, 0, width, height,
			0, height, width, height,
			width, height, width, height
		});*/
		Tessellator tesselator = Tessellator.getInstance();
		BufferBuilder bufferBuilder = tesselator.getBuffer();
		GlStateManager.enableBlend();
		GlStateManager.disableTexture2D();
		GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
		GlStateManager.color(1, 1, 1, 1);
		int colorTL = Rendering.scaleARGB(colorX0Y0);
		int colorBL = Rendering.scaleARGB(colorX0Y1);
		int colorBR = Rendering.scaleARGB(colorX1Y1);
		int colorTR = Rendering.scaleARGB(colorX1Y0);
		bufferBuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
		Runnable addX1Y0 = () -> bufferBuilder.pos(x1f, y0f, 0.0).color((colorTR >> 16 & 0xFF) / 255f, (colorTR >> 8 & 0xFF) / 255f, (colorTR & 0xFF) / 255f, (colorTR >> 24 & 0xFF) / 255f).endVertex();
		Runnable addX0Y0 = () -> bufferBuilder.pos(x0f, y0f, 0.0).color((colorTL >> 16 & 0xFF) / 255f, (colorTL >> 8 & 0xFF) / 255f, (colorTL & 0xFF) / 255f, (colorTL >> 24 & 0xFF) / 255f).endVertex();
		Runnable addX0Y1 = () -> bufferBuilder.pos(x0f, y1f, 0.0).color((colorBL >> 16 & 0xFF) / 255f, (colorBL >> 8 & 0xFF) / 255f, (colorBL & 0xFF) / 255f, (colorBL >> 24 & 0xFF) / 255f).endVertex();
		Runnable addX1Y1 = () -> bufferBuilder.pos(x1f, y1f, 0.0).color((colorBR >> 16 & 0xFF) / 255f, (colorBR >> 8 & 0xFF) / 255f, (colorBR & 0xFF) / 255f, (colorBR >> 24 & 0xFF) / 255f).endVertex();
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
		tesselator.draw();
		GlStateManager.enableTexture2D();
		GlStateManager.disableBlend();
		shader.unbind();
	}

	@Override
	public void axolotlclient_rendering$roundedRectGradientVarying(int x0, int y0, int x1, int y1, int colorTopLeft, int colorBottomLeft, int colorBottomRight, int colorTopRight, float roundingPxTopLeft, float roundingPxBottomLeft, float roundingPxBottomRight, float roundingPxTopRight) {
		int width = Math.abs(x1 - x0);
		int height = Math.abs(y1 - y0);
		ShaderManager.Shader shader = ExRenderPipelines.ROUNDED_RECT_PROGRAM;
		shader.bind();
		shader.setUniform("Rounding",
			/*radiusBottomRight*/ roundingPxBottomRight,
			/*radiusTopRight*/ roundingPxTopRight,
			/*radiusBottomLeft*/ roundingPxBottomLeft,
			/*radiusTopLeft*/ roundingPxTopLeft);
		java.nio.FloatBuffer buf = GLAllocation.createDirectFloatBuffer(16);
		buf.put(new float[]{
			width, 0, width, height,
			0, 0, width, height,
			0, height, width, height,
			width, height, width, height
		});
		buf.flip();
		shader.setUniform("Dimensions", buf);
		GlStateManager.color(1, 1, 1, 1);
		fill(x0, y0, x1, y1, colorTopLeft, colorBottomLeft, colorBottomRight, colorTopRight);
		shader.unbind();
	}

	@Override
	public void axolotlclient_rendering$outlineRoundedRectGradientVarying(int x0, int y0, int x1, int y1, int colorTopLeft, int colorBottomLeft, int colorBottomRight, int colorTopRight, float roundingPxTopLeft, float roundingPxBottomLeft, float roundingPxBottomRight, float roundingPxTopRight, float outlineWidth) {
		int width = Math.abs(x1 - x0);
		int height = Math.abs(y1 - y0);
		ShaderManager.Shader shader = ExRenderPipelines.ROUNDED_RECT_OUTLINE_PROGRAM;
		shader.bind();
		shader.setUniform("Rounding",
			/*radiusBottomRight*/ roundingPxBottomRight,
			/*radiusTopRight*/ roundingPxTopRight,
			/*radiusBottomLeft*/ roundingPxBottomLeft,
			/*radiusTopLeft*/ roundingPxTopLeft);
		java.nio.FloatBuffer buf = GLAllocation.createDirectFloatBuffer(16);
		buf.put(new float[]{
			width, 0, width, height,
			0, 0, width, height,
			0, height, width, height,
			width, height, width, height
		});
		buf.flip();
		shader.setUniform("Dimensions", buf);
		shader.setUniform("OutlineWidth", outlineWidth);
		GlStateManager.color(1, 1, 1, 1);
		fill(x0, y0, x1, y1, colorTopLeft, colorBottomLeft, colorBottomRight, colorTopRight);
		shader.unbind();
	}

	@Override
	public void axolotlclient_rendering$roundedBlitVarying(ResourceLocation location, int x, int y, float u, float v, int width, int height, int uWidth, int vHeight, int textureWidth, int textureHeight, int color, float roundingPxTopLeft, float roundingPxBottomLeft, float roundingPxBottomRight, float roundingPxTopRight) {
		this.innerBlit(location, x, x + width, y, y + height, (u + 0.0F) / textureWidth, (u + uWidth) / textureWidth, (v + 0.0F) / textureHeight, (v + vHeight) / textureHeight, color, roundingPxTopLeft, roundingPxBottomLeft, roundingPxBottomRight, roundingPxTopRight);
	}

	@Override
	public void axolotlclient_rendering$roundedBlitVarying(ResourceLocation atlas, int x0, int y0, int x1, int y1, float u0, float u1, float v0, float v1, int color, float roundingPxTopLeft, float roundingPxBottomLeft, float roundingPxBottomRight, float roundingPxTopRight) {
		this.innerBlit(atlas, x0, x1, y0, y1, u0, u1, v0, v1, color, roundingPxTopLeft, roundingPxBottomLeft, roundingPxBottomRight, roundingPxTopRight);
	}

	private void innerBlit(ResourceLocation atlas, int x0, int x1, int y0, int y1, float u0, float u1, float v0, float v1, int color, float roundingPxTopLeft, float roundingPxBottomLeft, float roundingPxBottomRight, float roundingPxTopRight) {
		net.minecraft.client.renderer.texture.ITextureObject tex = Minecraft.getMinecraft().getTextureManager().getTexture(atlas);
		if (tex == null) return;
		int width = Math.abs(x1 - x0);
		int height = Math.abs(y1 - y0);
		ShaderManager.Shader shader = ExRenderPipelines.ROUNDED_RECT_TEX_PROGRAM;
		GlStateManager.enableBlend();
		Minecraft.getMinecraft().getTextureManager().bindTexture(atlas);
		shader.bind();
		shader.setUniform("Rounding",
			/*radiusBottomRight*/ roundingPxBottomRight,
			/*radiusTopRight*/ roundingPxTopRight,
			/*radiusBottomLeft*/ roundingPxBottomLeft,
			/*radiusTopLeft*/ roundingPxTopLeft);
		java.nio.FloatBuffer buf = GLAllocation.createDirectFloatBuffer(16);
		buf.put(new float[]{
			width, 0, width, height,
			0, 0, width, height,
			0, height, width, height,
			width, height, width, height
		});
		buf.flip();
		shader.setUniform("Dimensions", buf);
		GlStateManager.color((color >> 16 & 0xFF) / 255f, (color >> 8 & 0xFF) / 255f, (color & 0xFF) / 255f, (color >> 24 & 0xFF) / 255f);
		Tessellator tesselator = Tessellator.getInstance();
		BufferBuilder bufferBuilder = tesselator.getBuffer();
		bufferBuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
		bufferBuilder.pos(x0, y1, 0.0).tex(u0, v1).endVertex();
		bufferBuilder.pos(x1, y1, 0.0).tex(u1, v1).endVertex();
		bufferBuilder.pos(x1, y0, 0.0).tex(u1, v0).endVertex();
		bufferBuilder.pos(x0, y0, 0.0).tex(u0, v0).endVertex();
		tesselator.draw();
		shader.unbind();
	}

	@Override
	public void axolotlclient_rendering$outlineRoundedBlitVarying(ResourceLocation location, int x, int y, float u, float v, int width, int height, int uWidth, int vHeight, int textureWidth, int textureHeight, int color, float roundingPxTopLeft, float roundingPxBottomLeft, float roundingPxBottomRight, float roundingPxTopRight, float outlineWidth) {
		this.innerBlit(location, x, x + width, y, y + height, u / textureWidth, (u + uWidth) / textureWidth, v / textureHeight, (v + vHeight) / textureHeight, color, roundingPxTopLeft, roundingPxBottomLeft, roundingPxBottomRight, roundingPxTopRight, outlineWidth);
	}

	@Override
	public void axolotlclient_rendering$outlineRoundedBlitVarying(ResourceLocation atlas, int x0, int y0, int x1, int y1, float u0, float u1, float v0, float v1, int color, float roundingPxTopLeft, float roundingPxBottomLeft, float roundingPxBottomRight, float roundingPxTopRight, float outlineWidth) {
		this.innerBlit(atlas, x0, x1, y0, y1, u0, u1, v0, v1, color, roundingPxTopLeft, roundingPxBottomLeft, roundingPxBottomRight, roundingPxTopRight, outlineWidth);
	}

	private void innerBlit(ResourceLocation atlas, int x0, int x1, int y0, int y1, float u0, float u1, float v0, float v1, int color, float roundingPxTopLeft, float roundingPxBottomLeft, float roundingPxBottomRight, float roundingPxTopRight, float outlineWidth) {
		net.minecraft.client.renderer.texture.ITextureObject tex = Minecraft.getMinecraft().getTextureManager().getTexture(atlas);
		if (tex == null) return;
		int width = Math.abs(x1 - x0);
		int height = Math.abs(y1 - y0);
		ShaderManager.Shader shader = ExRenderPipelines.ROUNDED_RECT_TEX_OUTLINE_PROGRAM;
		GlStateManager.enableBlend();
		Minecraft.getMinecraft().getTextureManager().bindTexture(atlas);
		shader.bind();
		shader.setUniform("Rounding",
			/*radiusBottomRight*/ roundingPxBottomRight,
			/*radiusTopRight*/ roundingPxTopRight,
			/*radiusBottomLeft*/ roundingPxBottomLeft,
			/*radiusTopLeft*/ roundingPxTopLeft);
		java.nio.FloatBuffer buf = GLAllocation.createDirectFloatBuffer(16);
		buf.put(new float[]{
			width, 0, width, height,
			0, 0, width, height,
			0, height, width, height,
			width, height, width, height
		});
		buf.flip();
		shader.setUniform("Dimensions", buf);
		shader.setUniform("OutlineWidth", outlineWidth);
		GlStateManager.color((color >> 16 & 0xFF) / 255f, (color >> 8 & 0xFF) / 255f, (color & 0xFF) / 255f, (color >> 24 & 0xFF) / 255f);
		Tessellator tesselator = Tessellator.getInstance();
		BufferBuilder bufferBuilder = tesselator.getBuffer();
		bufferBuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
		bufferBuilder.pos(x0, y1, 0.0).tex(u0, v1).endVertex();
		bufferBuilder.pos(x1, y1, 0.0).tex(u1, v1).endVertex();
		bufferBuilder.pos(x1, y0, 0.0).tex(u1, v0).endVertex();
		bufferBuilder.pos(x0, y0, 0.0).tex(u0, v0).endVertex();
		tesselator.draw();
		shader.unbind();
	}

	@Override
	public void axolotlclient_rendering$drawString(Font font, String text, float x, float y, int color, boolean shadow, int origin) {
		font.renderText(text, x, y, color, shadow, origin);
	}

	@Override
	public void axolotlclient_rendering$drawString(Font font, ITextComponent text, float x, float y, int color, boolean shadow, int origin) {
		font.renderText(text, x, y, color, shadow, origin);
	}

	@Override
	public void axolotlclient_rendering$blitSVG(ResourceLocation location, int x, int y, int width, int height, int color) {
		int scale = getWindow().getScaleFactor();
		ResourceLocation key = SVGCache.getInstance().load(location, (float) (width * scale), (float) (height * scale));
		GlStateManager.enableBlend();
		Minecraft.getMinecraft().getTextureManager().bindTexture(key);
		GlStateManager.color((color >> 16 & 0xFF) / 255f, (color >> 8 & 0xFF) / 255f, (color & 0xFF) / 255f, (color >> 24 & 0xFF) / 255f);
		Tessellator tesselator = Tessellator.getInstance();
		BufferBuilder bufferBuilder = tesselator.getBuffer();
		bufferBuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
		bufferBuilder.pos(x, y+height, 0.0).tex(0, 1f).endVertex();
		bufferBuilder.pos(x+width, y+height, 0.0).tex(1f, 1f).endVertex();
		bufferBuilder.pos(x+width, y, 0.0).tex(1f, 0).endVertex();
		bufferBuilder.pos(x, y, 0.0).tex(0, 0).endVertex();
		tesselator.draw();
	}

	@Override
	public void axolotlclient_rendering$roundedGaussianBlur(float x, float y, float width, float height, float radius, float blurRadius, float sigma, int tintColor) {
		ShaderManager.Shader shader = ExRenderPipelines.GAUSSIAN_BLUR_PROGRAM;
		if (shader == null) return;
		Minecraft mc = Minecraft.getMinecraft();
		Framebuffer target = mc.getFramebuffer();
		if (target != null) {
			GlStateManager.bindTexture(BackdropCopy.capture(target.framebufferTextureWidth, target.framebufferTextureHeight));
		}
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
		shader.bind();
		shader.setUniform("Dimensions", x, y, width, height);
		shader.setUniform("Rounding", radius, radius, radius, radius);
		shader.setUniform("ScreenSize", (float) mc.displayWidth, (float) mc.displayHeight);
		shader.setUniform("BlurRadius", blurRadius);
		shader.setUniform("Sigma", sigma);
		int c = Rendering.scaleARGB(tintColor);
		GlStateManager.color((c >> 16 & 0xFF) / 255f, (c >> 8 & 0xFF) / 255f, (c & 0xFF) / 255f, (c >> 24 & 0xFF) / 255f);

		Tessellator tesselator = Tessellator.getInstance();
		BufferBuilder bufferBuilder = tesselator.getBuffer();
		bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		bufferBuilder.pos(x, y + height, 0.0).tex(0.0, 0.0).endVertex();
		bufferBuilder.pos(x + width, y + height, 0.0).tex(1.0, 0.0).endVertex();
		bufferBuilder.pos(x + width, y, 0.0).tex(1.0, 1.0).endVertex();
		bufferBuilder.pos(x, y, 0.0).tex(0.0, 1.0).endVertex();
		tesselator.draw();
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
		ShaderManager.Shader shader = ExRenderPipelines.LIQUID_GLASS_PROGRAM;
		if (shader == null || style == null) return;
		Minecraft mc = Minecraft.getMinecraft();
		Framebuffer target = mc.getFramebuffer();
		if (target != null) {
			GlStateManager.bindTexture(BackdropCopy.capture(target.framebufferTextureWidth, target.framebufferTextureHeight));
		}
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
		shader.bind();
		shader.setUniform("Dimensions", x, y, width, height);
		shader.setUniform("Rounding", rounding, rounding, rounding, rounding);
		shader.setUniform("ScreenSize", (float) mc.displayWidth, (float) mc.displayHeight);
		shader.setUniform("BlurRadius", style.getBlurRadius());
		shader.setUniform("Optics0", style.getRefThickness(), style.getRefFactor(), style.getRefDispersion(), style.getRefFresnelRange());
		shader.setUniform("Optics1", style.getRefFresnelHardness(), style.getRefFresnelFactor(), style.getGlareRange(), style.getGlareHardness());

		int tc = Rendering.scaleARGB(style.getTintColor());
		float ta = style.getTintAlpha() > 0 ? style.getTintAlpha() : ((tc >> 24 & 0xFF) / 255f);
		shader.setUniform("GlassTint", (tc >> 16 & 0xFF) / 255f, (tc >> 8 & 0xFF) / 255f, (tc & 0xFF) / 255f, ta);

		int bc = Rendering.scaleARGB(style.getBorderColor());
		shader.setUniform("BorderColor", (bc >> 16 & 0xFF) / 255f, (bc >> 8 & 0xFF) / 255f, (bc & 0xFF) / 255f, (bc >> 24 & 0xFF) / 255f);
		shader.setUniform("BorderWidth", style.getBorderWidth());
		GlStateManager.color(1f, 1f, 1f, 1f);

		Tessellator tesselator = Tessellator.getInstance();
		BufferBuilder bufferBuilder = tesselator.getBuffer();
		bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		bufferBuilder.pos(x, y + height, 0.0).tex(0.0, 0.0).endVertex();
		bufferBuilder.pos(x + width, y + height, 0.0).tex(1.0, 0.0).endVertex();
		bufferBuilder.pos(x + width, y, 0.0).tex(1.0, 1.0).endVertex();
		bufferBuilder.pos(x, y, 0.0).tex(0.0, 1.0).endVertex();
		tesselator.draw();
		shader.unbind();
	}

	@Override
	public void axolotlclient_rendering$drawMsdfString(io.github.axolotlclient.rendering.font.msdf.MsdfFont font, String text, float x, float y, float size, int color, boolean dropShadow) {
		if (font == null) return;
		if (dropShadow) {
			font.drawWithShadow(text, x, y, size, color);
		} else {
			font.draw(text, x, y, size, color);
		}
	}
}
