package com.houzicore.shared.core.announce;

import org.bukkit.plugin.java.JavaPlugin;
import com.houzicore.shared.MiniPlugin;
import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;
import org.bukkit.event.EventHandler;

public class AnnounceManager extends MiniPlugin {
    
    private String _currentAnnouncementEN = null;
    private String _currentAnnouncementTH = null;
    private long _announceExpireTime = 0;
    
    // Scrolling logic
    private int _scrollIndex = 0;
    private int _scrollWindow = 16;
    
    private static AnnounceManager _instance;
    
    public AnnounceManager(JavaPlugin plugin, CoreClientManager clientManager) {
        super("Announce Manager", plugin);
        
        _instance = this;
        addCommand(new com.houzicore.shared.core.announce.command.AnnounceCommand(this));
        
        if (com.houzicore.shared.serverdata.commands.ServerCommandManager.getInstance() != null) {
            com.houzicore.shared.serverdata.commands.ServerCommandManager.getInstance().registerCommandType(
                com.houzicore.shared.serverdata.commands.AnnouncementCommand.class.getSimpleName(),
                com.houzicore.shared.serverdata.commands.AnnouncementCommand.class,
                new com.houzicore.shared.serverdata.commands.CommandCallback() {
                    @Override
                    public void run(com.houzicore.shared.serverdata.commands.ServerCommand command) {
                        if (command instanceof com.houzicore.shared.serverdata.commands.AnnouncementCommand) {
                            com.houzicore.shared.serverdata.commands.AnnouncementCommand announceCmd = (com.houzicore.shared.serverdata.commands.AnnouncementCommand) command;
                            announce(announceCmd.getMessage(), announceCmd.getDurationMillis());
                        }
                    }
                }
            );
        }
    }

    public static AnnounceManager getInstance() {
        return _instance;
    }

    public void announce(String message, long durationMillis) {
        String[] parts = message.split("\\|");
        String en = parts[0].trim();
        String th = parts.length > 1 ? parts[1].trim() : en;

        _currentAnnouncementEN = "   " + en + "   "; // Pad for scrolling
        _currentAnnouncementTH = "   " + th + "   ";
        _announceExpireTime = System.currentTimeMillis() + durationMillis;
        _scrollIndex = 0;
    }

    public String getCurrentScrollText(org.bukkit.entity.Player player) {
        if (_currentAnnouncementEN == null || System.currentTimeMillis() > _announceExpireTime) {
            _currentAnnouncementEN = null;
            _currentAnnouncementTH = null;
            return null;
        }
        
        boolean isThai = player != null
                && com.houzicore.shared.core.lang.LangManager.get() != null
                && com.houzicore.shared.core.lang.LangManager.get().isThai(player);
        String announcement = isThai ? _currentAnnouncementTH : _currentAnnouncementEN;
        
        if (announcement.length() <= _scrollWindow) {
            return announcement;
        }
        
        String doubled = announcement + announcement;
        
        int idx = _scrollIndex % announcement.length();
        return doubled.substring(idx, Math.min(doubled.length(), idx + _scrollWindow));
    }

    @EventHandler
    public void onUpdate(UpdateEvent event) {
        if (event.getType() == UpdateType.TICK) {
            // Scroll every few ticks
            if (System.currentTimeMillis() % 200 < 50) { 
                _scrollIndex++;
            }
        }
    }
}
