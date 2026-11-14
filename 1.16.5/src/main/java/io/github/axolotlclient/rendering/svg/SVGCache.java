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

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import com.mojang.blaze3d.platform.NativeImage;
import io.github.axolotlclient.rendering.RenderingCommon;
import io.github.axolotlclient.rendering.mixin.NativeImageAccessor;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import lombok.extern.log4j.Log4j2;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
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
		ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
			@Override
			public ResourceLocation getFabricId() {
				return new ResourceLocation(RenderingCommon.MODID, "svgcache");
			}

			@Override
			public void onResourceManagerReload(ResourceManager resourceManager) {
				cache.clear();
			}
		});
	}

	public ResourceLocation load(ResourceLocation loc, float w, float h) {
		var key = new ResourceLocation(loc.getNamespace(), loc.getPath() + "/" + Float.floatToIntBits(w) + "x" + Float.floatToIntBits(h));
		if (!cache.contains(key)) {
			cache.add(key);
			Optional<Resource> res = Optional.empty();
			try {
				res = Optional.ofNullable(minecraft.getResourceManager().getResource(loc));
			} catch (Exception ignored) {
			}
			if (res.isEmpty()) {
				return MissingTextureAtlasSprite.getLocation();
			}
			var n = new NativeImage(NativeImage.Format.RGBA, (int) (w + 0.5), (int) (h + 0.5), false);
			MemoryUtil.memSet(((NativeImageAccessor) (Object) n).getPointer(), 0, (long) n.getHeight() * n.getWidth() * 4);
			var tex = new DynamicTexture(n);
			minecraft.getTextureManager().register(key, tex);
			var resource = res.get();
			CompletableFuture.runAsync(() -> {
				try (var in = resource.getInputStream()) {
					var img = BatikAdapter.rasterizeSvg(in, w, h);
					var buf = MemoryUtil.memAlloc(img.length);
					buf.put(img).flip();
					//noinspection DataFlowIssue
					MemoryUtil.memCopy(MemoryUtil.memAddress(buf), ((NativeImageAccessor) (Object) n).getPointer(), img.length);
					MemoryUtil.memFree(buf);
					minecraft.execute(tex::upload);
				} catch (Exception e) {
					log.error("Failed to load SVG resource! ", e);
					minecraft.getTextureManager().register(key, MissingTextureAtlasSprite.getTexture());
				}
			});
		}
		return key;
	}
}
