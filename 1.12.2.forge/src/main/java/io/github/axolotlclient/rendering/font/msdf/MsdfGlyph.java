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

public class MsdfGlyph {
	public final int unicode;
	public final float advance;
	public final float planeLeft;
	public final float planeBottom;
	public final float planeRight;
	public final float planeTop;
	public final float atlasLeft;
	public final float atlasBottom;
	public final float atlasRight;
	public final float atlasTop;
	public final boolean hasPlaneBounds;

	public MsdfGlyph(int unicode, float advance,
					 float planeLeft, float planeBottom, float planeRight, float planeTop,
					 float atlasLeft, float atlasBottom, float atlasRight, float atlasTop,
					 boolean hasPlaneBounds) {
		this.unicode = unicode;
		this.advance = advance;
		this.planeLeft = planeLeft;
		this.planeBottom = planeBottom;
		this.planeRight = planeRight;
		this.planeTop = planeTop;
		this.atlasLeft = atlasLeft;
		this.atlasBottom = atlasBottom;
		this.atlasRight = atlasRight;
		this.atlasTop = atlasTop;
		this.hasPlaneBounds = hasPlaneBounds;
	}
}
