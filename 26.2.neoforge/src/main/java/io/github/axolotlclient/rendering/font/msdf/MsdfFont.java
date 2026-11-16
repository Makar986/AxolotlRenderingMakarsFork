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

package io.github.axolotlclient.rendering.font.msdf;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import io.github.axolotlclient.rendering.RenderingCommon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;

/**
 * Multi-channel signed distance field font. Draw it through
 * {@code GuiGraphicsExtension#axolotlclient_rendering$drawMsdfString}.
 */
public class MsdfFont implements Closeable {
	private static final AtomicInteger ID_GEN = new AtomicInteger(1);

	private final MsdfFontData data;
	private final Identifier textureLocation;
	private final DynamicTexture texture;

	public MsdfFont(MsdfFontData data, Identifier textureLocation, DynamicTexture texture) {
		this.data = data;
		this.textureLocation = textureLocation;
		this.texture = texture;
	}

	public static MsdfFont load(InputStream jsonStream, InputStream pngStream, String name) {
		MsdfFontData fontData = MsdfFontData.parse(jsonStream);
		try (pngStream) {
			NativeImage image = NativeImage.read(pngStream);
			String id = (name != null ? name.toLowerCase().replaceAll("[^a-z0-9_.-]", "_") : "font") + "_" + ID_GEN.getAndIncrement();
			DynamicTexture texture = new DynamicTexture(() -> "axolotlclient-rendering msdf " + id, image);
			Identifier location = Identifier.fromNamespaceAndPath(RenderingCommon.MODID, "msdf_" + id);
			Minecraft.getInstance().getTextureManager().register(location, texture);
			return new MsdfFont(fontData, location, texture);
		} catch (IOException e) {
			throw new RuntimeException("Failed to load MSDF atlas texture for " + name, e);
		}
	}

	public static MsdfFont fromZip(Path zipPath, String fontName) throws IOException {
		return fromZip(zipPath.toFile(), fontName);
	}

	public static MsdfFont fromZip(String zipPath, String fontName) throws IOException {
		return fromZip(new File(zipPath), fontName);
	}

	public static MsdfFont fromZip(File zipFile, String fontName) throws IOException {
		try (ZipFile zip = new ZipFile(zipFile)) {
			ZipEntry jsonEntry = null;
			ZipEntry pngEntry = null;
			Enumeration<? extends ZipEntry> entries = zip.entries();
			while (entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();
				String name = entry.getName();
				if (name.contains(fontName)) {
					if (name.endsWith(".json")) {
						jsonEntry = entry;
					} else if (name.endsWith(".png")) {
						pngEntry = entry;
					}
				}
			}
			if (jsonEntry == null || pngEntry == null) {
				throw new FileNotFoundException("MSDF font files matching '" + fontName + "' not found in " + zipFile.getAbsolutePath());
			}
			try (InputStream jsonIn = zip.getInputStream(jsonEntry);
				 InputStream pngIn = zip.getInputStream(pngEntry)) {
				return load(jsonIn, pngIn, fontName);
			}
		}
	}

	public float getWidth(String text, float size) {
		return data.getWidth(text, size);
	}

	public float getHeight(float size) {
		return data.getHeight(size);
	}

	public MsdfFontData getData() {
		return data;
	}

	public Identifier getTextureLocation() {
		return textureLocation;
	}

	public List<MsdfGlyphQuad> layout(String text, float x, float y, float size) {
		return data.layout(text, x, y, size);
	}

	/**
	 * The atlas with a bilinear sampler: MSDF needs interpolated distances, with nearest sampling the glyph edges
	 * turn into visible pixel steps.
	 */
	public TextureSetup textureSetup() {
		return TextureSetup.singleTexture(texture.getTextureView(), RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR));
	}

	@Override
	public void close() {
		Minecraft mc = Minecraft.getInstance();
		if (mc != null && mc.getTextureManager() != null) {
			mc.getTextureManager().release(textureLocation);
		}
	}
}
