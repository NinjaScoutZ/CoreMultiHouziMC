package com.houzicore.shared.core.title;

import com.houzicore.shared.common.Rank;

public enum TitleType {
    NEWCOMER("newcomer", "🌱", "ผู้มาเยือน", "Newcomer", "#a1a1aa,#d4d4d8", UnlockType.FREE, null, 0),
    ADVENTURER("adventurer", "🗺", "นักผจญภัย", "Adventurer", "#86efac,#22c55e", UnlockType.LEVEL, null, 10),
    FIGHTER("fighter", "⚔", "นักสู้", "Fighter", "#fca5a5,#ef4444", UnlockType.LEVEL, null, 25),
    BRAVE("brave", "🔥", "ผู้กล้าหาญ", "The Brave", "#fdba74,#f97316", UnlockType.LEVEL, null, 40),
    COMMANDER("commander", "🛡", "จอมทัพ", "Commander", "#93c5fd,#3b82f6", UnlockType.LEVEL, null, 60),
    LEGEND("legend", "⭐", "ตำนาน", "Legend", "#fde047,#eab308", UnlockType.LEVEL, null, 80),
    TRANSCENDENT("transcendent", "👑", "ผู้เหนือกาล", "Transcendent", "#c084fc,#a855f7", UnlockType.LEVEL, null, 100),
    STAFF_BUILDER("staff_builder", "🔨", "ผู้สร้างโลก", "World Builder", "#c084fc,#6366f1", UnlockType.RANK, Rank.MAPDEV, 0),
    STAFF_MOD("staff_mod", "⚡", "ผู้พิทักษ์", "Guardian", "#fde047,#f97316", UnlockType.RANK, Rank.MODERATOR, 0),
    STAFF_DEV("staff_dev", "💎", "ผู้สร้างสรรค์", "Creator", "#67e8f9,#06b6d4", UnlockType.RANK, Rank.DEVELOPER, 0);

    private final String _key;
    private final String _icon;
    private final String _displayNameTh;
    private final String _displayNameEn;
    private final String _gradient;
    private final UnlockType _unlockType;
    private final Rank _requiredRank;
    private final int _requiredLevel;

    TitleType(String key, String icon, String displayNameTh, String displayNameEn, String gradient, UnlockType unlockType, Rank requiredRank, int requiredLevel) {
        _key = key;
        _icon = icon;
        _displayNameTh = displayNameTh;
        _displayNameEn = displayNameEn;
        _gradient = gradient;
        _unlockType = unlockType;
        _requiredRank = requiredRank;
        _requiredLevel = requiredLevel;
    }

    public String getKey() {
        return _key;
    }

    public String getIcon() {
        return _icon;
    }

    public String getDisplayName(String language) {
        if (language != null && (language.equalsIgnoreCase("THA") || language.equalsIgnoreCase("TH"))) {
            return _displayNameTh;
        }
        return _displayNameEn;
    }

    public String getGradient() {
        return _gradient;
    }

    public UnlockType getUnlockType() {
        return _unlockType;
    }

    public Rank getRequiredRank() {
        return _requiredRank;
    }

    public int getRequiredLevel() {
        return _requiredLevel;
    }

    public static TitleType getByKey(String key) {
        if (key == null) return null;
        for (TitleType type : values()) {
            if (type.getKey().equalsIgnoreCase(key)) {
                return type;
            }
        }
        return null;
    }

    public enum UnlockType {
        FREE,
        LEVEL,
        RANK
    }
}
