package io.github.axolotlclient.rendering;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.FloatBuffer;
import java.util.stream.Collectors;

import io.github.axolotlclient.rendering.mixin.GameRendererAccessor;
import io.github.axolotlclient.rendering.mixin.MinecraftAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.Window;
import net.minecraft.resource.Identifier;
import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;

import static org.lwjgl.opengl.GL20.*;

public class ShaderManager {

	public static int createProgram(Identifier id) throws IOException {
		var vId = new Identifier(id.getNamespace(), "shaders/" + id.getPath() + ".vsh");
		var fId = new Identifier(id.getNamespace(), "shaders/" + id.getPath() + ".fsh");

		var program = glCreateProgram();
		var vShader = createShader(vId, GL_VERTEX_SHADER);
		var fShader = createShader(fId, GL_FRAGMENT_SHADER);
		glAttachShader(program, vShader);
		glAttachShader(program, fShader);
		glLinkProgram(program);
		int linked = glGetProgrami(program, GL_LINK_STATUS);
		if (linked == 0) {
			String programLog = glGetProgramInfoLog(program, glGetProgrami(program, GL_INFO_LOG_LENGTH));
			throw new AssertionError("Could not link program: " + programLog);
		}
		return program;
	}

	private static int createShader(Identifier resource, int type) throws IOException {
		int shader = glCreateShader(type);

		var res = Minecraft.getInstance().getResourceManager().getResource(resource);
		try (var in = res.asStream(); var reader = new BufferedReader(new InputStreamReader(in))) {
			var source = reader.lines().collect(Collectors.joining("\n"));
			glShaderSource(shader, source);
		}

		glCompileShader(shader);
		if (glGetShaderi(shader, GL_COMPILE_STATUS) == 0) {
			String shaderLog = glGetShaderInfoLog(shader, glGetShaderi(shader, GL_INFO_LOG_LENGTH));
			throw new IOException("Could not compile shader " + resource + ": " + shaderLog);
		}
		return shader;
	}

	private static final FloatBuffer floatValues = BufferUtils.createFloatBuffer(16);

	private static Matrix4f getProjectionMatrix(Minecraft minecraft) {
		var tick = ((MinecraftAccessor) minecraft).getTimer().partialTick;
		var fov =((GameRendererAccessor) minecraft.gameRenderer).invokeGetFov(tick, true);
		//var fov = 120;
		Matrix4f matrix4f = new Matrix4f();
		return matrix4f.perspective(fov * ((float) Math.PI / 180F), (float) minecraft.width / (float) minecraft.height, 0.0F, 100);
		//return matrix4f.perspective(fov * ((float) Math.PI / 180F), (float) minecraft.width / (float) minecraft.height, 0.05F, minecraft.options.viewDistance * 2);
	}

	public static void uploadUniforms(int program) {
		floatValues.clear();
		var mc = Minecraft.getInstance();
		var modelView = new Matrix4f(); //Rendering.MODELVIEW_MATRIX;
		glUniformMatrix4(glGetUniformLocation(program, "ModelViewMat"), false, modelView.get(floatValues));
		floatValues.clear();
		var projMat = new Matrix4f();//getProjectionMatrix(mc);
		var window = new Window(mc);
		//projMat.ortho(0.0F, (float) window.getScaledWidth(), (float) window.getScaledHeight(), 0, 0.05f, mc.options.viewDistance * 2);
		//var projMat = Rendering.PROJECTION_MATRIX;
		//projMat.identity();
		//projMat.ortho(0.0F, (float) window.getScaledWidth(), (float) window.getScaledHeight(), 0, 0.0f, 100);
		//projMat.setOrtho(0.0F, (float)(window.getWidth() / window.getScale()), (float)(window.getHeight() / window.getScale()), 0.0F, 1000.0F, 21000.0F);
		//projMat.ortho(0.0F, (float) window.getScaledWidth(), (float) window.getScaledHeight(), 0, 1000, 3000);



		glUniformMatrix4(glGetUniformLocation(program, "ProjMat"), false, projMat.get(floatValues));
		glUniform4f(glGetUniformLocation(program, "ColorModulator"), 1, 1, 1, 1);
		glUniform4f(glGetUniformLocation(program, "Rounding"), 1, 1, 1, 1);
		glUniform4f(glGetUniformLocation(program, "Dimensions"), 1, 1, 1, 1);
	}
}
