/*
 * Copyright © 2025 moehreag <moehreag@gmail.com> & Contributors
 *
 * This file is part of AxolotlClient (Rendering Mod).
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 * For more information, see the LICENSE file.
 */

package io.github.axolotlclient.rendering;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.FloatBuffer;
import java.util.stream.Collectors;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;
import org.lwjgl.opengl.GL20;

import static org.lwjgl.opengl.GL20.*;

@ApiStatus.Internal
public class ShaderManager {

	public static Shader createProgram(ResourceLocation id) throws IOException {
		return createProgram(id, id);
	}

	public static Shader createProgram(ResourceLocation vertexShader, ResourceLocation fragmentShader) throws IOException {
		ResourceLocation vId = new ResourceLocation(vertexShader.getNamespace(), "shaders/" + vertexShader.getPath() + ".vsh");
		ResourceLocation fId = new ResourceLocation(fragmentShader.getNamespace(), "shaders/" + fragmentShader.getPath() + ".fsh");

		int program = glCreateProgram();
		int vShader = createShader(vId, GL_VERTEX_SHADER);
		int fShader = createShader(fId, GL_FRAGMENT_SHADER);
		glAttachShader(program, vShader);
		glAttachShader(program, fShader);
		glLinkProgram(program);
		int linked = glGetProgrami(program, GL_LINK_STATUS);
		if (linked == 0) {
			String programLog = glGetProgramInfoLog(program, glGetProgrami(program, GL_INFO_LOG_LENGTH));
			throw new AssertionError("Could not link program: " + programLog);
		}
		glDeleteShader(vShader);
		glDeleteShader(fShader);
		return new Shader(program);
	}

	private static int createShader(ResourceLocation resource, int type) throws IOException {
		int shader = glCreateShader(type);

		IResource res = Minecraft.getMinecraft().getResourceManager().getResource(resource);
		try (InputStream in = res.getInputStream(); BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
			String source = reader.lines().collect(Collectors.joining("\n"));
			glShaderSource(shader, source);
		}

		glCompileShader(shader);
		if (glGetShaderi(shader, GL_COMPILE_STATUS) == 0) {
			String shaderLog = glGetShaderInfoLog(shader, glGetShaderi(shader, GL_INFO_LOG_LENGTH));
			throw new IOException("Could not compile shader " + resource + ": " + shaderLog);
		}
		return shader;
	}

	public static void safeClose(Shader... shaders) {
		for (Shader shader : shaders) {
			if (shader != null) {
				shader.delete();
			}
		}
	}

	public static class Shader {
		private final int programId;

		public Shader(int programId) {
			this.programId = programId;
		}

		public int programId() {
			return programId;
		}

		public void bind() {
			GL20.glUseProgram(programId);
		}

		public void unbind() {
			GL20.glUseProgram(0);
		}

		public void delete() {
			glDeleteProgram(programId);
		}

		public void setUniform(String name, float value) {
			glUniform1f(glGetUniformLocation(programId, name), value);
		}

		public void setUniform(String name, float v0, float v1) {
			glUniform2f(glGetUniformLocation(programId, name), v0, v1);
		}

		public void setUniform(String name, float v0, float v1, float v2, float v3) {
			glUniform4f(glGetUniformLocation(programId, name), v0, v1, v2, v3);
		}

		public void setUniform(String name, FloatBuffer value) {
			glUniformMatrix4(glGetUniformLocation(programId, name), false, value);
		}
	}
}
