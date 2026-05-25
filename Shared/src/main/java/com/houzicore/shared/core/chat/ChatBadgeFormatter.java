package com.houzicore.shared.core.chat;

import java.util.Locale;

import org.bukkit.entity.Player;

import com.houzicore.shared.common.Rank;
import com.houzicore.shared.common.util.HouziColorParser;
import com.houzicore.shared.common.util.UtilText;
import com.houzicore.shared.core.level.LvlManager;

public final class ChatBadgeFormatter
{
    private static final String FRAME = "§8";

    private ChatBadgeFormatter()
    {
    }

    public static Rank resolveVisibleRank(Rank rank, boolean ownsUltra)
    {
        Rank safeRank = rank != null ? rank : Rank.ALL;

        if (ownsUltra && !safeRank.Has(Rank.WARRIOR))
        {
            return Rank.WARRIOR;
        }

        return safeRank;
    }

    public static String buildPrefix(Player player, Rank rank, boolean ownsUltra, LvlManager lvlManager)
    {
        int level = lvlManager != null && player != null ? lvlManager.getLevel(player) : 1;
        return buildPrefix(level, rank, ownsUltra);
    }

    public static String buildPrefix(int level, Rank rank, boolean ownsUltra)
    {
        return buildLevelBadge(level) + buildRankBadge(rank, ownsUltra);
    }

    public static String buildLevelBadge(int level)
    {
        int safeLevel = Math.max(1, level);
        String icon = "★";
        String gradient;
        if (safeLevel >= 80)
        {
            gradient = "#c084fc,#7c3aed";
        }
        else if (safeLevel >= 60)
        {
            gradient = "#fbbf24,#f97316";
        }
        else if (safeLevel >= 40)
        {
            gradient = "#67e8f9,#06b6d4";
        }
        else if (safeLevel >= 20)
        {
            gradient = "#86efac,#22c55e";
        }
        else
        {
            gradient = "#d4d4d8,#a1a1aa";
        }

        return FRAME + "[" +
                HouziColorParser.parse("<GRADIENT:" + gradient + ">" + icon + safeLevel + "</GRADIENT>") +
                FRAME + "] ";
    }

    public static String buildRankBadge(Rank rank, boolean ownsUltra)
    {
        return buildRankBadge(resolveVisibleRank(rank, ownsUltra));
    }

    public static String buildRankBadge(Rank rank)
    {
        Rank safeRank = rank != null ? rank : Rank.ALL;
        if (safeRank == Rank.ALL)
        {
            return "";
        }

        return buildCustomBadge(rankLabel(safeRank), rankGradient(safeRank));
    }

    public static String buildSpecialBadge(String label, String gradient)
    {
        return buildCustomBadge(normalizeLabel(label), gradient);
    }

    private static String buildCustomBadge(String label, String gradient)
    {
        return FRAME + "[" +
                HouziColorParser.parse("<GRADIENT:" + gradient + ">" + label + "</GRADIENT>") +
                FRAME + "] ";
    }

    private static String rankLabel(Rank rank)
    {
        return switch (rank)
        {
            case LT -> normalizeLabel("lt");
            case OWNER -> normalizeLabel("owner");
            case DEVELOPER -> normalizeLabel("dev");
            case ADMIN -> normalizeLabel("admin");
            case JNR_DEV -> normalizeLabel("jr.dev");
            case SNR_MODERATOR -> normalizeLabel("sr.mod");
            case MODERATOR -> normalizeLabel("mod");
            case HELPER -> normalizeLabel("helper");
            case MAPLEAD -> normalizeLabel("map lead");
            case MAPDEV -> normalizeLabel("builder");
            case EVENT -> normalizeLabel("event");
            case YOUTUBE -> normalizeLabel("youtube");
            case TWITCH -> normalizeLabel("twitch");
            case WARRIOR -> normalizeLabel("warrior");
            case SOVEREIGN -> normalizeLabel("sovereign");
            case DIVINE -> normalizeLabel("divine");
            case ALL -> "";
        };
    }

    private static String rankGradient(Rank rank)
    {
        return switch (rank)
        {
            case LT, OWNER -> "#ff5f6d,#ffc371";
            case DEVELOPER, JNR_DEV, ADMIN -> "#fb7185,#ef4444";
            case SNR_MODERATOR, MODERATOR -> "#fbbf24,#f59e0b";
            case HELPER -> "#67e8f9,#06b6d4";
            case MAPLEAD, MAPDEV -> "#60a5fa,#a855f7";
            case EVENT -> "#e5e7eb,#94a3b8";
            case YOUTUBE -> "#ff6b6b,#ffffff";
            case TWITCH -> "#a78bfa,#7c3aed";
            case WARRIOR -> "#67e8f9,#22d3ee";
            case SOVEREIGN -> "#f0abfc,#d946ef";
            case DIVINE -> "#86efac,#22c55e";
            case ALL -> "#d4d4d8,#a1a1aa";
        };
    }

    private static String normalizeLabel(String raw)
    {
        if (raw == null || raw.isEmpty())
        {
            return "";
        }

        boolean asciiOnly = raw.chars().allMatch(ch -> ch < 128);
        if (!asciiOnly)
        {
            return raw;
        }

        return UtilText.toSmallCaps(raw.toLowerCase(Locale.ROOT));
    }
}
