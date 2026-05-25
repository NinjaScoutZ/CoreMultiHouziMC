package com.houzicore.mapparser;

import com.houzicore.arcade.GameType;

public class MapData {
    public String MapName = "null";
    public String MapCreator = "null";
    public GameType MapGameType = GameType.Event; // Default to Event (which shares all maps)
    
    public MapData(String name) {
        this.MapName = name;
    }
}
