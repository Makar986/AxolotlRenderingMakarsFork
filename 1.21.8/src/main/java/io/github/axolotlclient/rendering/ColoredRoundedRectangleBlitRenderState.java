package io.github.axolotlclient.rendering;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.render.state.GuiElementRenderState;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;

@Environment(EnvType.CLIENT)
public record ColoredRoundedRectangleBlitRenderState(
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
	int radiusTopLeft,
	int radiusBottomLeft,
	int radiusBottomRight,
	int radiusTopRight,
	@Nullable ScreenRectangle scissorArea,
	@Nullable ScreenRectangle bounds
) implements GuiElementRenderState {
	public ColoredRoundedRectangleBlitRenderState(
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
		int radiusTopLeft,
		int radiusBottomLeft,
		int radiusBottomRight,
		int radiusTopRight,
		@Nullable ScreenRectangle scissorArea
	) {
		this(pipeline, textureSetup, pose, x0, y0, x1, y1, u0, u1, v0, v1, col1, col2, col3, col4, radiusTopLeft, radiusBottomLeft, radiusBottomRight, radiusTopRight,
			scissorArea, DrawUtil.getBounds(x0, y0, x1, y1, pose, scissorArea));
	}

	@Override
	public void buildVertices(VertexConsumer consumer, float z) {
		int width = x1() - x0();
		int height = y1() - y0();
		consumer.addVertexWith2DPose(this.pose(), this.x0(), this.y0(), z).setUv(u0(), v0()).setColor(this.col1());
		ExRenderPipelines.addRounding(consumer, radiusTopLeft(), radiusBottomLeft(), radiusBottomRight(), radiusTopRight());
		ExRenderPipelines.addDimensions(consumer, 0, 0, width, height);

		consumer.addVertexWith2DPose(this.pose(), this.x0(), this.y1(), z).setUv(u0(), v1()).setColor(this.col2());
		ExRenderPipelines.addRounding(consumer, radiusTopLeft(), radiusBottomLeft(), radiusBottomRight(), radiusTopRight());
		ExRenderPipelines.addDimensions(consumer, 0, height, width, height);


		consumer.addVertexWith2DPose(this.pose(), this.x1(), this.y1(), z).setUv(u1(), v1()).setColor(this.col3());
		ExRenderPipelines.addRounding(consumer, radiusTopLeft(), radiusBottomLeft(), radiusBottomRight(), radiusTopRight());
		ExRenderPipelines.addDimensions(consumer, width, height, width, height);

		consumer.addVertexWith2DPose(this.pose(), this.x1(), this.y0(), z).setUv(u1(), v0()).setColor(this.col4());
		ExRenderPipelines.addRounding(consumer, radiusTopLeft(), radiusBottomLeft(), radiusBottomRight(), radiusTopRight());
		ExRenderPipelines.addDimensions(consumer, width, 0, width, height);
	}
}
