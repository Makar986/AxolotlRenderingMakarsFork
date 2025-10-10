package io.github.axolotlclient.rendering.font;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import lombok.Getter;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.apache.commons.lang3.mutable.MutableInt;
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

	public float stringWidth(Text content) {
		MutableFloat mutableFloat = new MutableFloat();
		StringDecomposer.iterateFormatted(content, new Style(), (maxWidth, style, codePoint) -> {
			mutableFloat.add(this.widthProvider.getWidth(codePoint, style));
			return true;
		});
		return mutableFloat.floatValue();
	}

	public int plainIndexAtWidth(String content, int maxWidth, Style style) {
		StringSplitter.WidthLimitedCharSink widthLimitedCharSink = new StringSplitter.WidthLimitedCharSink(maxWidth);
		StringDecomposer.iterate(content, style, widthLimitedCharSink);
		return widthLimitedCharSink.getPosition();
	}

	public String plainHeadByWidth(String content, int maxWidth, Style style) {
		return content.substring(0, this.plainIndexAtWidth(content, maxWidth, style));
	}

	public String plainTailByWidth(String content, int maxWidth, Style style) {
		MutableFloat mutableFloat = new MutableFloat();
		MutableInt mutableInt = new MutableInt(content.length());
		StringDecomposer.iterateBackwards(content, style, (maxWidthx, stylex, codePoint) -> {
			mutableFloat.add(this.widthProvider.getWidth(codePoint, stylex));
			float f = mutableFloat.getValue();
			if (f > maxWidth) {
				return false;
			} else {
				mutableInt.setValue(maxWidthx);
				return true;
			}
		});
		return content.substring(mutableInt.intValue());
	}

	public int formattedIndexByWidth(String content, int maxWidth, Style style) {
		StringSplitter.WidthLimitedCharSink widthLimitedCharSink = new StringSplitter.WidthLimitedCharSink(maxWidth);
		StringDecomposer.iterateFormatted(content, style, widthLimitedCharSink);
		return widthLimitedCharSink.getPosition();
	}

	@Nullable
	public Style componentStyleAtWidth(Text content, int maxWidth) {
		StringSplitter.WidthLimitedCharSink widthLimitedCharSink = new StringSplitter.WidthLimitedCharSink(maxWidth);
		var opt = Optional.<Style>empty();
		for (var t : content) {
			if (!StringDecomposer.iterateFormatted(t.getContent(), t.getStyle(), widthLimitedCharSink)) {
				opt = Optional.of(t.getStyle());
			}
			if (opt.isPresent()) {
				break;
			}
		}
		return opt.orElse(null);
	}

	public String formattedHeadByWidth(String content, int maxWidth, Style style) {
		return content.substring(0, this.formattedIndexByWidth(content, maxWidth, style));
	}

	/*public Text headByWidth(Text content, int maxWidth, Style style) {
		final StringSplitter.WidthLimitedCharSink widthLimitedCharSink = new StringSplitter.WidthLimitedCharSink(maxWidth);
		return (Text)content.visit(new Text.StyledContentConsumer<Text>() {
			private final ComponentCollector collector = new ComponentCollector();

			@Override
			public Optional<Text> accept(Style style, String string) {
				widthLimitedCharSink.resetPosition();
				if (!StringDecomposer.iterateFormatted(string, style, widthLimitedCharSink)) {
					String string2 = string.substring(0, widthLimitedCharSink.getPosition());
					if (!string2.isEmpty()) {
						this.collector.append(Text.of(string2, style));
					}

					return Optional.of(this.collector.getResultOrEmpty());
				} else {
					if (!string.isEmpty()) {
						this.collector.append(Text.of(string, style));
					}

					return Optional.empty();
				}
			}
		}, style).orElse(content);
	}*/

	public int findLineBreak(String content, int maxWidth, Style style) {
		StringSplitter.LineBreakFinder lineBreakFinder = new StringSplitter.LineBreakFinder(maxWidth);
		StringDecomposer.iterateFormatted(content, style, lineBreakFinder);
		return lineBreakFinder.getSplitPosition();
	}

	public static int getWordPosition(String content, int skipCount, int cursorPoint, boolean includeWhitespace) {
		int i = cursorPoint;
		boolean bl = skipCount < 0;
		int j = Math.abs(skipCount);

		for (int k = 0; k < j; k++) {
			if (bl) {
				while (includeWhitespace && i > 0 && (content.charAt(i - 1) == ' ' || content.charAt(i - 1) == '\n')) {
					i--;
				}

				while (i > 0 && content.charAt(i - 1) != ' ' && content.charAt(i - 1) != '\n') {
					i--;
				}
			} else {
				int l = content.length();
				int m = content.indexOf(32, i);
				int n = content.indexOf(10, i);
				if (m == -1 && n == -1) {
					i = -1;
				} else if (m != -1 && n != -1) {
					i = Math.min(m, n);
				} else if (m != -1) {
					i = m;
				} else {
					i = n;
				}

				if (i == -1) {
					i = l;
				} else {
					while (includeWhitespace && i < l && (content.charAt(i) == ' ' || content.charAt(i) == '\n')) {
						i++;
					}
				}
			}
		}

		return i;
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

	public List<Text> splitLines(String content, int maxWidth, Style style) {
		List<Text> list = new ArrayList<>();
		this.splitLines(content, maxWidth, style, false, (stylex, begin, end) -> {
			var t = new LiteralText(content.substring(begin, end));
			t.setStyle(stylex);
			list.add(t);
		});
		return list;
	}

	public List<Text> splitLines(Text content, int maxWidth, Style style) {
		List<Text> list = new ArrayList<>();
		this.splitLines(content, maxWidth, style, (text, value) -> list.add(text));
		return list;
	}

	/*public List<Text> splitLines(Text content, int maxWidth, Style style, Text prefix) {
		List<Text> list = new ArrayList<>();
		this.splitLines(content, maxWidth, style, (message, prefixx) -> list.add(prefixx ? Text.composite(prefix, message) : message));
		return list;
	}*/

	public void splitLines(Text content, int maxWidth, Style style, BiConsumer<Text, Boolean> splitifier) {
		List<StringSplitter.LineComponent> list = new ArrayList<>();
		for (var t : content) {
			if (!t.getContent().isEmpty()) {
				list.add(new StringSplitter.LineComponent(t.getContent(), t.getStyle()));
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
					Text Text = flatComponents.splitAt(i, bl6 ? 1 : 0, style2);
					splitifier.accept(Text, bl3);
					bl3 = !bl5;
					bl = true;
					break;
				}

				lineBreakFinder.addToOffset(lineComponent.contents.length());
			}
		}

		Text Text2 = flatComponents.getRemainder();
		if (Text2 != null) {
			splitifier.accept(Text2, bl3);
		} else if (bl2) {
			splitifier.accept(new LiteralText(""), false);
		}
	}

	static class FlatComponents {
		final List<StringSplitter.LineComponent> parts;
		private String flatParts;

		public FlatComponents(List<StringSplitter.LineComponent> parts) {
			this.parts = parts;
			this.flatParts = (String) parts.stream().map(part -> part.contents).collect(Collectors.joining());
		}

		public char charAt(int codePoint) {
			return this.flatParts.charAt(codePoint);
		}

		public Text splitAt(int begin, int end, Style style) {
			Text componentCollector = new LiteralText("");
			ListIterator<LineComponent> listIterator = this.parts.listIterator();
			int i = begin;
			boolean bl = false;

			while (listIterator.hasNext()) {
				StringSplitter.LineComponent lineComponent = listIterator.next();
				String string = lineComponent.contents;
				int j = string.length();
				if (!bl) {
					if (i > j) {
						componentCollector.append(lineComponent);
						listIterator.remove();
						i -= j;
					} else {
						String string2 = string.substring(0, i);
						if (!string2.isEmpty()) {
							var t = new LiteralText(string2);
							t.setStyle(lineComponent.style);
							componentCollector.append(t);
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
		public Text getRemainder() {
			Text componentCollector = new LiteralText("");
			this.parts.forEach(componentCollector::append);
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

	static class LineComponent extends LiteralText {
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

	class WidthLimitedCharSink implements FormattedCharSink {
		private float maxWidth;
		@Getter
		private int position;

		public WidthLimitedCharSink(float maxWidth) {
			this.maxWidth = maxWidth;
		}

		@Override
		public boolean accept(int i, Style style, int j) {
			this.maxWidth = this.maxWidth - StringSplitter.this.widthProvider.getWidth(j, style);
			if (this.maxWidth >= 0.0F) {
				this.position = i + Character.charCount(j);
				return true;
			} else {
				return false;
			}
		}

		public void resetPosition() {
			this.position = 0;
		}
	}

	@FunctionalInterface
	public interface WidthProvider {
		float getWidth(int i, Style style);
	}
}
