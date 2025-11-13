package io.github.axolotlclient.rendering.mixin;

import io.github.axolotlclient.rendering.Rendering;
import net.minecraft.client.options.GameOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameOptions.class)
public class GameOptionsMixin {

	@Inject(method = "setValue(Lnet/minecraft/client/options/GameOptions$Option;I)V", at = @At(value = "FIELD", target = "Lnet/minecraft/client/options/GameOptions;guiScale:I", ordinal = 0))
	private void refreshScale(GameOptions.Option option, int i, CallbackInfo ci) {
		Rendering.resetWindow();
	}
}
