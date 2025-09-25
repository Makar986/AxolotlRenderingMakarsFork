package io.github.axolotlclient.rendering.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.VertexFormat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(VertexFormat.class)
public class VertexFormatMixin {

	@WrapOperation(method = "addElement", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/VertexFormat;hasPositionElement()Z"))
	private boolean hasNoPositionElement(VertexFormat instance, Operation<Boolean> original){

		return false;
	}
}
