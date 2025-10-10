package io.github.axolotlclient.rendering.mixin;

import com.mojang.blaze3d.platform.Window;
import io.github.axolotlclient.rendering.font.Font;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Window.class)
public abstract class WindowMixin {

	@Inject(method = "setGuiScale", at = @At("TAIL"))
	private void onRescale(double scaleFactor, CallbackInfo ci) {
		Font.onScaleChange();
	}
}
