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
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.axolotlclient.rendering.StyleExtension;
import io.github.axolotlclient.rendering.font.TextColor;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Formatting;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Style;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Style.class)
public abstract class StyleMixin implements StyleExtension {
	@Shadow
	public abstract Formatting getColor();

	@Shadow
	public abstract boolean isEmpty();

	@Shadow
	private Formatting color;
	@Shadow
	private Boolean bold;
	@Shadow
	private Boolean italic;
	@Shadow
	private Boolean underlined;
	@Shadow
	private Boolean strikethrough;
	@Shadow
	private Boolean obfuscated;
	@Shadow
	private ClickEvent clickEvent;
	@Shadow
	private HoverEvent hoverEvent;
	@Shadow
	private String insertion;
	@Shadow
	@Final
	private static Style ROOT;

	@Shadow
	protected abstract Style getParent();

	@Unique
	private TextColor textColor = null;

	@Override
	public TextColor axolotlclient_rendering$getColor() {
		if ((Object) this == ROOT) return null;
		return textColor != null ? textColor : getParent().axolotlclient_rendering$getColor();
	}

	@Override
	public Style axolotlclient_rendering$setColor(TextColor value) {
		textColor = value;
		return (Style) (Object) this;
	}

	@Override
	public Style axolotlclient_rendering$applyTo(Style style) {
		if (this.isEmpty()) {
			return style;
		} else {
			var stm = (StyleMixin) (Object) style;
			if (style.isEmpty()) return (Style) (Object) this;
			var n = new Style();
			n.setColor(this.color != null ? this.color : stm.color);
			n.axolotlclient_rendering$setColor(this.textColor != null ? this.textColor : stm.textColor);
			n.setBold(this.bold != null ? this.bold : stm.bold);
			n.setItalic(this.italic != null ? this.italic : stm.italic);
			n.setUnderlined(this.underlined != null ? this.underlined : stm.underlined);
			n.setStrikethrough(this.strikethrough != null ? this.strikethrough : stm.strikethrough);
			n.setObfuscated(this.obfuscated != null ? this.obfuscated : stm.obfuscated);
			n.setClickEvent(this.clickEvent != null ? this.clickEvent : stm.clickEvent);
			n.setHoverEvent(this.hoverEvent != null ? this.hoverEvent : stm.hoverEvent);
			n.setInsertion(this.insertion != null ? this.insertion : stm.insertion);
			return n;
		}

	}

	@Inject(method = "asString", at = @At(value = "INVOKE", target = "Lnet/minecraft/text/Style;isBold()Z"))
	private void customColorCode(CallbackInfoReturnable<String> cir, @Local StringBuilder sb) {
		var c = axolotlclient_rendering$getColor();
		if (c != null) {
			sb.append("§#").append(StringUtils.leftPad(Integer.toUnsignedString(c.getValue() & 0xffffff, 16), 6, "0"));
		}
	}

	@WrapMethod(method = "isEmpty")
	private boolean isEmpty(Operation<Boolean> original) {
		return original.call() && textColor == null;
	}

	@ModifyReturnValue(method = "copy", at = @At("RETURN"))
	private Style copy(Style original) {
		original.axolotlclient_rendering$setColor(axolotlclient_rendering$getColor());
		return original;
	}

	@ModifyReturnValue(method = "deepCopy", at = @At("RETURN"))
	private Style deepCopy(Style original) {
		((StyleMixin) (Object) original).textColor = textColor;
		return original;
	}
}
