package io.github.axolotlclient.rendering.test.mixin;

import io.github.axolotlclient.rendering.test.RenderingTestMod;
import org.spongepowered.asm.mixin.Mixin;
import net.minecraft.client.gui.GameGui;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameGui.class)
public class GuiMixin {

	@Inject(method = "render", at = @At("TAIL"))
	private void renderHud(float f, CallbackInfo ci) {
		RenderingTestMod.renderHud();
	}
}
