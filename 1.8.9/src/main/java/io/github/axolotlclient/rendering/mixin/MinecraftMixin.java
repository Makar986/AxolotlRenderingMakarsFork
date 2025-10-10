package io.github.axolotlclient.rendering.mixin;

import io.github.axolotlclient.rendering.font.Font;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {

	@Inject(method = "onResolutionChanged()V", at = @At("TAIL"))
	private void onResolutionChanged(CallbackInfo ci) {
		Font.onScaleChange();
	}
}
