package io.github.axolotlclient.rendering.font.textogl;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.textures.FilterMode;
import io.github.axolotlclient.rendering.FontPageRenderState;
import io.github.axolotlclient.rendering.RenderingCommon;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.ApiStatus;
import org.joml.Matrix3x2f;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.freetype.FT_Face;
import org.lwjgl.util.freetype.FT_Vector;
import org.lwjgl.util.freetype.FreeType;

import static org.lwjgl.util.freetype.FreeType.*;

@SuppressWarnings({"resource", "DataFlowIssue"})
@Slf4j
public class Font implements AutoCloseable {

	private static final List<WeakReference<Font>> fonts = new ArrayList<>();

	@ApiStatus.Internal
	public static void reload() {
		Font.fonts.removeIf(w -> w.refersTo(null));
		Font.fonts.forEach(w -> w.get().clearPageMap());
	}

	public static void onScaleChange() {
		Font.fonts.removeIf(w -> w.refersTo(null));
		Font.fonts.forEach(w -> {
			Font font = w.get();
			font.resize(font.mcSize);
		});
	}

	private static long ftLib;
	private int ftRefCount;

	private final ByteBuffer fontMem;
	private final FT_Face face;
	private boolean hasKerningInfo;
	private final Bbox cellBox = new Bbox();
	private int mcSize;
	private int lineHeight;

	int texWidth, texHeight;

	Int2ObjectMap<Page> pageMap = new Int2ObjectOpenHashMap<>();

	private static final Object LIBRARY_LOCK = new Object();

	private static long getLibrary() {
		synchronized (LIBRARY_LOCK) {
			if (ftLib == 0L) {
				try (MemoryStack memoryStack = MemoryStack.stackPush()) {
					PointerBuffer buf = memoryStack.mallocPointer(1);
					if (FreeType.FT_Init_FreeType(buf) != FT_Err_Ok) {
						throw new IllegalStateException("initializing FreeType lib");
					}
					ftLib = buf.get();
				}
			}
			return ftLib;
		}
	}

	/**
	 * Create a font from this file.
	 *
	 * @param font the font file
	 * @param size the font size in pixels
	 * @throws IOException if an I/O exception occurs
	 */
	public static Font read(Path font, int size) throws IOException {
		var in = Files.readAllBytes(font);
		return read(in, size);
	}

	/**
	 * Create a font from this file.
	 *
	 * @param font the font file
	 * @param size the font size in pixels
	 * @throws IOException if an I/O exception occurs
	 */
	public static Font read(InputStream font, int size) throws IOException {
		return read(font.readAllBytes(), size);
	}

	/**
	 * Create a font from this file.
	 *
	 * @param font the font file
	 * @param size the font size in pixels
	 * @throws IOException if an I/O exception occurs
	 */
	public static Font read(byte[] font, int size) throws IOException {
		var buf = MemoryUtil.memAlloc(font.length);
		buf.put(font).flip();
		return read(buf, size);
	}

	/**
	 * Create a font from this file.
	 *
	 * @param font the font file. <strong>This memory will be freed on {@link #close()}</strong>.
	 * @param size the font size in pixels
	 * @throws IOException if an I/O exception occurs
	 */
	public static Font read(ByteBuffer font, int size) throws IOException {
		return new Font(font, size);
	}

	private Font(ByteBuffer font, int size) throws IOException {
		fonts.add(new WeakReference<>(this));
		this.fontMem = font;
		try (var stack = MemoryStack.stackPush()) {
			var faceBuf = stack.mallocPointer(1);
			var error = FT_New_Memory_Face(getLibrary(), font, 0, faceBuf);
			if (error != FT_Err_Ok) {
				if (error == FT_Err_Unknown_File_Format) {
					throw new IOException("Unknown format for font");
				}
				throw new IOException("Error reading font");
			}
			face = FT_Face.create(faceBuf.get());

			if (FreeType.FT_Select_Charmap(face, FreeType.FT_ENCODING_UNICODE) != FT_Err_Ok) {
				FT_Done_Face(face);
				throw new IOException("No unicode charmap in font");
			}

			try {
				resize(size);
			} catch (RuntimeException e) {
				FT_Done_Face(face);
				throw e;
			}

			ftRefCount++;
		}
	}

	public void resize(int mcSize) {
		this.mcSize = mcSize;
		var size = mcSize*Minecraft.getInstance().getWindow().getGuiScale();
		if (FT_Set_Pixel_Sizes(face, size, size) != FT_Err_Ok) {
			throw new RuntimeException("Can't set font size " + size);
		}

		cellBox.x0 = (int) (FT_MulFix(face.bbox().xMin(), face.size().metrics().x_scale()) / 64 - 2);
		cellBox.y0 = (int) (FT_MulFix(face.bbox().yMin(), face.size().metrics().y_scale()) / 64 + 2);
		cellBox.x1 = (int) (FT_MulFix(face.bbox().xMax(), face.size().metrics().x_scale()) / 64 + 2);
		cellBox.y1 = (int) (FT_MulFix(face.bbox().yMax(), face.size().metrics().y_scale()) / 64 - 2);

		lineHeight = (int) (FT_MulFix(face.height(), face.size().metrics().y_scale()) / 64);

		texWidth = cellBox.width() * 16;
		texHeight = cellBox.height() * 16;

		hasKerningInfo = FT_HAS_KERNING(face);

		clearPageMap();
	}

	private void clearPageMap() {
		for (var page : pageMap.values()) {
			Minecraft.getInstance().getTextureManager().release(page.tex);
		}
		pageMap.clear();
	}

	@Override
	public void close() {
		FT_Done_Face(face);

		if (--ftRefCount == 0) {
			FT_Done_FreeType(ftLib);
			ftLib = 0;
		}

		clearPageMap();
		MemoryUtil.memFree(fontMem);
	}

	public void renderText(GuiGraphics graphics, String text, float x, float y, int color, int alignFlags) {
		var built = buildText(text);

		renderTextCommon(graphics, color, x, y, alignFlags, built);
	}

	public void renderText(GuiGraphics graphics, String text, float x, float y, int color) {
		renderText(graphics, text, x, y+7, color, 0);
	}

	public float width(String text) {
		var built = buildText(text);
		return built.textBox()[2] - built.textBox()[0];
	}

	public int lineHeight() {
		return lineHeight;
	}

	public float height(String text) {
		var built = buildText(text);
		return built.textBox()[3] - built.textBox()[1];
	}

	private BuiltText buildText(String text) {
		float penX = 0;
		float penY = 0;
		var screenAndTexCoords = new Int2ObjectOpenHashMap<List<CoordData>>();
		var fontBox = new float[]{Float.MAX_VALUE, Float.MAX_VALUE, Float.MIN_VALUE, Float.MIN_VALUE};

		int prevGlyphI = 0;

		for (var codePt : text.codePoints().toArray()) {
			if (codePt == '\n') {
				penX = 0;
				penY += lineHeight;
				prevGlyphI = 0;
				continue;
			}

			int pageNo = codePt >> 8;
			var page = pageMap.get(pageNo);
			if (page == null) {
				page = loadPage(pageNo);
			}

			var c = page.charInfo[codePt & 0xFF];

			if (hasKerningInfo && prevGlyphI == 1 && c.glyphI == 1) {
				try (FT_Vector kerning = FT_Vector.create()) {
					if (FT_Get_Kerning(face, prevGlyphI, c.glyphI, FT_KERNING_DEFAULT, kerning) != FT_Err_Ok) {
						log.error("Can't load kerning for {} ({})", Integer.toHexString(codePt), Character.toString(codePt));
					}
					penX += kerning.x() / 64f;
					penY = kerning.y() / 64f;
				}
			}

			int texRow = ((codePt >> 4) & 0xF) + 1;
			int texCol = codePt & 0xF;

			float texOriginX = texCol * cellBox.width() - cellBox.x0;
			float texOriginY = texRow * cellBox.height() + cellBox.y0;
			var list = screenAndTexCoords.computeIfAbsent(pageNo, unused -> new ArrayList<>());

			list.add(new CoordData(pageNo, penX + c.bbox.x0, penY - c.bbox.y0,
				(texOriginX + c.bbox.x0) / texWidth, (texOriginY - c.bbox.y0) / texHeight,
				(texOriginX + c.bbox.x1) / texWidth, (texOriginY - c.bbox.y1) / texHeight,
				c.bbox.x1 - c.bbox.x0, c.bbox.y0 - c.bbox.y1));

			fontBox[0] = Math.min(fontBox[0], penX + c.bbox.x0);
			fontBox[1] = Math.min(fontBox[1], penY - c.bbox.y0);
			fontBox[2] = Math.max(fontBox[2], penX + c.bbox.x1);
			fontBox[3] = Math.max(fontBox[3], penY - c.bbox.y1);

			penX += c.advanceX / 64f;
			penY += c.advanceY / 64f;

			prevGlyphI = c.glyphI;
		}


		return new BuiltText(screenAndTexCoords, fontBox);
	}

	record BuiltText(Int2ObjectOpenHashMap<List<CoordData>> coordData, float[] textBox) {
	}

	private Page loadPage(int pageNo) {
		var page = new Page();
		pageMap.put(pageNo, page);

		var texData = new NativeImage(NativeImage.Format.LUMINANCE, texWidth, texHeight, false);
		MemoryUtil.memSet(texData.getPointer(), 0, (long) texWidth * texHeight);
		var slot = face.glyph();

		for (int codePt = pageNo << 8; codePt < (pageNo + 1) << 8; codePt++) {
			int tblRow = (codePt >> 4) & 0xF;
			tblRow += 1;
			int tblCol = codePt & 0xF;

			var glyphI = FT_Get_Char_Index(face, codePt);
			if (FT_Load_Glyph(face, glyphI, FT_LOAD_RENDER) != FT_Err_Ok) {
				log.error("Error loading glyph for: {} ({})", Integer.toHexString(codePt), Character.toString(codePt));
				continue;
			}
			var bmp = slot.bitmap();
			var c = new CharInfo();
			page.charInfo[codePt & 0xFF] = c;
			c.originX = -cellBox.x0 + slot.bitmap_left();
			c.originY = cellBox.y0 - slot.bitmap_top();
			c.bbox.x0 = slot.bitmap_left();
			c.bbox.y0 = slot.bitmap_top();
			c.bbox.x1 = bmp.width() + slot.bitmap_left();
			c.bbox.y1 = slot.bitmap_top() - bmp.rows();
			c.advanceX = slot.advance().x();
			c.advanceY = slot.advance().y();
			c.glyphI = glyphI;
			int size = bmp.width() * bmp.rows();
			var buffer = bmp.buffer(size);

			if (buffer == null) {
				continue;
			}

			var tblImgY = tblRow * cellBox.height() + cellBox.y0 - slot.bitmap_top();
			var tblImgX = tblCol * cellBox.width() - cellBox.x0 + slot.bitmap_left();
			int width = bmp.width();
			int height = bmp.rows();
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					long l = (tblImgX + x + (long) (tblImgY + y) * texWidth);
					MemoryUtil.memPutByte(texData.getPointer() + l, buffer.get(y * width + x));
				}
			}
		}

		page.tex = ResourceLocation.fromNamespaceAndPath(RenderingCommon.MODID, "font/page_" + pageNo);
		var tex = new DynamicTexture(page.tex::toString, texData);
		tex.getTexture().setTextureFilter(FilterMode.LINEAR, FilterMode.LINEAR, true);
		Minecraft.getInstance().getTextureManager().register(page.tex, tex);
		return page;
	}

	private void renderTextCommon(GuiGraphics graphics, int color, float x, float y, int alignFlags, BuiltText builtText) {
		float[] startOffset = new float[2];
		var textBox = builtText.textBox();

		int horizAlign = alignFlags & 0x3;
		startOffset[0] = switch (horizAlign) {
			case ORIGIN_HORIZ_LEFT -> textBox[0];
			case ORIGIN_HORIZ_RIGHT -> textBox[2];
			case ORIGIN_HORIZ_CENTER -> textBox[0] + (textBox[2] - textBox[0]) / 2f;
			//case ORIGIN_HORIZ_BASELINE -> {}
			default -> 0;
		};
		var vertAlign = alignFlags & 0xC;
		startOffset[1] = switch (vertAlign) {
			case ORIGIN_VERT_TOP -> textBox[1];
			case ORIGIN_VERT_BOTTOM -> textBox[3];
			case ORIGIN_VERT_CENTER -> textBox[1] + (textBox[3] - textBox[1]) / 2f;
			//case ORIGIN_VERT_BASELINE
			default -> 0;
		};

		graphics.pose().pushMatrix();
		graphics.pose().translate(x, y);
		graphics.pose().translate(startOffset[0], startOffset[1]);
		var scale = Minecraft.getInstance().getWindow().getGuiScale();
		graphics.pose().scale(1f/scale);

		renderTextCommon(graphics, color, builtText);
		graphics.pose().popMatrix();
	}

	private void renderTextCommon(GuiGraphics graphics, int color, BuiltText builtText) {
		var box = builtText.textBox();
		var scissor = graphics.scissorStack.peek();
		var rect = new ScreenRectangle(Mth.ceil(box[0]), Mth.ceil(box[1]), Mth.ceil(box[2]), Mth.ceil(box[3])).transformMaxBounds(graphics.pose());
		rect = scissor != null ? scissor.intersection(rect) : rect;
		for (var pageGlyphs : builtText.coordData().int2ObjectEntrySet()) {
			Page page = pageMap.get(pageGlyphs.getIntKey());
			graphics.guiRenderState.submitGuiElement(new FontPageRenderState(page.tex, new Matrix3x2f(graphics.pose()), pageGlyphs.getValue(), color, rect, scissor));
		}
	}

	public static final int ORIGIN_HORIZ_BASELINE = 0x00;
	public static final int ORIGIN_HORIZ_LEFT = 0x01;
	public static final int ORIGIN_HORIZ_RIGHT = 0x02;
	public static final int ORIGIN_HORIZ_CENTER = 0x03;

	public static final int ORIGIN_VERT_BASELINE = 0x00;
	public static final int ORIGIN_VERT_TOP = 0x04;
	public static final int ORIGIN_VERT_BOTTOM = 0x08;
	public static final int ORIGIN_VERT_CENTER = 0x0C;

	@AllArgsConstructor
	@RequiredArgsConstructor
	static
	class Bbox {
		int x0, y0, x1, y1;

		public int width() {
			return x1 - x0;
		}

		public int height() {
			return y1 - y0;
		}
	}

	@AllArgsConstructor
	@RequiredArgsConstructor
	static class CharInfo {
		int originX, originY;
		float advanceX, advanceY;
		Bbox bbox = new Bbox();
		int glyphI;
	}

	static class Page {
		ResourceLocation tex;
		CharInfo[] charInfo = new CharInfo[256];
	}

	@AllArgsConstructor
	public static class CoordData {
		int pageNo;
		public float x, y, u, v, u1, v1;
		public int width, height;
	}
}
