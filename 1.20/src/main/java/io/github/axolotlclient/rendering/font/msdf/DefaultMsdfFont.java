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

package io.github.axolotlclient.rendering.font.msdf;

import java.io.InputStream;

public final class DefaultMsdfFont {
	private static MsdfFont INSTANCE;

	private DefaultMsdfFont() {
	}

	public static synchronized MsdfFont get() {
		if (INSTANCE == null) {
			try (InputStream json = DefaultMsdfFont.class.getResourceAsStream("/assets/axolotlclient-rendering/fonts/msdf/default.json");
				 InputStream png = DefaultMsdfFont.class.getResourceAsStream("/assets/axolotlclient-rendering/fonts/msdf/default.png")) {
				if (json == null || png == null) {
					throw new java.io.FileNotFoundException("Default Montserrat MSDF font assets not found: " +
						(json == null ? "default.json is missing; " : "") +
						(png == null ? "default.png is missing" : ""));
				}
				INSTANCE = MsdfFont.load(json, png, "montserrat");
			} catch (Exception e) {
				throw new RuntimeException("Failed to load default Montserrat MSDF font", e);
			}
		}
		return INSTANCE;
	}

	public static MsdfFont montserrat() {
		return get();
	}
}
