package com.houzicore.shared.core.music;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.houzicore.shared.MiniPlugin;

public class NBSMusicManager extends MiniPlugin {
    
    public NBSMusicManager(JavaPlugin plugin) {
        super("Music Engine", plugin);
    }

    public void playSong(Player player, String songName) {
        // Stub for proprietary 2018 byte-level NBS parser buffer extraction
        player.playSound(player.getLocation(), Sound.MUSIC_DISC_CAT, 1.0f, 1.0f);
    }

    public void stopSong(Player player) {
        player.stopSound(Sound.MUSIC_DISC_CAT);
    }
}
