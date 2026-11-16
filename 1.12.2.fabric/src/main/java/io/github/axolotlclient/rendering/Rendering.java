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
import io.github.axolotlclient.rendering.mixin.GlStateManagerAccessor;
import io.github.axolotlclient.rendering.util.ARGB;
import lombok.extern.slf4j.Slf4j;
import net.fabricmc.api.ClientModInitializer;
import net.ornithemc.osl.lifecycle.api.client.MinecraftClientEvents;
import net.ornithemc.osl.resource.loader.api.client.ClientResourceLoaderEvents;
import org.jetbrains.annotations.ApiStatus;

@Slf4j
public class Rendering implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		ExRenderPipelines.init();
		ClientResourceLoaderEvents.END_RESOURCE_RELOAD.register((manager, ctx) -> Font.reload());
		MinecraftClientEvents.STOP.register(mc -> Font.closeAll());
	}

	@ApiStatus.Internal
	public static void resetWindow() {
		((DrawUtil)DrawUtil.get()).resetWindow();
	}

	@ApiStatus.Internal
	public static int scaleARGB(int color) {
		return ARGB.color(
			GlStateManagerAccessor.getColor().a == -1 ? ARGB.alpha(color) : ARGB.as8BitChannel(ARGB.alphaFloat(color)*GlStateManagerAccessor.getColor().a),
			GlStateManagerAccessor.getColor().r == -1 ? ARGB.red(color) : ARGB.as8BitChannel(ARGB.redFloat(color) * GlStateManagerAccessor.getColor().r),
			GlStateManagerAccessor.getColor().g == -1 ? ARGB.green(color) : ARGB.as8BitChannel(ARGB.greenFloat(color) * GlStateManagerAccessor.getColor().g),
			GlStateManagerAccessor.getColor().b == -1 ? ARGB.blue(color) : ARGB.as8BitChannel(ARGB.blueFloat(color) * GlStateManagerAccessor.getColor().b));
	}
}
