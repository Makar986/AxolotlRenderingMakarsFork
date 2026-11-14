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

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.datafixers.util.Pair;
import io.github.axolotlclient.rendering.ExRenderPipelines;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

	@Inject(method = "reloadShaders", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", ordinal = 0))
	private void loadCustomShaders(ResourceProvider resourceProvider, CallbackInfo ci, @Local(ordinal = 1) List<Pair<ShaderInstance, Consumer<ShaderInstance>>> list) throws IOException {
		list.add(Pair.of(new ShaderInstance(resourceProvider, "axolotlclient-rendering_rounded_rect", ExRenderPipelines.ROUNDED_RECT_FORMAT), s -> ExRenderPipelines.ROUNDED_RECT_SHADER = s));
		list.add(Pair.of(new ShaderInstance(resourceProvider, "axolotlclient-rendering_rounded_rect_tex", ExRenderPipelines.ROUNDED_RECT_TEX_FORMAT), s -> ExRenderPipelines.ROUNDED_RECT_TEX_SHADER = s));
		list.add(Pair.of(new ShaderInstance(resourceProvider, "axolotlclient-rendering_rounded_rect_outline", ExRenderPipelines.ROUNDED_RECT_OUTLINE_FORMAT), s -> ExRenderPipelines.ROUNDED_RECT_OUTLINE_SHADER = s));
		list.add(Pair.of(new ShaderInstance(resourceProvider, "axolotlclient-rendering_rounded_rect_tex_outline", ExRenderPipelines.ROUNDED_RECT_TEX_OUTLINE_FORMAT), s -> ExRenderPipelines.ROUNDED_RECT_TEX_OUTLINE_SHADER = s));
		list.add(Pair.of(new ShaderInstance(resourceProvider, "axolotlclient-rendering_font", DefaultVertexFormat.POSITION_COLOR_TEX), s -> ExRenderPipelines.FONT_SHADER = s));
		list.add(Pair.of(new ShaderInstance(resourceProvider, "axolotlclient-rendering_segment", ExRenderPipelines.ROUNDED_RECT_FORMAT), s -> ExRenderPipelines.SEGMENT_SHADER = s));
		list.add(Pair.of(new ShaderInstance(resourceProvider, "axolotlclient-rendering_gaussian_blur", ExRenderPipelines.ROUNDED_RECT_TEX_FORMAT), s -> ExRenderPipelines.GAUSSIAN_BLUR_SHADER = s));
		list.add(Pair.of(new ShaderInstance(resourceProvider, "axolotlclient-rendering_liquid_glass", ExRenderPipelines.ROUNDED_RECT_TEX_FORMAT), s -> ExRenderPipelines.LIQUID_GLASS_SHADER = s));
		list.add(Pair.of(new ShaderInstance(resourceProvider, "axolotlclient-rendering_msdf_font", DefaultVertexFormat.POSITION_TEX_COLOR), s -> ExRenderPipelines.MSDF_FONT_SHADER = s));
	}
}
