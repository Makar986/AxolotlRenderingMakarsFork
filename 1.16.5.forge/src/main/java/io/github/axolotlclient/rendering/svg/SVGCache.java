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
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import io.github.axolotlclient.rendering.mixin.NativeImageAccessor;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import lombok.extern.log4j.Log4j2;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.MissingTextureSprite;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.resources.IResource;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.system.MemoryUtil;

@Log4j2
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
		ResourceLocation key = new ResourceLocation(loc.getNamespace(), loc.getPath() + "/" + Float.floatToIntBits(w) + "x" + Float.floatToIntBits(h));
		if (!cache.contains(key)) {
			cache.add(key);
			Optional<IResource> res = Optional.empty();
			try {
				res = Optional.ofNullable(minecraft.getResourceManager().getResource(loc));
			} catch (Exception ignored) {
			}
			if (!res.isPresent()) {
				return MissingTextureSprite.getLocation();
			}
			NativeImage n = new NativeImage(NativeImage.PixelFormat.RGBA, (int) (w + 0.5), (int) (h + 0.5), false);
			MemoryUtil.memSet(((NativeImageAccessor) (Object) n).getPointer(), 0, (long) n.getHeight() * n.getWidth() * 4);
			DynamicTexture tex = new DynamicTexture(n);
			minecraft.getTextureManager().register(key, tex);
			IResource resource = res.get();
			CompletableFuture.runAsync(() -> {
				try (InputStream in = resource.getInputStream()) {
					byte[] img = BatikAdapter.rasterizeSvg(in, w, h);
					ByteBuffer buf = MemoryUtil.memAlloc(img.length);
					buf.put(img).flip();
					//noinspection DataFlowIssue
					MemoryUtil.memCopy(MemoryUtil.memAddress(buf), ((NativeImageAccessor) (Object) n).getPointer(), img.length);
					MemoryUtil.memFree(buf);
					minecraft.execute(tex::upload);
				} catch (Exception e) {
					log.error("Failed to load SVG resource! ", e);
					minecraft.getTextureManager().register(key, MissingTextureSprite.getTexture());
				}
			});
		}
		return key;
	}
}
