/*
 * Copyright © 2026 moehreag <moehreag@gmail.com> & Contributors
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

import java.nio.ByteBuffer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL30;

/**
 * A copy of the framebuffer currently drawn to, for the blur and liquid glass shaders.
 * <p>
 * Sampling the framebuffer that a draw call also writes to is a read/write feedback loop: the GPU may shade
 * some tiles after others have already been written, so the shader reads half-blurred pixels (visible as
 * garbage in the bottom-right corner). The shaders sample this copy instead.
 * <p>
 * Plain GL calls only (they compile against both LWJGL 2 and 3); every binding it touches is restored, so
 * the game's GL state caches stay in sync.
 */
public final class BackdropCopy {
	private static int texture = -1;
	private static int framebuffer = -1;
	private static int width;
	private static int height;

	private BackdropCopy() {
	}

	/**
	 * Copies the color buffer of the bound draw framebuffer ({@code width}x{@code height}, the size of its
	 * color texture) and returns the GL id of the copy.
	 */
	public static int capture(int width, int height) {
		int prevTexture = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
		int prevRead = GL11.glGetInteger(GL30.GL_READ_FRAMEBUFFER_BINDING);
		int prevDraw = GL11.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING);

		if (texture == -1 || BackdropCopy.width != width || BackdropCopy.height != height) {
			if (texture == -1) {
				texture = GL11.glGenTextures();
				framebuffer = GL30.glGenFramebuffers();
			}
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
			GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, (ByteBuffer) null);
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, prevTexture);
			GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, framebuffer);
			GL30.glFramebufferTexture2D(GL30.GL_DRAW_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, texture, 0);
			BackdropCopy.width = width;
			BackdropCopy.height = height;
		}

		GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, prevDraw);
		GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, framebuffer);
		GL30.glBlitFramebuffer(0, 0, width, height, 0, 0, width, height, GL11.GL_COLOR_BUFFER_BIT, GL11.GL_NEAREST);
		GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, prevRead);
		GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, prevDraw);
		return texture;
	}
}
