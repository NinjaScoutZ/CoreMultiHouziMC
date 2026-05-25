package com.houzicore.arcade.nautilus.game.arcade.scoreboard;

import net.kyori.adventure.text.Component;

public class ScoreboardLine
{
    private final String _legacyText;
    private final Component _component;

    private ScoreboardLine(String legacyText, Component component)
    {
        _legacyText = legacyText;
        _component = component;
    }

    public static ScoreboardLine legacy(String legacyText)
    {
        return new ScoreboardLine(legacyText, null);
    }

    public static ScoreboardLine component(Component component)
    {
        return new ScoreboardLine(null, component);
    }

    public boolean isComponent()
    {
        return _component != null;
    }

    public String getLegacyText()
    {
        return _legacyText;
    }

    public Component getComponent()
    {
        return _component;
    }
}
