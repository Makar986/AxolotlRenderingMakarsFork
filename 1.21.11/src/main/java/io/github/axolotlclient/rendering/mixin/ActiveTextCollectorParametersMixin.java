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

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.axolotlclient.rendering.ActiveTextCollectorParametersExtension;
import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.Font;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ActiveTextCollector.Parameters.class)
public abstract class ActiveTextCollectorParametersMixin implements ActiveTextCollectorParametersExtension {
	@Shadow
	public abstract ActiveTextCollector.Parameters withPose(Matrix3x2fc matrix3x2fc);

	@Shadow
	public abstract Matrix3x2fc pose();

	@Unique
	private Font font;

	@Override
	public Font axolotlclient_rendering$getFont() {
		return font;
	}

	@Override
	public void axolotlclient_rendering$setFont(Font font) {
		this.font = font;
	}

	@Override
	public ActiveTextCollector.Parameters axolotlclient_rendering$withFont(Font font) {
		if (font == this.font) return (ActiveTextCollector.Parameters) (Object) this;
		var c = withPose(new Matrix3x2f(pose()));
		c.axolotlclient_rendering$setFont(font);
		return c;
	}

	@ModifyReturnValue(method = {"withPose", "withOpacity", "withScissor(Lnet/minecraft/client/gui/navigation/ScreenRectangle;)Lnet/minecraft/client/gui/ActiveTextCollector$Parameters;"}, at = @At("TAIL"))
	private ActiveTextCollector.Parameters withFont(ActiveTextCollector.Parameters original) {
		//noinspection ConstantValue
		if (original != (Object) this) {
			original.axolotlclient_rendering$setFont(axolotlclient_rendering$getFont());
		}
		return original;
	}
}
