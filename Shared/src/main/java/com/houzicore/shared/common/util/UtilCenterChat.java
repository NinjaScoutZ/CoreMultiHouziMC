package com.houzicore.shared.common.util;

/**
 * UtilCenterChat — Centers text in Minecraft chat using pixel-width calculations.
 * <p>
 * Minecraft's default font is NOT monospace. Each character has a different pixel width.
 * The standard chat window is 320 pixels wide (at GUI scale 2).
 * A normal space character is 4px wide (3px glyph + 1px gap).
 * A bold space is 5px wide (4px glyph + 1px gap).
 * <p>
 * This utility strips color/formatting codes from the text to calculate its true pixel
 * width, then prepends the correct number of spaces to visually center the message.
 */
public final class UtilCenterChat
{
	/** Full chat line width in pixels (320 for default GUI scale). */
	private static final int CENTER_PX = 154;

	private UtilCenterChat() {}

	// ─── Pixel Width Lookup ─────────────────────────────────────────────

	/**
	 * Returns the pixel width of a single character in the default Minecraft font.
	 * Width includes the 1px gap between characters.
	 */
	private static int charWidth(char c, boolean bold)
	{
		int w;
		switch (c)
		{
			// 2px glyphs
			case '!': case ',': case '.': case ':': case ';': case 'i': case '|': case '¡':
				w = 2; break;
			// 3px glyphs
			case '\'': case 'l': case 'ì': case 'í':
				w = 3; break;
			// 4px glyphs  (also the default space)
			case ' ': case 'I': case '[': case ']': case 't': case 'ï':
			case '×':
				w = 4; break;
			// 5px glyphs
			case '"': case '(': case ')': case '*': case '<': case '>':
			case 'f': case 'k': case '{': case '}':
				w = 5; break;
			// 7px glyphs (wide characters)
			case '@': case '~': case '®':
				w = 7; break;
			// ─── Small Caps & Unicode ───
			case 'ᴀ': case 'ʙ': case 'ᴄ': case 'ᴅ': case 'ᴇ': case 'ꜰ':
			case 'ɢ': case 'ʜ': case 'ɪ': case 'ᴊ': case 'ᴋ': case 'ʟ':
			case 'ᴍ': case 'ɴ': case 'ᴏ': case 'ᴘ': case 'ǫ': case 'ʀ':
			case 'ꜱ': case 'ᴛ': case 'ᴜ': case 'ᴠ': case 'ᴡ': case 'ʏ':
			case 'ᴢ': case 'ғ':
				w = 6; break;
			// ─── Box-drawing & decorative ───
			case '▎': case '▏': case '│': case '┃':
				w = 2; break;
			case '▌':
				w = 5; break;
			case '█': case '▐':
				w = 9; break;
			case '▬': case '━': case '─':
				w = 6; break;
			case '★': case '✦': case '✧': case '✪': case '✫': case '✬': case '✯':
			case '✰': case '♦': case '♣': case '♠': case '♥':
			case '►': case '◄': case '▶': case '◀': case '▸': case '◂':
			case '●': case '○': case '◉': case '◎':
			case '✘': case '✔': case '✖': case '✚':
			case '⚔': case '⚡': case '⭐':
				w = 6; break;
			// Default: most characters (A-Z, a-z minus exceptions, 0-9, etc.) are 6px
			default:
				w = 6; break;
		}
		// Bold adds 1 extra pixel to glyph width
		if (bold) w += 1;
		// Add 1px gap between characters
		return w + 1;
	}

	// ─── Pixel Width of a String ────────────────────────────────────────

	/**
	 * Calculates the total pixel width of a string after stripping formatting codes.
	 * Handles both legacy §-codes AND MiniMessage tags.
	 */
	public static int getWidth(String message)
	{
		if (message == null || message.isEmpty()) return 0;

		// Strip MiniMessage tags first  (<bold>, <color:#hex>, etc.)
		String stripped = stripMiniMessage(message);

		int width = 0;
		boolean bold = false;
		boolean skip = false;

		for (int i = 0; i < stripped.length(); i++)
		{
			char c = stripped.charAt(i);

			// Legacy § color codes: skip the format character after §
			if (c == '§' || c == '&')
			{
				if (i + 1 < stripped.length())
				{
					char code = stripped.charAt(i + 1);
					if (code == 'l' || code == 'L')
					{
						bold = true;
					}
					else if (code == 'r' || code == 'R')
					{
						bold = false;
					}
					// Any other color code resets bold too
					else if ("0123456789abcdefABCDEFkKmMnNoO".indexOf(code) >= 0)
					{
						bold = false;
					}
				}
				i++; // skip the code char
				continue;
			}

			width += charWidth(c, bold);
		}

		return width;
	}

	/**
	 * Strip MiniMessage tags like &lt;bold&gt;, &lt;color:#hex&gt;, &lt;click:...&gt;, etc.
	 * Preserves the inner text content.
	 */
	private static String stripMiniMessage(String input)
	{
		if (input == null) return "";
		// Simple regex to remove all <...> tags (non-greedy)
		return input.replaceAll("<[^>]+>", "");
	}

	// ─── Center Methods ─────────────────────────────────────────────────

	/**
	 * Centers a legacy §-formatted string by prepending spaces.
	 *
	 * @param message The message to center (may contain § color codes).
	 * @return The centered message with leading spaces.
	 */
	public static String center(String message)
	{
		if (message == null || message.isEmpty()) return "";

		int msgPx = getWidth(message);
		int halved = (CENTER_PX - msgPx) / 2;
		if (halved <= 0) return message;

		// Normal space = 4px (3px glyph + 1px gap)
		int spaceWidth = charWidth(' ', false);  // 5
		int spaceCount = halved / spaceWidth;

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < spaceCount; i++)
		{
			sb.append(' ');
		}
		sb.append(message);
		return sb.toString();
	}

	/**
	 * Centers a MiniMessage-formatted string by prepending spaces.
	 * The MiniMessage tags are stripped for width calculation but preserved in output.
	 *
	 * @param miniMessage The MiniMessage-formatted string to center.
	 * @return The centered string with leading spaces (still MiniMessage formatted).
	 */
	public static String centerMiniMessage(String miniMessage)
	{
		if (miniMessage == null || miniMessage.isEmpty()) return "";

		int msgPx = getWidth(miniMessage);
		int halved = (CENTER_PX - msgPx) / 2;
		if (halved <= 0) return miniMessage;

		int spaceWidth = charWidth(' ', false);
		int spaceCount = halved / spaceWidth;

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < spaceCount; i++)
		{
			sb.append(' ');
		}
		sb.append(miniMessage);
		return sb.toString();
	}

	/**
	 * Creates a decorative line/separator centered in chat.
	 * Commonly used as a header/footer border.
	 *
	 * @param lineChar The character to repeat (e.g. '▬', '━', '─')
	 * @param colorCode The § color code to apply (e.g. "§8§m" for dark gray strikethrough)
	 * @param widthPx Target width in pixels (use CENTER_PX * 2 for full width)
	 * @return The formatted separator line.
	 */
	public static String separator(char lineChar, String colorCode, int widthPx)
	{
		int cw = charWidth(lineChar, false);
		int count = widthPx / cw;
		StringBuilder sb = new StringBuilder(colorCode);
		for (int i = 0; i < count; i++)
		{
			sb.append(lineChar);
		}
		return sb.toString();
	}

	/** Creates a full-width dark-gray separator using ▬ */
	public static String darkSeparator()
	{
		return separator('▬', "§8", CENTER_PX * 2);
	}

	/** Creates a full-width gold separator using ▬ */
	public static String goldSeparator()
	{
		return separator('▬', "§6", CENTER_PX * 2);
	}

	/** Creates a strikethrough separator */
	public static String strikeSeparator(String colorCode)
	{
		return colorCode + "§m" + "                                                                   ";
	}
}
