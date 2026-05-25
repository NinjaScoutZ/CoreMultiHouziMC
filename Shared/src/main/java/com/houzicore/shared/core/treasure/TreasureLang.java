package com.houzicore.shared.core.treasure;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.Player;

import com.houzicore.shared.core.lang.LangManager;

public final class TreasureLang {
	private TreasureLang() {
	}

	public static String get(Player player, String key) {
		return LangManager.get().get(player, namespaced(key));
	}

	public static String get(Player player, String key, String fallback) {
		return LangManager.get().getOrDefault(player, namespaced(key), fallback);
	}

	public static String get(Player player, String key, Object... args) {
		String value = get(player, key);
		for (int i = 0; i < args.length; i++) {
			value = value.replace("{" + i + "}", String.valueOf(args[i]));
		}
		return value;
	}

	public static String getEnglish(String key, String fallback) {
		return fromLocale("ENG", namespaced(key), fallback);
	}

	public static String getForLocale(String locale, String key, String fallback) {
		return fromLocale(locale, namespaced(key), fallback);
	}

	public static List<String> lines(Player player, String key) {
		String raw = get(player, key, "");
		if (raw == null || raw.isEmpty()) {
			return Collections.emptyList();
		}
		return Arrays.asList(raw.split("\\n"));
	}

	private static String fromLocale(String locale, String key, String fallback) {
		Map<String, String> map = LangManager.get().flat(locale);
		String value = map != null ? map.get(key) : null;
		if (value == null) {
			Map<String, String> english = LangManager.get().flat("ENG");
			value = english != null ? english.get(key) : null;
		}
		if (value == null) {
			value = fallback;
		}
		return value == null ? "" : value;
	}

	private static String namespaced(String key) {
		return key.startsWith("treasure.") ? key : "treasure." + key;
	}
}
