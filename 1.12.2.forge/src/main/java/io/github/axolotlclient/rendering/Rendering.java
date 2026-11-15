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

import java.nio.FloatBuffer;

import io.github.axolotlclient.rendering.font.Font;
import io.github.axolotlclient.rendering.svg.SVGCache;
import io.github.axolotlclient.rendering.util.ARGB;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import org.jetbrains.annotations.ApiStatus;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

@Mod(modid = "axolotlclient_rendering", name = "AxolotlClient Rendering", clientSideOnly = true, useMetadata = true)
public class Rendering implements IResourceManagerReloadListener {

	public Rendering() {
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		ExRenderPipelines.init();
		if (Minecraft.getMinecraft().getResourceManager() instanceof IReloadableResourceManager) {
			((IReloadableResourceManager) Minecraft.getMinecraft().getResourceManager()).registerReloadListener(this);
		}
	}

	@Override
	public void onResourceManagerReload(IResourceManager resourceManager) {
		ExRenderPipelines.reload();
		Font.reload();
		SVGCache.getInstance().clear();
	}

	@ApiStatus.Internal
	public static void resetWindow() {
		((DrawUtil) DrawUtil.get()).resetWindow();
	}

	@ApiStatus.Internal
	public static int scaleARGB(int color) {
		FloatBuffer buf = BufferUtils.createFloatBuffer(16);
		GL11.glGetFloat(GL11.GL_CURRENT_COLOR, buf);
		float r = buf.get(0);
		float g = buf.get(1);
		float b = buf.get(2);
		float a = buf.get(3);
		return ARGB.color(
			a == -1 ? ARGB.alpha(color) : ARGB.as8BitChannel(ARGB.alphaFloat(color) * a),
			r == -1 ? ARGB.red(color) : ARGB.as8BitChannel(ARGB.redFloat(color) * r),
			g == -1 ? ARGB.green(color) : ARGB.as8BitChannel(ARGB.greenFloat(color) * g),
			b == -1 ? ARGB.blue(color) : ARGB.as8BitChannel(ARGB.blueFloat(color) * b));
	}
}
