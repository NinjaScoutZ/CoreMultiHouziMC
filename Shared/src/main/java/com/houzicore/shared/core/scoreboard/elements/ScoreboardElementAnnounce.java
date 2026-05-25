package com.houzicore.shared.core.scoreboard.elements;

import java.util.ArrayList;
import org.bukkit.entity.Player;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.core.scoreboard.ScoreboardManager;
import com.houzicore.shared.core.announce.AnnounceManager;

public class ScoreboardElementAnnounce extends ScoreboardElement {
    
    private final AnnounceManager _announceManager;
    
    public ScoreboardElementAnnounce(AnnounceManager announceManager) {
        _announceManager = announceManager;
    }

    @Override
    public ArrayList<String> GetLines(ScoreboardManager manager, Player player) {
        ArrayList<String> output = new ArrayList<>();
        boolean isThai = com.houzicore.shared.core.lang.LangManager.get().isThai(player);
        
        output.add(org.bukkit.ChatColor.LIGHT_PURPLE + C.Bold + "\ud83d\udce2 " + (isThai ? "\u0e1b\u0e23\u0e30\u0e01\u0e32\u0e28" : "Announce"));
        
        if (_announceManager == null) {
            output.add(C.cGray + com.houzicore.shared.core.lang.LangManager.get().getOrDefault(player, "loading.data", isThai ? "กำลังโหลดข้อมูล..." : "Loading..."));
            return output;
        }
        
        String text = _announceManager.getCurrentScrollText(player);
        if (text != null && !text.isEmpty()) {
            output.add(C.cWhite + text);
        } else {
            output.add(C.cGray + com.houzicore.shared.core.lang.LangManager.get().getOrDefault(player, "announce.none", isThai ? "ไม่มีข่าวสารในขณะนี้" : "No active news"));
        }
        
        return output;
    }
}
