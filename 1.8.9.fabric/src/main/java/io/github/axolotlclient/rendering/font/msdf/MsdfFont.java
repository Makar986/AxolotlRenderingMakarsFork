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

import net.minecraft.client.render.platform.GlStateManager;
import net.minecraft.client.render.vertex.BufferBuilder;
import net.minecraft.client.render.vertex.DefaultVertexFormat;
import net.minecraft.client.render.vertex.Tesselator;
import io.github.axolotlclient.rendering.ExRenderPipelines;
import io.github.axolotlclient.rendering.RenderingCommon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.texture.DynamicTexture;
import net.minecraft.resource.Identifier;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
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

public class MsdfFont implements Closeable {
	private static final AtomicInteger ID_GEN = new AtomicInteger(1);

	private final MsdfFontData data;
	private final Identifier textureLocation;
	private final DynamicTexture dynamicTexture;

	public MsdfFont(MsdfFontData data, Identifier textureLocation, DynamicTexture dynamicTexture) {
		this.data = data;
		this.textureLocation = textureLocation;
		this.dynamicTexture = dynamicTexture;
	}

	public static MsdfFont load(InputStream jsonStream, InputStream pngStream, String name) {
		MsdfFontData fontData = MsdfFontData.parse(jsonStream);
		try (pngStream) {
			BufferedImage bufferedImage = ImageIO.read(pngStream);
			DynamicTexture texture = new DynamicTexture(bufferedImage);
			String id = (name != null ? name.toLowerCase().replaceAll("[^a-z0-9_.-]", "_") : "font") + "_" + ID_GEN.getAndIncrement();
			Identifier location = new Identifier(RenderingCommon.MODID, "msdf_" + id);
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

	public void draw(String text, float x, float y, float size, int color) {
		if (text == null || text.isEmpty()) return;
		if ((color & 0xFF000000) == 0) color |= 0xFF000000;
		List<MsdfGlyphQuad> quads = data.layout(text, x, y, size);
		if (quads.isEmpty()) return;

		var shader = ExRenderPipelines.MSDF_FONT_PROGRAM;
		if (shader == null) return;

		if (textureLocation != null) {
			Minecraft.getInstance().getTextureManager().bind(textureLocation);
		}
		GlStateManager.enableBlend();
		GlStateManager.blendFuncSeparate(770, 771, 1, 0);
		shader.bind();

		int a = (color >> 24) & 0xFF;
		int r = (color >> 16) & 0xFF;
		int g = (color >> 8) & 0xFF;
		int b = color & 0xFF;

		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder consumer = tesselator.getBuffer();
		consumer.begin(7, DefaultVertexFormat.POSITION_TEX_COLOR);

		for (MsdfGlyphQuad q : quads) {
			consumer.vertex(q.x0, q.y0, 0.0).texture(q.u0, q.v0).color(r, g, b, a).nextVertex();
			consumer.vertex(q.x0, q.y1, 0.0).texture(q.u0, q.v1).color(r, g, b, a).nextVertex();
			consumer.vertex(q.x1, q.y1, 0.0).texture(q.u1, q.v1).color(r, g, b, a).nextVertex();
			consumer.vertex(q.x1, q.y0, 0.0).texture(q.u1, q.v0).color(r, g, b, a).nextVertex();
		}

		tesselator.end();
		shader.unbind();
	}

	public void drawWithShadow(String text, float x, float y, float size, int color) {
		if ((color & 0xFF000000) == 0) color |= 0xFF000000;
		int shadowColor = ((color & 0xFCFCFC) >> 2) | (color & 0xFF000000);
		float offset = Math.max(1.0f, size * 0.05f);
		draw(text, x + offset, y + offset, size, shadowColor);
		draw(text, x, y, size, color);
	}

	public void drawCentered(String text, float x, float y, float size, int color) {
		float width = getWidth(text, size);
		draw(text, x - width * 0.5f, y, size, color);
	}

	public MsdfFontAdapter asAdapter(float size) {
		return new MsdfFontAdapter(this, size);
	}

	public MsdfFontAdapter asAdapter() {
		return new MsdfFontAdapter(this, 9.0f);
	}

	@Override
	public void close() {
		if (textureLocation != null && Minecraft.getInstance() != null && Minecraft.getInstance().getTextureManager() != null) {
			Minecraft.getInstance().getTextureManager().close(textureLocation);
		}
		if (dynamicTexture != null) {
			dynamicTexture.clearGlId();
		}
	}
}
