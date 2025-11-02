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
import io.github.axolotlclient.rendering.RenderingCommon;
import io.github.axolotlclient.rendering.mixin.NativeImageAccessor;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import lombok.extern.slf4j.Slf4j;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import org.lwjgl.system.MemoryUtil;

@Slf4j
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
		var key = loc.withSuffix("/" + Float.floatToIntBits(w) + "x" + Float.floatToIntBits(h));
		if (!cache.contains(key)) {
			cache.add(key);
			var res = minecraft.getResourceManager().getResource(loc);
			if (res.isEmpty()) {
				return MissingTextureAtlasSprite.getLocation();
			}
			try (var in = res.get().open()) {
				var img = BatikAdapter.rasterizeSvg(in, w, h);
				var n = new NativeImage(NativeImage.Format.RGBA, (int) (w + 0.5), (int) (h + 0.5), false);
				var buf = MemoryUtil.memAlloc(img.length);
				buf.put(img).flip();
				//noinspection DataFlowIssue
				MemoryUtil.memCopy(MemoryUtil.memAddress(buf), ((NativeImageAccessor) (Object) n).getPointer(), img.length);
				MemoryUtil.memFree(buf);
				minecraft.getTextureManager().register(key, new DynamicTexture(n));
			} catch (Exception e) {
				log.error("Failed to load SVG resource! ", e);
				return MissingTextureAtlasSprite.getLocation();
			}
		}
		return key;
	}
}
