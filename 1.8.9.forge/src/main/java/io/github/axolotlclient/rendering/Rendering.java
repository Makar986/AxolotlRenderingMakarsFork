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

import io.github.axolotlclient.rendering.font.Font;
import io.github.axolotlclient.rendering.svg.SVGCache;
import io.github.axolotlclient.rendering.util.ARGB;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import org.jetbrains.annotations.ApiStatus;

import java.lang.reflect.Field;

@Slf4j
@Mod(modid = "axolotlclient_rendering", name = "AxolotlClient Rendering", version = "1.0.0", clientSideOnly = true)
public class Rendering {

	private static Field colorStateField;
	private static Field colorRedField;
	private static Field colorGreenField;
	private static Field colorBlueField;
	private static Field colorAlphaField;

	static {
		try {
			for (Field f : GlStateManager.class.getDeclaredFields()) {
				if (f.getType().getName().endsWith("Color") || f.getName().equals("colorState")) {
					f.setAccessible(true);
					colorStateField = f;
					Class<?> colorClass = f.getType();
					for (Field cf : colorClass.getDeclaredFields()) {
						if (cf.getType() == float.class) {
							cf.setAccessible(true);
							if (cf.getName().equals("red") || cf.getName().equals("r")) colorRedField = cf;
							else if (cf.getName().equals("green") || cf.getName().equals("g")) colorGreenField = cf;
							else if (cf.getName().equals("blue") || cf.getName().equals("b")) colorBlueField = cf;
							else if (cf.getName().equals("alpha") || cf.getName().equals("a")) colorAlphaField = cf;
						}
					}
					break;
				}
			}
		} catch (Throwable t) {
			log.warn("Could not inspect GlStateManager color state fields", t);
		}
	}

	@Mod.EventHandler
	public void onInit(FMLInitializationEvent event) {
		ExRenderPipelines.init();
		Minecraft mc = Minecraft.getMinecraft();
		if (mc != null && mc.getResourceManager() instanceof IReloadableResourceManager) {
			((IReloadableResourceManager) mc.getResourceManager()).registerReloadListener(new IResourceManagerReloadListener() {
				@Override
				public void onResourceManagerReload(IResourceManager resourceManager) {
					Font.reload();
					SVGCache.getInstance().clear();
				}
			});
		}
	}

	@ApiStatus.Internal
	public static void resetWindow() {
		((DrawUtil) DrawUtil.get()).resetWindow();
	}

	@ApiStatus.Internal
	public static int scaleARGB(int color) {
		float a = -1, r = -1, g = -1, b = -1;
		try {
			if (colorStateField != null) {
				Object cs = colorStateField.get(null);
				if (cs != null) {
					if (colorAlphaField != null) a = colorAlphaField.getFloat(cs);
					if (colorRedField != null) r = colorRedField.getFloat(cs);
					if (colorGreenField != null) g = colorGreenField.getFloat(cs);
					if (colorBlueField != null) b = colorBlueField.getFloat(cs);
				}
			}
		} catch (Throwable ignored) {
		}
		return ARGB.color(
			a == -1 ? ARGB.alpha(color) : ARGB.as8BitChannel(ARGB.alphaFloat(color) * a),
			r == -1 ? ARGB.red(color) : ARGB.as8BitChannel(ARGB.redFloat(color) * r),
			g == -1 ? ARGB.green(color) : ARGB.as8BitChannel(ARGB.greenFloat(color) * g),
			b == -1 ? ARGB.blue(color) : ARGB.as8BitChannel(ARGB.blueFloat(color) * b));
	}
}
