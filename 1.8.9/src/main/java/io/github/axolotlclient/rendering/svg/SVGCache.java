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

import java.nio.ByteBuffer;

import com.mojang.blaze3d.platform.TextureUtil;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.texture.DynamicTexture;
import net.minecraft.resource.Identifier;
import net.ornithemc.osl.resource.loader.api.ResourceLoaderEvents;

@Slf4j
public class SVGCache {
	private static final SVGCache INSTANCE = new SVGCache();

	public static SVGCache getInstance() {
		return INSTANCE;
	}

	private final ObjectSet<Identifier> cache = new ObjectOpenHashSet<>();
	private final Minecraft minecraft = Minecraft.getInstance();

	private SVGCache() {
		ResourceLoaderEvents.END_RESOURCE_RELOAD.register(cache::clear);
	}

	public Identifier load(Identifier loc, float w, float h) {
		var key = new Identifier(loc.getNamespace(), loc.getPath() + "/" + w + "x" + h);
		if (!cache.contains(key)) {
			cache.add(key);
			try {
				var res = minecraft.getResourceManager().getResource(loc);
				try (var in = res.asStream()) {
					var img = BatikAdapter.rasterizeSvg(in, w, h);
					var tex = new DynamicTexture((int) (w + 0.5), (int) (h + 0.5));
					var buf = ByteBuffer.wrap(img);
					buf.asIntBuffer().get(tex.getPixels());
					for (int x = 0; x < tex.getPixels().length; x++) {
						tex.getPixels()[x] = Integer.rotateRight(tex.getPixels()[x], 8);
					}
					tex.upload();
					minecraft.getTextureManager().register(key, tex);
				}
			} catch (Exception e) {
				log.error("Failed to load SVG resource! ", e);
				minecraft.getTextureManager().register(key, TextureUtil.MISSING_TEXTURE);
			}
		}
		return key;
	}
}
