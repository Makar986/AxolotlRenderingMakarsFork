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

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.textures.FilterMode;
import com.mojang.renderpearl.api.textures.GpuTexture;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import com.mojang.renderpearl.api.GpuFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.TextureSetup;
import org.jetbrains.annotations.ApiStatus;

/**
 * A copy of the main framebuffer that blur / liquid glass sample from.
 * <p>
 * Since 1.21.6 the GUI is collected as render states and drawn later in one pass, so an element cannot read
 * "what is behind it" while drawing into the same target. Instead, elements that need the backdrop request this
 * texture when they are submitted, and {@link #captureIfRequested()} copies the main target into it right before the
 * GUI is drawn. The backdrop therefore contains the world (and anything drawn before the GUI pass), not other GUI
 * elements from the same frame.
 */
@ApiStatus.Internal
public final class BackdropTexture {
	private static GpuTexture texture;
	private static GpuTextureView view;
	private static final List<AutoCloseable> retired = new ArrayList<>();
	private static boolean requested;

	private BackdropTexture() {
	}

	public static TextureSetup request() {
		ensureSize();
		requested = true;
		return TextureSetup.singleTexture(view, RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR));
	}

	private static void ensureSize() {
		RenderTarget main = Minecraft.getInstance().gameRenderer.mainRenderTarget();
		if (texture != null && texture.getWidth(0) == main.width && texture.getHeight(0) == main.height) {
			return;
		}
		// The old view may still be referenced by states submitted earlier this frame; close it after the next draw.
		if (view != null) retired.add(view);
		if (texture != null) retired.add(texture);
		var device = RenderSystem.getDevice();
		texture = device.createTexture(() -> "axolotlclient-rendering backdrop",
			GpuTexture.USAGE_COPY_DST | GpuTexture.USAGE_TEXTURE_BINDING, GpuFormat.RGBA8_UNORM, main.width, main.height, 1, 1);
		view = device.createTextureView(texture);
	}

	public static void captureIfRequested() {
		if (!requested) return;
		requested = false;
		RenderTarget main = Minecraft.getInstance().gameRenderer.mainRenderTarget();
		GpuTexture source = main.getColorTexture();
		if (source == null || texture == null || texture.getWidth(0) != main.width || texture.getHeight(0) != main.height) {
			return;
		}
		RenderSystem.getDevice().createCommandEncoder()
			.copyTextureToTexture(source, texture, 0, 0, 0, 0, 0, main.width, main.height);
	}

	public static void afterGuiDrawn() {
		for (AutoCloseable c : retired) {
			try {
				c.close();
			} catch (Exception ignored) {
			}
		}
		retired.clear();
	}
}
