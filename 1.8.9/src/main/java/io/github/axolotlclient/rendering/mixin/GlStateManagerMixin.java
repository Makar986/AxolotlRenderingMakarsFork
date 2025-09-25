package io.github.axolotlclient.rendering.mixin;

import com.mojang.blaze3d.platform.GlStateManager;
import io.github.axolotlclient.rendering.Rendering;
import org.joml.Matrix4fStack;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GlStateManager.class)
public class GlStateManagerMixin {
	@Unique
	private static int currentMatrixMode;

	@Inject(method = "matrixMode", at = @At("HEAD"))
	private static void jomlMatrix$mode(int i, CallbackInfo ci) {
		currentMatrixMode = i;
	}

	@Unique
	private static Matrix4fStack current() {
		if (currentMatrixMode == GL11.GL_MODELVIEW) {
			return Rendering.MODELVIEW_MATRIX;
		} else if (currentMatrixMode == GL11.GL_PROJECTION) {
			return Rendering.PROJECTION_MATRIX;
		}
		throw new UnsupportedOperationException("Not implemented for mode " + currentMatrixMode);
	}

	@Inject(method = "pushMatrix", at = @At("HEAD"))
	private static void jomlMatrix$push(CallbackInfo ci) {
		if (currentMatrixMode != GL11.GL_MODELVIEW && currentMatrixMode != GL11.GL_PROJECTION) return;
		current().pushMatrix();
	}

	@Inject(method = "popMatrix", at = @At("HEAD"))
	private static void jomlMatrix$pop(CallbackInfo ci) {
		if (currentMatrixMode != GL11.GL_MODELVIEW && currentMatrixMode != GL11.GL_PROJECTION) return;
		current().popMatrix();
	}

	@Inject(method = "translatef", at = @At("HEAD"))
	private static void jomlMatrix$translatef(float x, float y, float z, CallbackInfo ci) {
		if (currentMatrixMode != GL11.GL_MODELVIEW && currentMatrixMode != GL11.GL_PROJECTION) return;
		current().translate(x, y, z);
	}

	@Inject(method = "translated", at = @At("HEAD"))
	private static void jomlMatrix$translated(double x, double y, double z, CallbackInfo ci) {
		if (currentMatrixMode != GL11.GL_MODELVIEW && currentMatrixMode != GL11.GL_PROJECTION) return;
		current().translate((float) x, (float) y, (float) z);
	}

	@Inject(method = "rotatef", at = @At("HEAD"))
	private static void jomlMatrix$rotatef(float ang, float x, float y, float z, CallbackInfo ci) {
		if (currentMatrixMode != GL11.GL_MODELVIEW && currentMatrixMode != GL11.GL_PROJECTION) return;
		current().rotate(ang, x, y, z);
	}

	@Inject(method = "scalef", at = @At("HEAD"))
	private static void jomlMatrix$scalef(float x, float y, float z, CallbackInfo ci) {
		if (currentMatrixMode != GL11.GL_MODELVIEW && currentMatrixMode != GL11.GL_PROJECTION) return;
		current().scale(x, y, z);
	}

	@Inject(method = "scaled", at = @At("HEAD"))
	private static void jomlMatrix$scaled(double x, double y, double z, CallbackInfo ci) {
		if (currentMatrixMode != GL11.GL_MODELVIEW && currentMatrixMode != GL11.GL_PROJECTION) return;
		current().scale((float) x, (float) y, (float) z);
	}

	@Inject(method = "loadIdentity", at = @At("HEAD"))
	private static void jomlMatrix$identity(CallbackInfo ci) {
		if (currentMatrixMode != GL11.GL_MODELVIEW && currentMatrixMode != GL11.GL_PROJECTION) return;
		current().identity();
	}

	@Inject(method = "ortho", at = @At("HEAD"))
	private static void jomlMatrix$ortho(double d, double e, double f, double g, double h, double i, CallbackInfo ci) {
		if (currentMatrixMode != GL11.GL_MODELVIEW && currentMatrixMode != GL11.GL_PROJECTION) return;
		current().ortho((float) d, (float) e, (float) f, (float) g, (float) h, (float) i);
	}
}
