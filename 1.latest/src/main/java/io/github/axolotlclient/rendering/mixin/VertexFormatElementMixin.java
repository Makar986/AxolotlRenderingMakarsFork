package io.github.axolotlclient.rendering.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import io.github.axolotlclient.rendering.ExRenderPipelines;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = VertexFormatElement.class, remap = false)
public class VertexFormatElementMixin {

	@Shadow
	@Final
	@Mutable
	public static int MAX_COUNT;

	@ModifyExpressionValue(method = "<clinit>", at = @At(value = "CONSTANT", args = "intValue=32"))
	private static int unstaticMaxCount(int original) {

		return original + ExRenderPipelines.NUM_CUSTOM_VERTEX_FORMAT_ELEMENTS;
	}

	@Inject(method = "<clinit>", at = @At("TAIL"))
	private static void increaseMaxCount(CallbackInfo ci) {
		MAX_COUNT += ExRenderPipelines.NUM_CUSTOM_VERTEX_FORMAT_ELEMENTS;
	}
}
