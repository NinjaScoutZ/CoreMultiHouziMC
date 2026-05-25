package com.houzicore.shared.common.util;

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;

public class UtilText {
	public static <T> String listToString(Collection<T> inputList, boolean comma) {
		String out = "";

		for (T cur : inputList) {
			out += cur.toString() + (comma ? ", " : " ");
		}

		if (out.length() > 0) {
			out = out.substring(0, out.length() - (comma ? 2 : 1));
		}

		return out;
	}
	
	public static int upperCaseCount(String input) {
		int count = 0;
		
		for (int k = 0; k < input.length(); k++) {
			
			
			char ch = input.charAt(k);
			if (Character.isUpperCase(ch)) 
				count++;
		
		}
		
		return count;
	}
	public static int lowerCaseCount(String input) {
		int count = 0;
		
		for (int k = 0; k < input.length(); k++) {
			
			
			char ch = input.charAt(k);
			if (Character.isLowerCase(ch)) 
				count++;
		
		}
		
		return count;
	}

	public static boolean isStringSimilar(String newString, String oldString, float matchRequirement)
	{
		if (newString.length() <= 3)
		{
			return newString.toLowerCase().equals(oldString.toLowerCase());
		}
		
		for (int i=0 ; i < newString.length() * matchRequirement ; i++)
		{
			int matchFromIndex = 0;
			
			//Look for substrings starting at i
			for (int j=0 ; j < oldString.length() ; j++)
			{
				//End of newString
				if (i+j >= newString.length())
				{
					break;
				}
				
				//Matched
				if (newString.charAt(i+j) == oldString.charAt(j))
				{
					matchFromIndex++;
					
					if (matchFromIndex >= newString.length() * matchRequirement)
						return true;
				}
				//No Match > Reset
				else
				{
					break;
				}
			}
		}
		
		return false;
	}

	public static String toSmallCaps(String text) {
		if (text == null) return null;
		StringBuilder sb = new StringBuilder();
		for (char c : text.toCharArray()) {
			switch(Character.toLowerCase(c)) {
				case 'a': sb.append('ᴀ'); break;
				case 'b': sb.append('ʙ'); break;
				case 'c': sb.append('ᴄ'); break;
				case 'd': sb.append('ᴅ'); break;
				case 'e': sb.append('ᴇ'); break;
				case 'f': sb.append('ꜰ'); break;
				case 'g': sb.append('ɢ'); break;
				case 'h': sb.append('ʜ'); break;
				case 'i': sb.append('ɪ'); break;
				case 'j': sb.append('ᴊ'); break;
				case 'k': sb.append('ᴋ'); break;
				case 'l': sb.append('ʟ'); break;
				case 'm': sb.append('ᴍ'); break;
				case 'n': sb.append('ɴ'); break;
				case 'o': sb.append('ᴏ'); break;
				case 'p': sb.append('ᴘ'); break;
				case 'q': sb.append('ǫ'); break;
				case 'r': sb.append('ʀ'); break;
				case 's': sb.append('s'); break;
				case 't': sb.append('ᴛ'); break;
				case 'u': sb.append('ᴜ'); break;
				case 'v': sb.append('ᴠ'); break;
				case 'w': sb.append('ᴡ'); break;
				case 'x': sb.append('x'); break;
				case 'y': sb.append('ʏ'); break;
				case 'z': sb.append('ᴢ'); break;
				default: sb.append(c);
			}
		}
		return sb.toString();
	}

	public static boolean startsWithVowel(String text) {
		if (text == null || text.trim().isEmpty()) return false;
		char c = Character.toLowerCase(text.trim().charAt(0));
		return c == 'a' || c == 'e' || c == 'i' || c == 'o' || c == 'u';
	}
	
	public static String withArticle(String text) {
		if (text == null || text.isEmpty()) return text;
		return (startsWithVowel(text) ? "an " : "a ") + text;
	}

    // ═══════════════════════════════════════════════════════════════
    // Ported from HypixelSkyBlock — StringUtility + AchievementTier
    // ═══════════════════════════════════════════════════════════════

    /**
     * Converts an integer to a Roman numeral string (1-3999).
     * <pre>
     *   toRomanNumeral(1)  → "I"
     *   toRomanNumeral(4)  → "IV"
     *   toRomanNumeral(14) → "XIV"
     * </pre>
     * Ported from: net.swofty.type.generic.achievement.AchievementTier#getRomanNumeral()
     */
    public static String toRomanNumeral(int num) {
        if (num <= 0 || num > 3999) return String.valueOf(num);
        String[] thousands = {"", "M", "MM", "MMM"};
        String[] hundreds  = {"", "C", "CC", "CCC", "CD", "D", "DC", "DCC", "DCCC", "CM"};
        String[] tens      = {"", "X", "XX", "XXX", "XL", "L", "LX", "LXX", "LXXX", "XC"};
        String[] ones      = {"", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX"};
        return thousands[num / 1000] + hundreds[(num % 1000) / 100]
             + tens[(num % 100) / 10] + ones[num % 10];
    }

    /**
     * Shortens a number to human-readable form.
     * <pre>
     *   shortenNumber(500)     → "500"
     *   shortenNumber(1500)    → "1.5K"
     *   shortenNumber(2500000) → "2.5M"
     * </pre>
     * Ported from: net.swofty.commons.StringUtility#shortenNumber()
     */
    public static String shortenNumber(long num) {
        if (num < 1_000) return String.valueOf(num);
        if (num < 1_000_000) {
            double val = num / 1_000.0;
            return (val == (long) val) ? (long) val + "K" : String.format("%.1fK", val);
        }
        if (num < 1_000_000_000) {
            double val = num / 1_000_000.0;
            return (val == (long) val) ? (long) val + "M" : String.format("%.1fM", val);
        }
        double val = num / 1_000_000_000.0;
        return (val == (long) val) ? (long) val + "B" : String.format("%.1fB", val);
    }

    /**
     * Formats a number with comma separators.
     * <pre>
     *   commaify(1234567) → "1,234,567"
     * </pre>
     * Ported from: net.swofty.commons.StringUtility#commaify()
     */
    public static String commaify(long num) {
        return String.format("%,d", num);
    }

    /**
     * Creates a text-based progress bar.
     * <pre>
     *   progressBar(0.6, 10, '▌', '▌') → "§a▌▌▌▌▌▌§7▌▌▌▌"
     * </pre>
     * @param progress 0.0 to 1.0
     * @param length   total number of characters
     * @param filled   character for filled portion
     * @param empty    character for empty portion
     * Ported from: net.swofty.commons.StringUtility#getAsProgressBar()
     */
    public static String progressBar(double progress, int length, char filled, char empty) {
        progress = Math.max(0.0, Math.min(1.0, progress));
        int filledCount = (int) Math.round(progress * length);
        StringBuilder sb = new StringBuilder();
        sb.append("§a");
        for (int i = 0; i < filledCount; i++) sb.append(filled);
        sb.append("§7");
        for (int i = filledCount; i < length; i++) sb.append(empty);
        return sb.toString();
    }

    /**
     * Smart word-wrap that respects § color codes.
     * จะไม่ตัดกลาง §x color code และจะ carry สีข้ามบรรทัดให้อัตโนมัติ
     * <pre>
     *   smartWordWrap("§aThis is a long green text", 15)
     *   → ["§aThis is a long", "§agreen text"]
     * </pre>
     * Ported from: net.swofty.commons.StringUtility#splitByWordAndLength()
     */
    public static List<String> smartWordWrap(String text, int maxLength) {
        List<String> lines = new ArrayList<>();
        if (text == null || text.isEmpty()) return lines;

        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();
        String lastColor = "";

        for (String word : words) {
            String stripped = currentLine.toString().replaceAll("§[0-9a-fk-orA-FK-OR]", "");
            String wordStripped = word.replaceAll("§[0-9a-fk-orA-FK-OR]", "");

            if (stripped.length() + wordStripped.length() + 1 > maxLength && stripped.length() > 0) {
                lines.add(currentLine.toString());
                currentLine = new StringBuilder(lastColor);
            }

            if (currentLine.length() > 0 && !currentLine.toString().equals(lastColor)) {
                currentLine.append(" ");
            }
            currentLine.append(word);

            for (int i = 0; i < word.length() - 1; i++) {
                if (word.charAt(i) == '§') {
                    char code = word.charAt(i + 1);
                    if ("0123456789abcdefABCDEF".indexOf(code) >= 0) {
                        lastColor = "§" + code;
                    } else if (code == 'r' || code == 'R') {
                        lastColor = "";
                    }
                }
            }
        }
        if (currentLine.length() > 0) {
            lines.add(currentLine.toString());
        }
        return lines;
    }

    /**
     * Converts a string to Title Case while respecting acronyms.
     * <pre>
     *   toTitleCase("tnt run")        → "TNT Run"
     *   toTitleCase("solo skywars")   → "Solo Skywars"
     *   toTitleCase("hide_and_seek")  → "Hide And Seek"
     * </pre>
     * Ported from: net.swofty.commons.StringUtility#toNiceString() + Acronym.java
     */
    public static String toTitleCase(String text) {
        if (text == null || text.isEmpty()) return text;
        text = text.replace('_', ' ').replace('-', ' ');
        String[] words = text.split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            if (i > 0) sb.append(' ');
            String word = words[i];
            if (AcronymRegistry.isAcronym(word)) {
                sb.append(word.toUpperCase());
            } else if (word.length() > 0) {
                sb.append(Character.toUpperCase(word.charAt(0)));
                if (word.length() > 1) sb.append(word.substring(1).toLowerCase());
            }
        }
        return sb.toString();
    }
}
