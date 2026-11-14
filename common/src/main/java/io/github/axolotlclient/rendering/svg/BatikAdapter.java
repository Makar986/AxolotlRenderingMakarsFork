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

package io.github.axolotlclient.rendering.svg;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.nio.ByteBuffer;

import com.github.weisj.jsvg.SVGDocument;
import com.github.weisj.jsvg.attributes.ViewBox;
import com.github.weisj.jsvg.parser.SVGLoader;

/**
 * Lightweight SVG rasterizer powered by JSVG.
 * Backward-compatible drop-in replacement for Apache Batik, reducing artifact size by megabytes.
 */
public class BatikAdapter {

	/*
	 * Returns the pixel bytes in INT_RGBA format
	 */
	public static byte[] rasterizeSvg(InputStream in, float w, float h) throws Exception {
		SVGLoader loader = new SVGLoader();
		SVGDocument doc = loader.load(in);
		if (doc == null) {
			return new byte[0];
		}

		int width = Math.max(1, (int) Math.ceil(w));
		int height = Math.max(1, (int) Math.ceil(h));

		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = img.createGraphics();
		try {
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
			g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
			doc.render(null, g, new ViewBox(0, 0, width, height));
		} finally {
			g.dispose();
		}

		byte[] rgb = new byte[width * height * 4];
		ByteBuffer bb = ByteBuffer.wrap(rgb);
		int[] i = new int[rgb.length / 4];
		img.getRGB(0, 0, width, height, i, 0, width);
		for (int x = 0; x < i.length; x++) {
			i[x] = Integer.rotateLeft(i[x], 8);
		}
		bb.asIntBuffer().put(i).flip();
		return rgb;
	}
}
