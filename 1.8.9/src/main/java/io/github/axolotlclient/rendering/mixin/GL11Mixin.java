package io.github.axolotlclient.rendering.mixin;

import java.nio.FloatBuffer;

import io.github.axolotlclient.rendering.Rendering;
import org.joml.Matrix4fStack;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("UnresolvedMixinReference")
@Mixin(value = GL11.class, remap = false)
public abstract class GL11Mixin {
	@Shadow
	public static int glGetInteger(int pname) {
		return 0;
	}

	@Shadow
	@Final
	public static int GL_MATRIX_MODE;
	@Unique
	private static int currentMatrixMode;

	/*@Inject(method = "glMatrixMode", at = @At("HEAD"))
	private static void jomlMatrix$mode(int mode, CallbackInfo ci) {
		currentMatrixMode = mode;
	}*/

	/*@Unique
	private static Matrix4fStack current() {
		if (currentMatrixMode == GL11.GL_MODELVIEW) {
			return Rendering.MODELVIEW_MATRIX;
		} else if (currentMatrixMode == GL11.GL_PROJECTION) {
			return Rendering.PROJECTION_MATRIX;
		}
		throw new UnsupportedOperationException("Not implemented for mode " + currentMatrixMode);
	}

	@Inject(method = {"glMultMatrix(Ljava/nio/FloatBuffer;)V", "glMultMatrixf(Ljava/nio/FloatBuffer;)V"}, at = @At("HEAD"), require = 1)
	private static void jomlMatrix$mult(FloatBuffer src, CallbackInfo ci) {
		currentMatrixMode = glGetInteger(GL_MATRIX_MODE);
		if (currentMatrixMode != GL11.GL_MODELVIEW && currentMatrixMode != GL11.GL_PROJECTION) return;
		int offset = src.position();
		current().mul(src.get(offset),
			src.get(offset + 1),
			src.get(offset + 2),
			src.get(offset + 3),
			src.get(offset + 4),
			src.get(offset + 5),
			src.get(offset + 6),
			src.get(offset + 7),
			src.get(offset + 8),
			src.get(offset + 9),
			src.get(offset + 10),
			src.get(offset + 11),
			src.get(offset + 12),
			src.get(offset + 13),
			src.get(offset + 14),
			src.get(offset + 15));

	}*/
}
