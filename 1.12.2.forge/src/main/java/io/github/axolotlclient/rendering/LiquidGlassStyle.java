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

package io.github.axolotlclient.rendering;

/**
 * Visual and optical styling properties for ReGlass-style LiquidGlass rendering.
 */
public class LiquidGlassStyle {
	private int tintColor = 0x000000;
	private float tintAlpha = 0.0f;

	private float refThickness = 13.15f;
	private float refFactor = 1.4f;
	private float refDispersion = 7.0f;
	private float refFresnelRange = 39.71f;
	private float refFresnelHardness = 0.2f;
	private float refFresnelFactor = 0.2f;

	private float glareRange = 30.0f;
	private float glareHardness = 0.2f;
	private float glareFactor = 0.9f;

	private float blurRadius = 8.0f;

	private int borderColor = 0x00000000;
	private float borderWidth = 0.0f;

	private float shadowExpand = 18.2f;
	private float shadowFactor = 0.208f;
	private int shadowColor = 0x000000;
	private float shadowColorAlpha = 0.0f;

	public static LiquidGlassStyle create() {
		return new LiquidGlassStyle();
	}

	public LiquidGlassStyle tint(int rgb, float alpha) {
		this.tintColor = rgb;
		this.tintAlpha = alpha;
		return this;
	}

	public LiquidGlassStyle refraction(float thickness, float factor, float dispersion) {
		this.refThickness = thickness;
		this.refFactor = factor;
		this.refDispersion = dispersion;
		return this;
	}

	public LiquidGlassStyle refractionThickness(float thickness) {
		this.refThickness = thickness;
		return this;
	}

	public LiquidGlassStyle refractionFactor(float factor) {
		this.refFactor = factor;
		return this;
	}

	public LiquidGlassStyle refractionDispersion(float dispersion) {
		this.refDispersion = dispersion;
		return this;
	}

	public LiquidGlassStyle fresnel(float range, float hardness, float factor) {
		this.refFresnelRange = range;
		this.refFresnelHardness = hardness;
		this.refFresnelFactor = factor;
		return this;
	}

	public LiquidGlassStyle glare(float range, float hardness, float factor) {
		this.glareRange = range;
		this.glareHardness = hardness;
		this.glareFactor = factor;
		return this;
	}

	public LiquidGlassStyle blurRadius(float radius) {
		this.blurRadius = Math.max(0.0f, radius);
		return this;
	}

	public LiquidGlassStyle border(int color, float width) {
		this.borderColor = color;
		this.borderWidth = Math.max(0.0f, width);
		return this;
	}

	public LiquidGlassStyle shadow(float expand, float factor, int color, float alpha) {
		this.shadowExpand = expand;
		this.shadowFactor = factor;
		this.shadowColor = color;
		this.shadowColorAlpha = alpha;
		return this;
	}

	public int getTintColor() { return tintColor; }
	public float getTintAlpha() { return tintAlpha; }
	public float getRefThickness() { return refThickness; }
	public float getRefFactor() { return refFactor; }
	public float getRefDispersion() { return refDispersion; }
	public float getRefFresnelRange() { return refFresnelRange; }
	public float getRefFresnelHardness() { return refFresnelHardness; }
	public float getRefFresnelFactor() { return refFresnelFactor; }
	public float getGlareRange() { return glareRange; }
	public float getGlareHardness() { return glareHardness; }
	public float getGlareFactor() { return glareFactor; }
	public float getBlurRadius() { return blurRadius; }
	public int getBorderColor() { return borderColor; }
	public float getBorderWidth() { return borderWidth; }
	public float getShadowExpand() { return shadowExpand; }
	public float getShadowFactor() { return shadowFactor; }
	public int getShadowColor() { return shadowColor; }
	public float getShadowColorAlpha() { return shadowColorAlpha; }
}
