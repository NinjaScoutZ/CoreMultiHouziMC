package com.houzicore.shared.common.util;

import com.houzicore.shared.common.Rank;

import org.bukkit.ChatColor;

public class F 
{
	private static int HSVtoRGB(float h, float s, float v) {
		int i = (int)(h * 6);
		float f = h * 6 - i;
		float p = v * (1 - s);
		float q = v * (1 - f * s);
		float t = v * (1 - (1 - f) * s);
		float r, g, b;
		switch (i % 6) {
			case 0: r = v; g = t; b = p; break;
			case 1: r = q; g = v; b = p; break;
			case 2: r = p; g = v; b = t; break;
			case 3: r = p; g = q; b = v; break;
			case 4: r = t; g = p; b = v; break;
			default: r = v; g = p; b = q; break;
		}
		return ((int)(r * 255) << 16) | ((int)(g * 255) << 8) | (int)(b * 255);
	}
	
	private static String getGradientForModule(String module) {
		int hash = module == null ? 0 : Math.abs(module.hashCode());
		float h = (hash % 360) / 360.0f;
		float h2 = (h + 0.15f) % 1.0f;
		
		String hex1 = String.format("#%06X", HSVtoRGB(h, 0.45f, 0.95f));
		String hex2 = String.format("#%06X", HSVtoRGB(h2, 0.60f, 0.95f));
		
		return "<GRADIENT:" + hex1 + "," + hex2 + ">";
	}

	public static String main(String module, String body)
	{
		String smallCapsModule = com.houzicore.shared.common.util.UtilText.toSmallCaps(module);
		String prefix = com.houzicore.shared.common.util.HouziColorParser.parse("<bold>" + getGradientForModule(module) + smallCapsModule + "</GRADIENT></bold>");
		return prefix + org.bukkit.ChatColor.DARK_GRAY + " \u00BB " + org.bukkit.ChatColor.WHITE + body;
	}
	
	public static String tute(String sender, String body)
	{
		return C.cGold + sender + org.bukkit.ChatColor.DARK_GRAY + " \u00BB " + C.cWhite + body;
	}
	
	public static String te(String message) 
	{
		return C.cYellow + message + C.cWhite;
	}
	
	public static String game(String elem)
	{
		return C.mGame + elem + C.mBody;
	}
	
	public static String ta(String message) 
	{
		return C.cGreen + message + C.cWhite;
	}
	
	public static String ts(String message) 
	{
		return C.cGold + message + C.cWhite;
	}

	public static String sys(String head, String body)
	{
		String smallCapsHead = com.houzicore.shared.common.util.UtilText.toSmallCaps(head);
		String prefix = com.houzicore.shared.common.util.HouziColorParser.parse("<bold>" + getGradientForModule(head) + smallCapsHead + "</GRADIENT></bold>");
		return prefix + org.bukkit.ChatColor.DARK_GRAY + " \u00BB " + org.bukkit.ChatColor.WHITE + body;
	}

	public static String elem(String elem)
	{
		return C.mElem + elem + ChatColor.RESET + C.mBody;
	}

	public static String name(String elem)
	{
		return C.mElem + elem + C.mBody;
	}

	public static String count(String elem)
	{
		return C.mCount + elem + C.mBody;
	}

	public static String item(String elem)
	{
		return C.mItem + elem + C.mBody;
	}

	public static String link(String elem)
	{
		return C.mLink + elem + C.mBody;
	}

	public static String value(String format, String value)
	{
		return format.replace("{0}", value);
	}

	public static String skill(String skill)
	{
		return C.mSkill + skill + C.mBody;
	}
	
	public static String skill(String a, String b)
	{

		return C.cYellow + " " + C.cGreen + b + C.mBody;
	}

	public static String time(String elem)
	{
		return C.mTime + elem + C.mBody;
	}

	public static String desc(String head, String body)
	{
		return C.descHead + head + ": " + C.descBody + body;
	}

	public static String wField(String field, String data)
	{
		return C.wFrame + "[" + C.wField + field + C.wFrame + "] " + C.mBody + data + " ";
	}

	public static String help(String cmd, String body, Rank rank)
	{
		return rank.GetColor() + cmd + " " + C.mBody + body + " " + rank(rank);
	}
	
	public static String rank(Rank rank)
	{
		if (rank == Rank.ALL)		
			return rank.GetColor() + "Player";
		
		return rank.GetTag(false, false);
	}

	public static String value(int a, String variable, String value)
	{
		String indent = "";
		while (indent.length() < a)
			indent += ChatColor.GRAY + ">";

		return indent + C.listTitle + variable + ": " + C.listValue + value;
	}

	public static String value(String variable, String value, boolean on)
	{
		return value(0, variable, value, on);
	}

	public static String value(int a, String variable, String value, boolean on)
	{
		String indent = "";
		while (indent.length() < a)
			indent += ChatColor.GRAY + ">";

		if (on)			return indent + C.listTitle + variable + ": " + C.listValueOn + value;
		else			return indent + C.listTitle + variable + ": " + C.listValueOff + value;
	}
	
	public static String ed(boolean var)
	{
		if (var)
			return C.listValueOn + "Enabled" + C.mBody;
		return C.listValueOff + "Disabled" + C.mBody;
	}

	public static String oo(boolean var)
	{
		if (var)
			return C.listValueOn + "On" + C.mBody;
		return C.listValueOff + "Off" + C.mBody;
	}

	public static String tf(boolean var)
	{
		if (var)
			return C.listValueOn + "True" + C.mBody;
		return C.listValueOff + "False" + C.mBody;
	}

	public static String oo(String variable, boolean value)
	{
		if (value)
			return C.listValueOn + variable + C.mBody;
		return C.listValueOff + variable + C.mBody;
	}

	public static String combine(String[] args, int start, String color, boolean comma)
	{
		if (args.length == 0)
			return "";

		String out = "";

		for (int i = start ; i<args.length ; i++)
		{
			if (color != null)
			{
				String preColor = ChatColor.getLastColors(args[i]);
				out += color + args[i] + preColor;
			}
			else
				out += args[i];

			if (comma)
				out += ", ";
			else
				out += " ";
		}

		if (out.length() > 0)
			if (comma)	out = out.substring(0, out.length() - 2);
			else		out = out.substring(0, out.length() - 1);

		return out;
	}

	
}
