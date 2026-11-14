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

import com.mojang.blaze3d.platform.NativeImage;
import io.github.axolotlclient.rendering.mixin.NativeImageAccessor;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.system.MemoryUtil;

import java.io.InputStream;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class SVGCache {
	private static final SVGCache INSTANCE = new SVGCache();

	public static SVGCache getInstance() {
		return INSTANCE;
	}

	private final ObjectSet<ResourceLocation> cache = new ObjectOpenHashSet<>();
	private final Minecraft minecraft = Minecraft.getInstance();

	private SVGCache() {
	}

	public void clear() {
		cache.clear();
	}

	public ResourceLocation load(ResourceLocation loc, float w, float h) {
		var key = new ResourceLocation(loc.getNamespace(), loc.getPath() + "/" + Float.floatToIntBits(w) + "x" + Float.floatToIntBits(h));
		if (!cache.contains(key)) {
			cache.add(key);
			var tex = new DynamicTexture((int) (w + 0.5), (int) (h + 0.5), false);
			minecraft.getTextureManager().register(key, tex);
			CompletableFuture.runAsync(() -> {
				var res = minecraft.getResourceManager().getResource(loc);
				if (res.isEmpty()) {
					log.error("SVG resource {} not found!", loc);
					minecraft.getTextureManager().register(key, MissingTextureAtlasSprite.getTexture());
					return;
				}
				try (InputStream in = res.get().open()) {
					var img = BatikAdapter.rasterizeSvg(in, w, h);
					NativeImage pixels = tex.getPixels();
					if (pixels != null) {
						var buf = MemoryUtil.memAlloc(img.length);
						try {
							buf.put(img).flip();
							MemoryUtil.memCopy(MemoryUtil.memAddress(buf), ((NativeImageAccessor) (Object) pixels).getPointer(), img.length);
						} finally {
							MemoryUtil.memFree(buf);
						}
						minecraft.execute(tex::upload);
					}
				} catch (Exception e) {
					log.error("Failed to load SVG resource! ", e);
					minecraft.getTextureManager().register(key, MissingTextureAtlasSprite.getTexture());
				}
			});
		}
		return key;
	}
}
