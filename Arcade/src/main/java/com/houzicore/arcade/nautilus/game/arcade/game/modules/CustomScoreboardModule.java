package com.houzicore.arcade.nautilus.game.arcade.game.modules;

import com.houzicore.arcade.nautilus.game.arcade.game.Game;
import java.util.ArrayList;
import java.util.List;

public class CustomScoreboardModule extends GameModule<Game> {
    private final List<com.houzicore.shared.core.scoreboard.elements.ScoreboardElement> _elements = new ArrayList<>();

    public CustomScoreboardModule(Game game) {
        super(game);
    }

    public void addElement(com.houzicore.shared.core.scoreboard.elements.ScoreboardElement element) {
        _elements.add(element);
    }

    public List<com.houzicore.shared.core.scoreboard.elements.ScoreboardElement> getElements() {
        return _elements;
    }
}
