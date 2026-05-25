package com.houzicore.shared.core.scoreboard;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ScoreboardFormatUtil {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MM/dd/yyyy");

    public static String formatCurrency(int value) {
        if (value < 1000) return String.valueOf(value);
        if (value < 1000000) return String.format("%.1fK", value / 1000.0f).replace(".0K", "K");
        if (value < 1000000000) return String.format("%.2fM", value / 1000000.0f).replace(".00M", "M");
        return String.format("%.2fB", value / 1000000000.0f).replace(".00B", "B");
    }

    public static String currentDate() {
        return DATE_FORMAT.format(new Date());
    }
}
