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
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;

@Mod(value = "axolotlclient_rendering", dist = Dist.CLIENT)
public class Rendering {

	public Rendering(IEventBus modBus) {
		modBus.addListener(this::onRegisterReloadListeners);
		modBus.addListener(ExRenderPipelines::init);
	}

	private void onRegisterReloadListeners(AddClientReloadListenersEvent event) {
		event.addListener(Identifier.fromNamespaceAndPath(RenderingCommon.MODID, "font_reloader"),
			new SimplePreparableReloadListener<Void>() {
				@Override
				protected Void prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
					return null;
				}

				@Override
				protected void apply(Void object, ResourceManager resourceManager, ProfilerFiller profiler) {
					Font.reload();
					SVGCache.getInstance().clear();
				}
			});
	}
}
