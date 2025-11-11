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

package io.github.axolotlclient.rendering.font;

import java.util.List;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.axolotlclient.rendering.ExRenderPipelines;
import net.minecraft.client.Minecraft;
import net.minecraft.client.StringSplitter;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.TextRenderable;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.util.StringDecomposer;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

/**
 * Convenience class to adapt {@link io.github.axolotlclient.rendering.font.Font} to
 * Minecraft's {@link Font} so it can be used in methods designed for the latter.
 * Only supports GUI rendering, everything else is silently delegated to Minecraft's {@link Font}.
 *
 * @implNote Assumes a font size of <b>9 pixels</b>. Spacing can be janky as a result of this assumption
 * as barely any font will have an integer font size. In order to get correct results, use methods
 * in {@link io.github.axolotlclient.rendering.GuiGraphicsExtension} that directly accept a {@link io.github.axolotlclient.rendering.font.Font}.
 */
@SuppressWarnings({"NullableProblems", "DataFlowIssue"})
public class FontAdapter extends Font {
	private static final Minecraft mc = Minecraft.getInstance();
	private final io.github.axolotlclient.rendering.font.Font font;

	public FontAdapter(io.github.axolotlclient.rendering.font.Font font) {
		super(null);
		this.font = font;
	}

	@Override
	public void drawInBatch(String text, float x, float y, int color, boolean drawShadow, Matrix4f pose, MultiBufferSource bufferSource, DisplayMode mode, int backgroundColor, int packedLightCoords) {
		mc.font.drawInBatch(text, x, y, color, drawShadow, pose, bufferSource, mode, backgroundColor, packedLightCoords);
	}

	@Override
	public void drawInBatch(Component text, float x, float y, int color, boolean drawShadow, Matrix4f pose, MultiBufferSource bufferSource, DisplayMode mode, int backgroundColor, int packedLightCoords) {
		mc.font.drawInBatch(text, x, y, color, drawShadow, pose, bufferSource, mode, backgroundColor, packedLightCoords);
	}

	@Override
	public void drawInBatch(FormattedCharSequence text, float x, float y, int color, boolean drawShadow, Matrix4f pose, MultiBufferSource bufferSource, DisplayMode mode, int backgroundColor, int packedLightCoords) {
		mc.font.drawInBatch(text, x, y, color, drawShadow, pose, bufferSource, mode, backgroundColor, packedLightCoords);
	}

	@Override
	public void drawInBatch8xOutline(FormattedCharSequence text, float x, float y, int color, int backgroundColor, Matrix4f pose, MultiBufferSource bufferSource, int packedLightCoords) {
		mc.font.drawInBatch8xOutline(text, x, y, color, backgroundColor, pose, bufferSource, packedLightCoords);
	}

	@Override
	public PreparedText prepareText(String text, float x, float y, int color, boolean dropShadow, int backgroundColor) {
		var t = isBidirectional() ? bidirectionalShaping(text) : text;
		var built = font.buildText(color, dropShadow, s -> StringDecomposer.iterateFormatted(t, Style.EMPTY, s));
		return getPreparedText(x, y, built, backgroundColor);
	}

	private @NotNull PreparedText getPreparedText(float x, float y, io.github.axolotlclient.rendering.font.Font.BuiltText built, int backgroundColor) {
		return new PreparedText() {
			@Override
			public void visit(GlyphVisitor visitor) {
				if (backgroundColor != 0) {
					visitor.acceptEffect(new TextRenderable() {
						@Override
						public void render(Matrix4f pose, VertexConsumer consumer, int packedLight, boolean noDepth) {
							consumer.addVertex(pose, x - 1f, y - 1f, 0.01f).setColor(backgroundColor);
							consumer.addVertex(pose, x - 1f, y - 1f + built.textBox()[3] + 1f, 0.01f).setColor(backgroundColor);
							consumer.addVertex(pose, x + built.textBox()[2] + 1f, y - 1f + built.textBox()[3] + 1f, 0.01f).setColor(backgroundColor);
							consumer.addVertex(pose, x + built.textBox()[2] + 1f, y - 1f, 0.01f).setColor(backgroundColor);
						}

						@Override
						public RenderType renderType(DisplayMode displayMode) {
							return null;
						}

						@Override
						public GpuTextureView textureView() {
							return null;
						}

						@Override
						public RenderPipeline guiPipeline() {
							return RenderPipelines.GUI;
						}

						@Override
						public float left() {
							return 0;
						}

						@Override
						public float top() {
							return 0;
						}

						@Override
						public float right() {
							return 0;
						}

						@Override
						public float bottom() {
							return 0;
						}
					});
				}
				for (var entry : built.coordData().entrySet()) {
					visitor.acceptGlyph(new TextRenderable() {
						@Override
						public void render(Matrix4f matrix, VertexConsumer consumer, int packedLight, boolean noDepth) {
							var pose = new Matrix4f(matrix);
							pose.translate(x, y + font.lineHeight(), 0);
							var s = 1f / mc.getWindow().getGuiScale();
							pose.scale(s, s, 1);
							float[] startOffset = io.github.axolotlclient.rendering.font.Font.getOffset(0, built);
							pose.translate(-startOffset[0], -startOffset[1], 0);
							for (var g : entry.getValue()) {
								consumer.addVertex(pose, g.x(), g.y(), 0).setUv(g.u(), g.v()).setColor(g.color());
								consumer.addVertex(pose, g.x(), g.y() + g.height(), 0).setUv(g.u(), g.v1()).setColor(g.color());
								consumer.addVertex(pose, g.x() + g.width(), g.y() + g.height(), 0).setUv(g.u1(), g.v1()).setColor(g.color());
								consumer.addVertex(pose, g.x() + g.width(), g.y(), 0).setUv(g.u1(), g.v()).setColor(g.color());
							}
						}

						@Override
						public RenderType renderType(DisplayMode displayMode) {
							return null;
						}

						@Override
						public GpuTextureView textureView() {
							return mc.getTextureManager().getTexture(entry.getKey()).getTextureView();
						}

						@Override
						public RenderPipeline guiPipeline() {
							return ExRenderPipelines.FONT;
						}

						@Override
						public float left() {
							return 0;
						}

						@Override
						public float top() {
							return 0;
						}

						@Override
						public float right() {
							return 0;
						}

						@Override
						public float bottom() {
							return 0;
						}
					});
				}
				for (var e : built.effects()) {
					visitor.acceptEffect(new TextRenderable() {
						@Override
						public void render(Matrix4f pose, VertexConsumer consumer, int packedLight, boolean noDepth) {
							consumer.addVertex(pose, e.x(), e.y(), 0).setColor(e.color());
							consumer.addVertex(pose, e.x(), e.y() + e.height(), 0).setColor(e.color());
							consumer.addVertex(pose, e.x() + e.width(), e.y() + e.height(), 0).setColor(e.color());
							consumer.addVertex(pose, e.x() + e.width(), e.y(), 0).setColor(e.color());
						}

						@Override
						public RenderType renderType(DisplayMode displayMode) {
							return null;
						}

						@Override
						public GpuTextureView textureView() {
							return null;
						}

						@Override
						public RenderPipeline guiPipeline() {
							return RenderPipelines.GUI;
						}

						@Override
						public float left() {
							return 0;
						}

						@Override
						public float top() {
							return 0;
						}

						@Override
						public float right() {
							return 0;
						}

						@Override
						public float bottom() {
							return 0;
						}
					});
				}
			}

			@Override
			public @NotNull ScreenRectangle bounds() {
				return new ScreenRectangle((int) (x + built.textBox()[0]), (int) (y + built.textBox()[1]), (int) built.textBox()[2], (int) built.textBox()[3]);
			}
		};
	}

	@Override
	public PreparedText prepareText(FormattedCharSequence text, float x, float y, int color, boolean dropShadow, int backgroundColor) {
		var built = font.buildText(color, dropShadow, text::accept);
		return getPreparedText(x, y, built, backgroundColor);
	}

	@Override
	public int width(String text) {
		return Mth.ceil(font.width(text));
	}

	@Override
	public int width(FormattedText text) {
		return Mth.ceil(getSplitter().stringWidth(text));
	}

	@Override
	public int width(FormattedCharSequence text) {
		return Mth.ceil(font.width(text));
	}

	@Override
	public String plainSubstrByWidth(String text, int maxWidth, boolean tail) {
		return tail ? getSplitter().plainTailByWidth(text, maxWidth, Style.EMPTY) : getSplitter().plainHeadByWidth(text, maxWidth, Style.EMPTY);
	}

	@Override
	public String plainSubstrByWidth(String text, int maxWidth) {
		return getSplitter().plainHeadByWidth(text, maxWidth, Style.EMPTY);
	}

	@Override
	public FormattedText substrByWidth(FormattedText text, int maxWidth) {
		return getSplitter().headByWidth(text, maxWidth, Style.EMPTY);
	}

	@Override
	public int wordWrapHeight(String text, int maxWidth) {
		return font.lineHeight() * getSplitter().splitLines(text, maxWidth, Style.EMPTY).size();
	}

	@Override
	public int wordWrapHeight(FormattedText text, int maxWidth) {
		return font.lineHeight() * getSplitter().splitLines(text, maxWidth, Style.EMPTY).size();
	}

	@Override
	public List<FormattedCharSequence> split(FormattedText text, int maxWidth) {
		return Language.getInstance().getVisualOrder(getSplitter().splitLines(text, maxWidth, Style.EMPTY));
	}

	@Override
	public List<FormattedText> splitIgnoringLanguage(FormattedText text, int maxWidth) {
		return getSplitter().splitLines(text, maxWidth, Style.EMPTY);
	}

	@Override
	public StringSplitter getSplitter() {
		return font.splitter;
	}
}
