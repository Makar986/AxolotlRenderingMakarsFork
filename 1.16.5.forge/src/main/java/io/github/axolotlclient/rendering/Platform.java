/*
 * Copyright © 2026 moehreag <moehreag@gmail.com> & Contributors
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

import java.util.function.Consumer;

import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import org.jetbrains.annotations.ApiStatus;

/**
 * Forge 1.16.5 stand-ins for the few Fabric API hooks the shared 1.16.5 code uses.
 */
@ApiStatus.Internal
public final class Platform {
	private Platform() {
	}

	/**
	 * Registers a synchronous client resource reload listener. It runs on the render thread during
	 * every resource reload, including the initial one after mod loading.
	 */
	public static void registerReloadListener(Consumer<ResourceManager> listener) {
		((ReloadableResourceManager) Minecraft.getInstance().getResourceManager())
			.registerReloadListener((ResourceManagerReloadListener) listener::accept);
	}

	/**
	 * Fabric frees GL/native resources on CLIENT_STOPPING. Forge 1.16.5 has no client-stopping event,
	 * and the process exits right after, so there is nothing to hook here.
	 */
	public static void onClientStopping(Runnable action) {
	}
}
