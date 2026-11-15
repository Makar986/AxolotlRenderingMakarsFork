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

package io.github.axolotlclient.rendering.font;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.ITextComponent;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.jetbrains.annotations.Nullable;

public class StringSplitter {
	final StringSplitter.WidthProvider widthProvider;

	public StringSplitter(StringSplitter.WidthProvider widthProvider) {
		this.widthProvider = widthProvider;
	}

	public float stringWidth(@Nullable String content) {
		if (content == null) {
			return 0.0F;
		} else {
			MutableFloat mutableFloat = new MutableFloat();
			StringDecomposer.iterateFormatted(content, new Style(), (maxWidth, style, codePoint) -> {
				mutableFloat.add(this.widthProvider.getWidth(codePoint, style));
				return true;
			});
			return mutableFloat.floatValue();
		}
	}

	public float stringWidth(ITextComponent content) {
		MutableFloat mutableFloat = new MutableFloat();
		StringDecomposer.iterateFormatted(content, new Style(), (maxWidth, style, codePoint) -> {
			mutableFloat.add(this.widthProvider.getWidth(codePoint, style));
			return true;
		});
		return mutableFloat.floatValue();
	}

	public void splitLines(String content, int maxWidth, Style style, boolean withNewLines, StringSplitter.LinePosConsumer linePos) {
		int i = 0;
		int j = content.length();
		Style style2 = style;

		while (i < j) {
			StringSplitter.LineBreakFinder lineBreakFinder = new StringSplitter.LineBreakFinder(maxWidth);
			boolean bl = StringDecomposer.iterateFormatted(content, i, style2, style, lineBreakFinder);
			if (bl) {
				linePos.accept(style2, i, j);
				break;
			}

			int k = lineBreakFinder.getSplitPosition();
			char c = content.charAt(k);
			int l = c != '\n' && c != ' ' ? k : k + 1;
			linePos.accept(style2, i, withNewLines ? l : k);
			i = l;
			style2 = lineBreakFinder.getSplitStyle();
		}
	}

	public List<ITextComponent> splitLines(String content, int maxWidth, Style style) {
		List<ITextComponent> list = new ArrayList<>();
		this.splitLines(content, maxWidth, style, false, (stylex, begin, end) -> {
			TextComponentString t = new TextComponentString(content.substring(begin, end));
			t.setStyle(stylex);
			list.add(t);
		});
		return list;
	}

	public List<ITextComponent> splitLines(ITextComponent content, int maxWidth, Style style) {
		List<ITextComponent> list = new ArrayList<>();
		this.splitLines(content, maxWidth, style, (text, value) -> list.add(text));
		return list;
	}

	public void splitLines(ITextComponent content, int maxWidth, Style style, BiConsumer<ITextComponent, Boolean> splitifier) {
		List<StringSplitter.LineComponent> list = new ArrayList<>();
		for (ITextComponent t : content) {
			if (!t.getUnformattedComponentText().isEmpty()) {
				list.add(new StringSplitter.LineComponent(t.getUnformattedComponentText(), t.getStyle()));
			}
		}
		StringSplitter.FlatComponents flatComponents = new StringSplitter.FlatComponents(list);
		boolean bl = true;
		boolean bl2 = false;
		boolean bl3 = false;

		while (bl) {
			bl = false;
			StringSplitter.LineBreakFinder lineBreakFinder = new StringSplitter.LineBreakFinder(maxWidth);

			for (StringSplitter.LineComponent lineComponent : flatComponents.parts) {
				boolean bl4 = StringDecomposer.iterateFormatted(lineComponent.contents, 0, lineComponent.style, style, lineBreakFinder);
				if (!bl4) {
					int i = lineBreakFinder.getSplitPosition();
					Style style2 = lineBreakFinder.getSplitStyle();
					char c = flatComponents.charAt(i);
					boolean bl5 = c == '\n';
					boolean bl6 = bl5 || c == ' ';
					bl2 = bl5;
					ITextComponent text = flatComponents.splitAt(i, bl6 ? 1 : 0, style2);
					splitifier.accept(text, bl3);
					bl3 = !bl5;
					bl = true;
					break;
				}

				lineBreakFinder.addToOffset(lineComponent.contents.length());
			}
		}

		ITextComponent text2 = flatComponents.getRemainder();
		if (text2 != null) {
			splitifier.accept(text2, bl3);
		} else if (bl2) {
			splitifier.accept(new TextComponentString(""), false);
		}
	}

	static class FlatComponents {
		final List<StringSplitter.LineComponent> parts;
		private String flatParts;

		public FlatComponents(List<StringSplitter.LineComponent> parts) {
			this.parts = parts;
			this.flatParts = parts.stream().map(part -> part.contents).collect(Collectors.joining());
		}

		public char charAt(int codePoint) {
			return this.flatParts.charAt(codePoint);
		}

		public ITextComponent splitAt(int begin, int end, Style style) {
			ITextComponent componentCollector = new TextComponentString("");
			ListIterator<LineComponent> listIterator = this.parts.listIterator();
			int i = begin;
			boolean bl = false;

			while (listIterator.hasNext()) {
				StringSplitter.LineComponent lineComponent = listIterator.next();
				String string = lineComponent.contents;
				int j = string.length();
				if (!bl) {
					if (i > j) {
						componentCollector.appendSibling(lineComponent);
						listIterator.remove();
						i -= j;
					} else {
						String string2 = string.substring(0, i);
						if (!string2.isEmpty()) {
							TextComponentString t = new TextComponentString(string2);
							t.setStyle(lineComponent.style);
							componentCollector.appendSibling(t);
						}

						i += end;
						bl = true;
					}
				}

				if (bl) {
					if (i <= j) {
						String string2 = string.substring(i);
						if (string2.isEmpty()) {
							listIterator.remove();
						} else {
							listIterator.set(new StringSplitter.LineComponent(string2, style));
						}
						break;
					}

					listIterator.remove();
					i -= j;
				}
			}

			this.flatParts = this.flatParts.substring(begin + end);
			return componentCollector;
		}

		@Nullable
		public ITextComponent getRemainder() {
			ITextComponent componentCollector = new TextComponentString("");
			this.parts.forEach(componentCollector::appendSibling);
			this.parts.clear();
			return componentCollector;
		}
	}

	class LineBreakFinder implements FormattedCharSink {
		private final float maxWidth;
		private int lineBreak = -1;
		private Style lineBreakStyle = new Style();
		private boolean hadNonZeroWidthChar;
		private float width;
		private int lastSpace = -1;
		private Style lastSpaceStyle = new Style();
		private int nextChar;
		private int offset;

		public LineBreakFinder(float maxWidth) {
			this.maxWidth = Math.max(maxWidth, 1.0F);
		}

		@Override
		public boolean accept(int i, Style style, int j) {
			int k = i + this.offset;
			switch (j) {
				case 10:
					return this.finishIteration(k, style);
				case 32:
					this.lastSpace = k;
					this.lastSpaceStyle = style;
				default:
					float f = StringSplitter.this.widthProvider.getWidth(j, style);
					this.width += f;
					if (!this.hadNonZeroWidthChar || !(this.width > this.maxWidth)) {
						this.hadNonZeroWidthChar |= f != 0.0F;
						this.nextChar = k + Character.charCount(j);
						return true;
					} else {
						return this.lastSpace != -1 ? this.finishIteration(this.lastSpace, this.lastSpaceStyle) : this.finishIteration(k, style);
					}
			}
		}

		private boolean finishIteration(int lineBreak, Style lineBreakStyle) {
			this.lineBreak = lineBreak;
			this.lineBreakStyle = lineBreakStyle;
			return false;
		}

		private boolean lineBreakFound() {
			return this.lineBreak != -1;
		}

		public int getSplitPosition() {
			return this.lineBreakFound() ? this.lineBreak : this.nextChar;
		}

		public Style getSplitStyle() {
			return this.lineBreakStyle;
		}

		public void addToOffset(int offset) {
			this.offset += offset;
		}
	}

	static class LineComponent extends TextComponentString {
		final String contents;
		final Style style;

		public LineComponent(String contents, Style style) {
			super(contents);
			setStyle(style);
			this.contents = contents;
			this.style = style;
		}
	}

	@FunctionalInterface
	public interface LinePosConsumer {
		void accept(Style style, int i, int j);
	}

	@FunctionalInterface
	public interface WidthProvider {
		float getWidth(int i, Style style);
	}
}
