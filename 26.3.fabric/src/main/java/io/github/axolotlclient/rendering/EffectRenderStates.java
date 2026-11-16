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

import java.util.List;

import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.axolotlclient.rendering.font.msdf.MsdfGlyphQuad;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;

/**
 * GUI render states for the backdrop effects (gaussian blur, liquid glass) and MSDF text.
 */
@ApiStatus.Internal
public final class EffectRenderStates {
	private EffectRenderStates() {
	}

	private static ScreenRectangle bounds(float x, float y, float width, float height, Matrix3x2f pose, @Nullable ScreenRectangle scissor) {
		return DrawUtil.getBounds((int) Math.floor(x), (int) Math.floor(y), (int) Math.ceil(x + width), (int) Math.ceil(y + height), pose, scissor);
	}

	/** Emits the four corners of a rounded effect quad; {@code extra} adds the per-vertex effect attributes. */
	private static void quad(VertexConsumer consumer, Matrix3x2f pose, float x, float y, float w, float h, int color, float rounding, Runnable extra) {
		float[][] corners = {{0, 0}, {0, h}, {w, h}, {w, 0}};
		for (float[] c : corners) {
			consumer.addVertexWith2DPose(pose, x + c[0], y + c[1]).setUv(w == 0 ? 0 : c[0] / w, h == 0 ? 0 : c[1] / h).setColor(color);
			ExRenderPipelines.addRounding(consumer, rounding, rounding, rounding, rounding);
			ExRenderPipelines.addDimensions(consumer, c[0], c[1], w, h);
			extra.run();
		}
	}

	public record GaussianBlur(
		TextureSetup textureSetup, Matrix3x2f pose,
		float x, float y, float width, float height, float rounding,
		float blurRadius, float sigma, int tintColor,
		@Nullable ScreenRectangle scissorArea, @Nullable ScreenRectangle bounds
	) implements GuiElementRenderState {
		public GaussianBlur(TextureSetup textureSetup, Matrix3x2f pose, float x, float y, float width, float height, float rounding,
							float blurRadius, float sigma, int tintColor, @Nullable ScreenRectangle scissorArea) {
			this(textureSetup, pose, x, y, width, height, rounding, blurRadius, sigma, tintColor, scissorArea,
				EffectRenderStates.bounds(x, y, width, height, pose, scissorArea));
		}

		@Override
		public RenderPipeline pipeline() {
			return ExRenderPipelines.GAUSSIAN_BLUR;
		}

		@Override
		public void buildVertices(VertexConsumer consumer) {
			quad(consumer, pose, x, y, width, height, tintColor, rounding,
				() -> ExRenderPipelines.addEffect(consumer, 0, blurRadius, sigma, 0, 0));
		}
	}

	public record LiquidGlass(
		TextureSetup textureSetup, Matrix3x2f pose,
		float x, float y, float width, float height, float rounding, LiquidGlassStyle style,
		@Nullable ScreenRectangle scissorArea, @Nullable ScreenRectangle bounds
	) implements GuiElementRenderState {
		public LiquidGlass(TextureSetup textureSetup, Matrix3x2f pose, float x, float y, float width, float height, float rounding,
						   LiquidGlassStyle style, @Nullable ScreenRectangle scissorArea) {
			this(textureSetup, pose, x, y, width, height, rounding, style, scissorArea,
				EffectRenderStates.bounds(x, y, width, height, pose, scissorArea));
		}

		@Override
		public RenderPipeline pipeline() {
			return ExRenderPipelines.LIQUID_GLASS;
		}

		@Override
		public void buildVertices(VertexConsumer consumer) {
			int tint = style.getTintColor();
			if ((tint & 0xFF000000) == 0 && style.getTintAlpha() > 0.0f) {
				tint |= ((int) (style.getTintAlpha() * 255.0f) << 24);
			}
			int bc = style.getBorderColor();
			quad(consumer, pose, x, y, width, height, tint, rounding, () -> {
				ExRenderPipelines.addEffect(consumer, 0, style.getRefThickness(), style.getRefFactor(), style.getRefDispersion(), style.getRefFresnelRange());
				ExRenderPipelines.addEffect(consumer, 1, style.getRefFresnelHardness(), style.getRefFresnelFactor(), style.getGlareRange(), style.getGlareHardness());
				ExRenderPipelines.addEffect(consumer, 2, style.getBlurRadius(), style.getBorderWidth(), 0, 0);
				ExRenderPipelines.addEffect(consumer, 3, (bc >> 16 & 0xFF) / 255f, (bc >> 8 & 0xFF) / 255f, (bc & 0xFF) / 255f, (bc >>> 24) / 255f);
			});
		}
	}

	public record MsdfText(
		TextureSetup textureSetup, Matrix3x2f pose, List<MsdfGlyphQuad> quads, int color,
		@Nullable ScreenRectangle scissorArea, @Nullable ScreenRectangle bounds
	) implements GuiElementRenderState {
		public static MsdfText of(TextureSetup textureSetup, Matrix3x2f pose, List<MsdfGlyphQuad> quads, int color, @Nullable ScreenRectangle scissorArea) {
			float minX = Float.MAX_VALUE, minY = Float.MAX_VALUE, maxX = -Float.MAX_VALUE, maxY = -Float.MAX_VALUE;
			for (MsdfGlyphQuad q : quads) {
				minX = Math.min(minX, Math.min(q.x0, q.x1));
				minY = Math.min(minY, Math.min(q.y0, q.y1));
				maxX = Math.max(maxX, Math.max(q.x0, q.x1));
				maxY = Math.max(maxY, Math.max(q.y0, q.y1));
			}
			return new MsdfText(textureSetup, pose, quads, color, scissorArea,
				EffectRenderStates.bounds(minX, minY, maxX - minX, maxY - minY, pose, scissorArea));
		}

		@Override
		public RenderPipeline pipeline() {
			return ExRenderPipelines.MSDF_FONT;
		}

		@Override
		public void buildVertices(VertexConsumer consumer) {
			for (MsdfGlyphQuad q : quads) {
				consumer.addVertexWith2DPose(pose, q.x0, q.y0).setUv(q.u0, q.v0).setColor(color);
				consumer.addVertexWith2DPose(pose, q.x0, q.y1).setUv(q.u0, q.v1).setColor(color);
				consumer.addVertexWith2DPose(pose, q.x1, q.y1).setUv(q.u1, q.v1).setColor(color);
				consumer.addVertexWith2DPose(pose, q.x1, q.y0).setUv(q.u1, q.v0).setColor(color);
			}
		}
	}
}
