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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MsdfFontData {
	private final String atlasType;
	private final float distanceRange;
	private final float nominalSize;
	private final int atlasWidth;
	private final int atlasHeight;
	private final boolean yOriginBottom;

	private final float emSize;
	private final float lineHeight;
	private final float ascender;
	private final float descender;
	private final float underlineY;
	private final float underlineThickness;

	private final Map<Integer, MsdfGlyph> glyphs = new HashMap<>();
	private final Map<Long, Float> kerning = new HashMap<>();

	public MsdfFontData(JsonObject root) {
		JsonObject atlas = root.getAsJsonObject("atlas");
		this.atlasType = atlas != null && atlas.has("type") ? atlas.get("type").getAsString() : "msdf";
		this.distanceRange = atlas != null && atlas.has("distanceRange") ? atlas.get("distanceRange").getAsFloat() : 4.0f;
		this.nominalSize = atlas != null && atlas.has("size") ? atlas.get("size").getAsFloat() : 32.0f;
		this.atlasWidth = atlas != null && atlas.has("width") ? atlas.get("width").getAsInt() : 256;
		this.atlasHeight = atlas != null && atlas.has("height") ? atlas.get("height").getAsInt() : 256;
		String yOrig = atlas != null && atlas.has("yOrigin") ? atlas.get("yOrigin").getAsString() : "bottom";
		this.yOriginBottom = "bottom".equalsIgnoreCase(yOrig);

		JsonObject metrics = root.getAsJsonObject("metrics");
		this.emSize = metrics != null && metrics.has("emSize") ? metrics.get("emSize").getAsFloat() : 1.0f;
		this.lineHeight = metrics != null && metrics.has("lineHeight") ? metrics.get("lineHeight").getAsFloat() : 1.2f;
		this.ascender = metrics != null && metrics.has("ascender") ? metrics.get("ascender").getAsFloat() : 0.95f;
		this.descender = metrics != null && metrics.has("descender") ? metrics.get("descender").getAsFloat() : -0.25f;
		this.underlineY = metrics != null && metrics.has("underlineY") ? metrics.get("underlineY").getAsFloat() : -0.1f;
		this.underlineThickness = metrics != null && metrics.has("underlineThickness") ? metrics.get("underlineThickness").getAsFloat() : 0.05f;

		if (root.has("glyphs") && root.get("glyphs").isJsonArray()) {
			JsonArray glyphArray = root.getAsJsonArray("glyphs");
			for (JsonElement elem : glyphArray) {
				if (!elem.isJsonObject()) continue;
				JsonObject g = elem.getAsJsonObject();
				int unicode = g.has("unicode") ? g.get("unicode").getAsInt() : 0;
				float advance = g.has("advance") ? g.get("advance").getAsFloat() : 0.0f;

				boolean hasPlaneBounds = g.has("planeBounds") && g.get("planeBounds").isJsonObject();
				float pLeft = 0, pBottom = 0, pRight = 0, pTop = 0;
				if (hasPlaneBounds) {
					JsonObject pb = g.getAsJsonObject("planeBounds");
					pLeft = pb.has("left") ? pb.get("left").getAsFloat() : 0;
					pBottom = pb.has("bottom") ? pb.get("bottom").getAsFloat() : 0;
					pRight = pb.has("right") ? pb.get("right").getAsFloat() : 0;
					pTop = pb.has("top") ? pb.get("top").getAsFloat() : 0;
				}

				boolean hasAtlasBounds = g.has("atlasBounds") && g.get("atlasBounds").isJsonObject();
				float aLeft = 0, aBottom = 0, aRight = 0, aTop = 0;
				if (hasAtlasBounds) {
					JsonObject ab = g.getAsJsonObject("atlasBounds");
					aLeft = ab.has("left") ? ab.get("left").getAsFloat() : 0;
					aBottom = ab.has("bottom") ? ab.get("bottom").getAsFloat() : 0;
					aRight = ab.has("right") ? ab.get("right").getAsFloat() : 0;
					aTop = ab.has("top") ? ab.get("top").getAsFloat() : 0;
				}

				MsdfGlyph glyph = new MsdfGlyph(unicode, advance, pLeft, pBottom, pRight, pTop, aLeft, aBottom, aRight, aTop, hasPlaneBounds);
				glyphs.put(unicode, glyph);
			}
		}

		if (root.has("kerning") && root.get("kerning").isJsonArray()) {
			JsonArray kerningArray = root.getAsJsonArray("kerning");
			for (JsonElement elem : kerningArray) {
				if (!elem.isJsonObject()) continue;
				JsonObject k = elem.getAsJsonObject();
				int u1 = k.has("unicode1") ? k.get("unicode1").getAsInt() : 0;
				int u2 = k.has("unicode2") ? k.get("unicode2").getAsInt() : 0;
				float adv = k.has("advance") ? k.get("advance").getAsFloat() : 0;
				kerning.put(packKerningKey(u1, u2), adv);
			}
		}
	}

	public static MsdfFontData parse(InputStream inputStream) {
		try (Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
			JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
			return new MsdfFontData(root);
		} catch (Exception e) {
			throw new RuntimeException("Failed to parse MSDF font JSON", e);
		}
	}

	public static MsdfFontData parse(String jsonString) {
		JsonObject root = JsonParser.parseString(jsonString).getAsJsonObject();
		return new MsdfFontData(root);
	}

	private static long packKerningKey(int u1, int u2) {
		return (((long) u1) << 32) | (u2 & 0xFFFFFFFFL);
	}

	public float getKerning(int u1, int u2) {
		Float adv = kerning.get(packKerningKey(u1, u2));
		return adv != null ? adv : 0.0f;
	}

	public MsdfGlyph getGlyph(int unicode) {
		return glyphs.get(unicode);
	}

	public float getWidth(String text, float fontSize) {
		if (text == null || text.isEmpty()) return 0;
		float maxWidth = 0;
		float cursorX = 0;
		int prevCodePoint = -1;

		for (int i = 0; i < text.length(); ) {
			int cp = text.codePointAt(i);
			i += Character.charCount(cp);

			if (cp == '\n') {
				if (cursorX > maxWidth) maxWidth = cursorX;
				cursorX = 0;
				prevCodePoint = -1;
				continue;
			}

			if (prevCodePoint != -1) {
				cursorX += getKerning(prevCodePoint, cp) * fontSize;
			}

			MsdfGlyph glyph = glyphs.get(cp);
			if (glyph == null) {
				glyph = glyphs.get((int) '?');
			}
			if (glyph != null) {
				cursorX += glyph.advance * fontSize;
			} else {
				cursorX += 0.5f * fontSize;
			}
			prevCodePoint = cp;
		}
		return Math.max(maxWidth, cursorX);
	}

	public float getHeight(float fontSize) {
		return lineHeight * fontSize;
	}

	public List<MsdfGlyphQuad> layout(String text, float startX, float startY, float fontSize) {
		List<MsdfGlyphQuad> quads = new ArrayList<>();
		if (text == null || text.isEmpty()) return quads;

		float baseline = startY + ascender * fontSize;
		float cursorX = startX;
		int prevCodePoint = -1;

		for (int i = 0; i < text.length(); ) {
			int cp = text.codePointAt(i);
			i += Character.charCount(cp);

			if (cp == '\n') {
				cursorX = startX;
				baseline += lineHeight * fontSize;
				prevCodePoint = -1;
				continue;
			}

			if (prevCodePoint != -1) {
				cursorX += getKerning(prevCodePoint, cp) * fontSize;
			}

			MsdfGlyph glyph = glyphs.get(cp);
			if (glyph == null) {
				glyph = glyphs.get((int) '?');
			}
			if (glyph != null) {
				if (glyph.hasPlaneBounds) {
					float x0 = cursorX + glyph.planeLeft * fontSize;
					float x1 = cursorX + glyph.planeRight * fontSize;
					float y0 = baseline - glyph.planeTop * fontSize;
					float y1 = baseline - glyph.planeBottom * fontSize;

					float u0 = glyph.atlasLeft / (float) atlasWidth;
					float u1 = glyph.atlasRight / (float) atlasWidth;
					float v0, v1;
					if (yOriginBottom) {
						v0 = (atlasHeight - glyph.atlasTop) / (float) atlasHeight;
						v1 = (atlasHeight - glyph.atlasBottom) / (float) atlasHeight;
					} else {
						v0 = glyph.atlasTop / (float) atlasHeight;
						v1 = glyph.atlasBottom / (float) atlasHeight;
					}

					quads.add(new MsdfGlyphQuad(x0, y0, x1, y1, u0, v0, u1, v1));
				}
				cursorX += glyph.advance * fontSize;
			} else {
				cursorX += 0.5f * fontSize;
			}
			prevCodePoint = cp;
		}
		return quads;
	}

	public String getAtlasType() { return atlasType; }
	public float getDistanceRange() { return distanceRange; }
	public float getNominalSize() { return nominalSize; }
	public int getAtlasWidth() { return atlasWidth; }
	public int getAtlasHeight() { return atlasHeight; }
	public boolean isYOriginBottom() { return yOriginBottom; }
	public float getEmSize() { return emSize; }
	public float getLineHeight() { return lineHeight; }
	public float getAscender() { return ascender; }
	public float getDescender() { return descender; }
	public float getUnderlineY() { return underlineY; }
	public float getUnderlineThickness() { return underlineThickness; }
	public Map<Integer, MsdfGlyph> getGlyphs() { return glyphs; }
}
