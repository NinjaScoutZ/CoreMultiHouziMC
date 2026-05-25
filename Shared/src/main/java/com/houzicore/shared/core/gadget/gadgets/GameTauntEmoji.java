package com.houzicore.shared.core.gadget.gadgets;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import com.houzicore.shared.common.util.UtilParticle;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.core.gadget.GadgetManager;
import com.houzicore.shared.core.gadget.types.GameTauntGadget;

public class GameTauntEmoji extends GameTauntGadget {
	public GameTauntEmoji(GadgetManager manager) {
		super(manager, "Emoji Taunt", new String[] { "Summons a laughing aura", "to mock your enemies!" }, -2, Material.NAME_TAG, (byte) 0);
	}

	@Override
	public void PlayTaunt(Player player) {
		player.getWorld().playSound(player.getLocation(), Sound.ENTITY_DONKEY_ANGRY, 1f, 1.2f);
		UtilParticle.PlayParticle(UtilParticle.ParticleType.HAPPY_VILLAGER, player.getLocation().add(0,1,0), 0.5f, 0.5f, 0.5f, 0f, 20, UtilParticle.ViewDist.NORMAL, UtilServer.getPlayers());
		// Hologram omitted internally since Shared lacks direct HologramManager instance access
	}
}
