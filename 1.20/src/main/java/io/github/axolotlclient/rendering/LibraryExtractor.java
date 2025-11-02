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
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.function.Consumer;

import lombok.extern.slf4j.Slf4j;
import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kamranzafar.jtar.TarEntry;
import org.kamranzafar.jtar.TarInputStream;
import org.tukaani.xz.XZInputStream;

@Slf4j
public class LibraryExtractor {

	public void run(Consumer<Path> classpathAdder) {
		if (FabricLoader.getInstance().isDevelopmentEnvironment()) return;
		try {
			var out = Files.createTempDirectory("axolotlclient_rendering_dependencies");
			Runtime.getRuntime().addShutdownHook(new Thread(() -> {
				try {
					recursiveDelete(out);
				} catch (IOException e) {
					log.warn("Failed to delete temp libraries dir!", e);
				}
			}));
			try (var in = this.getClass().getResourceAsStream("/libraries.tar.xz")) {
				if (in == null) {
					log.warn("Can't unpack libraries, assuming they are already on the classpath!");
					return;
				}

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

	private void recursiveDelete(Path p) throws IOException {
		if (!Files.exists(p)) return;
		Files.walkFileTree(p, new SimpleFileVisitor<>() {
			@Override
			public @NotNull FileVisitResult visitFile(@NotNull Path file, @NotNull BasicFileAttributes attrs) throws IOException {
				Files.deleteIfExists(file);
				return super.visitFile(file, attrs);
			}

			@Override
			public @NotNull FileVisitResult postVisitDirectory(@NotNull Path dir, @Nullable IOException exc) throws IOException {
				Files.deleteIfExists(dir);
				return super.postVisitDirectory(dir, exc);
			}
		});
		Files.deleteIfExists(p);
	}
}
