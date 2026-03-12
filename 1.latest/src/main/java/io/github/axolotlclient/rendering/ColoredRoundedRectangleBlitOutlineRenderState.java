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

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;

@ApiStatus.Internal
public record ColoredRoundedRectangleBlitOutlineRenderState(
	RenderPipeline pipeline,
	TextureSetup textureSetup,
	Matrix3x2f pose,
	int x0,
	int y0,
	int x1,
	int y1,
	float u0,
	float u1,
	float v0,
	float v1,
	int col1,
	int col2,
	int col3,
	int col4,
	float radiusTopLeft,
	float radiusBottomLeft,
	float radiusBottomRight,
	float radiusTopRight,
	float outlineWidth,
	@Nullable ScreenRectangle scissorArea,
	@Nullable ScreenRectangle bounds
) implements GuiElementRenderState {
	public ColoredRoundedRectangleBlitOutlineRenderState(
		RenderPipeline pipeline,
		TextureSetup textureSetup,
		Matrix3x2f pose,
		int x0,
		int y0,
		int x1,
		int y1,
		float u0,
		float u1,
		float v0,
		float v1,
		int col1,
		int col2,
		int col3,
		int col4,
		float radiusTopLeft,
		float radiusBottomLeft,
		float radiusBottomRight,
		float radiusTopRight,
		float outlineWidth,
		@Nullable ScreenRectangle scissorArea
	) {
		this(pipeline, textureSetup, pose, x0, y0, x1, y1, u0, u1, v0, v1, col1, col2, col3, col4, radiusTopLeft, radiusBottomLeft, radiusBottomRight, radiusTopRight,
			outlineWidth, scissorArea, DrawUtil.getBounds(x0, y0, x1, y1, pose, scissorArea));
	}

	@Override
	public void buildVertices(VertexConsumer consumer) {
		int width = x1() - x0();
		int height = y1() - y0();
		consumer.addVertexWith2DPose(this.pose(), this.x0(), this.y0()).setUv(u0(), v0()).setColor(this.col1());
		ExRenderPipelines.addRounding(consumer, radiusTopLeft(), radiusBottomLeft(), radiusBottomRight(), radiusTopRight());
		ExRenderPipelines.addDimensions(consumer, 0, 0, width, height);
		ExRenderPipelines.addOutlineWidth(consumer, outlineWidth());

		consumer.addVertexWith2DPose(this.pose(), this.x0(), this.y1()).setUv(u0(), v1()).setColor(this.col2());
		ExRenderPipelines.addRounding(consumer, radiusTopLeft(), radiusBottomLeft(), radiusBottomRight(), radiusTopRight());
		ExRenderPipelines.addDimensions(consumer, 0, height, width, height);
		ExRenderPipelines.addOutlineWidth(consumer, outlineWidth());

		consumer.addVertexWith2DPose(this.pose(), this.x1(), this.y1()).setUv(u1(), v1()).setColor(this.col3());
		ExRenderPipelines.addRounding(consumer, radiusTopLeft(), radiusBottomLeft(), radiusBottomRight(), radiusTopRight());
		ExRenderPipelines.addDimensions(consumer, width, height, width, height);
		ExRenderPipelines.addOutlineWidth(consumer, outlineWidth());

		consumer.addVertexWith2DPose(this.pose(), this.x1(), this.y0()).setUv(u1(), v0()).setColor(this.col4());
		ExRenderPipelines.addRounding(consumer, radiusTopLeft(), radiusBottomLeft(), radiusBottomRight(), radiusTopRight());
		ExRenderPipelines.addDimensions(consumer, width, 0, width, height);
		ExRenderPipelines.addOutlineWidth(consumer, outlineWidth());
	}
}
