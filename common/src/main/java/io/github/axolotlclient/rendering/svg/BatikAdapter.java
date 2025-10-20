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

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import it.unimi.dsi.fastutil.io.FastByteArrayOutputStream;
import org.apache.batik.transcoder.SVGAbstractTranscoder;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;

public class BatikAdapter {

	/*
	 * Returns the pixel bytes in INT_RGBA format
	 */
	public static byte[] rasterizeSvg(InputStream in, float w, float h) throws Exception {
		var s = new FastByteArrayOutputStream();
		var transcoder = new SVGReadTranscoder();
		transcoder.addTranscodingHint(SVGAbstractTranscoder.KEY_WIDTH, w);
		transcoder.addTranscodingHint(SVGAbstractTranscoder.KEY_HEIGHT, h);
		transcoder.addTranscodingHint(SVGAbstractTranscoder.KEY_MAX_WIDTH, w);
		transcoder.addTranscodingHint(SVGAbstractTranscoder.KEY_MAX_HEIGHT, h);
		transcoder.addTranscodingHint(ImageTranscoder.KEY_FORCE_TRANSPARENT_WHITE, false);
		transcoder.transcode(new TranscoderInput(in), new TranscoderOutput(s));
		s.flush();
		return s.array;

	}

	private static class SVGReadTranscoder extends ImageTranscoder {

		@Override
		public BufferedImage createImage(int width, int height) {
			return new BufferedImage(width, height, 2);
		}

		@Override
		public void writeImage(BufferedImage img, TranscoderOutput output) throws TranscoderException {
			var rgb = new byte[img.getWidth() * img.getHeight() * 4];
			var bb = ByteBuffer.wrap(rgb);
			var i = new int[rgb.length / 4];
			img.getRGB(0, 0, img.getWidth(), img.getHeight(), i, 0, img.getWidth());
			for (int x = 0; x < i.length; x++) {
				i[x] = Integer.rotateLeft(i[x], 8);
			}
			bb.asIntBuffer().put(i).flip();
			try {
				output.getOutputStream().write(rgb);
			} catch (IOException e) {
				throw new TranscoderException(e);
			}
		}
	}
}
