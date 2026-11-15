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

import java.lang.reflect.Field;

import com.mojang.blaze3d.platform.GlStateManager;
import io.github.axolotlclient.rendering.font.Font;
import io.github.axolotlclient.rendering.svg.SVGCache;
import io.github.axolotlclient.rendering.util.ARGB;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.resources.IResourceManagerReloadListener;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.jetbrains.annotations.ApiStatus;

@Mod("axolotlclient_rendering")
@Mod.EventBusSubscriber(modid = "axolotlclient_rendering", bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class Rendering {

	private static final Field colorField;
	private static final Field rField, gField, bField, aField;

	static {
		Field cf = null, rf = null, gf = null, bf = null, af = null;
		try {
			cf = GlStateManager.class.getDeclaredField("COLOR");
			cf.setAccessible(true);
			Class<?> colorCls = cf.getType();
			rf = colorCls.getDeclaredField("r");
			rf.setAccessible(true);
			gf = colorCls.getDeclaredField("g");
			gf.setAccessible(true);
			bf = colorCls.getDeclaredField("b");
			bf.setAccessible(true);
			af = colorCls.getDeclaredField("a");
			af.setAccessible(true);
		} catch (Throwable ignored) {
		}
		colorField = cf;
		rField = rf;
		gField = gf;
		bField = bf;
		aField = af;
	}

	public Rendering() {
	}

	@SubscribeEvent
	public static void onClientSetup(FMLClientSetupEvent event) {
		event.enqueueWork(() -> {
			ExRenderPipelines.init();
			if (Minecraft.getInstance().getResourceManager() instanceof IReloadableResourceManager) {
				((IReloadableResourceManager) Minecraft.getInstance().getResourceManager()).registerReloadListener(
					(IResourceManagerReloadListener) resourceManager -> {
						ExRenderPipelines.reload();
						Font.reload();
						SVGCache.getInstance().clear();
					}
				);
			}
		});
	}

	@ApiStatus.Internal
	public static int scaleARGB(int color) {
		float a = -1, r = -1, g = -1, b = -1;
		try {
			if (colorField != null) {
				Object cs = colorField.get(null);
				if (cs != null) {
					if (aField != null) a = aField.getFloat(cs);
					if (rField != null) r = rField.getFloat(cs);
					if (gField != null) g = gField.getFloat(cs);
					if (bField != null) b = bField.getFloat(cs);
				}
			}
		} catch (Throwable ignored) {
		}
		return ARGB.color(
			a == -1 ? ARGB.alpha(color) : ARGB.as8BitChannel(ARGB.alphaFloat(color) * a),
			r == -1 ? ARGB.red(color) : ARGB.as8BitChannel(ARGB.redFloat(color) * r),
			g == -1 ? ARGB.green(color) : ARGB.as8BitChannel(ARGB.greenFloat(color) * g),
			b == -1 ? ARGB.blue(color) : ARGB.as8BitChannel(ARGB.blueFloat(color) * b)
		);
	}
}
