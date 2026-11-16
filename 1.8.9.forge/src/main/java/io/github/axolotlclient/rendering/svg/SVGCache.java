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

package io.github.axolotlclient.rendering.svg;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import lombok.extern.log4j.Log4j2;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;

@Log4j2
public class SVGCache {
	private static final SVGCache INSTANCE = new SVGCache();

	public static SVGCache getInstance() {
		return INSTANCE;
	}

	private final ObjectSet<ResourceLocation> cache = new ObjectOpenHashSet<>();
	private final Minecraft minecraft = Minecraft.getMinecraft();

	private SVGCache() {
	}

	public void clear() {
		cache.clear();
	}

	public ResourceLocation load(ResourceLocation loc, float w, float h) {
		ResourceLocation key = new ResourceLocation(loc.getResourceDomain(), loc.getResourcePath() + "/" + Float.floatToIntBits(w) + "x" + Float.floatToIntBits(h));
		if (!cache.contains(key)) {
			cache.add(key);
			DynamicTexture tex = new DynamicTexture((int) (w + 0.5), (int) (h + 0.5));
			minecraft.getTextureManager().loadTexture(key, tex);
			CompletableFuture.runAsync(() -> {
				try {
					IResource res = minecraft.getResourceManager().getResource(loc);
					try (InputStream in = res.getInputStream()) {
						byte[] img = BatikAdapter.rasterizeSvg(in, w, h);
						ByteBuffer buf = ByteBuffer.wrap(img);
						buf.asIntBuffer().get(tex.getTextureData());
						for (int x = 0; x < tex.getTextureData().length; x++) {
							tex.getTextureData()[x] = Integer.rotateRight(tex.getTextureData()[x], 8);
						}
						minecraft.addScheduledTask(tex::updateDynamicTexture);
					}
				} catch (Exception e) {
					log.error("Failed to load SVG resource! ", e);
					minecraft.getTextureManager().loadTexture(key, TextureUtil.missingTexture);
				}
			});
		}
		return key;
	}
}
