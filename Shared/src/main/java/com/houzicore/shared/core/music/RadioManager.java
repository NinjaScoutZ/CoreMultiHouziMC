package com.houzicore.shared.core.music;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.plugin.java.JavaPlugin;

import com.houzicore.shared.core.preferences.PreferencesManager;

import com.houzicore.shared.MiniPlugin;
import com.houzicore.shared.common.actionbar.ActionBarChannel;
import com.houzicore.shared.common.actionbar.ActionBarService;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;
import com.houzicore.shared.core.music.NBSParser.NoteBlock;
import com.houzicore.shared.core.music.NBSParser.Song;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public class RadioManager extends MiniPlugin {

    private Song _activeSong;
    private int _tickIndex = 0;
    private boolean _playing = false;
    private boolean _loopMode = true; // Loops the playlist
    private boolean _shuffle = false;

    private final List<Song> _playlist = new CopyOnWriteArrayList<>();
    private int _playlistIndex = 0;

    private org.bukkit.scheduler.BukkitTask _playTask;
    private float _tickAccumulator = 0f;

    private final List<UUID> _listeners = new CopyOnWriteArrayList<>();

    public List<Song> getPlaylist() { return _playlist; }
    public Song getActiveSong() { return _activeSong; }
    public boolean isShuffle() { return _shuffle; }

    private final PreferencesManager _preferences;

    public RadioManager(JavaPlugin plugin, PreferencesManager preferences) {
        super("Radio Manager", plugin);
        _preferences = preferences;
    }
    
    public void loadSongs(java.io.File musicDir) {
        if (!musicDir.exists()) musicDir.mkdirs();
        java.io.File[] files = musicDir.listFiles((dir, name) -> name.endsWith(".nbs"));
        if (files != null) {
            for (java.io.File file : files) {
                Song song = NBSParser.parse(file);
                if (song != null) _playlist.add(song);
            }
        }
        if (!_playlist.isEmpty()) {
            playSongIndex(0);
        }
    }
    
    public void playSongIndex(int index) {
        if (_playlist.isEmpty()) return;
        if (index < 0) index = _playlist.size() - 1;
        if (index >= _playlist.size()) index = 0;
        
        _playlistIndex = index;
        _activeSong = _playlist.get(_playlistIndex);
        _tickIndex = 0;
        _playing = true;
        
        // Notify listeners
        for (UUID uuid : _listeners) {
            Player p = org.bukkit.Bukkit.getPlayer(uuid);
            if (p != null) {
                if (_preferences != null && !_preferences.Get(p).PlayRadio) continue;
                ActionBarService.display(p, ActionBarChannel.GAME_EVENT, net.kyori.adventure.text.Component.text(
                    com.houzicore.shared.common.util.C.cGold + "Now Playing: " + com.houzicore.shared.common.util.C.cWhite + _activeSong.name
                ));
            }
        }
        
        startPlaybackTask();
    }
    
    private void startPlaybackTask() {
        if (_playTask != null) {
            _playTask.cancel();
            _playTask = null;
        }
        _tickAccumulator = 0f;
        
        _playTask = org.bukkit.Bukkit.getScheduler().runTaskTimer(getPlugin(), () -> {
            if (!_playing || _activeSong == null) return;

            // Advance by the ratio of song TPS to server TPS (20)
            _tickAccumulator += (_activeSong.ticksPerSecond / 20.0f);

            while (_tickAccumulator >= 1.0f) {
                _tickAccumulator -= 1.0f;

                List<NoteBlock> notesThisTick = _activeSong.ticks.get(_tickIndex);
                if (notesThisTick != null) {
                    for (NoteBlock nb : notesThisTick) {
                        org.bukkit.Sound sound = nb.getBukkitSound();
                        float pitch = nb.getPitch();
                        
                        for (UUID uuid : _listeners) {
                            Player p = org.bukkit.Bukkit.getPlayer(uuid);
                            if (p != null) {
                                if (_preferences != null && !_preferences.Get(p).PlayRadio) continue;
                                p.playSound(p.getLocation(), sound, org.bukkit.SoundCategory.RECORDS, 1.0f, pitch);
                            }
                        }
                    }
                }

                _tickIndex++;

                // Song end
                if (_tickIndex > _activeSong.length) {
                    if (_playlist.isEmpty()) {
                        if (_loopMode) {
                            _tickIndex = 0;
                        } else {
                            stop();
                        }
                    } else {
                        nextSong();
                    }
                }
            }
        }, 1L, 1L);
    }
    
    public void nextSong() {
        if (_shuffle && !_playlist.isEmpty()) {
            playSongIndex((int) (Math.random() * _playlist.size()));
        } else {
            playSongIndex(_playlistIndex + 1);
        }
    }

    public void previousSong() {
        playSongIndex(_playlistIndex - 1);
    }

    public void toggleShuffle() {
        _shuffle = !_shuffle;
    }

    public void playSong(Song song, boolean loop) {
        this._activeSong = song;
        this._tickIndex = 0;
        this._loopMode = loop;
        this._playing = true;
        startPlaybackTask();
    }

    public void stop() {
        this._playing = false;
        this._tickIndex = 0;
        this._activeSong = null;
        if (_playTask != null) {
            _playTask.cancel();
            _playTask = null;
        }
    }

    public void addListener(Player player) {
        if (!_listeners.contains(player.getUniqueId())) {
            _listeners.add(player.getUniqueId());
        }
    }

    public void removeListener(Player player) {
        _listeners.remove(player.getUniqueId());
    }

    public void broadcastToAll() {
        _listeners.clear();
        for (Player p : UtilServer.getPlayers()) {
            _listeners.add(p.getUniqueId());
        }
    }

    @EventHandler
    public void onJoin(org.bukkit.event.player.PlayerJoinEvent event) {
        addListener(event.getPlayer());
    }

    @EventHandler
    public void onQuit(org.bukkit.event.player.PlayerQuitEvent event) {
        _listeners.remove(event.getPlayer().getUniqueId());
    }
}
