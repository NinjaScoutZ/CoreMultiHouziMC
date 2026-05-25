package com.houzicore.shared.core.booster;

import com.houzicore.shared.serverdata.commands.ServerCommand;

public class BoosterActivateCommand extends ServerCommand {
    
    private String _playerName;
    private long _durationMs;
    
    public BoosterActivateCommand(String playerName, long durationMs) {
        super();
        _playerName = playerName;
        _durationMs = durationMs;
    }
    
    public String getPlayerName() {
        return _playerName;
    }
    
    public long getDurationMs() {
        return _durationMs;
    }
    
    // Required empty constructor for Gson serialization
    public BoosterActivateCommand() {}
}
