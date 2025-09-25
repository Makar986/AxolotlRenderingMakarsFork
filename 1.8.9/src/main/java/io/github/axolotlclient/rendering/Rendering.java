package io.github.axolotlclient.rendering;

import net.fabricmc.api.ClientModInitializer;
import org.joml.Matrix4fStack;

public class Rendering implements ClientModInitializer {
	public static Matrix4fStack MODELVIEW_MATRIX = new Matrix4fStack(32);
	public static Matrix4fStack PROJECTION_MATRIX = new Matrix4fStack(32);
	@Override
	public void onInitializeClient() {

	}
}
