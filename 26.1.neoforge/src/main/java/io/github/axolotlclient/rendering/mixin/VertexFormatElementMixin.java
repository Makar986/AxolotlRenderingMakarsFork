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

package io.github.axolotlclient.rendering.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import io.github.axolotlclient.rendering.ExRenderPipelines;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = VertexFormatElement.class)
public abstract class VertexFormatElementMixin {

	@Shadow
	@Final
	@Mutable
	public static int MAX_COUNT;

	@ModifyExpressionValue(method = "<clinit>", at = @At(value = "CONSTANT", args = "intValue=32"))
	private static int unstaticMaxCount(int original) {

		return original + ExRenderPipelines.NUM_CUSTOM_VERTEX_FORMAT_ELEMENTS;
	}

	@Inject(method = "<clinit>", at = @At("TAIL"))
	private static void increaseMaxCount(CallbackInfo ci) {
		MAX_COUNT += ExRenderPipelines.NUM_CUSTOM_VERTEX_FORMAT_ELEMENTS;
	}
}
