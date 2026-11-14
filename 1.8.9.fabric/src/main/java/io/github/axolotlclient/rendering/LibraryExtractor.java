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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

import lombok.extern.slf4j.Slf4j;
import net.fabricmc.loader.api.FabricLoader;
import org.kamranzafar.jtar.TarEntry;
import org.kamranzafar.jtar.TarInputStream;
import org.tukaani.xz.XZInputStream;

@Slf4j
public class LibraryExtractor {

	private static final String MODID = "axolotlclient-rendering";

	public void run(Consumer<Path> classpathAdder) {
		if (FabricLoader.getInstance().isDevelopmentEnvironment()) return;
		try {
			var container = FabricLoader.getInstance().getModContainer(MODID).orElseThrow();
			var version = container.getMetadata().getVersion().getFriendlyString();
			var out = Path.of(System.getProperty("java.io.tmpdir"), MODID+"_"+version+"_dependencies");
			if (Files.exists(out)) {
				try (var libraries = Files.list(out)) {
					libraries.forEach(classpathAdder);
				}
				return;
			}
			Files.createDirectories(out);
			var path = container.findPath("libraries.tar.xz").orElseThrow(IOException::new);
			try (var in = Files.newInputStream(path)) {

				try (var buf = new BufferedInputStream(in);
					 var xz = new XZInputStream(buf);
					 var tar = new TarInputStream(xz)) {
					TarEntry currentEntry;
					while ((currentEntry = tar.getNextEntry()) != null) {
						var entry = tar.readNBytes((int) currentEntry.getSize());
						var res = out.resolve(currentEntry.getName());
						Files.write(res, entry);
						classpathAdder.accept(res);
					}
				}

			}
		} catch (IOException e) {
			log.error("Failed to extract libraries, the game is likely to crash!", e);
		}
	}
}
