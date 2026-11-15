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

import java.util.concurrent.CompletableFuture;

import com.mojang.blaze3d.platform.NativeImage;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.resources.Identifier;
import org.lwjgl.system.MemoryUtil;

@Slf4j
public class SVGCache {
	private static final SVGCache INSTANCE = new SVGCache();

	public static SVGCache getInstance() {
		return INSTANCE;
	}

	private final ObjectSet<Identifier> cache = new ObjectOpenHashSet<>();
	private final Minecraft minecraft = Minecraft.getInstance();

	private SVGCache() {
	}

	public void clear() {
		cache.clear();
	}

	public Identifier load(Identifier loc, float w, float h) {
		var key = loc.withSuffix("/" + Float.floatToIntBits(w) + "x" + Float.floatToIntBits(h));
		if (!cache.contains(key)) {
			cache.add(key);
			var res = minecraft.getResourceManager().getResource(loc);
			if (res.isEmpty()) {
				return MissingTextureAtlasSprite.getLocation();
			}
			var n = new NativeImage(NativeImage.Format.RGBA, (int) (w + 0.5), (int) (h + 0.5), false);
			MemoryUtil.memSet(n.getPointer(), 0, (long) n.getHeight() * n.getWidth() * 4);
			var tex = new DynamicTexture(key::toString, n);
			minecraft.getTextureManager().register(key, tex);
			CompletableFuture.runAsync(() -> {
				try (var in = res.get().open()) {
					var img = BatikAdapter.rasterizeSvg(in, w, h);
					var buf = MemoryUtil.memAlloc(img.length);
					buf.put(img).flip();
					MemoryUtil.memCopy(MemoryUtil.memAddress(buf), n.getPointer(), img.length);
					MemoryUtil.memFree(buf);
					minecraft.execute(tex::upload);
				} catch (Exception e) {
					log.error("Failed to load SVG resource! ", e);
					minecraft.getTextureManager().register(key, minecraft.getTextureManager().getTexture(MissingTextureAtlasSprite.getLocation()));
				}
			});
		}
		return key;
	}
}
