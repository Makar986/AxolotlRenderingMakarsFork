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

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import io.github.axolotlclient.rendering.ExRenderPipelines;
import io.github.axolotlclient.rendering.RenderingCommon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

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
	private final ResourceLocation textureLocation;
	private final DynamicTexture dynamicTexture;

	public MsdfFont(MsdfFontData data, ResourceLocation textureLocation, DynamicTexture dynamicTexture) {
		this.data = data;
		this.textureLocation = textureLocation;
		this.dynamicTexture = dynamicTexture;
	}

	public static MsdfFont load(InputStream jsonStream, InputStream pngStream, String name) {
		MsdfFontData fontData = MsdfFontData.parse(jsonStream);
		try (pngStream) {
			NativeImage nativeImage = NativeImage.read(pngStream);
			DynamicTexture texture = new DynamicTexture(nativeImage);
			String id = (name != null ? name.toLowerCase().replaceAll("[^a-z0-9_.-]", "_") : "font") + "_" + ID_GEN.getAndIncrement();
			ResourceLocation location = new ResourceLocation(RenderingCommon.MODID, "msdf_" + id);
			Minecraft.getInstance().getTextureManager().register(location, texture);
			// MSDF needs bilinear sampling of the atlas: with the default GL_NEAREST the distance field turns into visible pixel steps.
			texture.setFilter(true, false);
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

	public ResourceLocation getTextureLocation() {
		return textureLocation;
	}

	public void draw(GuiGraphics graphics, String text, float x, float y, float size, int color) {
		draw(graphics.pose().last().pose(), text, x, y, size, color);
	}

	public void draw(Matrix4f matrix, String text, float x, float y, float size, int color) {
		if (text == null || text.isEmpty()) return;
		if ((color & 0xFF000000) == 0) color |= 0xFF000000;
		List<MsdfGlyphQuad> quads = data.layout(text, x, y, size);
		if (quads.isEmpty()) return;

		if (textureLocation != null) {
			RenderSystem.setShaderTexture(0, textureLocation);
		}
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.setShader(() -> ExRenderPipelines.MSDF_FONT_SHADER);

		BufferBuilder consumer = Tesselator.getInstance().getBuilder();
		consumer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);

		for (MsdfGlyphQuad q : quads) {
			consumer.vertex(matrix, q.x0, q.y0, 0).uv(q.u0, q.v0).color(color).endVertex();
			consumer.vertex(matrix, q.x0, q.y1, 0).uv(q.u0, q.v1).color(color).endVertex();
			consumer.vertex(matrix, q.x1, q.y1, 0).uv(q.u1, q.v1).color(color).endVertex();
			consumer.vertex(matrix, q.x1, q.y0, 0).uv(q.u1, q.v0).color(color).endVertex();
		}

		BufferUploader.drawWithShader(consumer.end());
	}

	public void drawWithShadow(GuiGraphics graphics, String text, float x, float y, float size, int color) {
		drawWithShadow(graphics.pose().last().pose(), text, x, y, size, color);
	}

	public void drawWithShadow(Matrix4f matrix, String text, float x, float y, float size, int color) {
		if ((color & 0xFF000000) == 0) color |= 0xFF000000;
		int shadowColor = ((color & 0xFCFCFC) >> 2) | (color & 0xFF000000);
		float offset = Math.max(1.0f, size * 0.05f);
		draw(matrix, text, x + offset, y + offset, size, shadowColor);
		draw(matrix, text, x, y, size, color);
	}

	public void drawCentered(GuiGraphics graphics, String text, float x, float y, float size, int color) {
		float width = getWidth(text, size);
		draw(graphics, text, x - width * 0.5f, y, size, color);
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
			Minecraft.getInstance().getTextureManager().release(textureLocation);
		} else if (dynamicTexture != null) {
			dynamicTexture.close();
		}
	}
}
