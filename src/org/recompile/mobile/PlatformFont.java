/*
	This file is part of FreeJ2ME.

	FreeJ2ME is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.

	FreeJ2ME is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with FreeJ2ME.  If not, see http://www.gnu.org/licenses/
*/
package org.recompile.mobile;

import javax.microedition.lcdui.Font;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.awt.font.TextAttribute;

import java.util.Hashtable;
import java.util.Map;

public class PlatformFont
{
	private Graphics2D gc;

	public java.awt.Font awtFont;

	private static final String FONT_REGULAR = "freej2me_system" + File.separatorChar + "fonts" + File.separatorChar + "font_regular.ttf";
	private static final String FONT_BOLD = "freej2me_system" + File.separatorChar + "fonts" + File.separatorChar + "font_bold.ttf";
	private static final String FONT_ITALIC = "freej2me_system" + File.separatorChar + "fonts" + File.separatorChar + "font_italic.ttf";
	private static class FontLoader {
		private static final Map<String, java.awt.Font> loadedFonts = new Hashtable<>();

		private FontLoader() {
		}

		public static java.awt.Font getFont(String fontFilePath, int size) throws Exception {
			if (loadedFonts.containsKey(fontFilePath)) {
				return loadedFonts.get(fontFilePath).deriveFont(size);
			}
			java.awt.Font font = java.awt.Font.createFont(java.awt.Font.TRUETYPE_FONT, new java.io.File(fontFilePath));
			loadedFonts.put(fontFilePath, font);
			return font.deriveFont((float) size);
		}
	}

	public PlatformFont(Font font)
	{
		int size = font.getPointSize();
		int style = font.getStyle();
		
		try {
			if(font.getFace() == Font.FACE_MONOSPACE) { 
				awtFont = new java.awt.Font( java.awt.Font.MONOSPACED, font.getStyle(), font.getPointSize());
			} else if (style == java.awt.Font.BOLD) {
				awtFont = FontLoader.getFont(FONT_BOLD, size);
			} else if (style == java.awt.Font.ITALIC) {
				awtFont = FontLoader.getFont(FONT_ITALIC, size);
			} else 
			{
				awtFont = FontLoader.getFont(FONT_REGULAR, size);
			}
		} catch (Exception e) {
			awtFont = new java.awt.Font(java.awt.Font.SANS_SERIF, style, size);
		}

		if((font.getStyle() & Font.STYLE_UNDERLINED) > 0)
		{
			Map<TextAttribute, Object> map = new Hashtable<TextAttribute, Object>(1);
			map.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
			awtFont = awtFont.deriveFont(map);
		}
		
		gc = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB).createGraphics();
		gc.setFont(awtFont);
	}

	public int stringWidth(String str)
	{
		return gc.getFontMetrics().stringWidth(str);
	}

	public int getHeight()
	{
		return gc.getFontMetrics().getHeight();
	}

	public int getAscent()
	{
		return gc.getFontMetrics().getAscent();
	}
}
