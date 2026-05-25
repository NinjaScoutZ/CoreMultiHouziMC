package com.houzicore.shared.core.scoreboard.elements;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.houzicore.shared.core.scoreboard.ScoreboardManager;

public class ScoreboardElementScroll extends ScoreboardElement {
	
	private String _text;
	private int _width;

	public ScoreboardElementScroll(String text, int width) {
		_text = "          " + text + "          ";
		_width = width;
	}

	@Override
	public ArrayList<String> GetLines(ScoreboardManager manager, Player player) {
		ArrayList<String> output = new ArrayList<>();
		
		int tick = (int)((System.currentTimeMillis() / 125) % _text.length());
		String display = _text.substring(tick);
		
		if (display.length() < _width) {
			display += _text.substring(0, _width - display.length());
		} else {
			display = display.substring(0, _width);
		}
		
		output.add(ChatColor.GOLD + display);
		return output;
	}
}
