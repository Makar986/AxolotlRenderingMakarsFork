package io.github.axolotlclient.rendering;

import java.util.List;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.axolotlclient.rendering.font.Font;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.render.state.GuiElementRenderState;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;

public record FontPageRenderState(ResourceLocation tex, Matrix3x2f pose, List<Font.CoordData> glyphs,
								  ScreenRectangle rect, ScreenRectangle scissor) implements GuiElementRenderState {
	@Override
	public void buildVertices(VertexConsumer vertexConsumer) {
		for (var g : glyphs) {
			vertexConsumer.addVertexWith2DPose(this.pose(), g.x, g.y).setUv(g.u, g.v).setColor(g.color);
			vertexConsumer.addVertexWith2DPose(this.pose(), g.x, g.y + g.height).setUv(g.u, g.v1).setColor(g.color);
			vertexConsumer.addVertexWith2DPose(this.pose(), g.x + g.width, g.y + g.height).setUv(g.u1, g.v1).setColor(g.color);
			vertexConsumer.addVertexWith2DPose(this.pose(), g.x + g.width, g.y).setUv(g.u1, g.v).setColor(g.color);
		}
	}

	@Override
	public @NotNull RenderPipeline pipeline() {
		return ExRenderPipelines.FONT;
	}

	@Override
	public @NotNull TextureSetup textureSetup() {
		return TextureSetup.singleTexture(Minecraft.getInstance().getTextureManager().getTexture(tex).getTextureView());
	}

	@Override
	public @Nullable ScreenRectangle scissorArea() {
		return scissor;
	}

	@Override
	public @Nullable ScreenRectangle bounds() {
		return rect;
	}
}
