package io.github.axolotlclient.rendering.mixin;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.datafixers.util.Pair;
import io.github.axolotlclient.rendering.ExRenderPipelines;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

	@Inject(method = "reloadShaders", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", ordinal = 0))
	private void loadCustomShaders(ResourceProvider resourceProvider, CallbackInfo ci, @Local(ordinal = 1) List<Pair<ShaderInstance, Consumer<ShaderInstance>>> list) throws IOException {
		list.add(Pair.of(new ShaderInstance(resourceProvider, "rounded_rect", ExRenderPipelines.ROUNDED_RECT_FORMAT), s -> ExRenderPipelines.ROUNDED_RECT_SHADER = s));
	}
}
