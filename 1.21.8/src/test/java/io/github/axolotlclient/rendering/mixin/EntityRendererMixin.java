package io.github.axolotlclient.rendering.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.axolotlclient.rendering.ColoredRoundedRectangleRenderState;
import io.github.axolotlclient.rendering.ExRenderPipelines;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.network.chat.Component;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin {

	@WrapOperation(method = "renderNameTag", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Font;drawInBatch(Lnet/minecraft/network/chat/Component;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/gui/Font$DisplayMode;II)V", ordinal = 0))
	private void roundBg(Font instance, Component text, float x, float y, int color, boolean drawShadow, Matrix4f pose, MultiBufferSource bufferSource, Font.DisplayMode mode, int backgroundColor, int packedLightCoords, Operation<Void> original) {
		bufferSource.getBuffer(RenderType.textBackground());
		int width = x1() - x0();
		int height = y1() - y0();
		consumer.addVertexWith2DPose(this.pose(), this.x0(), this.y0(), z).setColor(this.col1());
		ExRenderPipelines.addRounding(consumer, radiusTopLeft(), radiusBottomLeft(), radiusBottomRight(), radiusTopRight());
		ExRenderPipelines.addDimensions(consumer, 0, 0, width, height);

		consumer.addVertexWith2DPose(this.pose(), this.x0(), this.y1(), z).setColor(this.col2());
		ExRenderPipelines.addRounding(consumer, radiusTopLeft(), radiusBottomLeft(), radiusBottomRight(), radiusTopRight());
		ExRenderPipelines.addDimensions(consumer, 0, height, width, height);

		consumer.addVertexWith2DPose(this.pose(), this.x1(), this.y1(), z).setColor(this.col3());
		ExRenderPipelines.addRounding(consumer, radiusTopLeft(), radiusBottomLeft(), radiusBottomRight(), radiusTopRight());
		ExRenderPipelines.addDimensions(consumer, width, height, width, height);

		consumer.addVertexWith2DPose(this.pose(), this.x1(), this.y0(), z).setColor(this.col4());
		ExRenderPipelines.addRounding(consumer, radiusTopLeft(), radiusBottomLeft(), radiusBottomRight(), radiusTopRight());
		ExRenderPipelines.addDimensions(consumer, width, 0, width, height);
	}

}
