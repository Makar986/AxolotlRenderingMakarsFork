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

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.MemoryTracker;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tessellator;
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
	private void fill(int x0, int y0, int x1, int y1, int colorTopLeft, int colorBottomLeft, int colorBottomRight, int colorTopRight) {
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferBuilder = tessellator.getBuilder();
		GlStateManager.enableBlend();
		GlStateManager.disableTexture();
		GlStateManager.blendFuncSeparate(770, 771, 1, 0);
		GlStateManager.color4f(1f, 1f, 1f, 1f);
		bufferBuilder.begin(7, DefaultVertexFormat.POSITION_COLOR);
		bufferBuilder.vertex(x1, y0, 0.0).color((colorTopRight >> 16 & 0xFF) / 255f, (colorTopRight >> 8 & 0xFF) / 255f, (colorTopRight & 0xFF) / 255f, (colorTopRight >> 24 & 0xFF) / 255f).nextVertex();
		bufferBuilder.vertex(x0, y0, 0.0).color((colorTopLeft >> 16 & 0xFF) / 255f, (colorTopLeft >> 8 & 0xFF) / 255f, (colorTopLeft & 0xFF) / 255f, (colorTopLeft >> 24 & 0xFF) / 255f).nextVertex();
		bufferBuilder.vertex(x0, y1, 0.0).color((colorBottomLeft >> 16 & 0xFF) / 255f, (colorBottomLeft >> 8 & 0xFF) / 255f, (colorBottomLeft & 0xFF) / 255f, (colorBottomLeft >> 24 & 0xFF) / 255f).nextVertex();
		bufferBuilder.vertex(x1, y1, 0.0).color((colorBottomRight >> 16 & 0xFF) / 255f, (colorBottomRight >> 8 & 0xFF) / 255f, (colorBottomRight & 0xFF) / 255f, (colorBottomRight >> 24 & 0xFF) / 255f).nextVertex();
		tessellator.end();
		GlStateManager.enableTexture();
		GlStateManager.disableBlend();
	}

	@Override
	public void axolotlclient_rendering$roundedRectGradientVarying(int x0, int y0, int x1, int y1, int colorTopLeft, int colorBottomLeft, int colorBottomRight, int colorTopRight, int roundingPxTopLeft, int roundingPxBottomLeft, int roundingPxBottomRight, int roundingPxTopRight) {
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
		fill(x0, y0, x1, y1, colorTopLeft, colorBottomLeft, colorBottomRight, colorTopRight);
		shader.unbind();
	}

	@Override
	public void axolotlclient_rendering$outlineRoundedRectGradientVarying(int x0, int y0, int x1, int y1, int colorTopLeft, int colorBottomLeft, int colorBottomRight, int colorTopRight, int roundingPxTopLeft, int roundingPxBottomLeft, int roundingPxBottomRight, int roundingPxTopRight, float outlineWidth) {
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
		fill(x0, y0, x1, y1, colorTopLeft, colorBottomLeft, colorBottomRight, colorTopRight);
		shader.unbind();
	}

	@Override
	public void axolotlclient_rendering$roundedBlitVarying(Identifier location, int x, int y, float u, float v, int width, int height, int uWidth, int vHeight, int textureWidth, int textureHeight, int color, int roundingPxTopLeft, int roundingPxBottomLeft, int roundingPxBottomRight, int roundingPxTopRight) {
		this.innerBlit(location, x, x + width, y, y + height, (u + 0.0F) / textureWidth, (u + uWidth) / textureWidth, (v + 0.0F) / textureHeight, (v + vHeight) / textureHeight, color, roundingPxTopLeft, roundingPxBottomLeft, roundingPxBottomRight, roundingPxTopRight);
	}

	@Override
	public void axolotlclient_rendering$roundedBlitVarying(Identifier atlas, int x0, int y0, int x1, int y1, float u0, float u1, float v0, float v1, int color, int roundingPxTopLeft, int roundingPxBottomLeft, int roundingPxBottomRight, int roundingPxTopRight) {
		this.innerBlit(atlas, x0, x1, y0, y1, u0, u1, v0, v1, color, roundingPxTopLeft, roundingPxBottomLeft, roundingPxBottomRight, roundingPxTopRight);
	}

	@Unique
	private void innerBlit(Identifier atlas, int x0, int x1, int y0, int y1, float u0, float u1, float v0, float v1, int color, int roundingPxTopLeft, int roundingPxBottomLeft, int roundingPxBottomRight, int roundingPxTopRight) {
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
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferBuilder = tessellator.getBuilder();
		bufferBuilder.begin(7, DefaultVertexFormat.POSITION_TEX);
		bufferBuilder.vertex(x0, y1, 0.0).texture(u0, v1).nextVertex();
		bufferBuilder.vertex(x1, y1, 0.0).texture(u1, v1).nextVertex();
		bufferBuilder.vertex(x1, y0, 0.0).texture(u1, v0).nextVertex();
		bufferBuilder.vertex(x0, y0, 0.0).texture(u0, v0).nextVertex();
		tessellator.end();
		shader.unbind();
	}

	@Override
	public void axolotlclient_rendering$outlineRoundedBlitVarying(Identifier location, int x, int y, float u, float v, int width, int height, int uWidth, int vHeight, int textureWidth, int textureHeight, int color, int roundingPxTopLeft, int roundingPxBottomLeft, int roundingPxBottomRight, int roundingPxTopRight, float outlineWidth) {
		this.innerBlit(location, x, x + width, y, y + height, u / textureWidth, (u + uWidth) / textureWidth, v / textureHeight, (v + vHeight) / textureHeight, color, roundingPxTopLeft, roundingPxBottomLeft, roundingPxBottomRight, roundingPxTopRight, outlineWidth);
	}

	@Override
	public void axolotlclient_rendering$outlineRoundedBlitVarying(Identifier atlas, int x0, int y0, int x1, int y1, float u0, float u1, float v0, float v1, int color, int roundingPxTopLeft, int roundingPxBottomLeft, int roundingPxBottomRight, int roundingPxTopRight, float outlineWidth) {
		this.innerBlit(atlas, x0, x1, y0, y1, u0, u1, v0, v1, color, roundingPxTopLeft, roundingPxBottomLeft, roundingPxBottomRight, roundingPxTopRight, outlineWidth);
	}

	@Unique
	private void innerBlit(Identifier atlas, int x0, int x1, int y0, int y1, float u0, float u1, float v0, float v1, int color, int roundingPxTopLeft, int roundingPxBottomLeft, int roundingPxBottomRight, int roundingPxTopRight, float outlineWidth) {
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
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferBuilder = tessellator.getBuilder();
		bufferBuilder.begin(7, DefaultVertexFormat.POSITION_TEX);
		bufferBuilder.vertex(x0, y1, 0.0).texture(u0, v1).nextVertex();
		bufferBuilder.vertex(x1, y1, 0.0).texture(u1, v1).nextVertex();
		bufferBuilder.vertex(x1, y0, 0.0).texture(u1, v0).nextVertex();
		bufferBuilder.vertex(x0, y0, 0.0).texture(u0, v0).nextVertex();
		tessellator.end();
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
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferBuilder = tessellator.getBuilder();
		bufferBuilder.begin(7, DefaultVertexFormat.POSITION_TEX);
		bufferBuilder.vertex(x, y+height, 0.0).texture(0, 1f).nextVertex();
		bufferBuilder.vertex(x+width, y+height, 0.0).texture(1f, 1f).nextVertex();
		bufferBuilder.vertex(x+width, y, 0.0).texture(1f, 0).nextVertex();
		bufferBuilder.vertex(x, y, 0.0).texture(0, 0).nextVertex();
		tessellator.end();
	}
}
