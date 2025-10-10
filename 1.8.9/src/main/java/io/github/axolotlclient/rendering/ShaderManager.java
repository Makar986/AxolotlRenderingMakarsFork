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
import java.io.InputStreamReader;
import java.nio.FloatBuffer;
import java.util.stream.Collectors;

import net.minecraft.client.Minecraft;
import net.minecraft.resource.Identifier;
import org.jetbrains.annotations.ApiStatus;
import org.lwjgl.opengl.GL20;

import static org.lwjgl.opengl.GL20.*;

@ApiStatus.Internal
public class ShaderManager {

	public static Shader createProgram(Identifier id) throws IOException {
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
		glDeleteShader(vShader);
		glDeleteShader(fShader);
		return new Shader(program);
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

	public static void safeClose(Shader... shaders) {
		for (var shader : shaders) {
			if (shader != null) {
				shader.delete();
			}
		}
	}

	public record Shader(int programId) {
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

		public void setUniform(String name, float v0, float v1, float v2, float v3) {
			glUniform4f(glGetUniformLocation(programId, name), v0, v1, v2, v3);
		}

		public void setUniform(String name, FloatBuffer value) {
			glUniformMatrix4fv(glGetUniformLocation(programId, name), false, value);
		}
	}
}
