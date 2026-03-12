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

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GuiGraphicsExtractor.RenderingTextCollector.class)
public abstract class GuiGraphicsExtractorRenderingTextCollectorMixin {

	@WrapOperation(method = "accept(Lnet/minecraft/client/gui/TextAlignment;IILnet/minecraft/client/gui/ActiveTextCollector$Parameters;Lnet/minecraft/util/FormattedCharSequence;)V", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;font:Lnet/minecraft/client/gui/Font;", opcode = Opcodes.GETFIELD))
	private Font allowCustomFont(Minecraft instance, Operation<Font> original, @Local(argsOnly = true) ActiveTextCollector.Parameters parameters) {
		var f = parameters.axolotlclient_rendering$getFont();
		if (f != null) return f;
		return original.call(instance);
	}

	@WrapOperation(method = "acceptScrolling", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;font:Lnet/minecraft/client/gui/Font;", opcode = Opcodes.GETFIELD))
	private Font allowCustomFont$2(Minecraft instance, Operation<Font> original, @Local(argsOnly = true) ActiveTextCollector.Parameters parameters) {
		var f = parameters.axolotlclient_rendering$getFont();
		if (f != null) return f;
		return original.call(instance);
	}
}
