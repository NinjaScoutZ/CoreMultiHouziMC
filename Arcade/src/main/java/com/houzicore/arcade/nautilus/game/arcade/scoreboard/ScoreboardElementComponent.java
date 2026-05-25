package com.houzicore.arcade.nautilus.game.arcade.scoreboard;

import java.util.ArrayList;

import net.kyori.adventure.text.Component;

public class ScoreboardElementComponent extends ScoreboardElement
{
    private final Component _component;

    public ScoreboardElementComponent(Component component)
    {
        _component = component;
    }

    @Override
    public ArrayList<ScoreboardLine> GetLines()
    {
        ArrayList<ScoreboardLine> lines = new ArrayList<ScoreboardLine>();
        lines.add(ScoreboardLine.component(_component));
        return lines;
    }
}
