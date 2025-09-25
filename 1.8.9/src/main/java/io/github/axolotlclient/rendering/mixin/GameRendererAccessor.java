package io.github.axolotlclient.rendering.mixin;

import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(GameRenderer.class)
public interface GameRendererAccessor {

	@Invoker("getFov")
	float invokeGetFov(float f, boolean bl);

}
