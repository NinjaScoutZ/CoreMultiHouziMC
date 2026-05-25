package com.houzicore.arcade.nautilus.game.arcade.game.modules;

import org.bukkit.event.EventHandler;
import com.houzicore.arcade.nautilus.game.arcade.game.Game;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;
import org.bukkit.WorldBorder;
import org.bukkit.Location;

public class MapCrumbleModule extends GameModule<Game> {
    private final long _startTime;
    private final int _warningTime;
    private final double _startSize;
    private final double _endSize;
    private final long _crumbleSpeed;
    private boolean _triggered = false;

    public MapCrumbleModule(Game game, long startTime, int warningTime, double startSize, double endSize, long crumbleSpeed) {
        super(game);
        _startTime = startTime;
        _warningTime = warningTime;
        _startSize = startSize;
        _endSize = endSize;
        _crumbleSpeed = crumbleSpeed;
    }

    @EventHandler
    public void onUpdate(UpdateEvent event) {
        if (!isActive() || _game.GetState() != Game.GameState.Live || _triggered) return;
        if (event.getType() != UpdateType.SEC) return;

        long liveTime = System.currentTimeMillis() - _game.getGameLiveTime();
        if (liveTime > _startTime) {
            _triggered = true;
            WorldBorder border = _game.WorldData.World.getWorldBorder();
            Location center = _game.WorldData.World.getSpawnLocation();
            if (center != null) {
                border.setCenter(center);
                border.setSize(_startSize);
                border.setWarningTime(_warningTime);
                border.setSize(_endSize, _crumbleSpeed);
            }
        }
    }
}
